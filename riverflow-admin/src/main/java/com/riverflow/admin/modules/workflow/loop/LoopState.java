package com.riverflow.admin.modules.workflow.loop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 循环状态模型
 * 用于保存 foreach / while 循环的执行进度，支持序列化到 context_json。
 */
@Slf4j
@Data
public class LoopState {

    /**
     * 循环节点 ID
     */
    private String loopNodeId;

    /**
     * 循环结束节点 ID
     */
    private String endNodeId;

    /**
     * 循环体入口节点 ID
     */
    private String bodyEntryNodeId;

    /**
     * 循环体节点 ID 列表（用于图解析校验）
     */
    private List<String> bodyNodeIds = new ArrayList<>();

    /**
     * foreach 的原始集合（运行时缓存，不序列化）
     */
    private transient List<Object> items = new ArrayList<>();

    /**
     * foreach 当前迭代项（序列化，用于 context 恢复后重建 scopeStack）
     */
    private Object currentItem;

    /**
     * foreach 的循环源表达式，如 context.orderList
     */
    private String sourceExpr;

    /**
     * 当前迭代下标（foreach）
     */
    private int index = 0;

    /**
     * 总迭代次数（foreach）
     */
    private int total = 0;

    /**
     * 迭代计数（while）
     */
    private int iterationCount = 0;

    /**
     * 聚合结果
     */
    private List<Object> results = new ArrayList<>();

    /**
     * 循环变量名
     */
    private String itemVar = "item";

    /**
     * 下标变量名
     */
    private String indexVar = "index";

    /**
     * 结果变量名
     */
    private String resultVar;

    /**
     * 最大迭代数
     */
    private int maxIterations = 100;

    /**
     * 循环开始时间
     */
    private long startTime = 0L;

    /**
     * 是否已初始化（幂等用）
     */
    private boolean initialized = false;

    /**
     * 最近一次聚合的 index（幂等用）
     */
    private int lastAggregatedIndex = -1;

    /**
     * 循环类型：foreach / while
     */
    private String loopType = "foreach";

    /**
     * foreach 中断条件表达式
     */
    private String breakExpr;

    /**
     * while 条件表达式
     */
    private String conditionExpr;

    /**
     * 超时时间
     */
    private int timeout = 30000;

    /**
     * 是否并行执行
     */
    private boolean parallel = false;

    /**
     * 并行循环批次号
     */
    private String batchNo;

    /**
     * 并行度配置（异步并行模式下表示线程数）
     */
    private int parallelLimit = 0;

    /**
     * 失败是否继续（异步并行批次内使用）
     */
    private boolean continueOnFail = false;

    public LoopState() {
    }

    public LoopState(String loopNodeId, Collection<?> items, LoopConfig config) {
        this.loopNodeId = loopNodeId;
        this.loopType = "foreach";
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        this.total = this.items.size();
        this.index = 0;
        this.currentItem = this.items.isEmpty() ? null : this.items.get(0);
        this.sourceExpr = config.getSourceExpr();
        this.itemVar = config.getItemVar() != null ? config.getItemVar() : "item";
        this.indexVar = config.getIndexVar() != null ? config.getIndexVar() : "index";
        this.resultVar = config.getResultVar() != null ? config.getResultVar() : "loopResult_" + loopNodeId;
        this.maxIterations = config.getMaxIterations() != null ? config.getMaxIterations() : 100;
        this.timeout = config.getTimeout() != null ? config.getTimeout() : 30000;
        this.breakExpr = config.getBreakExpr();
        this.parallelLimit = config.getParallelLimit() != null ? config.getParallelLimit() : 0;
        this.continueOnFail = Boolean.TRUE.equals(config.getContinueOnFail());
    }

