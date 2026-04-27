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
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@SuppressWarnings("unchecked")
public class OpenApiErrorCodeConfig {
    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_RESPONSE_SCHEMA = "ErrorResponse";
    private static final String ERROR_CODE_SCHEMA = "ErrorCode";

    @Bean
    public OpenApiCustomizer globalErrorResponsesOpenApiCustomizer() {
        return openApi -> {
            registerErrorSchemas(openApi);
            addErrorResponsesToOperations(openApi);
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
}
