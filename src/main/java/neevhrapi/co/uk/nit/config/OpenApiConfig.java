package neevhrapi.co.uk.nit.config;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NIM API")
                        .version("1.0")
                        .description("This is a Spring Boot RESTful service using springdoc-openapi and OpenAPI 3.")
                        .termsOfService("http://example.com/terms/")
                        .contact(new Contact()
                                .name("API Support")
                                .url("http://example.com/support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(new Server().url("http://localhost:8080")))
                .tags(List.of(new Tag().name("Fetch Queue Status").description("Provide dta for NIM tool")));
    }
    public GroupedOpenApi api(){
        return GroupedOpenApi.builder()
                .group("dev Environment")
                .pathsToMatch("nim/**")
                .build();
    }
}


