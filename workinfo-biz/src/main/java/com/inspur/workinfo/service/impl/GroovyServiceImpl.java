/*
 *    Copyright (c) 2018-2025, yunho All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the yunho.io developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: yunho
 */
package com.inspur.workinfo.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inspur.workinfo.entity.PreApasinfo;
import com.inspur.workinfo.mapper.PreApasinfoMapper;

import com.inspur.workinfo.service.GroovyService;
import com.inspur.workinfo.util.RedisCache;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class GroovyServiceImpl extends ServiceImpl<PreApasinfoMapper, PreApasinfo> implements GroovyService {

    private static final Map<String,Script> cache = new ConcurrentHashMap<>();

    public JSONObject invokeScript(String scriptString, String args)throws Exception{
        JSONObject result = new JSONObject();
        GroovyShell groovyShell = new GroovyShell();
        try {
            scriptString = "package groovy\n" +
                    "import groovy.json.JsonSlurper  \n"+
                    "import com.alibaba.fastjson.JSONObject \n"+
                    "import com.alibaba.fastjson.JSONArray \n"+
                    "import groovy.xml.MarkupBuilder\n" +
                    "import java.text.SimpleDateFormat\n"+
                    "import com.inspur.workinfo.util.AesEncryptUtil\n"+
                    "import com.inspur.workinfo.util.AESUtils\n"+
                    "import com.inspur.workinfo.util.UploadUtil\n"+
                    "import java.util.UUID\n"+
                    "import cn.hutool.core.util.StrUtil\n"+
                    "import groovy.xml.Namespace\n" +
                    "import groovy.xml.QName\n"+
                    "import groovy.xml.XmlUtil\n"+
                    "def GroovyScript(String args){" +
                        scriptString +
                    "}";
            //装载解析脚本代码
            String scriptMd5 = DigestUtils.md5DigestAsHex(scriptString.getBytes());
            Script script;
            if (cache.get(scriptMd5)!=null){
               script = cache.get(scriptMd5);
            }else{
                script = groovyShell.parse(scriptString);
                cache.put(scriptMd5,script);
            }
            result = (JSONObject) script.invokeMethod("GroovyScript",new Object[]{args});
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw e;
        }
        return result;
    }



}
