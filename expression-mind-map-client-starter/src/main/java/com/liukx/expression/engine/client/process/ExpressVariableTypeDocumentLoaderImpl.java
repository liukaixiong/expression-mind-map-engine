package com.liukx.expression.engine.client.process;

import com.google.common.base.Splitter;
import com.liukx.expression.engine.client.api.ExpressVariableTypeDocumentLoader;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.provider.SpringDocJavadocProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author liukaixiong
 * @date 2024/11/14 - 15:48
 */
public class ExpressVariableTypeDocumentLoaderImpl implements ExpressVariableTypeDocumentLoader, InitializingBean {

    private final SpringDocJavadocProvider javadocProvider = new SpringDocJavadocProvider();
    private final ClassPathScanningCandidateComponentProvider componentProvider =
            new ClassPathScanningCandidateComponentProvider(false);
    @Autowired
    private ExpressionProperties properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 设置扫描规则
        componentProvider.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
    }

    @Override
    public List<VariableApiModel> loadList() {
        final String injectTypePackage = properties.getInjectTypePackage();
        if (!StringUtils.hasText(injectTypePackage)) {
            return Collections.emptyList();
        }
        final List<String> backageList = Splitter.on(",").splitToList(injectTypePackage);
        List<VariableApiModel> list = new ArrayList<>();
        for (String basePackage : backageList) {
            Set<BeanDefinition> beanDefinitions = componentProvider.findCandidateComponents(basePackage);
            beanDefinitions.forEach(beanDefinition -> {
                try {
                    Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                    final List<VariableApiModel> classVariable = getClassVariable(clazz);
                    if (!CollectionUtils.isEmpty(classVariable)) {
                        list.addAll(classVariable);
                    }
                } catch (ClassNotFoundException e) {

                }
            });
        }
        return list;
    }


    private List<VariableApiModel> getClassVariable(Class<?> clazz) {
        final String simpleName = clazz.getSimpleName();
        final List<Field> allFields = FieldUtils.getAllFieldsList(clazz);
        List<VariableApiModel> varList = new ArrayList<>();
        for (Field field : allFields) {
            final String name = field.getName();
            final String type = field.getType().getSimpleName();
            final String fieldJavadoc = javadocProvider.getFieldJavadoc(field);
            VariableApiModel varApiModel = new VariableApiModel();
            varApiModel.setName(name);
            varApiModel.setDescribe(fieldJavadoc);
            varApiModel.setType(type);
            varApiModel.setGroupName(simpleName);
            varList.add(varApiModel);
        }
        return varList;
    }

}
