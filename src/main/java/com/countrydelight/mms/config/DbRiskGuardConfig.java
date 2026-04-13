package com.countrydelight.mms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copies the bundled report.html from classpath to the filesystem
 * on startup so the db-risk-guard-web controller can serve it.
 */
@Slf4j
@Component
public class DbRiskGuardConfig implements ApplicationRunner {

    @Value("${db-risk-guard.base-dir:${java.io.tmpdir}/db-risk-guard}")
    private String baseDir;

    @Override
    public void run(ApplicationArguments args) {
        Path target = Path.of(baseDir, "report.html");
        ClassPathResource resource = new ClassPathResource("db-risk-guard/report.html");

        if (!resource.exists()) {
            log.warn("db-risk-guard/report.html not found on classpath, skipping");
            return;
        }

        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("db-risk-guard report.html deployed to {}", target);
        } catch (IOException e) {
            log.warn("Could not deploy report.html to {}: {}", target, e.getMessage());
        }
    }
}
