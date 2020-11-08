package com.databasedictionary.utils;

/**
 * 符号枚举类
 */
public enum SignEnum {
    /**
     * 逗号
     */
    comma(","),
    /**
     * 点
     */
    point("."),
    /**
     * 左括号
     */
    left_brackets("\\("),
    /**
     * 右括号
     */
    right_brackets("\\)"),
    /**
     * 引号
     */
    single_quotation_marks("'"),
    /**
     * 等号
     */
    equal_sign("="),
    /**
     * 反单引号
     */
    back_quote("`");
    private String desc;

    SignEnum() {

    }

    public String getDesc() {
        return desc;
    }

    SignEnum(String desc) {
        this.desc = desc;
    }
}
