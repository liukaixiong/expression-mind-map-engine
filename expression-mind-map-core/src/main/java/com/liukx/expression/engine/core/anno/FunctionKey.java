//package com.liukx.expression.engine.core.anno;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
///**
// * 函数注册
// */
//@Retention(RetentionPolicy.RUNTIME)
//@Target({ElementType.METHOD, ElementType.TYPE})
//public @interface FunctionKey {
//
//    /**
//     * 名称
//     *
//     * @return
//     */
//    String name();
//
//    /**
//     * 描述
//     *
//     * @return
//     */
//    String describe();
//
//    /**
//     * 请求类型
//     * @return
//     */
//    Class<?> requestClass();
//
//    String requestExample() default "";
//}
