package com.riverflow.admin.infra.openapi;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 嵌套参数解析器
 * 用于解析 application/x-www-form-urlencoded 中的嵌套结构参数
 * 支持点号路径（baseInfo.person.name）和中括号路径（baseInfo[person][name]）
 */
public class NestedParamResolver {

    /**
     * 将 HttpServletRequest 的平级参数解析为嵌套 Map 结构
     */
    public static Map<String, Object> resolve(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            Object value = values.length == 1 ? values[0] : values;

            // 先尝试中括号解析
            if (key.contains("[")) {
                parseBracketPath(result, key, value);
            } else if (key.contains(".")) {
                parseDotPath(result, key, value);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 按点号路径解析（如 baseInfo.person.name）
     */
    @SuppressWarnings("unchecked")
    private static void parseDotPath(Map<String, Object> root, String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object next = current.get(part);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(part, next);
            }
            current = (Map<String, Object>) next;
        }
        current.put(parts[parts.length - 1], value);
    }

    /**
     * 按中括号路径解析（如 baseInfo[person][name]）
     */
    @SuppressWarnings("unchecked")
    private static void parseBracketPath(Map<String, Object> root, String key, Object value) {
        // 提取根键名，如 baseInfo[person][name] → baseInfo
        int firstBracket = key.indexOf('[');
        String rootKey = key.substring(0, firstBracket);

        // 提取所有中括号内的路径段
        List<String> segments = new ArrayList<>();
        int i = firstBracket;
        while (i < key.length()) {
            if (key.charAt(i) == '[') {
                int end = key.indexOf(']', i);
                if (end > i) {
                    segments.add(key.substring(i + 1, end));
                    i = end + 1;
                } else {
                    break;
                }
            } else {
                i++;
            }
        }

        Map<String, Object> current = root;
        if (!current.containsKey(rootKey)) {
            current.put(rootKey, new LinkedHashMap<String, Object>());
        }
        current = (Map<String, Object>) current.get(rootKey);

        for (int j = 0; j < segments.size() - 1; j++) {
            String segment = segments.get(j);
            Object next = current.get(segment);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(segment, next);
            }
            current = (Map<String, Object>) next;
        }
        current.put(segments.get(segments.size() - 1), value);
    }

    /**
     * 按路径从嵌套 Map 中取值
     * 支持点号分隔，如 "baseInfo.person.name"
     */
    @SuppressWarnings("unchecked")
    public static Object getValueByPath(Map<String, Object> map, String path) {
        if (map == null || path == null || path.isEmpty()) {
            return null;
        }
        // 如果直接存在该 key（兼容平级参数），直接返回
        if (map.containsKey(path)) {
            return map.get(path);
        }
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
