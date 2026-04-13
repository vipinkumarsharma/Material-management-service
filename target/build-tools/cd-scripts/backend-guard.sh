#!/usr/bin/env bash
# ==============================================================================
# backend-guard.sh
# ==============================================================================
# Static analysis guard that catches patterns which compile successfully but
# indicate production-quality violations. Runs as a CI gate alongside Maven.
#
# Exit codes:
#   0 = All checks passed
#   1 = One or more violations found
#
# Usage:
#   ./scripts/backend-guard.sh [directory]
#   Default directory: src/
# ==============================================================================

set -euo pipefail

SCAN_DIR="${1:-src/}"
VIOLATIONS=0
REPORT=""

RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BOLD='\033[1m'
NC='\033[0m'

# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------

add_violation() {
    local category="$1"
    local file="$2"
    local line_num="$3"
    local message="$4"
    VIOLATIONS=$((VIOLATIONS + 1))
    REPORT+="  ${RED}[${category}]${NC} ${file}:${line_num} - ${message}\n"
}

scan_java_files() {
    local pattern="$1"
    local category="$2"
    local message="$3"
    local exclude_pattern="${4:-}"

    while IFS=: read -r file line_num content; do
        # Skip test files for certain checks
        if [[ "$file" == *"/test/"* ]] && [[ "$category" != "SECURITY" ]]; then
            continue
        fi
        # Apply exclude pattern if provided
        if [[ -n "$exclude_pattern" ]] && echo "$content" | grep -qE "$exclude_pattern"; then
            continue
        fi
        add_violation "$category" "$file" "$line_num" "$message"
    done < <(grep -rnE "$pattern" "$SCAN_DIR" --include="*.java" 2>/dev/null || true)
}

# --------------------------------------------------------------------------
# Header
# --------------------------------------------------------------------------

echo -e "${BOLD}========================================${NC}"
echo -e "${BOLD}  Backend Guard - Static Analysis${NC}"
echo -e "${BOLD}========================================${NC}"
echo -e "Scanning: ${SCAN_DIR}"
echo ""

# ==========================================================================
# CHECK 1: System.out.println / System.err.println
# ==========================================================================
echo -e "${YELLOW}[1/9]${NC} Checking for System.out/err usage..."
scan_java_files \
    'System\.(out|err)\.(print|println)' \
    "LOGGING" \
    "Use SLF4J logger instead of System.out/err"

# ==========================================================================
# CHECK 2: Empty catch blocks
# ==========================================================================
echo -e "${YELLOW}[2/9]${NC} Checking for empty catch blocks..."
while IFS=: read -r file line_num _; do
    if [[ "$file" == *"/test/"* ]]; then
        continue
    fi
    add_violation "ERROR-HANDLING" "$file" "$line_num" "Empty catch block detected. Log the exception or rethrow."
done < <(grep -rnPzo 'catch\s*\([^)]*\)\s*\{\s*\}' "$SCAN_DIR" --include="*.java" 2>/dev/null \
    | grep -aP '^\S+:\d+:' || true)

