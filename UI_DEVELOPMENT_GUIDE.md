# Material Management System - UI Development Guide

## 1. System Overview

### Business Domain
ERP-grade Material Management System for managing inventory across multiple branches with:
- Multi-branch operations
- FIFO (First-In-First-Out) compliance
- Approval workflows
- Role-based access control
- Real-time stock tracking

### Technical Context
- **Backend**: Spring Boot REST API
- **Base URL**: `https://qa-cd-mm.countrydelight.in/mms/api/v1`
- **Context Path**: `/mms`
- **API Docs**: `https://qa-cd-mm.countrydelight.in/mms/swagger-ui.html`

---

## 2. Recommended Tech Stack

### Frontend Framework (Choose one)
```
Option A: React + TypeScript (Recommended)
├── React 18+ with TypeScript
├── React Router v6 (navigation)
├── TanStack Query / React Query (API state)
├── Zustand / Redux Toolkit (client state)
└── Axios (HTTP client)

Option B: Vue 3 + TypeScript
├── Vue 3 Composition API
├── Vue Router
├── Pinia (state management)
└── Axios

Option C: Angular 17+
├── Standalone components
├── RxJS for reactive state
└── HttpClient
```

### UI Component Library (Choose one)
```
- Ant Design (Recommended for ERP systems - rich table/form components)
- Material-UI (MUI)
- PrimeReact/PrimeVue
- Shadcn/ui + Tailwind (Modern, customizable)
```

### Additional Tools
```
- Tailwind CSS (utility-first styling)
- React Hook Form / Formik (form management)
- Zod / Yup (validation)
- date-fns / Day.js (date handling)
- React-PDF / PDF-Lib (invoice viewing)
- Chart.js / Recharts (reports/analytics)
```

---

## 3. Application Architecture

### Folder Structure (React Example)
```
src/
├── api/                        # API integration
│   ├── client.ts              # Axios instance with interceptors
│   ├── endpoints.ts           # API endpoint constants
│   └── services/
│       ├── master.service.ts  # Master data APIs
│       ├── grn.service.ts     # GRN APIs
│       ├── issue.service.ts   # Issue APIs
│       ├── transfer.service.ts
│       ├── stock.service.ts
│       └── report.service.ts
│
├── components/                 # Reusable UI components
│   ├── common/
│   │   ├── Layout/
│   │   │   ├── AppLayout.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Header.tsx
│   │   ├── DataTable/
│   │   ├── FormFields/
│   │   ├── Modal/
│   │   └── StatusBadge/
│   └── domain/                # Business-specific components
│       ├── GrnForm/
│       ├── IssueForm/
│       ├── StockCard/
│       └── ApprovalWidget/
│
├── pages/                      # Page components (routes)
│   ├── Dashboard/
│   ├── Master/
│   │   ├── Branches/
│   │   ├── Items/
│   │   ├── Suppliers/
│   │   └── ...
│   ├── Inward/
│   │   ├── GrnList.tsx
│   │   ├── GrnCreate.tsx
│   │   └── GrnDetail.tsx
│   ├── Outward/
│   ├── Transfer/
│   ├── Stock/
│   └── Reports/
│
├── hooks/                      # Custom React hooks
│   ├── useMasterData.ts
│   ├── useGrn.ts
│   ├── useStock.ts
│   └── usePermissions.ts
│
├── types/                      # TypeScript types/interfaces
│   ├── master.types.ts
│   ├── grn.types.ts
│   ├── issue.types.ts
│   ├── transfer.types.ts
│   └── common.types.ts
│
├── utils/                      # Utility functions
│   ├── formatters.ts          # Date, currency, number formatting
│   ├── validators.ts
│   └── constants.ts
│
├── store/                      # Global state management
│   ├── authStore.ts
│   ├── masterDataStore.ts
│   └── userStore.ts
│
└── App.tsx                     # Root component
```

---

## 4. Screen Hierarchy & Navigation

### Primary Navigation Structure
```
┌─────────────────────────────────────────────┐
│  Header (User, Notifications, Branch)       │
├──────────┬──────────────────────────────────┤
│          │                                   │
│ Sidebar  │         Content Area             │
│          │                                   │
│ • Home   │  [Dynamic content based on route]│
│ • Master │                                   │
│ • Inward │                                   │
│ • Outward│                                   │
│ • Transfer                                   │
│ • Stock  │                                   │
│ • Reports│                                   │
│ • Admin  │                                   │
│          │                                   │
└──────────┴──────────────────────────────────┘
```

