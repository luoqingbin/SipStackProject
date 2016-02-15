package com.crte.sipstackhome.dao;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.customview.SideBar;
import com.crte.sipstackhome.models.BaseBean;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.utils.CharacterParser;
import com.crte.sipstackhome.utils.PinyinComparator;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class DaoImpl<T extends BaseBean> {
    private CharacterParser mCharacterParser;
    private PinyinComparator mPinyinComparator;

    /**
     * 添加一条新数据
     */
    public abstract void insertNewDatas(ContentResolver contentResolver, T data);

    /**
     * 删除一条数据
     */
    public abstract void deleteDatas(ContentResolver contentResolver, int userId);

    /**
     * 修改一条数据
     */
    public abstract void updateDatas(ContentResolver contentResolver, T data);

    /**
     * 查询一条数据
     */
    public abstract Cursor queryDatas(ContentResolver contentResolver, int userId);

    /**
     * 添加多条数据
     */
    public abstract void insertAllNewDatas(ContentResolver contentResolver, ArrayList<T> dataList);

    /**
     * 删除多条数据
     */
    public abstract void deleteAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList);

    /**
     * 更新多条数据
     */
    public abstract void updateAllDatas(ContentResolver contentResolver, ArrayList<T> dataList);

    /**
     * 查询多条数据
     */
    public abstract void queryAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList);

    /**
     * 返回ContentValues对象，用于数据库操作
     */
    public abstract ContentValues getContentValuesDatas(T datas);

    public abstract T getPrototype(String data);

    public void setFastIndex(Activity activity) {
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
        List<T> tList = filledData(activity.getResources().getStringArray(R.array.date)); // 测试数据
        // 排序
        Collections.sort(tList, mPinyinComparator);

        // 设置标记
        for (int i = 0; i < SideBar.b.length; i++) {
            char first = SideBar.b[i].toUpperCase().charAt(0);
            for (int j = 0; j < tList.size(); j++) {
                char firstChar = tList.get(j).sortLetters.toUpperCase().charAt(0);
                if (first == firstChar) {
                    tList.get(j).isFister = 1;
                    break;
                }
            }
        }

        for (int i = 0; i < tList.size(); i++) {
            insertNewDatas(activity.getContentResolver(), tList.get(i)); // 目前是一条一条添加，需优化
        }
    }

    private List<T> filledData(String[] date) {
        List<T> mSortList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < date.length; i++) {
            T t = getPrototype(date[i]);
            String pinyin = mCharacterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                t.sortLetters = sortString.toUpperCase();
            } else {
                t.sortLetters = "#";
            }
            // 添加颜色测试
            t.color = random.nextInt(BaseActivity.HEADER_COLOR.length);
            LogUtils.d("DaoImpl", "Color: " + t.color);
            mSortList.add(t);
        }
        return mSortList;
    }
}