# Fallback: line-by-line detection for catch blocks with only whitespace
while IFS= read -r file; do
    if [[ "$file" == *"/test/"* ]]; then
        continue
    fi
    awk '
    /catch\s*\(/ {
        in_catch=1; brace_count=0; start_line=NR; content=""
    }
    in_catch {
        for (i=1; i<=length($0); i++) {
            c = substr($0, i, 1)
            if (c == "{") brace_count++
            if (c == "}") brace_count--
            if (brace_count > 0 && c != "{" && c != "}" && c != " " && c != "\t" && c != "\n" && c != "\r") {
                content = content c
            }
            if (brace_count == 0 && NR > start_line) {
                if (content == "") {
                    print FILENAME ":" start_line ": empty catch block"
                }
                in_catch=0
            }
        }
    }
    ' "$file"
done < <(find "$SCAN_DIR" -name "*.java" -not -path "*/test/*" 2>/dev/null) \
    | while IFS=: read -r file line_num message; do
        add_violation "ERROR-HANDLING" "$file" "$line_num" "Empty catch block. Log or rethrow the exception."
    done

# ==========================================================================
# CHECK 3: SQL string concatenation (SQL injection risk)
# ==========================================================================
echo -e "${YELLOW}[3/9]${NC} Checking for SQL string concatenation..."
scan_java_files \
    '(\"(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE|AND|OR)\s.*\"\s*\+|\+\s*\".*\s(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE|AND|OR)\s)' \
    "SECURITY" \
    "SQL string concatenation detected. Use parameterized queries or JPA Criteria API."

# Additional pattern: StringBuilder with SQL
scan_java_files \
    '(StringBuilder|StringBuffer).*\.(append)\(.*\"(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)' \
    "SECURITY" \
    "SQL built via StringBuilder. Use parameterized queries."

# ==========================================================================
# CHECK 4: SELECT * usage
# ==========================================================================
echo -e "${YELLOW}[4/9]${NC} Checking for SELECT * usage..."
scan_java_files \
    '(select|SELECT)\s+\*\s+(from|FROM)' \
    "PERFORMANCE" \
    "SELECT * detected. Specify explicit columns to avoid fetching unnecessary data."

# Also check in XML mapper files
while IFS=: read -r file line_num content; do
    add_violation "PERFORMANCE" "$file" "$line_num" "SELECT * in SQL mapping. Specify explicit columns."
done < <(grep -rnEi '(select|SELECT)\s+\*\s+(from|FROM)' "$SCAN_DIR" --include="*.xml" 2>/dev/null || true)

# ==========================================================================
# CHECK 5: Large-table queries without pagination
# ==========================================================================
echo -e "${YELLOW}[5/9]${NC} Checking for queries without pagination on repository methods..."

# Detect findAll() calls without Pageable parameter
scan_java_files \
    'findAll\(\s*\)' \
    "PERFORMANCE" \
    "findAll() without pagination detected. Use Pageable for large tables (100M+ rows)."

# Detect @Query without LIMIT or Pageable
while IFS=: read -r file line_num content; do
    if [[ "$file" == *"/test/"* ]]; then
        continue
    fi
    # Check if the next method signature includes Pageable
    next_lines=$(sed -n "$((line_num)),$((line_num + 5))p" "$file" 2>/dev/null || echo "")
    if ! echo "$next_lines" | grep -qi "Pageable\|LIMIT\|FETCH FIRST\|ROWNUM\|TOP "; then
        add_violation "PERFORMANCE" "$file" "$line_num" "@Query without LIMIT or Pageable. Add pagination for large tables."
    fi
done < <(grep -rnE '@Query.*SELECT.*FROM' "$SCAN_DIR" --include="*.java" 2>/dev/null | grep -viE 'LIMIT|Pageable|FETCH FIRST|COUNT\(' || true)

# ==========================================================================
# CHECK 6: RestTemplate / WebClient without timeout
# ==========================================================================
echo -e "${YELLOW}[6/9]${NC} Checking for HTTP clients without timeout configuration..."

# Find files that create RestTemplate
while IFS=: read -r file line_num content; do
    if [[ "$file" == *"/test/"* ]]; then
        continue
    fi
    # Check if the file contains timeout configuration
    if ! grep -qE '(setConnectTimeout|setReadTimeout|connectTimeout|responseTimeout|timeout|ReadTimeout|ConnectTimeout)' "$file" 2>/dev/null; then
        add_violation "RELIABILITY" "$file" "$line_num" "RestTemplate created without timeout configuration. Set connect and read timeouts."
    fi
done < <(grep -rnE 'new\s+RestTemplate\s*\(' "$SCAN_DIR" --include="*.java" 2>/dev/null || true)

# Find files that create WebClient without timeout
while IFS=: read -r file line_num content; do
    if [[ "$file" == *"/test/"* ]]; then
        continue
    fi
    if ! grep -qE '(responseTimeout|timeout|connectTimeout)' "$file" 2>/dev/null; then
        add_violation "RELIABILITY" "$file" "$line_num" "WebClient created without timeout. Configure responseTimeout."
    fi
done < <(grep -rnE 'WebClient\.(builder|create)\s*\(' "$SCAN_DIR" --include="*.java" 2>/dev/null || true)

# ==========================================================================
# CHECK 7: Merge conflict markers
# ==========================================================================
echo -e "${YELLOW}[7/9]${NC} Checking for merge conflict markers..."
while IFS=: read -r file line_num content; do
    add_violation "GIT" "$file" "$line_num" "Merge conflict marker detected. Resolve conflicts before committing."
done < <(grep -rnE '^(<{7}|>{7}|={7})' "$SCAN_DIR" --include="*.java" 2>/dev/null || true)

# Also check non-java files
while IFS=: read -r file line_num content; do
    add_violation "GIT" "$file" "$line_num" "Merge conflict marker detected in config file."
done < <(grep -rnE '^(<{7}|>{7}|={7})' "$SCAN_DIR" --include="*.yml" --include="*.yaml" --include="*.xml" --include="*.properties" 2>/dev/null || true)

# ==========================================================================
# CHECK 8: Hardcoded secrets
# ==========================================================================
echo -e "${YELLOW}[8/9]${NC} Checking for hardcoded secrets..."

# Password patterns
scan_java_files \
    '(password|passwd|secret|api[_-]?key|apikey|token|auth[_-]?token)\s*=\s*"[^"]{4,}"' \
    "SECURITY" \
    "Possible hardcoded secret detected. Use environment variables or secrets manager." \
    '(test|mock|fake|dummy|example|placeholder|TODO|FIXME|xxx|changeme)'

# AWS keys
scan_java_files \
    '(AKIA[0-9A-Z]{16}|[0-9a-zA-Z/+]{40})' \
    "SECURITY" \
    "Possible AWS credential detected. Use IAM roles or environment variables."

# JDBC URLs with credentials
scan_java_files \
    'jdbc:[a-z]+://[^"]*password=[^"&]+' \
    "SECURITY" \
    "JDBC URL with embedded password. Use Spring externalized configuration."

# ==========================================================================
# CHECK 9: Additional anti-patterns
# ==========================================================================
echo -e "${YELLOW}[9/9]${NC} Checking for additional anti-patterns..."

# Thread.sleep in non-test code
scan_java_files \
    'Thread\.sleep\(' \
    "RELIABILITY" \
    "Thread.sleep() in production code. Use scheduled executors or async patterns."

# @Transactional on private methods (doesn't work with Spring proxies)
scan_java_files \
    '@Transactional.*\n.*private\s' \
    "CORRECTNESS" \
    "@Transactional on private method has no effect. Make the method public or package-private."

# Catching generic Exception/Throwable
scan_java_files \
    'catch\s*\(\s*(Exception|Throwable)\s' \
    "ERROR-HANDLING" \
    "Catching generic Exception/Throwable. Catch specific exceptions." \
    '@ExceptionHandler|@ControllerAdvice|main\s*\('

# ==========================================================================
# Report
# ==========================================================================

echo ""
echo -e "${BOLD}========================================${NC}"
echo -e "${BOLD}  Results${NC}"
echo -e "${BOLD}========================================${NC}"

if [[ $VIOLATIONS -gt 0 ]]; then
    echo -e "${RED}${BOLD}FAILED: ${VIOLATIONS} violation(s) found${NC}"
    echo ""
    echo -e "$REPORT"
    echo ""
    echo -e "${YELLOW}Fix these violations before merging. If a violation is a false positive,${NC}"
    echo -e "${YELLOW}add a suppression comment: // backend-guard:ignore <reason>${NC}"
    exit 1
else
    echo -e "${GREEN}${BOLD}PASSED: No violations detected${NC}"
    exit 0
fi