### Screen Map
```
1. Dashboard (/)
   └── KPIs: Stock value, pending approvals, recent transactions

2. Master Data (/master)
   ├── Branches         (/master/branches)
   ├── Items            (/master/items)
   ├── Groups           (/master/groups)
   ├── Sub-Groups       (/master/sub-groups)
   ├── Units            (/master/units)
   ├── Manufacturers    (/master/manufacturers)
   ├── Suppliers        (/master/suppliers)
   ├── Locations        (/master/locations)
   ├── Roles            (/master/roles)
   └── Approval Rules   (/master/approval-rules)

3. Inward (GRN) (/inward)
   ├── GRN List         (/inward/grn)
   ├── Create GRN       (/inward/grn/new)
   ├── View/Edit GRN    (/inward/grn/:id)
   └── Price Suggestions (/inward/price-suggestions)

4. Outward (Issue) (/outward)
   ├── Issue List       (/outward/issues)
   ├── Create Issue     (/outward/issues/new)
   └── View/Edit Issue  (/outward/issues/:id)

5. Transfer (/transfer)
   ├── Transfer List    (/transfer)
   ├── Create Transfer  (/transfer/new)
   ├── View Transfer    (/transfer/:id)
   └── Receive Transfer (/transfer/:id/receive)

6. Stock (/stock)
   ├── Stock Summary    (/stock/summary)
   ├── Stock Ledger     (/stock/ledger)
   └── Stock Aging      (/stock/aging)

7. Reports (/reports)
   ├── GRN Reports      (/reports/grn)
   ├── Issue Reports    (/reports/issue)
   ├── Stock Reports    (/reports/stock)
   ├── Transfer Reports (/reports/transfer)
   └── Custom Reports   (/reports/custom)

8. Admin (/admin)
   ├── Users            (/admin/users)
   ├── User Roles       (/admin/user-roles)
   ├── System Settings  (/admin/settings)
   └── Audit Logs       (/admin/audit)
```

---

## 5. Core User Workflows

### 5.1 Material Inward (GRN) Flow
```
┌─────────────┐
│ Create GRN  │
│ (DRAFT)     │
└──────┬──────┘
       │ Fill details:
       │ - Branch, Location
       │ - Supplier, Invoice
       │ - Line items (item, qty, rate)
       ▼
┌─────────────┐      No       ┌──────────────┐
│  Submit?    ├──────────────►│ Save as DRAFT│
└──────┬──────┘               └──────────────┘
       │ Yes
       ▼
┌─────────────────┐
│ Approval Check  │
│ (Backend)       │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────────┐
│ AUTO  │  │ PENDING      │
│ POST  │  │ APPROVAL     │
└───┬───┘  └──────┬───────┘
    │             │ Approver
    │             │ Approves/Rejects
    │             ▼
    │      ┌──────────┐
    │      │ APPROVED │
    │      └─────┬────┘
    │            │
    └────────────┤
                 ▼
         ┌───────────────┐
         │ POSTED        │
         │ (Stock Updated)│
         └───────────────┘
```

### 5.2 Material Outward (Issue) Flow
```
┌─────────────┐
│ Create Issue│
│ (DRAFT)     │
└──────┬──────┘
       │ Fill:
       │ - From Location
       │ - To (Department/Project)
       │ - Items & quantities
       ▼
┌─────────────┐      No       ┌──────────────┐
│  Post?      ├──────────────►│ Save as DRAFT│
└──────┬──────┘               └──────────────┘
       │ Yes
       ▼
┌─────────────────┐
│ Stock Check     │
│ (Frontend)      │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐  ┌───────────────┐
│ Success│  │ Insufficient  │
│        │  │ Stock Error   │
└───┬────┘  └───────────────┘
    │
    ▼
┌────────────────┐
│ FIFO Consume   │
│ (Backend)      │
└───────┬────────┘
        │
        ▼
┌───────────────┐
│ POSTED        │
│ (Stock Reduced)│
└───────────────┘
```

