package com.databasedictionary.utils;

/**
 * 字段基础信息枚举类
 */
public enum ColumnBasicEnum {
    /**
     * 列名-字段
     */
    Field("Field"),
    /**
     * 列名-字段
     */
    Extra("Extra"),
    /**
     * 类型字段
     */
    Type("Type"),
    /**
     * 注释字段
     */
    Comment("Comment"),
    /**
     * 是否为空字段
     */
    Null("Null"),
    /**
     * 默认值字段
     */
    Default("Default");
    private String desc;

    ColumnBasicEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
