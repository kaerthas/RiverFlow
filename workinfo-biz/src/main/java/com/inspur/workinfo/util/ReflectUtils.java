package com.inspur.workinfo.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils<T> {
        /**
         * 对属性进行赋值
         */
        public void fieldIsNull(Class<?> aClass,T data){

            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {

                try {
                    String name = field.getName();//获取属性名
                    name = name.substring(0,1).toUpperCase()+name.substring(1);//将属性名首字母大写
                    String type = field.getGenericType().toString();//获取属性的类型
                    Method getMethod = data.getClass().getMethod("get" + name);//获取get方法
                    Object invoke = getMethod.invoke(data);//获取该属性的值
                    if (invoke==null) {
                        if (type.equals("class java.lang.String")) {//如果是字符串类型
                            Method method = data.getClass().getMethod("set" + name, String.class);//获取set方法
                            method.invoke(data, "未命名");
                        }
                        if (type.equals("class java.lang.Integer")) {
                            Method method = data.getClass().getMethod("set" + name, Integer.class);
                            //给对象的这个属性赋值 Integer类型 此处赋值为0
                            method.invoke(data, 0);
                        }
                        if (type.equals("class java.lang.Long")) {
                            Method method = data.getClass().getMethod("set" + name, Long.class);
                            //给对象的这个属性赋值 Long类型 此处赋值为0
                            method.invoke(data, 0L);
                        }
                        if (type.equals("int")) {
                            Method method = data.getClass().getMethod("set" + name, int.class);
                            //给对象的这个属性赋值 int类型
                            method.invoke(data, 0);
                        }
                        if (type.equals("long")) {
                            Method method = data.getClass().getMethod("set" + name, long.class);
                            //给对象的这个属性赋值 long类型
                            method.invoke(data, 0L);
                        }
                        //其他类型的属性自行添加、判断
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
}
