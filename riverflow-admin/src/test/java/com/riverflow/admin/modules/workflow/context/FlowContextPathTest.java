package com.riverflow.admin.modules.workflow.context;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowContext JSONPath 解析测试，覆盖带横线、数组下标等 key
 */
class FlowContextPathTest {

    @Test
    void testJsonPathWithHyphenKey() {
        JSONObject obj = new JSONObject();
        obj.put("X-HW-ID", "12345");
        obj.put("apiResult", JSON.parseObject("{\"data\":[{\"user-name\":\"tom\"}]}"));

        assertEquals("12345", JSONPath.eval(obj, "$['X-HW-ID']"));
        assertEquals("tom", JSONPath.eval(obj, "$['apiResult']['data'][0]['user-name']"));
    }

    @Test
    void testGetByPathWithHyphenKey() {
        FlowContext context = new FlowContext();
        context.set("X-HW-ID", "header-id");
        context.set("X-HW-AppKey", "secret-key");

        assertEquals("header-id", context.getByPath("context.X-HW-ID"));
        assertEquals("secret-key", context.getByPath("X-HW-AppKey"));
    }

    @Test
    void testGetByPathWithNestedHyphenKeyAndArray() {
        FlowContext context = new FlowContext();
        context.set("apiResult", JSON.parseObject("{\"data\":[{\"user-name\":\"tom\"},{\"user-name\":\"jerry\"}]}"));

        assertEquals("tom", context.getByPath("context.apiResult.data[0].user-name"));
        assertEquals("jerry", context.getByPath("apiResult.data[1].user-name"));
    }

    @Test
    void testGetByPathItemWithHyphenKey() {
        FlowContext context = new FlowContext();
        context.set("item", JSON.parseObject("{\"X-HW-ID\":\"item-id\"}"));

        assertEquals("item-id", context.getByPath("item.X-HW-ID"));
    }
}
