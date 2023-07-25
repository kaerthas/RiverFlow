package com.inspur.workinfo.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

@Slf4j
public class BaseDbHelper {

        public String insertXmlDataProvider(Map<String, Object> params) {

            return new SQL(){{

                INSERT_INTO(params.get("tableName").toString());
                String[]  colums = (String[]) params.get("columns");
                String keyword  =  String.valueOf(params.get("keyword"));
                for (int i = 0; i < colums.length ; i++) {
                    if (params.get(colums[i].toString())!=null){
                        VALUES(colums[i],params.get(colums[i].toString()).toString());
                    }
                }
                if (StrUtil.isNotBlank(keyword)){
                    VALUES(keyword,params.get("keywordvalue").toString());
                }
            }
            }.toString();


        }
        public String selectXmlDataByKeyWord(Map<String, Object> params) {

            StringBuffer sql = new StringBuffer();
            String[] columns   = (String[]) params.get("columns");
            if (columns!=null&&columns.length>0){

                sql.append("SELECT ");
                for (int i = 0; i <columns.length  ; i++) {
                    if (columns[i]!=null) {
                        sql.append(columns[i]).append(",");
                    }
                }
                sql.deleteCharAt(sql.lastIndexOf(","))
                        .append(" FROM ").append(params.get("tableName").toString())
                        .append(" WHERE ").append(" 1=1 and ").append(params.get("keyword")+" = '"+ params.get("keywordValue")+"'");
                log.error("@@"+sql.toString());
            }

            return sql.toString();


        }

}
