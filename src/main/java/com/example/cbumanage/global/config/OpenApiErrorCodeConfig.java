package com.example.cbumanage.global.config;

import com.example.cbumanage.global.error.ErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@SuppressWarnings("unchecked")
public class OpenApiErrorCodeConfig {
    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_RESPONSE_SCHEMA = "ErrorResponse";
    private static final String ERROR_CODE_SCHEMA = "ErrorCode";
    private static final String ACCESS_TOKEN_COOKIE_SECURITY = "accessTokenCookie";
    private static final Pattern ROLE_PATTERN = Pattern.compile("ROLE_[A-Z_]+");

    @Bean
    public OpenApiCustomizer globalErrorResponsesOpenApiCustomizer() {
        return openApi -> {
            registerErrorSchemas(openApi);
            registerSecuritySchemes(openApi);
            addErrorResponsesToOperations(openApi);
            addAuthenticationDescriptionsToProtectedOperations(openApi);
        };
    }

    @Bean
    public OperationCustomizer operationAuthorityOpenApiCustomizer() {
        return (operation, handlerMethod) -> {
            Optional<String> expression = preAuthorizeExpression(handlerMethod);
            if (expression.isEmpty()) {
                return operation;
            }

            addAccessTokenCookieSecurity(operation);
            appendPermissionDescription(operation, expression.get());
            return operation;
        };
    }

    private void registerErrorSchemas(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSchemas(ERROR_RESPONSE_SCHEMA, errorResponseSchema());
        components.addSchemas(ERROR_CODE_SCHEMA, errorCodeSchema());
    }

    private void registerSecuritySchemes(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSecuritySchemes(ACCESS_TOKEN_COOKIE_SECURITY,
                new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("accessToken")
                        .description("로그인 API가 발급하는 accessToken 쿠키"));
    }

    private Schema<?> errorResponseSchema() {
        return new ObjectSchema()
                .description("공통 에러 응답 형식")
                .addProperty("code", new StringSchema()
                        .description("에러 코드")
                        .example(ErrorCode.INVALID_REQUEST.getCode()))
                .addProperty("message", new StringSchema()
                        .description("에러 메시지")
                        .example(ErrorCode.INVALID_REQUEST.getMessage()))
                .addProperty("data", new Schema<>().nullable(true)
                        .description("에러 응답에서는 null"));
    }

    private Schema<?> errorCodeSchema() {
        List<String> codes = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .toList();

        return new StringSchema()
                .description(errorCodeDescription())
                ._enum(codes);
    }

    private String errorCodeDescription() {
        return Arrays.stream(ErrorCode.values())
                .map(errorCode -> String.format("- `%s` (%d %s): %s",
                        errorCode.getCode(),
                        errorCode.getHttpStatus().value(),
                        errorCode.getHttpStatus().getReasonPhrase(),
                        errorCode.getMessage()))
                .collect(Collectors.joining("\n"));
    }

    private void addErrorResponsesToOperations(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        Map<HttpStatus, List<ErrorCode>> errorCodesByStatus = Arrays.stream(ErrorCode.values())
                .collect(Collectors.groupingBy(
                        ErrorCode::getHttpStatus,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> addErrorResponses(operation, errorCodesByStatus));
    }

    private void addAuthenticationDescriptionsToProtectedOperations(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((method, operation) -> {
                    if (isPublicOperation(method, path)) {
                        return;
                    }
                    addAccessTokenCookieSecurity(operation);
                    appendPermissionDescription(operation, "isAuthenticated()");
                }));
    }

    private void addErrorResponses(Operation operation, Map<HttpStatus, List<ErrorCode>> errorCodesByStatus) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        for (Map.Entry<HttpStatus, List<ErrorCode>> entry : errorCodesByStatus.entrySet()) {
            String statusCode = String.valueOf(entry.getKey().value());
            ApiResponse response = responses.computeIfAbsent(statusCode, ignored -> new ApiResponse());

            response.description(errorResponseDescription(entry.getKey(), entry.getValue()));
            response.content(errorResponseContent(entry.getValue()));
        }
    }

    private String errorResponseDescription(HttpStatus httpStatus, List<ErrorCode> errorCodes) {
        String codes = errorCodes.stream()
                .map(errorCode -> String.format("- `%s`: %s", errorCode.getCode(), errorCode.getMessage()))
                .collect(Collectors.joining("\n"));

        return String.format("%d %s\n\n에러 코드:\n%s",
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                codes);
    }

    private Content errorResponseContent(List<ErrorCode> errorCodes) {
        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + ERROR_RESPONSE_SCHEMA));

        errorCodes.forEach(errorCode -> mediaType.addExamples(errorCode.name(), errorExample(errorCode)));

        return new Content().addMediaType(APPLICATION_JSON, mediaType);
    }

    private Example errorExample(ErrorCode errorCode) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("code", errorCode.getCode());
        value.put("message", errorCode.getMessage());
        value.put("data", null);

        return new Example()
                .summary(errorCode.getCode())
                .description(errorCode.name())
                .value(value);
    }

    private Optional<String> preAuthorizeExpression(HandlerMethod handlerMethod) {
        PreAuthorize methodPreAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
        if (methodPreAuthorize != null) {
            return Optional.of(methodPreAuthorize.value());
        }

        PreAuthorize classPreAuthorize = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
        if (classPreAuthorize != null) {
            return Optional.of(classPreAuthorize.value());
        }

        return Optional.empty();
    }

    private void addAccessTokenCookieSecurity(Operation operation) {
        boolean alreadyRegistered = operation.getSecurity() != null
                && operation.getSecurity().stream()
                .anyMatch(requirement -> requirement.containsKey(ACCESS_TOKEN_COOKIE_SECURITY));
        if (!alreadyRegistered) {
            operation.addSecurityItem(new SecurityRequirement().addList(ACCESS_TOKEN_COOKIE_SECURITY));
        }
    }

    private void appendPermissionDescription(Operation operation, String expression) {
        String permissionDescription = permissionDescription(expression);
        String description = operation.getDescription();
        if (description == null || description.isBlank()) {
            operation.setDescription(permissionDescription);
            return;
        }
        if (description.contains("권한:")) {
            return;
        }
        operation.setDescription(description + "\n\n" + permissionDescription);
    }

    private String permissionDescription(String expression) {
        List<String> roles = ROLE_PATTERN.matcher(expression).results()
                .map(MatchResult::group)
                .distinct()
                .toList();
        if (roles.isEmpty()) {
            return "권한: 로그인 필요";
        }
        return "권한: " + String.join(", ", roles);
    }

    private boolean isPublicOperation(PathItem.HttpMethod method, String path) {
        if (method == PathItem.HttpMethod.POST) {
            return path.equals("/api/v1/login")
                    || path.equals("/api/v1/login/signup")
                    || path.equals("/api/v1/login/refresh")
                    || path.equals("/api/v1/login/password/reset")
                    || path.equals("/api/v1/validate")
                    || path.equals("/api/v1/applications")
                    || path.equals("/api/v1/applications/my")
                    || path.equals("/api/v1/mail/send")
                    || path.equals("/api/v1/mail/verify");
        }
        if (method == PathItem.HttpMethod.GET) {
            return path.equals("/api/v1/applications/questions/current");
        }
        return false;
    }
}
