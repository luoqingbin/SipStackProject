package com.crte.sipstackhome.models;

/**
 * 录音文件
 *
 * @author Administrator
 */
public class AudioFileBean extends BaseBean {
    public int _id;
    public String fileName; // 文件名
    public String time; // 创建时间
    public String fromName; // 接听方
    public String toName; // 拨打方

    @Override
    public String toString() {
        return "AudioFileBean [_id=" + _id + ", fileName=" + fileName + ", time=" + time + ", fromName=" + fromName
                + ", toName=" + toName + "]";
    }
}
