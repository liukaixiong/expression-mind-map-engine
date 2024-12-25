package com.liukx.expression.engine.core.utils;


import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonsTest {

    @Test
    public void parseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a1", "b");
        map.put("a2", "b");
        map.put("a3", "b");

        final String jsonString = Jsons.toJsonString(map);

        final Map<String, Object> stringObjectMap = Jsons.parseMap(jsonString);
        System.out.println(stringObjectMap);


    }

    @Test
    public void objToMap() {

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setBusinessCode("a");
        request.setUnionId("a");
        request.setEventName("a");

        final Map<String, Object> stringObjectMap = Jsons.objToMap(request);
        System.out.println(stringObjectMap);
    }
}
