package com.crte.sipstackhome.dao;

import com.crte.sipstackhome.models.BaseBean;

/**
 * 用于生成对应的布局格表示
 * Created by Administrator on 2015/11/5 0005.
 */
public class BaseDao {
    public static final int VIEW_TITLE = 1; // 标题
    public static final int VIEW_CONTENT = 2; // 基本内容
    public static final int VIEW_MESSAGE = 3; // 短信
    public static final int VIEW_PHONE = 4; // 通话记录
    public static final int VIEW_GROUP = 5; // 组信息
    public static final int VIEW_CALL = 6; // 拨号界面

    public static BaseBean getBaseBeanLayout(int state, String content) {
        BaseBean baseBean = new BaseBean();
        baseBean.stateFlag = state;
        baseBean.stateDescription = content;
        return baseBean;
    }
}
