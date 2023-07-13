package com.inspur.workinfo.util;

import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

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
}
