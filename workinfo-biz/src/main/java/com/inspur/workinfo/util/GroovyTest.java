package com.inspur.workinfo.util;

import com.alibaba.fastjson.JSONObject;
import com.inspur.workinfo.constant.CommonConstants;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个是Groovy的第一个小程序，脚本为：
 *
 package groovy

 def helloworld(){
 println "hello world"
 }
 *
 */



public class GroovyTest {

    public static void main(String[] args) throws Exception {
//        System.exit(0);
        //创建GroovyShell
//        GroovyShell groovyShell = new GroovyShell();
//        //装载解析脚本代码
//        Script script = groovyShell.parse("package groovy\n" +
//                "\n" +
//                "def HelloWorld(){\n" +
//                "    println \"hello world\"\n" +
//                "}");
//        //执行
//        script.invokeMethod("HelloWorld", null);


                //创建GroovyShell
                GroovyShell groovyShell = new GroovyShell();
                //装载解析脚本代码
                Script script = groovyShell.parse("package groovy\n" +
                        "\n" +
                        "/**\n" +
                        " * 简易加法\n" +
                        " * @param a 数字a\n" +
                        " * @param b 数字b\n" +
                        " * @return 和\n" +
                        " */\n" +
                        "import groovy.json.JsonSlurper  \n"+
                        "import com.alibaba.fastjson.JSONObject \n"+
                        "import groovy.xml.MarkupBuilder\n" +
                        "import com.inspur.workinfo.util.AesEncryptUtil\n"+
                        "def add(String json) {\n" +

                        "\n" +
                        " def writer = new StringWriter()\n" +
                        "    def builder = new MarkupBuilder(writer)\n" +
                        "    builder.mkp.xmlDeclaration(version: \"1.0\", encoding: \"UTF-8\")\n" +
                        "    builder.ApproveDataInfo{\n" +
                        "       Region(\"数据产生地区的6位行政区划代码（省市县）\")\n" +
                        "       ApplyInfo{\n" +
                        "\t\t\tProjectNo(\"地方政务的办件编号\")\n" +
                        "\t\t\tCataLogCode()\n" +
                        "\t\t\tTaskCode()\n" +
                        "\t\t\tTaskHandleItem()\n" +
                        "\t\t\tTaskName()\n" +
                        "\t\t\tApplyerName()\n" +
                        "\t\t\tApplyerType(\"1\")\n" +
                        "\t\t\tApplyerPageType(\"111\")\n" +
                        "\t\t\tApplyerPageCode(\"\")\n" +
                        "\t\t\tContactName()\n" +
                        "\t\t\tContactMobile()\n" +
                        "\t\t\tApplyDate()\n" +
                        "\t\t\tApplyType()\n" +
                        "\t\t\tDeliverType()\n" +
                        "\t\t\tAddress()\n" +
                        "\t\t\tProjectType()\n" +
                        "\t\t\tSystemNo(\"垂管系统编码\")\n" +
                        "\t\t\tFormData{\n" +
                        "\t\t\t\tsx()\n" +
                        "\t\t\t\tblfs_type()\n" +
                        "\t\t\t\tcdpf_renzheng_version{\n" +
                        "\t\t\t\t\tidcard()\n" +
                        "\t\t\t\t\tname()\n" +
                        "\t\t\t\t\tbrith_time()\n" +
                        "\t\t\t\t\tsex()\n" +
                        "\t\t\t\t\tnation()\n" +
                        "\t\t\t\t\teducation()\n" +
                        "\t\t\t\t\tmarital()\n" +
                        "\t\t\t\t\tresidence()\n" +
                        "\t\t\t\t\tcon_tel()\n" +
                        "\t\t\t\t\tcon_phone()\n" +
                        "\t\t\t\t\tdomicile_area()\n" +
                        "\t\t\t\t\tresidence_area()\n" +
                        "\t\t\t\t\tguard_name()\n" +
                        "\t\t\t\t\tguard_contelphone()\n" +
                        "\t\t\t\t\tguard_phone()\n" +
                        "\t\t\t\t\trelation()\n" +
                        "\t\t\t\t\tdeformity_type()\n" +
                        "\t\t\t\t\tarea_code()\n" +
                        "\t\t\t\t\tresidence_code()\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t}\n" +
                        "\t   }\n" +
                        " MaterialData {\n" +
                                "\t\t\tfor(i=0;i<2;i++){\n" +

                                "Material{}\n"+
                                "\t\t\t}\n" +

                                "\t   }\n"+
                        "    }\n" +
                        "    String result = writer.toString()\n" +
                        "    println result\n"+
                        "}\n" +
                        "\n" +
                        "/**\n" +
                        " * map转化为String\n" +
                        " * @param paramMap 参数map\n" +
                        " * @return 字符串\n" +
                        " */\n" +
                        "def mapToString(Map<String, String> paramMap) {\n" +
                        "    StringBuilder stringBuilder = new StringBuilder();\n" +
                        "    paramMap.forEach({ key, value ->\n" +
                        "        stringBuilder.append(\"key:\" + key + \";value:\" + value)\n" +
                        "    })\n" +
//                        "stringBuilder.append(AesEncryptUtil.sign(\"key\"))\n"+
                        "  def timestamp =  System.currentTimeMillis()\n"+
                        "stringBuilder.append(timestamp)\n"+
                        "    return stringBuilder.toString()\n" +
                        "}");
                //执行加法脚本
                Object[] params1 = new Object[]{"{\"name\":\"John\",\"age\":\"30\",\"city\":\"New York\"}"};
        String sum = (String ) script.invokeMethod("add", params1);
                System.out.println("a加b的和为:" + sum);
                //执行解析脚本
                Map<String, String> paramMap = new HashMap<>();
                paramMap.put("科目1", "语文");
                paramMap.put("科目2", "数学");
                Object[] params2 = new Object[]{paramMap};
                String result = (String) script.invokeMethod("mapToString", params2);
                System.out.println("mapToString:" + result);
                System.exit(0);






            }

}

