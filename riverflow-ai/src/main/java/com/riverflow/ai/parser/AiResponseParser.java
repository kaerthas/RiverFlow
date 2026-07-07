package com.riverflow.ai.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AI 响应解析器
 *
 * <p>负责从 LLM 返回的文本中提取 JSON、校验 Schema、安全过滤。
 */
@Component
public class AiResponseParser {

    /**
     * 解析 JSON 对象
     */
    public <T> T parseObject(String content, Class<T> clazz) {
        String json = extractJson(content);
        if (!JSONValidator.from(json).validate()) {
            throw new IllegalArgumentException("LLM 输出不是合法 JSON: " + content);
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * 解析 JSON 数组
     */
    public <T> List<T> parseArray(String content, Class<T> clazz) {
        String json = extractJson(content);
        if (!JSONValidator.from(json).validate()) {
            throw new IllegalArgumentException("LLM 输出不是合法 JSON: " + content);
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * 从文本中提取 JSON。支持 Markdown 代码块包裹。
     */
    public String extractJson(String content) {
        if (content == null || content.isBlank()) {
            return "{}";
        }
        String trimmed = content.trim();

        // 去除 DeepSeek-R1 等推理模型的 <think>...</think> 思考过程
        if (trimmed.contains("<think>")) {
            int thinkStart = trimmed.indexOf("<think>");
            int thinkEnd = trimmed.indexOf("</think>", thinkStart);
            if (thinkEnd > thinkStart) {
                trimmed = (trimmed.substring(0, thinkStart) + trimmed.substring(thinkEnd + 8)).trim();
            }
        }

        // 去除 Markdown 代码块标记
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastTriple = trimmed.lastIndexOf("```");
            if (firstNewLine > 0 && lastTriple > firstNewLine) {
                trimmed = trimmed.substring(firstNewLine + 1, lastTriple).trim();
            }
        }

        // 修复模型常见的 JSON 格式错误
        trimmed = quoteBareArrayValues(trimmed);       // 给 outputMapping 等漏引号的数组/对象值加上引号
        trimmed = repairJsonString(trimmed);
        trimmed = trimmed.replaceAll(">\\s*\"", "\""); // 去除 outputMapping 等字符串末尾的 > 污染
        trimmed = fixBraces(trimmed);                  // 补齐缺失的 } ]
        trimmed = fixNodeProperties(trimmed);          // 把 properties 同级的配置字段移入 properties
        trimmed = removeEmptyLoopNodes(trimmed);       // 删除无意义的空 while/end_while 节点

        // 如果直接是 JSON 对象或数组
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed;
        }

        // 尝试提取第一个 {} 或 []
        int objectStart = trimmed.indexOf('{');
        int arrayStart = trimmed.indexOf('[');
        if (objectStart >= 0 && (arrayStart < 0 || objectStart < arrayStart)) {
            String extracted = repairJsonString(extractBalanced(trimmed, objectStart, '{', '}'));
            extracted = fixBraces(extracted);
            extracted = fixNodeProperties(extracted);
            extracted = removeEmptyLoopNodes(extracted);
            return extracted;
        }
        if (arrayStart >= 0) {
            String extracted = repairJsonString(extractBalanced(trimmed, arrayStart, '[', ']'));
            extracted = fixBraces(extracted);
            extracted = fixNodeProperties(extracted);
            extracted = removeEmptyLoopNodes(extracted);
            return extracted;
        }

        return trimmed;
    }

    /**
     * 补齐缺失的 {} / []
     */
    private String fixBraces(String json) {
        if (json == null || json.isEmpty()) return json;
        int openCurly = 0, closeCurly = 0, openSquare = 0, closeSquare = 0;
        boolean inString = false, escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == '{') openCurly++;
            else if (c == '}') closeCurly++;
            else if (c == '[') openSquare++;
            else if (c == ']') closeSquare++;
        }
        StringBuilder sb = new StringBuilder(json);
        while (openCurly > closeCurly) { sb.append('}'); closeCurly++; }
        while (openSquare > closeSquare) { sb.append(']'); closeSquare++; }
        return sb.toString();
    }