### 5.3 Inter-Branch Transfer Flow
```
┌─────────────────┐
│ Create Transfer │
│ (Source Branch) │
└────────┬────────┘
         │ Fill:
         │ - From Location
         │ - To Branch/Location
         │ - Items & quantities
         ▼
┌─────────────────┐
│ DISPATCH        │
│ (Issue from src)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ IN_TRANSIT      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ RECEIVE         │
│ (Dest Branch)   │
│ - Verify items  │
│ - Note discrepancies
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ COMPLETED       │
│ (GRN at dest)   │
└─────────────────┘
```

---

## 6. API Integration Patterns

### 6.1 API Client Setup (TypeScript)
```typescript
// api/client.ts
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ||
                     'https://qa-cd-mm.countrydelight.in/mms/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor (add auth token)
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor (handle errors globally)
apiClient.interceptors.response.use(
  (response) => response.data, // Return ApiResponse<T>
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 6.2 Type Definitions
```typescript
// types/common.types.ts
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

// types/master.types.ts
export interface Branch {
  branchId: string;
  branchName: string;
  address?: string;
  pincode?: string;
  cityName?: string;
  stateName?: string;
  contactPerson?: string;
  contactNo?: string;
  email?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Item {
  itemId: string;
  itemName: string;
  groupId: string;
  subGroupId: string;
  manufId: string;
  unitId: string;
  hsnCode?: string;
  minStock?: number;
  maxStock?: number;
  reorderLevel?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// types/grn.types.ts
export interface GrnHeader {
  grnNo: string;
  branchId: string;
  locationId: string;
  suppId: string;
  suppInvoiceNo?: string;
  suppInvoiceDate?: string;
  grnDate: string;
  status: 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'POSTED' | 'REJECTED';
  totalAmount?: number;
  remarks?: string;
  createdBy: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface GrnDetail {
  grnNo: string;
  srNo: number;
  itemId: string;
  qty: number;
  rate: number;
  amount: number;
  batchNo?: string;
  expiryDate?: string;
}

export interface GrnCreateRequest {
  header: Omit<GrnHeader, 'grnNo' | 'createdAt' | 'updatedAt'>;
  details: Omit<GrnDetail, 'grnNo'>[];
}
```

### 6.3 Service Layer
```typescript
// api/services/master.service.ts
import { apiClient } from '../client';
import { ApiResponse } from '@/types/common.types';
import { Branch, Item, Supplier } from '@/types/master.types';

export const masterService = {
  // Branches
  getBranches: (name?: string) =>
    apiClient.get<ApiResponse<Branch[]>>('/master/branches', {
      params: { name }
    }),

  getBranch: (branchId: string) =>
    apiClient.get<ApiResponse<Branch>>(`/master/branches/${branchId}`),

  createBranch: (branch: Omit<Branch, 'createdAt' | 'updatedAt'>) =>
    apiClient.post<ApiResponse<Branch>>('/master/branches', branch),

  updateBranch: (branchId: string, branch: Partial<Branch>) =>
    apiClient.put<ApiResponse<Branch>>(`/master/branches/${branchId}`, branch),

  deleteBranch: (branchId: string) =>
    apiClient.delete<ApiResponse<void>>(`/master/branches/${branchId}`),

  // Items
  getItems: (filters?: { name?: string; groupId?: string; subGroupId?: string }) =>
    apiClient.get<ApiResponse<Item[]>>('/master/items', { params: filters }),

  getItem: (itemId: string) =>
    apiClient.get<ApiResponse<Item>>(`/master/items/${itemId}`),

  // ... similar for other master entities
};

// api/services/grn.service.ts
import { GrnCreateRequest, GrnHeader } from '@/types/grn.types';

export const grnService = {
  getGrns: (filters?: { branchId?: string; status?: string; fromDate?: string; toDate?: string }) =>
    apiClient.get<ApiResponse<GrnHeader[]>>('/grn', { params: filters }),

  getGrn: (grnNo: string) =>
    apiClient.get<ApiResponse<GrnHeader>>(`/grn/${grnNo}`),

  createGrn: (request: GrnCreateRequest) =>
    apiClient.post<ApiResponse<GrnHeader>>('/grn', request),

  submitGrn: (grnNo: string) =>
    apiClient.post<ApiResponse<GrnHeader>>(`/grn/${grnNo}/submit`),

  deleteGrn: (grnNo: string) =>
    apiClient.delete<ApiResponse<void>>(`/grn/${grnNo}`),
};
```

### 6.4 React Query Hooks
```typescript
// hooks/useMasterData.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { masterService } from '@/api/services/master.service';
import { Branch } from '@/types/master.types';

export const useBranches = (name?: string) => {
  return useQuery({
    queryKey: ['branches', name],
    queryFn: () => masterService.getBranches(name),
  });
};

export const useCreateBranch = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (branch: Omit<Branch, 'createdAt' | 'updatedAt'>) =>
      masterService.createBranch(branch),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['branches'] });
    },
  });
};

