package com.algorena.common.config;

import com.algorena.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.*;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Configuration
@SuppressWarnings(NULL_AWAY_INIT)
public class OpenApiConfig {

    private static final String BASE_PACKAGE = "com.algorena";

    @Value("${BACKEND_URL:http://localhost:8080}")
    private String backendUrl;

    private final ObjectMapper objectMapper;
    private @Nullable Map<String, Class<?>> classCache;

    public OpenApiConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ========== Bean Definitions ==========

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * Customizer that adds global error responses and processes @Nullable annotations.
     * @see <a href="https://stackoverflow.com/questions/77702787">Stack Overflow: Global error response codes</a>
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            addGlobalErrorResponses(openApi);
            processNullableAnnotations(openApi);
        };
    }

    // ========== OpenAPI Structure ==========

    private Info createApiInfo() {
        return new Info()
                .title("Algorena Backend API")
                .version("0.1.0")
                .description("REST API for Algorena with OAuth2 and JWT authentication. " +
                        "Use the 'Bearer JWT' scheme to authorize with a JWT token obtained after OAuth2 login.");
    }

    private List<Server> createServers() {
        return List.of(
                new Server().url("http://localhost:8080").description("Local Development Server"),
                new Server().url(backendUrl).description("Production Server")
        );
    }

    private Components createComponents() {
        Map<String, Schema> schemas = new LinkedHashMap<>(ModelConverters.getInstance().read(ErrorResponse.class));
        schemas.putAll(ModelConverters.getInstance().read(ErrorResponse.ValidationError.class));

        return new Components()
                .schemas(schemas)
                .addSecuritySchemes("bearer-jwt", createBearerJwtScheme())
                .addSecuritySchemes("oauth2-google", createOAuth2GoogleScheme())
                .addResponses("BadRequest", createErrorResponse("Bad Request", 400, "Bad Request", "Invalid request parameters or validation failed"))
                .addResponses("Unauthorized", createErrorResponse("Unauthorized", 401, "Unauthorized", "Authentication required or invalid credentials"))
                .addResponses("Forbidden", createErrorResponse("Forbidden", 403, "Forbidden", "Access denied - insufficient permissions"))
                .addResponses("NotFound", createErrorResponse("Not Found", 404, "Not Found", "The requested resource was not found"))
                .addResponses("Conflict", createErrorResponse("Conflict", 409, "Conflict", "Resource conflict - duplicate or concurrent modification"))
                .addResponses("InternalServerError", createErrorResponse("Internal Server Error", 500, "Internal Server Error", "An unexpected error occurred"));
    }

    private SecurityScheme createBearerJwtScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained after OAuth2 login");
    }

    private SecurityScheme createOAuth2GoogleScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Google OAuth2 Authentication")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(backendUrl + "/oauth2/authorization/google")
                                .tokenUrl("https://oauth2.googleapis.com/token")
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect")
                                        .addString("profile", "View your profile")
                                        .addString("email", "View your email address"))));
    }

    // ========== Error Responses ==========

    private void addGlobalErrorResponses(OpenAPI openApi) {
        openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    var responses = operation.getResponses();
                    responses.addApiResponse("400", createErrorResponse("Bad Request", 400, "Bad Request", "Invalid request parameters or validation failed"));
                    responses.addApiResponse("401", createErrorResponse("Unauthorized", 401, "Unauthorized", "Authentication required or invalid credentials"));
                    responses.addApiResponse("403", createErrorResponse("Forbidden", 403, "Forbidden", "Access denied - insufficient permissions"));
                    responses.addApiResponse("404", createErrorResponse("Resource Not Found", 404, "Not Found", "The requested resource was not found"));
                    responses.addApiResponse("409", createErrorResponse("Conflict", 409, "Conflict", "Resource conflict - duplicate or concurrent modification"));
                    responses.addApiResponse("500", createErrorResponse("Internal Server Error", 500, "Internal Server Error", "An unexpected error occurred"));
                }));
    }

    private ApiResponse createErrorResponse(String description, int status, String error, String message) {
        var builder = ErrorResponse.builder()
                .timestamp(LocalDateTime.parse("2025-11-04T10:30:00"))
                .status(status)
                .error(error)
                .message(message)
                .path("/api/v1/example");

        if (status == 400) {
            builder.validationErrors(List.of(
                    ErrorResponse.ValidationError.builder().field("email").message("must be a valid email address").build(),
                    ErrorResponse.ValidationError.builder().field("username").message("must be between 3 and 20 characters").build()));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> example = objectMapper.convertValue(builder.build(), Map.class);

        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<ErrorResponse>().$ref("#/components/schemas/ErrorResponse"))
                                .example(example)));
    }

    // ========== Nullability Processing ==========

    private void processNullableAnnotations(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        openApi.getComponents().getSchemas().forEach(this::processSchemaForNullability);
    }

    private void processSchemaForNullability(String schemaName, Schema<?> schema) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }

        Class<?> clazz = findClassBySchemaName(schemaName);
        if (clazz == null) {
            return;
        }

        Set<String> nullableFields = getNullableFields(clazz);
        List<String> required = new ArrayList<>();

        properties.forEach((name, propertySchema) -> {
            if (nullableFields.contains(name)) {
                propertySchema.setNullable(true);
            } else {
                propertySchema.setNullable(false);
                required.add(name);
            }
        });

        if (!required.isEmpty()) {
            schema.setRequired(required);
        }
    }

    private Set<String> getNullableFields(Class<?> clazz) {
        Set<String> nullableFields = new HashSet<>();

        if (clazz.isRecord()) {
            for (RecordComponent component : clazz.getRecordComponents()) {
                if (hasNullableAnnotation(component.getAnnotatedType()) || hasNullableAnnotation(component)) {
                    nullableFields.add(component.getName());
                }
            }
        } else {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (hasNullableAnnotation(field.getAnnotatedType()) || hasNullableAnnotation(field)) {
                    nullableFields.add(field.getName());
                }
            }
        }

        return nullableFields;
    }

    private boolean hasNullableAnnotation(AnnotatedElement element) {
        for (Annotation ann : element.getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    // ========== Class Discovery ==========

    private @Nullable Class<?> findClassBySchemaName(String schemaName) {
        if (classCache == null) {
            classCache = buildClassCache();
        }
        return classCache.get(schemaName);
    }

    private Map<String, Class<?>> buildClassCache() {
        Map<String, Class<?>> cache = new HashMap<>();

        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

        for (BeanDefinition bd : scanner.findCandidateComponents(BASE_PACKAGE)) {
            String className = bd.getBeanClassName();
            if (className == null) continue;

            try {
                Class<?> clazz = Class.forName(className);
                cache.put(clazz.getSimpleName(), clazz);

                for (Class<?> inner : clazz.getDeclaredClasses()) {
                    cache.put(inner.getSimpleName(), inner);
                }
            } catch (ClassNotFoundException e) {
                // Ignore classes that can't be loaded
            }
        }

        return cache;
    }
}