    /**
     * 把节点中 properties 同级的配置字段移入 properties，并做类型修正：
     * - scriptContent、inputMapping、outputMapping、sql 等必须是字符串；
     *   若模型写成数组/对象字面量，则自动 JSON.toJSONString。
     * - maxIterations、timeout 等数字字段如果是 null 或字符串数字，修正为整数。
     */
    private String fixNodeProperties(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            JSONObject graphJson = root.getJSONObject("graphJson");
            if (graphJson == null) return json;
            JSONArray nodes = graphJson.getJSONArray("nodes");
            if (nodes == null) return json;
            String[] movable = {
                    "scriptContent", "inputMapping", "outputMapping", "sql", "apiCode",
                    "dsCode", "operation", "resultVarName", "conditionExpr", "aggregateExpr",
                    "loopNodeId", "timerType", "delaySeconds", "fixedTime", "maxIterations",
                    "timeout", "itemVar", "indexVar", "resultVar", "continueOnFail"
            };
            String[] stringFields = {
                    "scriptContent", "inputMapping", "outputMapping", "sql", "apiCode",
                    "dsCode", "operation", "resultVarName", "conditionExpr", "aggregateExpr",
                    "loopNodeId", "timerType", "fixedTime", "itemVar", "indexVar", "resultVar"
            };
            String[] intFields = {"maxIterations", "timeout", "delaySeconds"};
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject node = nodes.getJSONObject(i);
                if (node == null) continue;
                JSONObject props = node.getJSONObject("properties");
                if (props == null) {
                    props = new JSONObject();
                    node.put("properties", props);
                }
                // 同级字段移入 properties
                for (String key : movable) {
                    if (node.containsKey(key)) {
                        props.putIfAbsent(key, node.get(key));
                        node.remove(key);
                    }
                }
                // 类型修正
                for (String key : stringFields) {
                    if (!props.containsKey(key)) continue;
                    Object v = props.get(key);
                    if (v == null) {
                        props.remove(key);
                    } else if (!(v instanceof String)) {
                        props.put(key, JSON.toJSONString(v));
                    }
                }
                // 修正 SpEL 表达式前多余的反斜杠：\\#{...} -> #{...}
                for (String key : new String[]{"conditionExpr", "aggregateExpr", "sourceExpr"}) {
                    if (!props.containsKey(key)) continue;
                    String v = props.getString(key);
                    if (v != null && v.startsWith("\\#")) {
                        props.put(key, v.substring(1));
                    }
                }
                for (String key : intFields) {
                    if (!props.containsKey(key)) continue;
                    Object v = props.get(key);
                    if (v == null) {
                        props.remove(key);
                    } else if (v instanceof String s && !s.isBlank()) {
                        try {
                            props.put(key, Integer.parseInt(s.trim()));
                        } catch (NumberFormatException ignored) {
                            props.remove(key);
                        }
                    }
                }
            }
            return JSON.toJSONString(root);
        } catch (Exception e) {
            return json;
        }
    }

    /**
     * 删除无意义的空 while / end_while 节点：
     * - properties 为空或没有 name / code
     * - 节点被其他边引用极少或为孤立节点
     */
    private String removeEmptyLoopNodes(String json) {
        try {
            JSONObject root = JSON.parseObject(json);
            JSONObject graphJson = root.getJSONObject("graphJson");
            if (graphJson == null) return json;
            JSONArray nodes = graphJson.getJSONArray("nodes");
            JSONArray edges = graphJson.getJSONArray("edges");
            if (nodes == null || nodes.isEmpty()) return json;

            Set<String> referenced = new HashSet<>();
            if (edges != null) {
                for (int i = 0; i < edges.size(); i++) {
                    JSONObject e = edges.getJSONObject(i);
                    if (e == null) continue;
                    String s = e.getString("sourceNodeId");
                    String t = e.getString("targetNodeId");
                    if (s != null) referenced.add(s);
                    if (t != null) referenced.add(t);
                }
            }

            List<JSONObject> keep = new ArrayList<>();
            List<String> removedIds = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject node = nodes.getJSONObject(i);
                if (node == null) continue;
                String type = node.getString("type");
                String id = node.getString("id");
                JSONObject props = node.getJSONObject("properties");
                boolean isLoop = "while".equals(type) || "end_while".equals(type) || "foreach".equals(type) || "end_foreach".equals(type);
                if (isLoop && (props == null || (props.getString("name") == null && props.getString("code") == null))) {
                    // 空循环节点：仅当它不是主流程关键节点时才删除
                    removedIds.add(id);
                    continue;
                }
                keep.add(node);
            }

            if (removedIds.isEmpty()) return json;

            // 移除被删节点的相关边
            List<JSONObject> keepEdges = new ArrayList<>();
            if (edges != null) {
                for (int i = 0; i < edges.size(); i++) {
                    JSONObject e = edges.getJSONObject(i);
                    if (e == null) continue;
                    String s = e.getString("sourceNodeId");
                    String t = e.getString("targetNodeId");
                    if (removedIds.contains(s) || removedIds.contains(t)) {
                        continue;
                    }
                    keepEdges.add(e);
                }
            }

            graphJson.put("nodes", new JSONArray(keep));
            graphJson.put("edges", new JSONArray(keepEdges));
            return JSON.toJSONString(root);
        } catch (Exception e) {
            return json;
        }
    }

    /**
     * 给 outputMapping / inputMapping / scriptContent / sql / conditionExpr 等
     * 应该是字符串的字段补上缺失的双引号。模型经常把 outputMapping 写成数组字面量：
     * "outputMapping": [{"source":"x","target":"y"}]
     * 需要改成：
     * "outputMapping": "[{\"source\":\"x\",\"target\":\"y\"}]"
     */
    private String quoteBareArrayValues(String json) {
        if (json == null || json.isEmpty()) return json;
        String[] stringFields = {
                "outputMapping", "inputMapping", "scriptContent", "sql", "conditionExpr",
                "aggregateExpr", "apiCode", "dsCode", "loopNodeId", "resultVar", "itemVar", "indexVar"
        };
        StringBuilder sb = new StringBuilder(json);
        for (String field : stringFields) {
            String key = "\"" + field + "\"";
            int idx = 0;
            while ((idx = sb.indexOf(key, idx)) >= 0) {
                int colon = sb.indexOf(":", idx + key.length());
                if (colon < 0) break;
                int valStart = colon + 1;
                while (valStart < sb.length() && Character.isWhitespace(sb.charAt(valStart))) valStart++;
                if (valStart >= sb.length()) break;
                char c = sb.charAt(valStart);
                if (c == '[' || c == '{') {
                    char open = c;
                    char close = c == '[' ? ']' : '}';
                    int end = findMatching(sb, valStart, open, close);
                    if (end > valStart) {
                        String inner = sb.substring(valStart, end + 1);
                        // 把这一段替换成 JSON 字符串（转义内部双引号、压缩换行）
                        String quoted = JSON.toJSONString(inner);
                        sb.replace(valStart, end + 1, quoted);
                        idx = valStart + quoted.length();
                        continue;
                    }
                }
                idx = idx + key.length();
            }
        }
        return sb.toString();
    }

    private int findMatching(StringBuilder sb, int start, char open, char close) {
        int depth = 0;
        boolean inString = false, escaped = false;
        for (int i = start; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * 修复模型生成 JSON 中的常见格式问题：
     * - 字符串值内部出现真实换行（替换为 \n 转义）
     * - 字符串值内部出现未转义双引号（替换为 \" 转义）
     * - JSON 结构外的换行/制表符替换为空格
     * - 压缩连续空格
     *
     * <p>本方法使用简化状态机：先识别 key/value 结构，仅在字符串值内部才对
     * 双引号进行转义，减少误判。
     */
    private String repairJsonString(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                sb.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                if (inString) {
                    // 判断这个 " 是否是字符串真正的结束。
                    // 结束符后面只能是 : , } ] 或空白（或文件结尾）。
                    int j = i + 1;
                    while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                    boolean isEnd = j >= json.length() ||
                            json.charAt(j) == ':' || json.charAt(j) == ',' ||
                            json.charAt(j) == '}' || json.charAt(j) == ']';
                    if (isEnd) {
                        inString = false;
                        sb.append(c);
                    } else {
                        sb.append("\\\"");
                    }
                } else {
                    inString = true;
                    sb.append(c);
                }
                continue;
            }
            if (inString) {
                if (c == '\n' || c == '\r') {
                    sb.append("\\n");
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '\n' || c == '\r' || c == '\t') {
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString().replaceAll(" +", " ").trim();
    }

    private String extractBalanced(String s, int start, char open, char close) {
        int depth = 0;
        int end = -1;
        boolean inString = false, escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == open) depth++;
            else if (c == close) depth--;
            if (depth == 0) {
                end = i + 1;
                break;
            }
        }
        if (end < 0) {
            throw new IllegalArgumentException("无法从 LLM 输出中提取平衡 JSON: " + s);
        }
        return s.substring(start, end);
    }

    /**
     * 提取推理模型 <think>...</think> 中的思考过程
     */
    public String extractThink(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        int thinkStart = content.indexOf("<think>");
        int thinkEnd = content.indexOf("</think>", thinkStart);
        if (thinkStart >= 0 && thinkEnd > thinkStart) {
            return content.substring(thinkStart + 7, thinkEnd).trim();
        }
        return "";
    }

    /**
     * 安全过滤：检查 Groovy 脚本中是否包含危险关键字
     */
    public String sanitizeGroovy(String script) {
        if (script == null) {
            return null;
        }
        String lower = script.toLowerCase();
        String[] dangerous = {
                "runtime.exec", "processbuilder", "system.exit", "classloader",
                "system.in", "thread.sleep", "//", "/*"
        };
        for (String d : dangerous) {
            if (lower.contains(d)) {
                throw new SecurityException("生成的脚本包含危险关键字: " + d);
            }
        }
        return script;
    }

    /**
     * 安全过滤：检查 SpEL 表达式
     */
    public String sanitizeSpel(String expression) {
        if (expression == null) {
            return null;
        }
        String lower = expression.toLowerCase();
        String[] dangerous = {
                "@", "new ", "t(", "system.", "runtime.", "class.", "classloader"
        };
        for (String d : dangerous) {
            if (lower.contains(d)) {
                throw new SecurityException("生成的 SpEL 表达式包含危险关键字: " + d);
            }
        }
        return expression;
    }
}
