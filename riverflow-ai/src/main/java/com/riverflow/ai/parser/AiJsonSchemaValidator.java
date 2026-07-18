package com.riverflow.ai.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 输出 JSON Schema 校验器
 */
@Slf4j
@Component
public class AiJsonSchemaValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private final ConcurrentHashMap<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    /**
     * 校验 JSON 是否符合 Schema
     *
     * @param json       JSON 字符串
     * @param schemaJson Schema 字符串
     * @return 校验通过返回 true，失败抛出 IllegalArgumentException
     */
    public boolean validate(String json, String schemaJson) {
        if (!StringUtils.hasText(schemaJson)) {
            return true;
        }
        if (!StringUtils.hasText(json)) {
            throw new IllegalArgumentException("待校验 JSON 为空");
        }
        try {
            JsonSchema schema = schemaCache.computeIfAbsent(schemaJson, this::buildSchema);
            JsonNode node = objectMapper.readTree(json);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) {
                String msg = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("JSON Schema 校验失败");
                throw new IllegalArgumentException("AI 输出不符合 Schema: " + msg);
            }
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSON Schema 校验异常", e);
            throw new IllegalArgumentException("JSON Schema 校验异常: " + e.getMessage(), e);
        }
    }

    private JsonSchema buildSchema(String schemaJson) {
        return schemaFactory.getSchema(schemaJson);
    }
}
