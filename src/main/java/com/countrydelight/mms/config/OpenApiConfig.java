package com.countrydelight.mms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI/Swagger configuration for Material Management System.
 *
 * Accessible at:
 * - Local: http://localhost:8080/mms/swagger-ui.html
 * - QA: https://qa-cd-mm.countrydelight.in/mms/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/mms}")
    private String contextPath;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI materialManagementOpenApi() {
        List<Server> servers = new ArrayList<>();

        // Add environment-specific servers
        if ("dev".equals(activeProfile) || "default".equals(activeProfile)) {
            servers.add(new Server()
                    .url("http://localhost:8080" + contextPath)
                    .description("Local Development Server"));
        }

        servers.add(new Server()
                .url("https://qa-cd-mm.countrydelight.in" + contextPath)
                .description("QA Server"));

        servers.add(new Server()
                .url(contextPath)
                .description("Current Server"));

        return new OpenAPI()
                .info(new Info()
                        .title("Material Management System API")
                        .description("ERP-grade Material Management System with FIFO compliance, approval workflows, and multi-branch operations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Beejapuri Dairy Pvt Ltd")
                                .email("support@countrydelight.in"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.countrydelight.in")))
                .servers(servers);
    }
}