    public LoopState(String loopNodeId, LoopConfig config) {
        this.loopNodeId = loopNodeId;
        this.loopType = "while";
        this.itemVar = config.getItemVar() != null ? config.getItemVar() : "item";
        this.indexVar = config.getIndexVar() != null ? config.getIndexVar() : "index";
        this.resultVar = config.getResultVar() != null ? config.getResultVar() : "loopResult_" + loopNodeId;
        this.maxIterations = config.getMaxIterations() != null ? config.getMaxIterations() : 100;
        this.timeout = config.getTimeout() != null ? config.getTimeout() : 30000;
        this.conditionExpr = config.getConditionExpr();
        this.iterationCount = 0;
    }

    public static String key(String loopNodeId) {
        return "_loop_state_" + loopNodeId;
    }

    public String getStateKey() {
        return key(loopNodeId);
    }

    public String getStartTimeKey() {
        return "_loop_start_" + loopNodeId;
    }

    public String getAggregatedFlagKey() {
        return "_loop_aggregated_" + loopNodeId;
    }

    public boolean isForeach() {
        return "foreach".equals(loopType);
    }

    public boolean isWhile() {
        return "while".equals(loopType);
    }

    public Object getCurrentItem() {
        return getCurrentItem(null);
    }

    @SuppressWarnings("unchecked")
    public Object getCurrentItem(FlowContext context) {
        if (!isForeach() || index < 0 || index >= total) {
            return null;
        }
        // 优先使用已序列化的当前项，避免从 sourceExpr 动态求值产生递归
        if (currentItem != null) {
            return currentItem;
        }
        // 兼容运行时缓存
        if (items != null && index < items.size()) {
            return items.get(index);
        }
        // 从上下文动态获取集合
        if (context != null && sourceExpr != null && !sourceExpr.isEmpty()) {
            try {
                Object collection = LoopUtils.evaluateExpression(sourceExpr, context);
                if (collection instanceof List) {
                    List<Object> list = (List<Object>) collection;
                    if (index < list.size()) {
                        return list.get(index);
                    }
                } else if (collection instanceof Collection) {
                    List<Object> list = new ArrayList<>((Collection<Object>) collection);
                    if (index < list.size()) {
                        return list.get(index);
                    }
                }
            } catch (Exception e) {
                log.warn("[LoopState] 从上下文获取当前迭代项失败: sourceExpr={}, index={}", sourceExpr, index, e);
            }
        }
        return null;
    }

    public void nextIndex() {
        nextIndex(null);
    }

    public void nextIndex(FlowContext context) {
        if (isForeach()) {
            this.index++;
            // 同步更新 currentItem，便于反序列化后恢复
            if (items != null && index >= 0 && index < items.size()) {
                this.currentItem = items.get(index);
            } else if (context != null) {
                // 反序列化后 items 可能为空，从上下文动态获取当前迭代项
                Object previousItem = this.currentItem;
                this.currentItem = null;
                Object nextItem = getCurrentItem(context);
                if (nextItem != null) {
                    this.currentItem = nextItem;
                } else {
                    // 回退并记录，避免 currentItem 被意外清空
                    this.currentItem = previousItem;
                    log.warn("[LoopState] nextIndex 无法从上下文获取当前迭代项: sourceExpr={}, index={}", sourceExpr, index);
                }
            }
        }
    }

    public void nextIteration() {
        if (isWhile()) {
            this.iterationCount++;
        }
    }

