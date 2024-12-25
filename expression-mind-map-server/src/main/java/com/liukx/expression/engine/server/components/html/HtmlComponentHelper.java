package com.liukx.expression.engine.server.components.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HTML相关组件工具列
 *
 * @author liukaixiong
 * @date 2022/7/18 - 13:15
 */
public class HtmlComponentHelper {

    private HtmlComponentHelper() {
        throw new IllegalStateException("not instance");
    }

    public static List<DefaultDataRender> convertDataRender(Class<? extends Enum> enumsClazz) {
        return convertDataRender(enumsClazz, null, null);
    }

    public static List<DefaultDataRender> convertDataRender(Class<? extends Enum> enumsClazz,
                                                            Function<DataRender, String> labelFunction) {
        return convertDataRender(enumsClazz, null, labelFunction);
    }

    public static <T> List<DefaultDataRender> convertDataRender(Class<? extends Enum> enumsClazz,
                                                                Function<DataRender, String> codeFunction, Function<DataRender, String> labelFunction) {
        return Arrays.stream(enumsClazz.getEnumConstants()).map(enums -> {
            if (enums instanceof DataRender) {
                Object code = ((DataRender) enums).getCode();
                String label = ((DataRender) enums).getLabel();
                if (codeFunction != null) {
                    code = codeFunction.apply((DataRender) enums);
                }
                if (labelFunction != null) {
                    label = labelFunction.apply((DataRender) enums);
                }
                return new DefaultDataRender(code, label);
            } else {
                return new DefaultDataRender(enums.name(), enums.name());
            }
        }).collect(Collectors.toList());
    }

    public static <T> List<DefaultDataRender> convertDataRender(List<T> list, Function<T, Object> code,
                                                                Function<T, String> label) {
        List<DefaultDataRender> dataRenders = new ArrayList<>();
        for (T data : list) {
            Object codeValue = code.apply(data);
            String labelValue = label.apply(data);
            DefaultDataRender dataRender = new DefaultDataRender(codeValue, labelValue);
            dataRenders.add(dataRender);
        }
        return dataRenders;
    }

}
