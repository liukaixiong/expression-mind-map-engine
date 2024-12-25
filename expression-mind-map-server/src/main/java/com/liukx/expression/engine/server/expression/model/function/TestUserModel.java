package com.liukx.expression.engine.server.expression.model.function;

import com.liukx.expression.engine.core.anno.PropertyDefinition;

public class TestUserModel {
    @PropertyDefinition(value = "用户名称")
    private String name;
    @PropertyDefinition(value = "年龄")
    private int age;
    @PropertyDefinition(value = "主键", required = true)
    private Integer id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