    public void addResult(Object value) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(value);
    }

    public void setResult(int index, Object value) {
        if (results == null) {
            results = new ArrayList<>();
        }
        while (results.size() <= index) {
            results.add(null);
        }
        results.set(index, value);
    }

    public boolean isCurrentIndexAggregated() {
        return isForeach() && lastAggregatedIndex == index;
    }

    public void markCurrentIndexAggregated() {
        if (isForeach()) {
            this.lastAggregatedIndex = index;
        }
    }

    public void clearAggregatedFlag() {
        this.lastAggregatedIndex = -1;
    }

    public boolean isExhausted() {
        if (isForeach()) {
            return index >= total;
        }
        return iterationCount >= maxIterations;
    }

    public Map<String, Object> toMap() {
        JSONObject json = new JSONObject();
        json.put("loopNodeId", loopNodeId);
        json.put("endNodeId", endNodeId);
        json.put("batchNo", batchNo);
        json.put("bodyEntryNodeId", bodyEntryNodeId);
        json.put("bodyNodeIds", bodyNodeIds);
        // 不保存完整 items，只保存 sourceExpr 和当前项，恢复时动态从上下文获取
        json.put("sourceExpr", sourceExpr);
        json.put("currentItem", currentItem);
        json.put("index", index);
        json.put("total", total);
        json.put("iterationCount", iterationCount);
        json.put("results", results);
        json.put("itemVar", itemVar);
        json.put("indexVar", indexVar);
        json.put("resultVar", resultVar);
        json.put("maxIterations", maxIterations);
        json.put("timeout", timeout);
        json.put("startTime", startTime);
        json.put("initialized", initialized);
        json.put("lastAggregatedIndex", lastAggregatedIndex);
        json.put("loopType", loopType);
        json.put("breakExpr", breakExpr);
        json.put("conditionExpr", conditionExpr);
        json.put("parallel", parallel);
        json.put("parallelLimit", parallelLimit);
        json.put("continueOnFail", continueOnFail);
        return json;
    }

    @SuppressWarnings("unchecked")
    public static LoopState from(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            JSONObject json;
            if (obj instanceof Map) {
                json = new JSONObject((Map<String, Object>) obj);
            } else if (obj instanceof String) {
                json = JSON.parseObject((String) obj);
            } else {
                json = JSON.parseObject(JSON.toJSONString(obj));
            }
            if (json == null || json.isEmpty()) {
                return null;
            }
            LoopState state = new LoopState();
            state.setLoopNodeId(json.getString("loopNodeId"));
            state.setEndNodeId(json.getString("endNodeId"));
            state.setBodyEntryNodeId(json.getString("bodyEntryNodeId"));
            state.setBodyNodeIds(json.getList("bodyNodeIds", String.class));
            state.setSourceExpr(json.getString("sourceExpr"));
            state.setCurrentItem(json.get("currentItem"));
            // 兼容旧数据：旧版 toMap 保存了完整 items，恢复时保留到运行时缓存
            Object itemsObj = json.get("items");
            if (itemsObj instanceof List) {
                state.setItems(new ArrayList<>((List<Object>) itemsObj));
            }
            state.setIndex(json.getIntValue("index", 0));
            state.setTotal(json.getIntValue("total", 0));
            state.setIterationCount(json.getIntValue("iterationCount", 0));
            state.setResults(json.getList("results", Object.class));
            state.setItemVar(json.getString("itemVar"));
            state.setIndexVar(json.getString("indexVar"));
            state.setResultVar(json.getString("resultVar"));
            state.setMaxIterations(json.getIntValue("maxIterations", 100));
            state.setTimeout(json.getIntValue("timeout", 30000));
            state.setStartTime(json.getLongValue("startTime", 0L));
            state.setInitialized(json.getBooleanValue("initialized", false));
            state.setLastAggregatedIndex(json.getIntValue("lastAggregatedIndex", -1));
            state.setLoopType(json.getString("loopType"));
            state.setBreakExpr(json.getString("breakExpr"));
            state.setConditionExpr(json.getString("conditionExpr"));
            state.setParallel(json.getBooleanValue("parallel", false));
            state.setBatchNo(json.getString("batchNo"));
            state.setParallelLimit(json.getIntValue("parallelLimit", 0));
            state.setContinueOnFail(json.getBooleanValue("continueOnFail", false));
            return state;
        } catch (Exception e) {
            log.warn("LoopState 反序列化失败: {}", obj, e);
            return null;
        }
    }
}
