package com.crte.sipstackhome.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/1/22.
 */
public class CallState implements Parcelable {
    public int accId;
    public int callId;
    public int stateId;
    public boolean useVideo;
    public int stateCode;
    public boolean multiCall;

    public CallState(){}

    public CallState(int accId, int callId, int stateId, boolean useVideo, int stateCode, boolean multiCall) {
        this.accId = accId;
        this.callId = callId;
        this.stateId = stateId;
        this.useVideo = useVideo;
        this.stateCode = stateCode;
        this.multiCall = multiCall;
    }

    private CallState(Parcel in) {
        accId = in.readInt();
        callId = in.readInt();
        stateId = in.readInt();
        useVideo = (in.readInt() == 1) ? true : false;
        stateCode = in.readInt();
        multiCall = (in.readInt() == 1) ? true : false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(accId);
        dest.writeInt(callId);
        dest.writeInt(stateId);
        dest.writeInt(useVideo ? 1 : 0);
        dest.writeInt(stateCode);
        dest.writeInt(multiCall ? 1 : 0);
    }

    public static final Parcelable.Creator<CallState> CREATOR = new Parcelable.Creator<CallState>() {
        public CallState createFromParcel(Parcel in) {
            return new CallState(in);
        }

        public CallState[] newArray(int size) {
            return new CallState[size];
        }
    };

    @Override
    public String toString() {
        return "CallState{" +
                "accId=" + accId +
                ", callId=" + callId +
                ", stateId=" + stateId +
                ", useVideo=" + useVideo +
                ", stateCode=" + stateCode +
                ", multiCall=" + multiCall +
                '}';
    }
}
