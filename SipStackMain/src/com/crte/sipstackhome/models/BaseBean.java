package com.crte.sipstackhome.models;

/**
 * 保存所有Bean的通用属性
 */
public class BaseBean {
    /**
     * 状态标记 用于标记当前的内容是什么类型
     */
    public static final String FIELD_STATE_FLAG = "state_flag";
    /**
     * 状态描述
     */
    public static final String FIELD_STATE_DESCRIPTION = "state_description";
    /**
     * 显示数据拼音的首字母
     */
    public static final String FIELD_SORT_LETTERS = "sort_letters";
    /**
     * 是否是第一个
     */
    public static final String FIELD_IS_FISTER = "is_fister";
    /**
     * 每个用户的唯一颜色标示
     */
    public static final String FIELD_COLOR = "color";
    /**
     * 外键，可忽略
     */
    public static final String FIELD_PID = "pid";

    public int stateFlag;
    public String stateDescription;
    public String sortLetters;
    public int isFister;
    public int color;
    public int pid;

    @Override
    public String toString() {
        return "BaseBean{" +
                "stateFlag=" + stateFlag +
                ", stateDescription='" + stateDescription + '\'' +
                ", sortLetters='" + sortLetters + '\'' +
                ", isFister=" + isFister +
                '}';
    }
}
