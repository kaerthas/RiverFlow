package com.inspur.workinfo.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author : Jason
 * @date : 2020/6/17 11:04
 * @description :
 */
public interface StaConstants {
    String totalstaQuerySql="select count(distinct t.projid) totalNum,count(case when t.isok='2' then t.projid else null end) errNum" +
            " from %s t where t.sysmark = '%s'";
    String TOTAL_STA_SQL="select count(distinct t.projid) totalNum,count(case when t.isok='2' then t.projid else null end) errNum" +
            " from %s t inner join EA_JC_STEP_BASICINFO b on t.projid = b.projid where %s and b.cd_operation!='D' ";
    String APPLY_TABLE = "PRE_APASINFO";
    String ACCEPT_TABLE = "EA_JC_STEP_BASICINFO";
    String PROC_TABLE = "EA_JC_STEP_PROC";
    String DONE_TABLE = "EA_JC_STEP_DONE";
    String SPECIAL_TABLE = "EA_JC_STEP_SPECIALNODE";
    String FILE_TABLE = "PRE_FILE";
    List<String> staTableList= Arrays.asList(APPLY_TABLE,ACCEPT_TABLE,PROC_TABLE
            ,DONE_TABLE,SPECIAL_TABLE,FILE_TABLE
//            ,"EA_JC_FEEINFO","EA_JC_SERVEVAL","PRE_COMM_FORM","PRE_FORM_FILE"
    );

    String PUSH_SERVER = "/homedata/server";
}
