//package com.liukx.expression.engine.core.utils;
//
//import cn.hutool.core.convert.Convert;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONUtil;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * json解析工具类
// * @author liukaixiong
// * @date : 2022/6/14 - 10:05
// */
//public class Jsons2 {
//
//
//    public static String toJsonString(Object obj){
//        return JSONUtil.toJsonStr(obj);
//    }
//
//    public static <T> T parseObject(String json,Class<T> clazz){
//        return JSONUtil.toBean(json,clazz);
//    }
//    public static <T> List<T> parseList(String json,Class<T> clazz){
//        if(StringUtils.isEmpty(json)){
//            return new ArrayList<>();
//        }
//        JSONArray array = JSONUtil.parseArray(json);
//        return JSONUtil.toList(array,clazz);
//    }
//
//    public static Map<String,Object> parseMap(String json){
//        return JSONUtil.toBean(json,Map.class);
//    }
//
//    public static Map<String,Object> objToMap(Object obj){
//        return Convert.convert(Map.class,obj);
//    }
//
//}
