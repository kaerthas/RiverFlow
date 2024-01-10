package com.inspur.workinfo.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class BaseDbHelper {

        public String insertXmlDataProvider(Map<String, Object> params) throws Exception {
            try {
//                System.out.println(new SQL() {
//                    {
//
//                        INSERT_INTO(params.get("tableName").toString());
//                        String[] colums = (String[]) params.get("columns");
//                        String keyword = String.valueOf(params.get("keyword"));
//                        for (int i = 0; i < colums.length; i++) {
//                            if (colums[i] != null && params.get(colums[i].toString()) != null) {
//                                //                    VALUES(colums[i],params.get(colums[i].toString()).toString());
//                                VALUES(colums[i], "'" + params.get(colums[i].toString()).toString() + "'");
////                        VALUES(colums[i],"#{"+colums[i]+"}");
//
//                            }
//                        }
//                        if (StrUtil.isNotBlank(keyword)) {
//                            VALUES(keyword, "'" + params.get("keywordvalue").toString() + "'");
//                        }
//                        //暂时写死
//                        VALUES("SEQ_ID", "'" + UUID.randomUUID().toString().replaceAll("-", "") + "'");
//                    }
//                }.toString());
            return new SQL() {
                {

                    INSERT_INTO(params.get("tableName").toString());
                    String[] colums = (String[]) params.get("columns");
                    String keyword = String.valueOf(params.get("keyword"));
                    for (int i = 0; i < colums.length; i++) {
                          if (colums[i]!=null&&params.get(colums[i].toString())!=null){
                      //  if (params.get(colums[i].toString()) != null) {
                            VALUES(colums[i], "'" + params.get(colums[i].toString()).toString() + "'");
                        }
                    }
                    if (StrUtil.isNotBlank(keyword)) {
                        VALUES(keyword, "'" + params.get("keywordvalue").toString() + "'");
                    }
                    VALUES("SEQ_ID", "'" + UUID.randomUUID().toString().replaceAll("-", "") + "'");

                }
            }.toString();
        }catch (Exception e){
                e.printStackTrace();
                throw e;
            }


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


    public String updateXmlDataProvider(Map<String, Object> params) throws Exception {
        try {
            StringBuffer sql = new StringBuffer();
            Map<String,Object> columns   = (Map<String, Object>) params.get("columns");
            if (columns!=null&&columns.size()>0){

                sql.append("UPDATE ");
                sql.append(params.get("tableName").toString()).append(" SET ");
                for (int i = 0; i <columns.size()  ; i++) {
                    if (MapUtil.isNotEmpty(columns)){
                        columns.forEach((k,v) -> sql.append(k).append(" = ").append("'").append(String.valueOf(v)).append("',"));
                    }
                }
                sql.deleteCharAt(sql.lastIndexOf(","))
                        .append(" WHERE ").append(" 1=1 and ").append(params.get("keyword")+" = '"+ params.get("keywordValue")+"'");
                log.error("@@"+sql.toString());
            }
            return sql.toString();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

}
