package com.riverflow.admin.infra.datascope;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.riverflow.admin.infra.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 数据权限拦截器
 * 根据当前登录用户的部门/用户名，自动追加数据范围过滤条件
 */
@Slf4j
public class DataScopeInnerInterceptor extends JsqlParserSupport implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        DataScopeContextHolder.DataScopeConfig config = DataScopeContextHolder.get();
        if (config == null) {
            return;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        if (loginUser.isAdmin()) {
            return;
        }
        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        String originalSql = mpBoundSql.sql();
        String newSql = parserSingle(originalSql, new DataScopeContext(config, loginUser));
        mpBoundSql.sql(newSql);
        log.debug("数据权限 SQL 改写: original={}\nnew={}", originalSql, newSql);
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        DataScopeContext context = (DataScopeContext) obj;
        Select selectBody = select.getSelectBody();
        if (!(selectBody instanceof PlainSelect)) {
            return;
        }
        PlainSelect plainSelect = (PlainSelect) selectBody;
        Expression dataScopeExpression = buildDataScopeExpression(plainSelect, context);
        if (dataScopeExpression == null) {
            return;
        }
        Expression where = plainSelect.getWhere();
        if (where == null) {
            plainSelect.setWhere(dataScopeExpression);
        } else {
            plainSelect.setWhere(new AndExpression(where, dataScopeExpression));
        }
    }

    private Expression buildDataScopeExpression(PlainSelect plainSelect, DataScopeContext context) {
        if (!(plainSelect.getFromItem() instanceof Table)) {
            return null;
        }
        Table table = (Table) plainSelect.getFromItem();
        String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();

        DataScopeContextHolder.DataScopeConfig config = context.getConfig();
        LoginUser loginUser = context.getLoginUser();
        int scope = config.getScope();

        switch (scope) {
            case DataScopeScope.ALL:
                return null;
            case DataScopeScope.DEPT_ONLY:
            case DataScopeScope.DEPT_AND_CHILD:
            case DataScopeScope.CUSTOM:
                return buildDeptExpression(alias, config.getDeptColumn(), config.getCustomDeptIds());
            case DataScopeScope.SELF_ONLY:
                return buildUserExpression(alias, config.getUserColumn(), loginUser.getUsername());
            default:
                return null;
        }
    }

    private Expression buildDeptExpression(String alias, String deptColumn, Set<Long> deptIds) {
        if (!StringUtils.hasText(deptColumn) || CollectionUtils.isEmpty(deptIds)) {
            return null;
        }
        Column column = new Column(alias + "." + deptColumn);
        if (deptIds.size() == 1) {
            return new EqualsTo(column, new LongValue(deptIds.iterator().next()));
        }
        List<Expression> expressions = new ArrayList<>(deptIds.size());
        deptIds.forEach(id -> expressions.add(new LongValue(id)));
        return new InExpression(column, new ExpressionList(expressions));
    }

    private Expression buildUserExpression(String alias, String userColumn, String username) {
        if (!StringUtils.hasText(userColumn) || !StringUtils.hasText(username)) {
            return null;
        }
        Column column = new Column(alias + "." + userColumn);
        return new EqualsTo(column, new StringValue(username));
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        return null;
    }

    private static class DataScopeContext {
        private final DataScopeContextHolder.DataScopeConfig config;
        private final LoginUser loginUser;

        DataScopeContext(DataScopeContextHolder.DataScopeConfig config, LoginUser loginUser) {
            this.config = config;
            this.loginUser = loginUser;
        }

        public DataScopeContextHolder.DataScopeConfig getConfig() {
            return config;
        }

        public LoginUser getLoginUser() {
            return loginUser;
        }
    }
}
