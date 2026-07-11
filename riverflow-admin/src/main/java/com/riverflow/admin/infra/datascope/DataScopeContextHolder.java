package com.riverflow.admin.infra.datascope;

import java.util.Collections;
import java.util.Set;

/**
 * 数据权限上下文持有者
 * 通过 ThreadLocal 在当前线程传递数据权限配置
 */
public class DataScopeContextHolder {

    private static final ThreadLocal<DataScopeConfig> CONTEXT = new ThreadLocal<>();

    public static void set(DataScopeConfig config) {
        CONTEXT.set(config);
    }

    public static DataScopeConfig get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 数据权限配置
     */
    public static class DataScopeConfig {
        private final int scope;
        private final String deptColumn;
        private final String userColumn;
        private final Set<Long> customDeptIds;

        public DataScopeConfig(int scope, String deptColumn, String userColumn, Set<Long> customDeptIds) {
            this.scope = scope;
            this.deptColumn = deptColumn;
            this.userColumn = userColumn;
            this.customDeptIds = customDeptIds == null ? Collections.emptySet() : customDeptIds;
        }

        public int getScope() {
            return scope;
        }

        public String getDeptColumn() {
            return deptColumn;
        }

        public String getUserColumn() {
            return userColumn;
        }

        public Set<Long> getCustomDeptIds() {
            return customDeptIds;
        }
    }
}