// hooks/useGrn.ts
import { useQuery, useMutation } from '@tanstack/react-query';
import { grnService } from '@/api/services/grn.service';

export const useGrns = (filters?: any) => {
  return useQuery({
    queryKey: ['grns', filters],
    queryFn: () => grnService.getGrns(filters),
  });
};

export const useGrn = (grnNo: string) => {
  return useQuery({
    queryKey: ['grn', grnNo],
    queryFn: () => grnService.getGrn(grnNo),
    enabled: !!grnNo,
  });
};

export const useCreateGrn = () => {
  return useMutation({
    mutationFn: grnService.createGrn,
  });
};
```

---

## 7. Key UI Components

### 7.1 Master Data Table (Reusable)
**Features:**
- Pagination
- Search/filter
- Sort
- CRUD actions (Add, Edit, Delete)
- Export to Excel/CSV
- Bulk operations

**Example Usage:**
```tsx
<MasterDataTable
  title="Branches"
  columns={branchColumns}
  data={branches}
  onAdd={handleAddBranch}
  onEdit={handleEditBranch}
  onDelete={handleDeleteBranch}
  searchPlaceholder="Search by branch name..."
/>
```

### 7.2 Transaction Form (GRN/Issue/Transfer)
**Features:**
- Header section (branch, date, supplier, etc.)
- Line items table with:
  - Item dropdown (searchable)
  - Quantity input
  - Rate input (auto-calculated from last price)
  - Amount (auto-calculated)
  - Batch/expiry (for GRN)
  - Delete row
- Add line item button
- Totals section
- Draft/Submit actions
- Validation

### 7.3 Approval Widget
**Features:**
- Show pending approvals count
- List of transactions requiring approval
- Quick approve/reject actions
- Comments section

### 7.4 Stock Display Components
**Stock Summary Card:**
```tsx
<StockCard
  item="Milk Powder"
  currentQty={500}
  unit="KG"
  location="Warehouse A > Shelf 1"
  reorderLevel={100}
  status="ok" // ok | low | critical
/>
```

**Stock Aging Table:**
- Show items with age buckets (0-30, 31-60, 61-90, 90+ days)
- Highlight slow-moving items

---

## 8. State Management Strategy

### Global State (Zustand/Redux)
```typescript
// store/authStore.ts
interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
}

// store/masterDataStore.ts
interface MasterDataState {
  branches: Branch[];
  items: Item[];
  suppliers: Supplier[];
  // Cached master data for dropdowns
  loadMasterData: () => Promise<void>;
}

