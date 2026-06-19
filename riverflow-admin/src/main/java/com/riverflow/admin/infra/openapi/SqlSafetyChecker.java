package com.riverflow.admin.infra.openapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 语句安全检查器
 * <p>
 * 用于开放接口（api_type = sql）的 SQL 配置校验，限制可执行的语句类型，
 * 防止通过接口配置执行 DDL、高危 DML 或多语句。
 *
 * 规则：
 * 1. 仅允许单条 SELECT / INSERT / UPDATE / DELETE 语句。
 * 2. 禁止 DROP / TRUNCATE / ALTER / CREATE / GRANT / RENAME 等 DDL / DCL。
 * 3. 禁止 EXEC / EXECUTE / CALL 等执行类关键字。
 * 4. UPDATE / DELETE 必须包含 WHERE 子句。
 * 5. 不允许多语句（按分号拆分后必须都是允许的语句）。
 */
public class SqlSafetyChecker {

    private static final Set<String> ALLOWED_STATEMENTS = new HashSet<>(Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE"));

    private static final Set<String> BLACKLIST_KEYWORDS = new HashSet<>(Arrays.asList(
            "DROP", "TRUNCATE", "ALTER", "CREATE", "RENAME", "GRANT", "REVOKE",
            "EXEC", "EXECUTE", "CALL", "SP_EXECUTESQL", "XP_CMDSHELL",
            "DATABASE", "SCHEMA", "PROCEDURE", "FUNCTION", "TRIGGER", "VIEW", "INDEX"
    ));

    private static final Pattern FIRST_TOKEN_PATTERN = Pattern.compile("^\\s*(\\w+)");
    private static final Pattern WORD_PATTERN_TEMPLATE = Pattern.compile("\\b(%s)\\b");

    /**
     * 校验 SQL 安全性
     *
     * @param sql 原始 SQL 语句
     * @return 校验结果
     */
    public static SqlCheckResult validate(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return SqlCheckResult.ok();
        }

        // 按分号拆分多语句
        String[] statements = sql.split(";");
        int nonEmptyCount = 0;
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            nonEmptyCount++;
            SqlCheckResult result = validateSingleStatement(trimmed);
            if (!result.isPassed()) {
                return result;
            }
        }

        if (nonEmptyCount == 0) {
            return SqlCheckResult.fail("SQL 不能为空");
        }

        return SqlCheckResult.ok();
    }

    private static SqlCheckResult validateSingleStatement(String sql) {
        // 1. 去除字符串常量，避免字符串内容误命中关键字
        String noStringSql = removeStringLiterals(sql);

        // 2. 去除注释
        String noCommentSql = removeComments(noStringSql);

        // 3. 提取第一个有效 token 作为语句类型
        String firstToken = extractFirstToken(noCommentSql);
        if (firstToken == null) {
            return SqlCheckResult.fail("无法识别 SQL 语句类型");
        }

        if (!ALLOWED_STATEMENTS.contains(firstToken)) {
            return SqlCheckResult.fail("不允许执行 " + firstToken + " 类型 SQL，仅允许 SELECT/INSERT/UPDATE/DELETE");
        }

        // 4. 黑名单关键字检查（独立单词）
        if (containsBlacklistedKeyword(noCommentSql)) {
            return SqlCheckResult.fail("SQL 包含禁止关键字（如 DROP/TRUNCATE/ALTER 等）");
        }

        // 5. UPDATE / DELETE 必须带 WHERE
        if (("UPDATE".equals(firstToken) || "DELETE".equals(firstToken)) && !containsWhere(noCommentSql)) {
            return SqlCheckResult.fail("UPDATE/DELETE 语句必须包含 WHERE 条件");
        }

        return SqlCheckResult.ok();
    }

    /**
     * 将 SQL 中的字符串常量替换为空字符串，避免字符串内容干扰关键字判断
     */
    private static String removeStringLiterals(String sql) {
        // 匹配单引号字符串，支持转义单引号 ''
        return sql.replaceAll("'(?:[^']|'')*'", "''");
    }

    /**
     * 去除 SQL 注释（块注释和行注释）
     */
    private static String removeComments(String sql) {
        String result = sql.replaceAll("/\\*.*?\\*/", " ");
        result = result.replaceAll("--.*$", " ");
        return result;
    }

    private static String extractFirstToken(String sql) {
        Matcher matcher = FIRST_TOKEN_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return null;
    }

    private static boolean containsBlacklistedKeyword(String sql) {
        String upper = sql.toUpperCase();
        for (String keyword : BLACKLIST_KEYWORDS) {
            Pattern pattern = Pattern.compile(String.format("\\b%s\\b", keyword));
            if (pattern.matcher(upper).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWhere(String sql) {
        return Pattern.compile("\\bWHERE\\b").matcher(sql.toUpperCase()).find();
    }
}
