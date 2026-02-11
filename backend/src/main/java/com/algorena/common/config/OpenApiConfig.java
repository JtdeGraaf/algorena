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
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.*;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Configuration
@SuppressWarnings(NULL_AWAY_INIT)
public class OpenApiConfig {

    @Value("${BACKEND_URL:http://localhost:8080}")
    private String backendUrl;

    private final ObjectMapper objectMapper;

    public OpenApiConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        // Generate schemas from annotated classes
        Map<String, Schema> schemas = new LinkedHashMap<>(ModelConverters.getInstance().read(ErrorResponse.class));
        schemas.putAll(ModelConverters.getInstance().read(ErrorResponse.ValidationError.class));

        return new OpenAPI()
                .info(new Info()
                        .title("Algorena Backend API")
                        .version("0.1.0")
                        .description("REST API for Algorena with OAuth2 and JWT authentication. " +
                                "Use the 'Bearer JWT' scheme to authorize with a JWT token obtained after OAuth2 login.")
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url(backendUrl)
                                .description("Production Server")
                ))
                .components(new Components()
                        // Add generated schemas
                        .schemas(schemas)
                        // Bearer JWT Security Scheme (HTTP Bearer)
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained after OAuth2 login")
                        )
                        // OAuth2 Security Scheme (for reference/documentation)
                        .addSecuritySchemes("oauth2-google", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Google OAuth2 Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(backendUrl + "/oauth2/authorization/google")
                                                .tokenUrl("https://oauth2.googleapis.com/token")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "View your profile")
                                                        .addString("email", "View your email address")
                                                )
                                        )

                                )
                        )
                        // Standard Error Responses (schemas auto-generated from annotated classes)
                        .addResponses("BadRequest", createErrorResponse(
                                "Bad Request",
                                400,
                                "Bad Request",
                                "Invalid request parameters or validation failed"
                        ))
                        .addResponses("Unauthorized", createErrorResponse(
                                "Unauthorized",
                                401,
                                "Unauthorized",
                                "Authentication required or invalid credentials"
                        ))
                        .addResponses("Forbidden", createErrorResponse(
                                "Forbidden",
                                403,
                                "Forbidden",
                                "Access denied - insufficient permissions"
                        ))
                        .addResponses("NotFound", createErrorResponse(
                                "Not Found",
                                404,
                                "Not Found",
                                "The requested resource was not found"
                        ))
                        .addResponses("Conflict", createErrorResponse(
                                "Conflict",
                                409,
                                "Conflict",
                                "Resource conflict - duplicate or concurrent modification"
                        ))
                        .addResponses("InternalServerError", createErrorResponse(
                                "Internal Server Error",
                                500,
                                "Internal Server Error",
                                "An unexpected error occurred"
                        ))
                )
                // Apply Bearer JWT as the default security requirement
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt")
                );
    }

    /**
     * Helper method to create standardized error responses with proper JSON examples
     * Uses ObjectMapper to automatically generate examples from ErrorResponse objects,
     * ensuring all fields are always included even when the class changes.
     */
    private ApiResponse createErrorResponse(String description, int status, String error, String message) {
        // Build an ErrorResponse object
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(LocalDateTime.parse("2025-11-04T10:30:00"))
                .status(status)
                .error(error)
                .message(message)
                .path("/api/v1/example");

        // For Bad Request (400), include validation errors example
        if (status == 400) {
            List<ErrorResponse.ValidationError> validationErrors = List.of(
                    ErrorResponse.ValidationError.builder()
                            .field("email")
                            .message("must be a valid email address")
                            .build(),
                    ErrorResponse.ValidationError.builder()
                            .field("username")
                            .message("must be between 3 and 20 characters")
                            .build()
            );
            builder.validationErrors(validationErrors);
        }

        ErrorResponse errorResponse = builder.build();

        // Convert to Map using ObjectMapper - this automatically includes all fields
        @SuppressWarnings("unchecked")
        Map<String, Object> example = objectMapper.convertValue(errorResponse, Map.class);

        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<ErrorResponse>().$ref("#/components/schemas/ErrorResponse"))
                                .example(example)
                        )
                );
    }


    // Customizer to add global error responses and handle @Nullable annotations
    // Source - https://stackoverflow.com/questions/77702787/openapi-springboot-3-how-to-add-global-error-response-codes
    // Posted by Toni
    // Retrieved 2025-11-07, License - CC BY-SA 4.0
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        // Build class map once at bean creation time
        Map<String, Class<?>> classMap = buildClassMap();

        return openApi -> {
            // Add error responses to all operations
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        operation.getResponses().addApiResponse("400", createErrorResponse("Bad Request", 400, "Bad Request", "Invalid request parameters or validation failed"));
                        operation.getResponses().addApiResponse("401", createErrorResponse("Unauthorized", 401, "Unauthorized", "Authentication required or invalid credentials"));
                        operation.getResponses().addApiResponse("403", createErrorResponse("Forbidden", 403, "Forbidden", "Access denied - insufficient permissions"));
                        operation.getResponses().addApiResponse("404", createErrorResponse("Resource Not Found", 404, "Not Found", "The requested resource was not found"));
                        operation.getResponses().addApiResponse("409", createErrorResponse("Conflict", 409, "Conflict", "Resource conflict - duplicate or concurrent modification"));
                        operation.getResponses().addApiResponse("500", createErrorResponse("Internal Server Error", 500, "Internal Server Error", "An unexpected error occurred"));
                    })
            );

            // Process schemas for nullability based on @Nullable annotations
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().getSchemas().forEach((schemaName, schema) -> {
                    processSchemaForNullability(schemaName, schema, classMap);
                });
            }
        };
    }

    private Map<String, Class<?>> buildClassMap() {
        Map<String, Class<?>> map = new HashMap<>();

        // Register all known DTO/model classes
        List<Class<?>> classes = List.of(
                com.algorena.bots.dto.BotDTO.class,
                com.algorena.bots.dto.BotStatsDTO.class,
                com.algorena.bots.dto.CreateBotRequest.class,
                com.algorena.bots.dto.UpdateBotRequest.class,
                com.algorena.games.dto.MatchDTO.class,
                com.algorena.games.dto.MatchMoveDTO.class,
                com.algorena.games.dto.MatchParticipantDTO.class,
                com.algorena.games.dto.CreateMatchRequest.class,
                com.algorena.games.dto.GameStateDTO.class,
                com.algorena.games.dto.ChessGameStateDTO.class,
                com.algorena.games.dto.Connect4GameStateDTO.class,
                com.algorena.games.dto.BotMoveRequest.class,
                com.algorena.games.dto.BotMoveResponse.class,
                com.algorena.games.dto.BotLeaderboardEntryDTO.class,
                com.algorena.games.dto.UserLeaderboardEntryDTO.class,
                com.algorena.games.dto.RatingHistoryDTO.class,
                com.algorena.users.dto.UserDTO.class,
                com.algorena.users.dto.UpdateUserRequest.class,
                com.algorena.common.exception.ErrorResponse.class,
                com.algorena.common.exception.ErrorResponse.ValidationError.class
        );

        for (Class<?> clazz : classes) {
            map.put(clazz.getSimpleName(), clazz);
        }

        return map;
    }

    private void processSchemaForNullability(String schemaName, Schema<?> schema, Map<String, Class<?>> classMap) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }

        Class<?> clazz = classMap.get(schemaName);
        if (clazz == null) {
            return;
        }

        Set<String> nullableFields = getNullableFields(clazz);
        List<String> required = new ArrayList<>();

        properties.forEach((propertyName, propertySchema) -> {
            if (nullableFields.contains(propertyName)) {
                propertySchema.setNullable(true);
            } else {
                propertySchema.setNullable(false);
                required.add(propertyName);
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
                if (hasNullableAnnotation(component)) {
                    nullableFields.add(component.getName());
                }
            }
        } else {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (hasNullableAnnotation(field)) {
                    nullableFields.add(field.getName());
                }
            }
        }

        return nullableFields;
    }

    private boolean hasNullableAnnotation(RecordComponent component) {
        // Check TYPE_USE annotations on the type
        for (Annotation ann : component.getAnnotatedType().getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        // Also check element annotations
        for (Annotation ann : component.getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNullableAnnotation(java.lang.reflect.Field field) {
        // Check TYPE_USE annotations on the type
        for (Annotation ann : field.getAnnotatedType().getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        // Also check element annotations
        for (Annotation ann : field.getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

}
