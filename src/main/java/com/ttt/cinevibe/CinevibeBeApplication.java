package com.ttt.cinevibe;

import com.ttt.cinevibe.dto.AppContactDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(AppContactDto.class)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@OpenAPIDefinition(
        info = @Info(
                title = "Cinevibe REST API Documentation",
                description = "Cinevibe REST API Documentation, the API is used to manage of the system",
                version = "v1",
                contact = @Contact(
                        name = "vku-k23",
                        email = "vietnq23ceb@vku.udn.vn",
                        url = "https://github.com/vku-k23/cinevibe-be"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "github.com/vku-k23"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Cinevibe REST API Documentation",
                url = "https://localhost:8080/swagger-ui.html"
        )
)
public class CinevibeBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinevibeBeApplication.class, args);
    }

}
