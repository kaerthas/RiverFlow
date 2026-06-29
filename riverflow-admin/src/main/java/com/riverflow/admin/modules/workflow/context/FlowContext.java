package com.riverflow.admin.modules.workflow.context;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.riverflow.admin.modules.workflow.loop.LoopState;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程数据上下文
 * 负责节点间的数据流转与共享
 * <p>
 * 完整阶段扩展：引入作用域栈（scopeStack），支持循环节点内的变量隔离。
 * 全局变量仍然通过 globalVariables 存储；循环体内的 set() 优先写入当前 scope。
 */
@Slf4j
public class FlowContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 全局上下文变量存储（跨循环共享）
     */
    private final Map<String, Object> globalVariables = new ConcurrentHashMap<>();

    /**
     * 作用域栈，循环体内变量写入当前 scope
     */
    private final Deque<Map<String, Object>> scopeStack = new ArrayDeque<>();

    /**
     * 内置系统变量
     */
    private static final String SYS_INSTANCE_ID = "_instanceId";
    private static final String SYS_BUSINESS_KEY = "_businessKey";
    private static final String SYS_FLOW_CODE = "_flowCode";
    private static final String SYS_CURRENT_TIME = "_currentTime";

    /**
     * 当前流程的节点与边（不序列化，仅运行时用于循环体解析）
     */
    private transient List<FlowNode> nodes;
    private transient List<FlowEdge> edges;

    /**
     * 当前循环栈（不序列化），用于监控循环执行进度
     */
    private transient Deque<Map<String, Object>> loopStack = new ArrayDeque<>();

    /**
     * 是否处于异步调度上下文（不序列化）
     */
    private transient boolean asyncMode = false;

    /**
     * 表达式求值缓存（不序列化），用于缓存 evaluateCollection 转换后的大集合
     */
    private transient Map<String, Object> evaluationCache = new ConcurrentHashMap<>();

    public FlowContext() {
    }

    public FlowContext(Long instanceId, String businessKey, String flowCode) {
        this.globalVariables.put(SYS_INSTANCE_ID, instanceId);
        this.globalVariables.put(SYS_BUSINESS_KEY, businessKey);
        this.globalVariables.put(SYS_FLOW_CODE, flowCode);
    }

    /**
     * 压入一个新的作用域
     */
    public void pushScope() {
        scopeStack.push(new HashMap<>());
    }

    /**
     * 弹出当前作用域
     */
    public void popScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
        }
    }

    /**
     * 压入循环帧
     */
    public void pushLoopFrame(String loopNodeId, Integer iterationIndex) {
        if (loopStack == null) {
            loopStack = new ArrayDeque<>();
        }
        Map<String, Object> frame = new HashMap<>();
        frame.put("loopNodeId", loopNodeId);
        frame.put("iterationIndex", iterationIndex);
        loopStack.push(frame);
    }

    /**
     * 弹出循环帧
     */
    public void popLoopFrame() {
        if (loopStack != null && !loopStack.isEmpty()) {
            loopStack.pop();
        }
    }

    /**
     * 获取当前循环帧（最内层）
     */
    public Map<String, Object> getCurrentLoopFrame() {
        if (loopStack == null || loopStack.isEmpty()) {
            return null;
        }
        return loopStack.peek();
    }

    /**
     * 判断当前是否处于某个作用域内
     */
    public boolean inScope() {
        return !scopeStack.isEmpty();
    }

    /**
     * 获取当前作用域深度
     */
    public int getScopeDepth() {
        return scopeStack.size();
    }

    /**
     * 设置变量：
     * 1. 当前作用域已存在该 key，更新当前作用域；
     * 2. 当前作用域不存在但全局已存在，更新全局（如 while 循环体中脚本修改 counter）；
     * 3. 都不存在，则在当前作用域顶层创建新变量。
     * 保持向后兼容：旧代码未使用 pushScope 时等价于旧行为。
     */
    public void set(String key, Object value) {
        if (!scopeStack.isEmpty()) {
            Map<String, Object> currentScope = scopeStack.peek();
            if (currentScope.containsKey(key)) {
                currentScope.put(key, value);
                return;
            }
            // 若全局已存在，则更新全局，避免循环体内对全局变量的修改被写入 scope 后丢失
            if (globalVariables.containsKey(key)) {
                globalVariables.put(key, value);
                return;
            }
            // 否则在当前 scope 顶层创建新变量
            currentScope.put(key, value);
        } else {
            globalVariables.put(key, value);
        }
    }

    /**
     * 强制写入全局变量
     */
    public void setGlobal(String key, Object value) {
        globalVariables.put(key, value);
    }

    /**
     * 读取全局变量
     */
    public Object getGlobal(String key) {
        return globalVariables.get(key);
    }

    /**
     * 移除全局变量
     */
    public void removeGlobal(String key) {
        globalVariables.remove(key);
    }

    /**
     * 获取全局变量副本（用于恢复循环作用域等场景）
     */
    public Map<String, Object> getGlobals() {
        return new HashMap<>(globalVariables);
    }

    /**
     * 获取变量（优先从当前 scope 向上查找，最后查找 global，最后从 LoopState 动态恢复）
     */
    public Object get(String key) {
        for (Map<String, Object> scope : scopeStack) {
            if (scope.containsKey(key)) {
                return scope.get(key);
            }
        }
        Object value = globalVariables.get(key);
        if (value != null) {
            return value;
        }
        return resolveLoopItem(key);
    }

    /**
     * 判断变量是否存在（按 scope 优先级）
     */
    public boolean contains(String key) {
        for (Map<String, Object> scope : scopeStack) {
            if (scope.containsKey(key)) {
                return true;
            }
        }
        return globalVariables.containsKey(key);
    }

    /**
     * 获取变量（支持JSONPath）
     * 例如：context.apiResult.data[0].name
     */
    public Object getByPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String trimmed = path.trim();

        // 如果是简单的顶层key，直接返回
        if (!trimmed.contains(".")) {
            return get(trimmed);
        }

        // 支持 context.xxx.yyy 格式
        if (trimmed.startsWith("context.")) {
            trimmed = trimmed.substring(8);
        }

        // 针对 context.item.xxx 的循环变量路径，优先从 scopeStack 取 item 单独解析
        // 避免序列化整个上下文，同时解决 Map/JavaBean 字段访问不一致问题
        if (trimmed.startsWith("item.")) {
            Object item = get("item");
            if (item != null) {
                try {
                    JSONObject itemObject = JSON.parseObject(JSON.toJSONString(item));
                    String subPath = trimmed.substring(4); // 去掉 "item"
                    Object result = JSONPath.eval(itemObject, "$" + subPath);
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    log.warn("item JSONPath解析失败: path={}, item={}", path, item, e);
                }
            }
        }

        // 兜底：使用JSONPath解析嵌套路径
        try {
            // 先序列化再反序列化，确保 Map 中的 Java Bean（如循环体 item）被转成 JSONObject，
            // 否则 JSONPath 无法读取 Java Bean 的字段属性。
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(toMap()));
            return JSONPath.eval(jsonObject, "$" + (trimmed.startsWith(".") ? trimmed : "." + trimmed));
        } catch (Exception e) {
            log.warn("JSONPath解析失败: path={}", path, e);
            return null;
        }
    }

    /**
     * 从 LoopState 动态解析循环变量（用于 context 恢复后 scopeStack 为空场景）
     */
    private Object resolveLoopItem(String varName) {
        if (varName == null || globalVariables == null || globalVariables.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Object> entry : globalVariables.entrySet()) {
            if (!entry.getKey().startsWith("_loop_state_")) {
                continue;
            }
            try {
                LoopState state = LoopState.from(entry.getValue());
                if (state != null && state.isForeach() && varName.equals(state.getItemVar())) {
                    // 只从运行时缓存取，避免 get -> getCurrentItem -> evaluateExpression -> toMap 死循环
                    Object item = state.getCurrentItem(null);
                    if (item != null) {
                        return item;
                    }
                }
            } catch (Exception e) {
                log.warn("解析 LoopState 失败: key={}", entry.getKey(), e);
            }
        }
        return null;
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        Object value = getByPath(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 转为Map（用于SpEL求值）
     */
    public Map<String, Object> toMap() {
        // 更新系统变量
        globalVariables.put(SYS_CURRENT_TIME, System.currentTimeMillis());

        Map<String, Object> merged = new HashMap<>(globalVariables);
        // 从栈底到栈顶合并，后入覆盖先入
        Deque<Map<String, Object>> temp = new ArrayDeque<>(scopeStack);
        while (!temp.isEmpty()) {
            merged.putAll(temp.pollLast());
        }
        // 兜底：从 LoopState 恢复当前循环变量（scopeStack 未持久化时）
        for (Map.Entry<String, Object> entry : new HashMap<>(globalVariables).entrySet()) {
            if (!entry.getKey().startsWith("_loop_state_")) {
                continue;
            }
            try {
                LoopState state = LoopState.from(entry.getValue());
                if (state != null && state.isForeach()) {
                    // 如果 scopeStack 中已经包含 itemVar，直接用它，避免 getCurrentItem 触发 SpEL 求值
                    // 否则在 toMap -> evaluateExpression -> toMap 中形成死循环
                    boolean hasItemInScope = false;
                    for (Map<String, Object> scope : scopeStack) {
                        if (scope.containsKey(state.getItemVar())) {
                            hasItemInScope = true;
                            break;
                        }
                    }
                    if (!hasItemInScope) {
                        // 只从运行时缓存取，不再触发表达式求值
                        Object item = state.getCurrentItem(null);
                        if (item != null && !merged.containsKey(state.getItemVar())) {
                            merged.put(state.getItemVar(), item);
                        }
                    }
                    if (!merged.containsKey(state.getIndexVar())) {
                        merged.put(state.getIndexVar(), state.getIndex());
                    }
                }
            } catch (Exception e) {
                log.warn("toMap 解析 LoopState 失败: key={}", entry.getKey(), e);
            }
        }
        return merged;
    }

    /**
     * 转为JSON字符串（只序列化 globalVariables，不序列化 scopeStack）
     * <p>
     * scopeStack 中的循环变量通过 LoopState 动态恢复，避免大数据重复存储。
     */
    public String toJsonString() {
        globalVariables.put(SYS_CURRENT_TIME, System.currentTimeMillis());
        return JSON.toJSONString(globalVariables);
    }

    /**
     * 从JSON字符串恢复
     */
    public static FlowContext fromJson(String json) {
        FlowContext context = new FlowContext();
        if (json != null && !json.isEmpty()) {
            try {
                Map<String, Object> map = JSON.parseObject(json, Map.class);
                if (map != null) {
                    context.globalVariables.putAll(map);
                }
            } catch (Exception e) {
                log.warn("FlowContext 从JSON恢复失败: {}", json, e);
            }
        }
        return context;
    }

    /**
     * 深拷贝全局变量，创建独立上下文（用于并行迭代）
     */
    public FlowContext fork() {
        FlowContext copy = new FlowContext();
        copy.globalVariables.putAll(deepCopy(this.globalVariables));
        // scopeStack 不拷贝，新上下文无 scope
        // evaluationCache 不拷贝，每个迭代独立
        return copy;
    }

    /**
     * 获取表达式求值缓存
     */
    public Object getEvaluationCache(String key) {
        return evaluationCache != null ? evaluationCache.get(key) : null;
    }

    /**
     * 设置表达式求值缓存
     */
    public void putEvaluationCache(String key, Object value) {
        if (evaluationCache == null) {
            evaluationCache = new ConcurrentHashMap<>();
        }
        evaluationCache.put(key, value);
    }

    /**
     * 清空表达式求值缓存
     */
    public void clearEvaluationCache() {
        if (evaluationCache != null) {
            evaluationCache.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> source) {
        try {
            String json = JSON.toJSONString(source);
            return JSON.parseObject(json, Map.class);
        } catch (Exception e) {
            log.warn("FlowContext fork 深拷贝失败，使用浅拷贝", e);
            return new HashMap<>(source);
        }
    }

    public Long getInstanceId() {
        Object val = get(SYS_INSTANCE_ID);
        return val != null ? Long.valueOf(val.toString()) : null;
    }

    public String getBusinessKey() {
        return getString(SYS_BUSINESS_KEY);
    }

    public String getFlowCode() {
        return getString(SYS_FLOW_CODE);
    }

    public List<FlowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FlowNode> nodes) {
        this.nodes = nodes;
    }

    public List<FlowEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<FlowEdge> edges) {
        this.edges = edges;
    }

    /**
     * 获取当前 scope 中的变量（用于调试）
     */
    public List<Map<String, Object>> getScopeStackView() {
        return new ArrayList<>(scopeStack);
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }

    public void setAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
    }
}
