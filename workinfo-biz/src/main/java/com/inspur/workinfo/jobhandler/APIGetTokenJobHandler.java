package  com.inspur.workinfo.jobhandler;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApiInputInfo;
import com.inspur.workinfo.entity.ApiOutputInfo;
import com.inspur.workinfo.entity.ApiServiceCatalog;
import com.inspur.workinfo.service.ApiInputInfoService;
import com.inspur.workinfo.service.ApiOutputInfoService;
import com.inspur.workinfo.service.ApiServiceCatalogService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.util.RedisCache;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@JobHandler(value = "apiGetTokenJobHandler")
@Component
@Slf4j
public class APIGetTokenJobHandler extends IJobHandler {

    @Autowired
    private ApiServiceCatalogService serviceCatalogService;
    @Autowired
    private ApiInputInfoService inputInfoService;
    @Autowired
    private ApiOutputInfoService outputInfoService;
    @Autowired
    private RedisCache redisCache;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
       // XxlJobLogger.log("Job 001 start timemark:"+new Date());
        boolean isok = false;
        XxlJobLogger.log("【获取消息APIGetTokenTask】开始执行：{}", DateUtil.formatDateTime(new Date()));

        try{
            //获取api类型为token并且需要定时的

            List<ApiServiceCatalog> apiServiceCatalogs =  serviceCatalogService
                    .list(new QueryWrapper<ApiServiceCatalog>()
                            .eq("TYPE", CommonConstants.API_TOKEN)
                            .eq("IS_SCHEDULE","1"));
            //判断是否
            if (apiServiceCatalogs!=null&&apiServiceCatalogs.size()>0){

                for (ApiServiceCatalog apiServiceCatalog:apiServiceCatalogs
                        ) {
                    //从表里获取input参数
                    List<ApiInputInfo> apiInputInfos =  inputInfoService
                            .list(new QueryWrapper<ApiInputInfo>()
                                    .eq("API_ID",apiServiceCatalog.getId())
                                    .eq("TYPE",CommonConstants.API_INPUT_NORMAL));
                    Map<String , Object> map   =   new HashMap<>();
                    apiInputInfos.stream().forEach(n->
                            map.put(n.getKey(),n.getValue())
                    );
                    //包括后台脚本处理
                    R jsonObject = inputInfoService.getServiceByMap(apiServiceCatalog.getId(),map,"");
                    //获取出参配置
                    List<ApiOutputInfo> apiOutputInfos =  outputInfoService
                            .list(new QueryWrapper<ApiOutputInfo>().eq("API_ID",apiServiceCatalog.getId()));
                    if (apiOutputInfos != null&&apiInputInfos.size()>0) {
                        Object objectData =jsonObject.getData();
                        JSONObject res =   JSONObject.parseObject(objectData.toString());
                        //存放redis
                        //计算过期时间然后处理
                        for (ApiOutputInfo apiOutputInfo:
                                apiOutputInfos) {

                            redisCache.setCacheObject(CommonConstants.BACK_END_PROJECT+"_"+apiServiceCatalog.getId()+"_"+apiOutputInfo.getKey(), res.get(apiOutputInfo.getKey()),Integer.valueOf(getRemainSecondsOneDay(new Date())+""), TimeUnit.SECONDS);

                        }
                    }
                }
            }
            isok =true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

        XxlJobLogger.log("【获取消息APIGetTokenTask】结束执行：{}", DateUtil.formatDateTime(new Date()));
        if(isok){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }

    //控制当天零点过期
    private long getRemainSecondsOneDay(Date currentDate) {
        //使用plusDays加传入的时间加1天，将时分秒设置成0
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        //使用ChronoUnit.SECONDS.between方法，传入两个LocalDateTime对象即可得到相差的秒数
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return  seconds;
    }
}
