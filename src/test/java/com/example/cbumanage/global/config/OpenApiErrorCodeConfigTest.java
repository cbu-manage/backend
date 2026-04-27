package com.example.cbumanage.global.config;

import com.example.cbumanage.global.error.ErrorCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiErrorCodeConfigTest {

    private final OpenApiErrorCodeConfig config = new OpenApiErrorCodeConfig();

    @Test
    @SuppressWarnings("unchecked")
    void addsErrorCodeSchemasAndExamplesToEveryOperation() {
        OpenAPI openApi = new OpenAPI()
                .path("/test", new PathItem().get(new Operation()));

        config.globalErrorResponsesOpenApiCustomizer().customise(openApi);

        Operation operation = openApi.getPaths().get("/test").getGet();
        Example invalidRequestExample = operation.getResponses()
                .get("400")
                .getContent()
                .get("application/json")
                .getExamples()
                .get(ErrorCode.INVALID_REQUEST.name());

        assertThat(openApi.getComponents().getSchemas())
                .containsKeys("ErrorResponse", "ErrorCode");
        assertThat(operation.getResponses())
                .containsKeys("400", "401", "403", "404", "409");
        assertThat((Map<String, Object>) invalidRequestExample.getValue())
                .containsEntry("code", ErrorCode.INVALID_REQUEST.getCode())
                .containsEntry("message", ErrorCode.INVALID_REQUEST.getMessage())
                .containsEntry("data", null);
    }
}
