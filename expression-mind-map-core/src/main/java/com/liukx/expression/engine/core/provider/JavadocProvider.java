package com.liukx.expression.engine.core.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The interface Javadoc provider.
 * @author bnasslashen
 */
public interface JavadocProvider {

    /**
     * Gets class description.
     *
     * @param cl the class
     * @return the class description
     */
    String getClassJavadoc(Class<?> cl);

    /**
     * Gets param descripton of record class.
     *
     * @param cl the class
     * @return map of field and param descriptions
     */
    Map<String, String> getRecordClassParamJavadoc(Class<?> cl);

    /**
     * Gets method description.
     *
     * @param method the method
     * @return the method description
     */
    String getMethodJavadocDescription(Method method);

    /**
     * Gets method javadoc return.
     *
     * @param method the method
     * @return the method javadoc return
     */
    String getMethodJavadocReturn(Method method);

    /**
     * Gets method throws declaration.
     *
     * @param method the method
     * @return the method throws (name-description map)
     */
    Map<String, String> getMethodJavadocThrows(Method method);

    /**
     * Gets param javadoc.
     *
     * @param method the method
     * @param name the name
     * @return the param javadoc
     */
    String getParamJavadoc(Method method, String name);

    /**
     * Gets field javadoc.
     *
     * @param field the field
     * @return the field javadoc
     */
    String getFieldJavadoc(Field field);


    /**
     * Returns the first sentence of a javadoc comment.
     * @param text the javadoc comment's text
     * @return the first sentence based on javadoc guidelines
     */
    String getFirstSentence(String text);
}
