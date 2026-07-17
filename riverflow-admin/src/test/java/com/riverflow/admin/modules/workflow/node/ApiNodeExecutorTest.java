package com.riverflow.admin.modules.workflow.node;

import com.riverflow.api.entity.ApiCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API 节点执行器单元测试
 */
class ApiNodeExecutorTest {

    @Test
    void testIsSuccessCodeDefault() {
        ApiNodeExecutor executor = new ApiNodeExecutor();
        ApiCatalog catalog = new ApiCatalog();

        assertTrue(executor.isSuccessCode(catalog, "200"));
        assertFalse(executor.isSuccessCode(catalog, "1"));
        assertFalse(executor.isSuccessCode(catalog, "500"));
    }

    @Test
    void testIsSuccessCodeWithMultipleCodes() {
        ApiNodeExecutor executor = new ApiNodeExecutor();
        ApiCatalog catalog = new ApiCatalog();
        catalog.setSuccessCode("200,0,1");

        assertTrue(executor.isSuccessCode(catalog, "200"));
        assertTrue(executor.isSuccessCode(catalog, "0"));
        assertTrue(executor.isSuccessCode(catalog, "1"));
        assertFalse(executor.isSuccessCode(catalog, "500"));
    }

    @Test
    void testIsSuccessCodeTrimSpaces() {
        ApiNodeExecutor executor = new ApiNodeExecutor();
        ApiCatalog catalog = new ApiCatalog();
        catalog.setSuccessCode(" 200 , 1 ");

        assertTrue(executor.isSuccessCode(catalog, "200"));
        assertTrue(executor.isSuccessCode(catalog, "1"));
    }
}
