package com.liukx.expression.engine.core.provider;

import com.github.therapi.runtimejavadoc.*;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

/**
 * The type Spring doc javadoc provider.
 * @author bnasslahsen
 */
public class SpringDocJavadocProvider implements JavadocProvider {

    /**
     * The comment formatter.
     */
    private final CommentFormatter formatter = new CommentFormatter();


    /**
     * Gets class description.
     *
     * @param cl the class
     * @return the class description
     */
    @Override
    public String getClassJavadoc(Class<?> cl) {
        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(cl);
        return formatter.format(classJavadoc.getComment());
    }

    /**
     * Gets param descripton of record class.
     *
     * @param cl the class
     * @return map of field and param descriptions
     */
    @Override
    public Map<String, String> getRecordClassParamJavadoc(Class<?> cl) {
        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(cl);
        return classJavadoc.getRecordComponents().stream()
                .collect(Collectors.toMap(ParamJavadoc::getName, recordClass -> formatter.format(recordClass.getComment())));
    }

    /**
     * Gets method javadoc description.
     *
     * @param method the method
     * @return the method javadoc description
     */
    @Override
    public String getMethodJavadocDescription(Method method) {
        MethodJavadoc methodJavadoc = RuntimeJavadoc.getJavadoc(method);
        return formatter.format(methodJavadoc.getComment());
    }

    /**
     * Gets method javadoc return.
     *
     * @param method the method
     * @return the method javadoc return
     */
    @Override
    public String getMethodJavadocReturn(Method method) {
        MethodJavadoc methodJavadoc = RuntimeJavadoc.getJavadoc(method);
        return formatter.format(methodJavadoc.getReturns());
    }

    /**
     * Gets method throws declaration.
     *
     * @param method the method
     * @return the method throws (name-description map)
     */
    public Map<String, String> getMethodJavadocThrows(Method method) {
        return RuntimeJavadoc.getJavadoc(method)
                .getThrows()
                .stream()
                .collect(toMap(ThrowsJavadoc::getName, javadoc -> formatter.format(javadoc.getComment())));
    }

    /**
     * Gets param javadoc.
     *
     * @param method the method
     * @param name the name
     * @return the param javadoc
     */
    @Override
    public String getParamJavadoc(Method method, String name) {
        MethodJavadoc methodJavadoc = RuntimeJavadoc.getJavadoc(method);
        List<ParamJavadoc> paramsDoc = methodJavadoc.getParams();
        return paramsDoc.stream().filter(paramJavadoc1 -> name.equals(paramJavadoc1.getName())).findAny()
                .map(paramJavadoc1 -> formatter.format(paramJavadoc1.getComment())).orElse(null);
    }

    /**
     * Gets field javadoc.
     *
     * @param field the field
     * @return the field javadoc
     */
    @Override
    public String getFieldJavadoc(Field field) {
        FieldJavadoc fieldJavadoc = RuntimeJavadoc.getJavadoc(field);
        return formatter.format(fieldJavadoc.getComment());
    }

    @Override
    public String getFirstSentence(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        int pOpenIndex = text.indexOf("<p>");
        int pCloseIndex = text.indexOf("</p>");
        int dotIndex = text.indexOf(".");
        if (pOpenIndex != -1) {
            if (pOpenIndex == 0 && pCloseIndex != -1) {
                if (dotIndex != -1) {
                    return text.substring(3, min(pCloseIndex, dotIndex));
                }
                return text.substring(3, pCloseIndex);
            }
            if (dotIndex != -1) {
                return text.substring(0, min(pOpenIndex, dotIndex));
            }
            return text.substring(0, pOpenIndex);
        }
        if (dotIndex != -1
                && text.length() != dotIndex + 1
                && Character.isWhitespace(text.charAt(dotIndex + 1))) {
            return text.substring(0, dotIndex + 1);
        }
        return text;
    }
}