// store/userPrefsStore.ts
interface UserPrefsState {
  selectedBranch: string | null;
  theme: 'light' | 'dark';
  dateFormat: string;
}
```

### Server State (React Query)
- All API calls
- Automatic caching
- Background refetching
- Optimistic updates

---

## 9. Development Phases

### Phase 1: Foundation (Week 1-2)
- [ ] Setup project (Vite/CRA + TypeScript)
- [ ] Configure routing
- [ ] Setup API client with interceptors
- [ ] Create type definitions for all entities
- [ ] Build Layout (Header, Sidebar, Footer)
- [ ] Implement authentication flow
- [ ] Setup React Query
- [ ] Create reusable components (Table, Form, Modal)

### Phase 2: Master Data (Week 3)
- [ ] Branches CRUD
- [ ] Items CRUD
- [ ] Suppliers CRUD
- [ ] Manufacturers CRUD
- [ ] Groups/SubGroups CRUD
- [ ] Units CRUD
- [ ] Locations CRUD (hierarchical tree)
- [ ] Roles CRUD
- [ ] Approval Rules CRUD

### Phase 3: Inward (GRN) (Week 4-5)
- [ ] GRN List page with filters
- [ ] Create GRN form
  - [ ] Header section
  - [ ] Line items table
  - [ ] Price suggestions integration
- [ ] View/Edit GRN
- [ ] Submit GRN workflow
- [ ] Approval interface

### Phase 4: Outward (Issue) (Week 6)
- [ ] Issue List page
- [ ] Create Issue form
  - [ ] Stock availability check
  - [ ] FIFO batch selection display
- [ ] Post Issue workflow

### Phase 5: Transfer (Week 7)
- [ ] Transfer List
- [ ] Create Transfer form
- [ ] Dispatch workflow
- [ ] Receive workflow (at destination branch)

### Phase 6: Stock & Reports (Week 8-9)
- [ ] Stock Summary page
- [ ] Stock Ledger view
- [ ] Stock Aging report
- [ ] GRN reports
- [ ] Issue reports
- [ ] Transfer reports
- [ ] Dashboard with KPIs

### Phase 7: Polish & Deploy (Week 10)
- [ ] Error handling improvements
- [ ] Loading states
- [ ] Form validations
- [ ] Responsive design
- [ ] Cross-browser testing
- [ ] Performance optimization
- [ ] Documentation

---

## 10. Design Guidelines

### Color Scheme (ERP Standard)
```
Primary:    #1890ff (Blue - actions, links)
Success:    #52c41a (Green - posted, approved)
Warning:    #faad14 (Orange - pending approval)
Error:      #f5222d (Red - rejected, errors)
Info:       #13c2c2 (Cyan - informational)
Neutral:    #8c8c8c (Gray - disabled, secondary text)
```

### Status Colors
```
DRAFT:           #d9d9d9 (Gray)
PENDING_APPROVAL: #faad14 (Orange)
APPROVED:        #52c41a (Green)
POSTED:          #1890ff (Blue)
REJECTED:        #f5222d (Red)
IN_TRANSIT:      #13c2c2 (Cyan)
```

### Typography
```
Headings:    16px-24px, Semi-Bold
Body:        14px, Regular
Small:       12px (table data, labels)
Font:        Inter, Roboto, or system fonts
```

### Spacing
```
Form fields:  16px margin-bottom
Sections:     24px margin-bottom
Cards:        16px padding
Tables:       Compact mode for large datasets
```

---

## 11. Testing Strategy

### Unit Tests
- Utility functions
- Form validation logic
- Data transformations

### Integration Tests
- API service calls
- Component interactions
- Form submissions

### E2E Tests (Playwright/Cypress)
- Complete GRN flow
- Complete Issue flow
- Transfer workflow
- Approval workflow

---

## 12. Deployment Checklist

- [ ] Environment variables configured
- [ ] API base URL set correctly
- [ ] Build optimized for production
- [ ] CORS configured on backend
- [ ] HTTPS enabled
- [ ] Error tracking setup (Sentry)
- [ ] Analytics setup (Google Analytics)
- [ ] Performance monitoring
- [ ] Backup strategy

---

## 13. Quick Start Commands

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run tests
npm test

# Lint code
npm run lint
```

---

## 14. Important Notes for UI Team

1. **Always validate stock** before posting issues
2. **Show clear error messages** for business rule violations
3. **Implement optimistic updates** for better UX
4. **Cache master data** (branches, items) in app state
5. **Use loading skeletons** instead of spinners
6. **Implement keyboard shortcuts** for power users (Ctrl+S to save, etc.)
7. **Add confirmation modals** for destructive actions
8. **Display audit info** (created by, created at) where relevant
9. **Support multi-tab workflow** (user might have multiple GRNs open)
10. **Implement auto-save drafts** to prevent data loss

---

## 15. API Documentation

All endpoints are documented at:
**https://qa-cd-mm.countrydelight.in/mms/swagger-ui.html**

Use Swagger UI to:
- Explore all available endpoints
- See request/response schemas
- Test API calls directly
- Generate client code

---

## Questions & Support

For backend API questions, refer to:
- Swagger documentation
- `CLAUDE.md` in the repository
- Backend team contact

For design/UX decisions:
- Refer to similar ERP systems (SAP, Odoo)
- Focus on efficiency for data entry operators
- Prioritize keyboard navigation