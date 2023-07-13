package com.inspur.workinfo.util;

import cn.hutool.core.util.StrUtil;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 *  XML文档组装和解析工具类
 *  @author lrq
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XmlHandleUtil {
    /**
     * 缺省字符集
     * */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 私有构造函数，阻止非法调用构造函数
     * */
    private XmlHandleUtil() {
    }

    /**
     * Return the child element with the given name.  The element must be in
     *   the same name space as the parent element.
     *  @param element The parent element
     *  @param name The child element name
     *  @return The child element
     */
    public static Element child(Element element, String name) {
        return element.element(new QName(name, element.getNamespace()));
    }

    /**
     * 得到给定结点下的孩子节点
     * @param element 节点
     * @param name 子节点名称
     * @param optional 是否是可选的
     * @return 子节点
     * @throws Exception
     */
    public static Element child(Element element,
                                String name,
                                boolean optional)
            throws Exception {
        Element child = element.element(new QName(name, element.getNamespace()));
        if (child == null && !optional) {
            throw new Exception(name + " element expected as child of " +
                    element.getName() + ".");
        }
        return child;
    }

    /** Return the child elements with the given name.  The elements must be in
     the same name space as the parent element.
     @param element The parent element
     @param name The child element name
     @return The child elements
     */
    public static List children(Element element, String name) {
        return element.elements(new QName(name, element.getNamespace()));
    }


    /**
     * 得到某个节点下的属性信息
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static String getAttribute(Element element,
                                      String name,
                                      boolean optional)
            throws Exception {
        Attribute attr = null;
        if(element!=null)
            attr = element.attribute(name);
        if (attr == null && !optional) {
            if(element!=null)
                throw new Exception("Attribute " + name + " of " +
                        element.getName() + " expected.");
            else
                return null;
        } else if (attr != null) {
            return attr.getValue();
        }
        else {
            return null;
        }
    }

    /**
     * 得到节点属性值，并且作为日期型返回
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */

    public static Date getAttributeAsDate(Element element,
                                                    String name,
                                                    boolean optional)
            throws Exception {
        String value = getAttribute(element, name, optional);
        if ( (optional) && ( (value == null) || (value.equals("")))) {
            return null;
        }
        else {
            try {
                //如果可选就不抛出异常
                return DateUtils.formatDate(value,"yyyy-MM-dd HH:mm:ss");
            }
            catch (ParseException exception) {
                throw new Exception(element.getName() + "/@" + name +
                        " attribute: value format error.",
                        exception);
            }
        }
    }

    /**
     * 得到某个节点下的属性信息，值以字符串的形式返回
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static String getAttributeAsString(Element element,
                                              String name,
                                              boolean optional)
            throws Exception {
        return getAttribute(element, name, optional);
    }

    /**
     * 得到某个节点下的属性信息，值以整数的形式返回。
     * 如果没有值或是转化为整形，那么抛出异常。
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static int getAttributeAsInt(Element element,
                                        String name,
                                        boolean optional)
            throws Exception {
        try {
            return Integer.parseInt(getAttribute(element, name, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() + "/@" + name +
                    " attribute: value format error.",
                    exception);
        }
    }

    /**
     * 得到某个节点下的属性信息，值以整数的形式返回。
     * 如果该值是可选的，并且没有该值的话，就返回调用者提供缺省值。
     * @param element 节点
     * @param name 属性名
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static int getAttributeAsInt(Element element,
                                        String name,
                                        int defaultValue,
                                        boolean optional)
            throws Exception {
        String value = getAttribute(element, name, optional);
        if ( (optional) && ( (value == null) || (value.equals("")))) {
            return defaultValue;
        }
        else {
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() + "/@" + name +
                        " attribute: value format error.",
                        exception);
            }
        }
    }

    /**
     * 得到某个节点下的属性信息，值以float的形式返回。
     * 如果没有值或是转化为float，那么抛出异常。
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static float getAttributeAsFloat(Element element,
                                            String name,
                                            boolean optional)
            throws Exception {
        try {
            return Float.parseFloat(getAttribute(element, name, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() + "/@" + name +
                    " attribute: value format error.",
                    exception);
        }
    }

    /**
     * 得到某个节点下的属性信息，值以float的形式返回。
     * 如果没有值,返回缺省值；如果有，那么转化为float，如果不能转化那么抛出异常。
     * @param element 节点
     * @param name 属性名
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static float getAttributeAsFloat(Element element,
                                            String name,
                                            float defaultValue,
                                            boolean optional)
            throws Exception {
        String value = getAttribute(element, name, optional);
        if ( (optional) && ( (value == null) || (value.equals("")))) {
            return defaultValue;
        }
        else {
            try {
                return Float.parseFloat(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() + "/@" + name +
                        " attribute: value format error.",
                        exception);
            }
        }
    }

    /**
     * 得到某个节点下的属性信息，值以长整数的形式返回。
     * 如果没有值或是转化为整形，那么抛出异常。
     * @param element 节点
     * @param name 属性名
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static long getAttributeAsLong(Element element,
                                          String name,
                                          boolean optional)
            throws Exception {
        try {
            return Long.parseLong(getAttribute(element, name, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() + "/@" + name +
                    " attribute: value format error.",
                    exception);
        }
    }

    /**
     * 得到某个节点下的属性信息，值以整数的形式返回。
     * 如果该值是可选的，并且没有该值的话，就返回调用者提供缺省值。
     * @param element 节点
     * @param name 属性名
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static long getAttributeAsLong(Element element, String name, long defaultValue, boolean optional) throws Exception {
        String value = getAttribute(element, name, optional);
        if ( (optional) && ( (value == null) || (value.equals("")))) {
            return defaultValue;
        }
        else {
            try {
                return Long.parseLong(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() + "/@" + name +
                        " attribute: value format error.",
                        exception);
            }
        }
    }

    /**
     * 得到某个节点下的某名字的第一个孩子节点
     * @param element 节点
     * @param name 子节点名称
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static Element getFirstChild(Element element,
                                        String name,
                                        boolean optional)
            throws Exception {
        List list = element.elements(new QName(name,
                element.getNamespace()));
        //如果数目大于0，那么直接取第一个就可以了
        if (list.size() > 0) {
            return (Element) list.get(0);
        }
        else {
            if (!optional) {
                throw new Exception(name +
                        " element expected as first child of " +
                        element.getName() + ".");
            }
            else {
                return null;
            }
        }
    }

    /**
     * 得到同名兄弟节点,同名的第一个节点，可以是自己
     * @param element 节点
     * @param optional 是否是可选的
     * @return 节点
     * @throws Exception
     */
    public static Element getSibling(Element element, boolean optional)
            throws Exception {
        return getSibling(element, element.getName(), optional);
    }

    /**
     * 按名称得到兄弟节点
     * @param element 节点
     * @param name 子节点名称
     * @param optional 是否是可选的
     * @return 节点
     * @throws Exception
     */
    public static Element getSibling(Element element,
                                     String name,
                                     boolean optional)
            throws Exception {
        List<?> list = element.getParent().elements(name);
        if (list.size() > 0) {
            return (Element) list.get(0);
        }
        else {
            if (!optional) {
                throw new Exception(name + " element expected after " +
                        element.getName() + ".");
            }
            else {
                return null;
            }
        }
    }

    /**
     * 得到给定节点的值,以字符串返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static String getContent(Element element, boolean optional)
            throws Exception {
        String content = null;
        if(element!=null)
            content =element.getText();
        if (content == null && !optional) {
            if(element!=null)
                throw new Exception(element.getName() +
                        " element: content expected.");
            else
                return null;
        } else {
            return content;
        }
    }

    /**
     * 得到给定节点的值,以字符串返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static String getContentAsString(Element element, boolean optional)
            throws Exception {
        return getContent(element, optional);
    }

    /**
     * 得到给定节点的值,以整数类型返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static int getContentAsInt(Element element, boolean optional)
            throws Exception {
        try {
            return Integer.parseInt(getContent(element, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() +
                    " element: content format error.",
                    exception);
        }
    }

    /**
     * 得到给定节点的值,以整数类型返回
     * @param element 节点
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static int getContentAsInt(Element element,
                                      int defaultValue,
                                      boolean optional)
            throws Exception {
        String value = getContent(element, optional);
        if ( (optional) && (value == null || value.equals(""))) {
            return defaultValue;
        }
        else {
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() +
                        " element: content format error.",
                        exception);
            }
        }
    }

    /**
     * 得到给定节点的值,以长整类型返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static long getContentAsLong(Element element, boolean optional)
            throws Exception {
        try {
            return Long.parseLong(getContent(element, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() +
                    " element: content format error.",
                    exception);
        }
    }

    /**
     * 得到给定节点的值,以整数类型返回
     * @param element 节点
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static long getContentAsLong(Element element,
                                        long defaultValue,
                                        boolean optional)
            throws Exception {
        String value = getContent(element, optional);
        if ( (optional) && (value == null || value.equals(""))) {
            return defaultValue;
        }
        else {
            try {
                return Long.parseLong(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() +
                        " element: content format error.",
                        exception);
            }
        }
    }

    /**
     * 得到给定节点的值,以浮点类型返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static float getContentAsFloat(Element element, boolean optional)
            throws Exception {
        try {
            return Float.parseFloat(getContent(element, optional));
        }
        catch (NumberFormatException exception) {
            throw new Exception(element.getName() +
                    " element: content format error.",
                    exception);
        }
    }

    /**
     * 得到给定节点的值,以浮点类型返回
     * @param element 节点
     * @param defaultValue 缺省值
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     */
    public static float getContentAsFloat(Element element,
                                          float defaultValue,
                                          boolean optional)
            throws Exception {
        String value = getContent(element, optional);
        if ( (optional) && (value == null || value.equals(""))) {
            return defaultValue;
        }
        else {
            try {
                return Float.parseFloat(value);
            }
            catch (NumberFormatException exception) {
                throw new Exception(element.getName() +
                        " element: content format error.",
                        exception);
            }
        }
    }

    /**
     * 得到给定节点的值,以日期类型返回
     * @param element 节点
     * @param optional 是否是可选的
     * @return 值
     * @throws Exception
     * @throws ParseException
     */
    public static Date getContentAsDate(Element element,
                                                  boolean optional)
            throws Exception, ParseException {
        String value = getContent(element, optional);
        if ( (optional) && (value == null || value.equals(""))) {
            return null;
        }
        else {
            try {
                return DateUtils.formatDate(value, "yyyy-MM-dd HH:mm:ss");
            }
            catch (ParseException exception) {
                throw new Exception(element.getName() +
                        " element: content format error.",
                        exception);
            }
        }
    }

    /**
     * 给定父节点和子节点名称，得到子节点值
     * @param root 父节点
     * @param subTagName 子节点
     * @return 值
     */
    public static String getSubTagValue(Element root, String subTagName) {
        String returnString = StrUtil.EMPTY;
        try {
            returnString = root.elementText(subTagName);
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnString;
    }

    /**
     * 给定父节点，子节点名称，孙节点名称；得到值
     * @param root   父节点
     * @param tagName 子节点名称
     * @param subTagName 孙节点名称
     * @return 值
     */
    public static String getSubTagValue(Element root,
                                        String tagName,
                                        String subTagName) {
        Element child = root.element(tagName);
        String returnString = child.elementText(subTagName);
        return returnString;
    }

    /**
     * 新Element节点，值为String类型
     * @param parent 父节点
     * @param name 新节点名称
     * @param value 新节点值
     * @return element
     * @throws Exception
     */
    public static Element appendChild(Element parent,
                                      String name,
                                      String value) {
        Element element = parent.addElement(new QName(name, parent.getNamespace()));
        if (value != null) {
            element.addText(value);
        }
        return element;
    }

    /**
     * 增加新Element节点，无值
     * @param parent 父节点
     * @param name 新节点名称
     * @return Element 新建节点
     * @throws Exception
     */
    public static Element appendChild(Element parent, String name) {
        return parent.addElement(new QName(name, parent.getNamespace()));
    }

    /**
     * 增加新Element节点，值为int类型
     * @param parent 父节点
     * @param name 新节点名称
     * @param value 新节点值
     * @return element
     * @throws Exception
     */
    public static Element appendChild(Element parent,
                                      String name,
                                      int value) {
        return appendChild(parent, name, String.valueOf(value));
    }

    /**
     * 增加新Element节点，值为长整形
     * @param parent 父节点
     * @param name 新节点名称
     * @param value 新节点值
     * @return element
     * @throws Exception
     */
    public static Element appendChild(Element parent,
                                      String name,
                                      long value) {
        return appendChild(parent, name, String.valueOf(value));
    }

    /**
     * 新加一个float值类型的节点，值为浮点型
     * @param parent 父节点
     * @param name 新节点的名称
     * @param value 新节点的值
     * @return element
     * @throws Exception
     */
    public static Element appendChild(Element parent,
                                      String name,
                                      float value) {
        return appendChild(parent, name, String.valueOf(value));
    }

    /**
     * 增加新Element节点，值为日期型
     * @param parent 父节点
     * @param name 新节点名称
     * @param value 新节点值
     * @return element
     * @throws Exception
     */
    public static Element appendChild(Element parent, String name, Date value) {

        return appendChild(parent, name, value.toString());
    }

    /**
     * 检查文档dtd定义是否正确
     * @param document 文档节点
     * @param dtdPublicId dtd定义
     * @return boolean  相同返回true,否则false
     */
    public static boolean checkDocumentType(Document document,
                                            String dtdPublicId) {
        DocumentType documentType = document.getDocType();
        if (documentType != null) {
            String publicId = documentType.getPublicID();
            return publicId != null && publicId.equals(dtdPublicId);
        }
        return true;
    }

    /**
     * 新建文档
     * @return Document  文档节点
     * @throws Exception
     */
    public static Document createDocument()
            throws Exception {
        DocumentFactory factory = new DocumentFactory();
        Document document = factory.createDocument();
        return document;
    }

    /**
     * 通过Reader读取Document文档
     * 如果encodingStr为null或是""，那么采用缺省编码GB2312
     * @param in Reader器
     * @param encoding 编码器
     * @return documment
     * @throws Exception 
     */
    public static Document fromXML(Reader in, String encoding)
            throws Exception {
        try {
            if (encoding == null || encoding.equals("")) {
                encoding = DEFAULT_ENCODING;
            }
            SAXReader reader = new SAXReader();
            Document document = reader.read(in, encoding);
            return document;
        }
        catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    /**
     * 给定输入流读取XML的Document。
     * 如果encodingStr为null或是""，那么采用缺省编码GB2312
     * @param inputSource 输入源
     * @param encoding 编码器
     * @return document
     * @throws Exception
     */
    public static Document fromXML(InputStream inputSource, String encoding)
            throws Exception {
        try {
            if (encoding == null || encoding.equals("")) {
                encoding = DEFAULT_ENCODING;
            }
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputSource, encoding);
            return document;
        }
        catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    /**
     * 直接从字符串得到XML的Document
     * @param source 把一个字符串文本转化为XML的Document对象
     * @param encoding 编码器
     * @return <code>Document</code>
     * @throws Exception
     */
    public static Document fromXML(String source, String encoding)
            throws Exception {
        return fromXML(new StringReader(source), encoding);
    }

    /**
     * 把XML的Document转化为java.io.Writer输出流
     * 不支持给定Schema文件的校验
     * @param document XML文档
     * @param outWriter 输出写入器
     * @param encoding 编码类型
     * @throws Exception 如果有任何异常转化为该异常输出
     */
    public static void toXML(Document document, Writer outWriter,
                             String encoding)
            throws Exception {
        //
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        if (encoding == null || encoding.trim().equals("")) {
            encoding = DEFAULT_ENCODING;
        }
        //设置编码类型
        outformat.setEncoding(encoding);
        XMLWriter xmlWriter = null;
        try {
            xmlWriter = new XMLWriter(outWriter, outformat);
            xmlWriter.write(document);
            xmlWriter.flush();
        }
        catch (IOException ex) {
            throw new Exception(ex);
        }
        finally {
            if (xmlWriter != null) {
                try {
                    xmlWriter.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    /**
     * 把XML的Document转化为java.io.Writer输出流
     * 不支持给定Schema文件的校验
     * @param document XML文档
     * @param outStream 输出写入器
     * @param encoding 编码类型
     * @throws Exception 如果有任何异常转化为该异常输出
     */
    public static void toXML(Document document, OutputStream outStream,
                             String encoding)
            throws Exception {
        //
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        if (encoding == null || encoding.trim().equals("")) {
            encoding = DEFAULT_ENCODING;
        }
        //设置编码类型
        outformat.setEncoding(encoding);
        XMLWriter xmlWriter = null;
        try {
            xmlWriter = new XMLWriter(outStream, outformat);
            xmlWriter.write(document);
            xmlWriter.flush();
        }
        catch (IOException ex) {
            throw new Exception(ex);
        }
        finally {
            if (xmlWriter != null) {
                try {
                    xmlWriter.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    /**
     * 把XML文档转化为String返回
     * @param document 要转化的XML的Document
     * @param encoding 编码类型
     * @return <code>String</code>
     * @throws Exception 如果有任何异常转化为该异常输出
     */
    public static String toXML(Document document, String encoding)
            throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        toXML(document, stream, encoding);
        if (stream != null) {
            try {
                stream.close();
            }
            catch (IOException ex) {
            }
        }
        return stream.toString();
    }

    //自测试代码
//    public static void main(String[] args) throws IOException, Exception {
        // 第一种方式：创建文档，并创建根元素  
        // 创建文档:使用了一个Helper类  
/*        Document document = DocumentHelper.createDocument();  
        // 创建根节点并添加进文档  
        Element root = DocumentHelper.createElement("RESULT"); */ 
    	
//        Element root = DocumentHelper.createElement("RESULT");
//        Document document = DocumentHelper.createDocument(root);
//        document.setRootElement(root);
//
//        Element helloElement = root.addElement("hello");
//        helloElement.addText("200");
//
//        // 输出
//        XmlHandleUtil xmlUtil = new XmlHandleUtil();
//        String xml  = xmlUtil.toXML(document, "utf-8");
//        System.out.println(xml);
        // 输出到控制台  
//        XMLWriter xmlWriter = new XMLWriter();  
//        xmlWriter.write(document);  

        //输出到文件  
        //其中的"  "表示格式，true参数表示另起一行，gb2312表示编码,如果不写这个参数则默认utf-8编码  
        //1、OutputFormat format=new OutputFormat("  ",true,"gb2312");   
        //生成压缩格式、紧凑格式的xml  其中的compactFormat 翻译：压缩格式      
        //2、 OutputFormat format = OutputFormat.createCompactFormat();  
        //调用静态方法创建一个没有格式的打印方式  
        //3、 OutputFormat format = OutputFormat.createPrettyPrint();      
        //format.setEncoding("gb2312");  // 设置编码  
//        OutputFormat format = new OutputFormat("  ", true);// 设置缩进为2个空格，并且另起一行为true  
//        XMLWriter xmlWriter2 = new XMLWriter(  
//                new FileOutputStream("F:/student1.xml"), format);  
//        xmlWriter2.write(document2);  
//        xmlWriter2.flush();  
//        xmlWriter2.close();  

        // 另一种输出方式，记得要调用flush()方法,否则输出的文件中显示空白，调用close() 方法释放资源  
//        XMLWriter xmlWriter3 = new XMLWriter(new FileWriter("F:/student2.xml"),  
//                format);  
//        xmlWriter3.write(document2);  
//        xmlWriter3.flush();  
//        xmlWriter3.close();  
//    }

}