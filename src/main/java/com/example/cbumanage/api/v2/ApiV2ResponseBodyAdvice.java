package com.example.cbumanage.api.v2;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ControllerAdvice
@RequiredArgsConstructor
public class ApiV2ResponseBodyAdvice implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    private static final Map<String, String> USER_ID_FIELD_RENAMES = Map.of(
            "userId", "userUuid",
            "authorId", "authorUuid",
            "targetUserId", "targetUserUuid",
            "leaderId", "leaderUuid",
            "memberId", "memberUuid"
    );
    private static final Set<String> RESERVED_FIELDS = Set.of("groupMemberId", "postId", "groupId", "commentId", "newsId", "problemId", "resourceId");

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final HttpServletRequest servletRequest;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String uri = (String) servletRequest.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (uri == null) {
            uri = servletRequest.getRequestURI();
        }
        return uri != null && uri.startsWith("/api/v2");
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null || body instanceof String || body instanceof byte[]) {
            return body;
        }
        if (!(body instanceof ApiResponse<?>) && !isJson(selectedContentType)) {
            return body;
        }
        JsonNode tree = objectMapper.valueToTree(body);
        JsonNode converted = convertNode(tree);
        if (body instanceof ApiResponse<?>) {
            return objectMapper.convertValue(converted, ApiResponse.class);
        }
        return converted;
    }

    private boolean isJson(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        return MediaType.APPLICATION_JSON.includes(mediaType)
                || MediaType.APPLICATION_PROBLEM_JSON.includes(mediaType)
                || (mediaType.getSubtype() != null && mediaType.getSubtype().endsWith("+json"));
    }

    private JsonNode convertNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            node.forEach(child -> array.add(convertNode(child)));
            return array;
        }
        ObjectNode source = (ObjectNode) node;
        ObjectNode target = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();
            if ("memberIds".equals(fieldName) && value.isArray()) {
                target.set("memberUuids", convertUserIdArray(value));
                continue;
            }
            if (USER_ID_FIELD_RENAMES.containsKey(fieldName) && !RESERVED_FIELDS.contains(fieldName) && value.canConvertToLong()) {
                target.put(USER_ID_FIELD_RENAMES.get(fieldName), findUserUuid(value.asLong()).map(UUID::toString).orElse(null));
                continue;
            }
            target.set(fieldName, convertNode(value));
        }
        return target;
    }

    private ArrayNode convertUserIdArray(JsonNode value) {
        ArrayNode array = objectMapper.createArrayNode();
        value.forEach(id -> {
            if (id.canConvertToLong()) {
                findUserUuid(id.asLong()).ifPresent(uuid -> array.add(uuid.toString()));
            }
        });
        return array;
    }

    private Optional<UUID> findUserUuid(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId).map(User::getUserUuid);
    }
}
