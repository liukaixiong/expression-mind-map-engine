package com.liukx.expression.engine.client.helper;

import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liukaixiong
 * @date 2025/12/9 - 18:18
 */
public class FunHelper {
    public static List<Object> convertArgsList(Map<String, Object> env, AviatorObject[] args) {
        List<Object> argList = new ArrayList<>();

        for (AviatorObject arg : args) {
            argList.add(arg.getValue(env));
        }

        return argList;
    }
}
