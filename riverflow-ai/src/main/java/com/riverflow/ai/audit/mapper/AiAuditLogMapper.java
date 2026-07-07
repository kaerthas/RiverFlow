package com.riverflow.ai.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.audit.entity.AiAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 审计日志 Mapper
 */
@Mapper
public interface AiAuditLogMapper extends BaseMapper<AiAuditLog> {

    @Select("<script>" +
            "SELECT COUNT(*) FROM wf_ai_audit_log " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "</script>")
    Long countTotal(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT COUNT(*) FROM wf_ai_audit_log " +
            "WHERE success = #{success} " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "</script>")
    Long countSuccess(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                      @Param("success") Integer success);

    @Select("<script>" +
            "SELECT COALESCE(SUM(total_tokens), 0) FROM wf_ai_audit_log " +
            "WHERE success = 1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "</script>")
    Long sumTokens(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT COALESCE(AVG(response_time_ms), 0) FROM wf_ai_audit_log " +
            "WHERE success = 1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "</script>")
    Double avgResponseTime(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT scene AS name, COUNT(*) AS value FROM wf_ai_audit_log " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "GROUP BY scene ORDER BY value DESC" +
            "</script>")
    List<Map<String, Object>> groupByScene(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT DATE(create_time) AS name, COUNT(*) AS value FROM wf_ai_audit_log " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "GROUP BY DATE(create_time) ORDER BY name" +
            "</script>")
    List<Map<String, Object>> groupByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT provider AS name, COUNT(*) AS value FROM wf_ai_audit_log " +
            "WHERE 1=1 " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "GROUP BY provider ORDER BY value DESC" +
            "</script>")
    List<Map<String, Object>> groupByProvider(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
