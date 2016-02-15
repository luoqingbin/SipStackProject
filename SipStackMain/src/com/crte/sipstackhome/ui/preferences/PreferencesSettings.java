package com.crte.sipstackhome.ui.preferences;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.crte.sipstackhome.api.SipConfigManager;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 偏好设置工具
 * Created by Administrator on 2015/12/25.
 */
public class PreferencesSettings {
    private static String TAG = "PreferencesSettings";

    private Context mContext;
    private ContentResolver mContentResolver;
    private ConnectivityManager mConnectivityManager;

    public PreferencesSettings(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /*
    * 获得各种不同类型的偏好
    * */
    public boolean getPreferenceBooleanValue(String string, boolean b) {
        return SipConfigManager.getPreferenceBooleanValue(mContext, string, b);
    }

    public Boolean getPreferenceBooleanValue(String string) {
        return SipConfigManager.getPreferenceBooleanValue(mContext, string);
    }

    public String getPreferenceStringValue(String key) {
        return SipConfigManager.getPreferenceStringValue(mContext, key);
    }

    public String getPreferenceStringValue(String key, String defaultVal) {
        return SipConfigManager.getPreferenceStringValue(mContext, key, defaultVal);
    }

    public int getPreferenceIntegerValue(String key) {
        return SipConfigManager.getPreferenceIntegerValue(mContext, key);
    }

    public float getPreferenceFloatValue(String key) {
        return SipConfigManager.getPreferenceFloatValue(mContext, key);
    }

    public float getPreferenceFloatValue(String key, float f) {
        return SipConfigManager.getPreferenceFloatValue(mContext, key, f);
    }

    public void setPreferenceStringValue(String key, String newValue) {
        SipConfigManager.setPreferenceStringValue(mContext, key, newValue);
    }

    public void setPreferenceBooleanValue(String key, boolean newValue) {
        SipConfigManager.setPreferenceBooleanValue(mContext, key, newValue);
    }

    public void setPreferenceFloatValue(String key, float newValue) {
        SipConfigManager.setPreferenceFloatValue(mContext, key, newValue);
    }

    /**
     * 设置在WiFi是否可用
     */
    private boolean isValidWifiConnectionFor(NetworkInfo ni, String suffix) {
        boolean valid_for_wifi = getPreferenceBooleanValue("use_wifi_" + suffix, true);
        // 我们考虑以太网WiFi
        if (valid_for_wifi && ni != null) {
            int type = ni.getType();
            // 无线连接
            if (ni.isConnected() && (type == ConnectivityManager.TYPE_WIFI || type == 9)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查可接受的移动数据网络连接
     */
    private boolean isValidMobileConnectionFor(NetworkInfo ni, String suffix) {
        boolean valid_for_3g = getPreferenceBooleanValue("use_3g_" + suffix, false);
        boolean valid_for_edge = getPreferenceBooleanValue("use_edge_" + suffix, false);
        boolean valid_for_gprs = getPreferenceBooleanValue("use_gprs_" + suffix, false);
        boolean valid_for_roaming = getPreferenceBooleanValue("use_roaming_" + suffix, true);

        if (!valid_for_roaming && ni != null) {
            if (ni.isRoaming()) {
                return false;
            }
        }

        if ((valid_for_3g || valid_for_edge || valid_for_gprs) && ni != null) {
            int type = ni.getType();
            // 任何移动网络连接
            if (ni.isConnected() &&
                    // 类型为 3,4,5 和其他的移动数据网络
                    (type == ConnectivityManager.TYPE_MOBILE || (type <= 5 && type >= 3))) {
                int subType = ni.getSubtype();

                // 3G (更高)
                if (valid_for_3g && subType >= TelephonyManager.NETWORK_TYPE_UMTS) {
                    return true;
                }

                // GPRS (或未知)
                if (valid_for_gprs && (subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_UNKNOWN)) {
                    return true;
                }

                // EDGE
                if (valid_for_edge && subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查其他
     */
    private boolean isValidOtherConnectionFor(NetworkInfo ni, String suffix) {
        boolean valid_for_other = getPreferenceBooleanValue("use_other_" + suffix, true);
        if (valid_for_other && ni != null && ni.getType() != ConnectivityManager.TYPE_MOBILE && ni.getType() != ConnectivityManager.TYPE_WIFI) {
            return ni.isConnected();
        }
        return false;
    }

    private boolean isValidAnywayConnectionFor(NetworkInfo ni, String suffix) {
        return getPreferenceBooleanValue("use_anyway_" + suffix, false);
    }

    // 传入和输出的通用设置
    private boolean isValidConnectionFor(NetworkInfo ni, String suffix) {
        if (isValidWifiConnectionFor(ni, suffix)) {
            LogUtils.d(TAG, "We are valid for WIFI");
            return true;
        }
        if (isValidMobileConnectionFor(ni, suffix)) {
            LogUtils.d(TAG, "We are valid for MOBILE");
            return true;
        }
        if (isValidOtherConnectionFor(ni, suffix)) {
            LogUtils.d(TAG, "We are valid for OTHER");
            return true;
        }
        if (isValidAnywayConnectionFor(ni, suffix)) {
            LogUtils.d(TAG, "We are valid ANYWAY");
            return true;
        }
        return false;
    }

    /**
     * 当前连接是否有效，用于发送呼叫
     *
     * @return true 连接有效
     */
    public boolean isValidConnectionForOutgoing() {
        return isValidConnectionForOutgoing(true);
    }

    /**
     * 当前连接是否有效？用户发送呼叫
     * @param considerQuit pass true if we should consider app quitted as a reason to not consider available for outgoing
     * @return true if connection is valid
     */
    public boolean isValidConnectionForOutgoing(boolean considerQuit) {
        if(considerQuit) {
            if (getPreferenceBooleanValue(PreferencesWrapper.HAS_BEEN_QUIT, false)) {
                // Don't go further, we have been explicitly stopped
                return false;
            }
        }
        NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        return isValidConnectionFor(ni, "out");
    }

    /**
     * Say whether current connection is valid for incoming calls
     *
     * @return true if connection is valid
     */
    public boolean isValidConnectionForIncoming() {
        NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        return isValidConnectionFor(ni, "in");
    }

    public ArrayList<String> getAllIncomingNetworks() {
        ArrayList<String> incomingNetworks = new ArrayList<String>();
        String[] availableNetworks = {"3g", "edge", "gprs", "wifi", "other"};
        for (String network : availableNetworks) {
            if (getPreferenceBooleanValue("use_" + network + "_in")) {
                incomingNetworks.add(network);
            }
        }

        return incomingNetworks;
    }

    public int getLogLevel() {
        int prefsValue = SipConfigManager.getPreferenceIntegerValue(mContext, SipConfigManager.LOG_LEVEL, 1);
        if (prefsValue <= 6 && prefsValue >= 1) {
            return prefsValue;
        }
        return 1;
    }

    /**
     * Get the audio codec quality setting
     *
     * @return the audio quality
     */
    public int getInCallMode() {
        String mode = getPreferenceStringValue(SipConfigManager.SIP_AUDIO_MODE);
        try {
            return Integer.parseInt(mode);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "In call mode " + mode + " not well formated");
        }

        return AudioManager.MODE_NORMAL;
    }

    /**
     * Get current clock rate
     * @param mediaManager
     *
     * @return clock rate in Hz
     */
//    public long getClockRate(MediaManager mediaManager) {
//        String clockRate = getPreferenceStringValue(SipConfigManager.SND_CLOCK_RATE);
//        long defaultRate = 16000;
//        try {
//            long rate = Integer.parseInt(clockRate);
//            if(rate == 0) {
//                return mediaManager.getBestSampleRate(defaultRate);
//            }
//            return rate;
//        } catch (NumberFormatException e) {
//            LogUtils.e(TAG, "Clock rate " + clockRate + " not well formated");
//        }
//        return defaultRate;
//    }

    public boolean useRoutingApi() {
        return getPreferenceBooleanValue(SipConfigManager.USE_ROUTING_API);
    }

    public boolean useModeApi() {
        return getPreferenceBooleanValue(SipConfigManager.USE_MODE_API);
    }

    public boolean generateForSetCall() {
        return getPreferenceBooleanValue(SipConfigManager.SET_AUDIO_GENERATE_TONE);
    }

    public float getInitialVolumeLevel() {
        return (float) (getPreferenceFloatValue(SipConfigManager.SND_STREAM_LEVEL, 8.0f) / 10.0f);
    }

    /**
     * Get sip ringtone
     *
     * @return string uri
     */
    public String getRingtone() {
        String ringtone = getPreferenceStringValue(SipConfigManager.RINGTONE,
                Settings.System.DEFAULT_RINGTONE_URI.toString());

        if (TextUtils.isEmpty(ringtone)) {
            ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
        }
        return ringtone;
    }

    // / ---- PURE SIP SETTINGS -----

    public boolean isTCPEnabled() {
        return getPreferenceBooleanValue(SipConfigManager.ENABLE_TCP);
    }

    public boolean isUDPEnabled() {
        return getPreferenceBooleanValue(SipConfigManager.ENABLE_UDP);
    }

    public boolean isTLSEnabled() {
        return getPreferenceBooleanValue(SipConfigManager.ENABLE_TLS);
    }

    public boolean useIPv6() {
        return getPreferenceBooleanValue(SipConfigManager.USE_IPV6);
    }

    private int getPrefPort(String key) {
        int port = getPreferenceIntegerValue(key);
        if (isValidPort(port)) {
            return port;
        }
        return Integer.parseInt(PreferencesWrapper.STRING_PREFS.get(key));
    }

    public int getUDPTransportPort() {
        return getPrefPort(SipConfigManager.UDP_TRANSPORT_PORT);
    }

    public int getTCPTransportPort() {
        return getPrefPort(SipConfigManager.TCP_TRANSPORT_PORT);
    }

    public int getTLSTransportPort() {
        return getPrefPort(SipConfigManager.TLS_TRANSPORT_PORT);
    }

    private int getKeepAliveInterval(String wifi_key, String mobile_key) {
        NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI) {
            return getPreferenceIntegerValue(wifi_key);
        }
        return getPreferenceIntegerValue(mobile_key);
    }

    /**
     * Retrieve UDP keep alive interval for the current connection
     *
     * @return KA Interval in second
     */
    public int getUdpKeepAliveInterval() {
        return getKeepAliveInterval(SipConfigManager.KEEP_ALIVE_INTERVAL_WIFI,
                SipConfigManager.KEEP_ALIVE_INTERVAL_MOBILE);
    }

    /**
     * Retrieve TCP keep alive interval for the current connection
     *
     * @return KA Interval in second
     */
    public int getTcpKeepAliveInterval() {
        return getKeepAliveInterval(SipConfigManager.TCP_KEEP_ALIVE_INTERVAL_WIFI,
                SipConfigManager.TCP_KEEP_ALIVE_INTERVAL_MOBILE);
    }

    /**
     * Retrieve TLS keep alive interval for the current connection
     *
     * @return KA Interval in second
     */
    public int getTlsKeepAliveInterval() {
        return getKeepAliveInterval(SipConfigManager.TLS_KEEP_ALIVE_INTERVAL_WIFI,
                SipConfigManager.TLS_KEEP_ALIVE_INTERVAL_MOBILE);
    }

    public int getRTPPort() {
        return getPrefPort(SipConfigManager.RTP_PORT);
    }

    public boolean enableDNSSRV() {
        return getPreferenceBooleanValue(SipConfigManager.ENABLE_DNS_SRV);
    }

    public int getTLSMethod() {
        return getPreferenceIntegerValue(SipConfigManager.TLS_METHOD);
    }

//    public String getUserAgent(Context ctx) {
//        String userAgent = getPreferenceStringValue(SipConfigManager.USER_AGENT);
//        if (userAgent.equalsIgnoreCase(CustomDistribution.getUserAgent())) {
//            // If that's the official -not custom- user agent, send the release,
//            // the device and the api level
//            PackageInfo pinfo = getCurrentPackageInfos(ctx);
//            if (pinfo != null) {
//                userAgent += "_" + android.os.Build.DEVICE + "-" + Compatibility.getApiLevel()
//                        + "/r" + pinfo.versionCode;
//            }
//        }
//        return userAgent;
//    }

//    public final static PackageInfo getCurrentPackageInfos(Context ctx) {
//        PackageInfo pinfo = null;
//        try {
//            pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
//        } catch (NameNotFoundException e) {
//            Log.e(THIS_FILE, "Impossible to find version of current package !!");
//        }
//        return pinfo;
//    }

    // Utils

    /**
     * Check TCP/UDP validity of a network port
     */
    private boolean isValidPort(int port) {
        return (port >= 0 && port < 65535);
    }

    /**
     * Get a property from android property subsystem
     *
     * @param prop property to get
     * @return the value of the property command line or null if failed
     */
    public String getSystemProp(String prop) {
        // String re1 = "^\\d+(\\.\\d+){3}$";
        // String re2 = "^[0-9a-f]+(:[0-9a-f]*)+:[0-9a-f]+$";
        try {
            String line;
            Process p = Runtime.getRuntime().exec("getprop " + prop);
            InputStream in = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                return line;
            }
        } catch (Exception e) {
            // ignore resolutely
        }
        return null;
    }

    // Media part

    /**
     * Get auto close time after end of the call To avoid crash after hangup --
     * android 1.5 only but even sometimes crash
     */
    public int getAutoCloseTime() {
        return getPreferenceIntegerValue(SipConfigManager.SND_AUTO_CLOSE_TIME);
    }

    /**
     * 是否启用回音消除
     *
     * @return true if enabled
     */
    public boolean hasEchoCancellation() {
        return getPreferenceBooleanValue(SipConfigManager.ECHO_CANCELLATION);
    }

    public long getEchoCancellationTail() {
        if (!hasEchoCancellation()) {
            return 0;
        }
        return getPreferenceIntegerValue(SipConfigManager.ECHO_CANCELLATION_TAIL);
    }

    /**
     * Get the audio codec quality setting
     *
     * @return the audio quality
     */
    public long getMediaQuality() {
        String mediaQuality = getPreferenceStringValue(SipConfigManager.SND_MEDIA_QUALITY);
        // prefs.getString(SND_MEDIA_QUALITY, String.valueOf(defaultValue));
        try {
            int prefsValue = Integer.parseInt(mediaQuality);
            if (prefsValue <= 10 && prefsValue >= 0) {
                return prefsValue;
            }
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "Audio quality " + mediaQuality + " not well formated");
        }

        return 4;
    }

    /**
     * Get whether turn is enabled
     *
     * @return 1 if enabled (pjstyle)
     */
    public int getStunEnabled() {
        return getPreferenceBooleanValue(SipConfigManager.ENABLE_STUN) ? 1 : 0;
    }

    /**
     * Get turn server
     *
     * @return host:port or blank if not set
     */
    public String getTurnServer() {
        return getPreferenceStringValue(SipConfigManager.TURN_SERVER);
    }

    /**
     * Setup codecs list Should be only done by the service that get infos from
     * the sip stack(s)
     *
     * @param codecs the list of codecs
     */
    public void setCodecList(List<String> codecs) {
        if (codecs != null) {
            setPreferenceStringValue(PreferencesWrapper.CODECS_LIST,
                    TextUtils.join(PreferencesWrapper.CODECS_SEPARATOR, codecs));
        }
    }

    public void setVideoCodecList(List<String> codecs) {
        if (codecs != null) {
            setPreferenceStringValue(PreferencesWrapper.CODECS_VIDEO_LIST,
                    TextUtils.join(PreferencesWrapper.CODECS_SEPARATOR, codecs));
        }
    }

    public void setLibCapability(String cap, boolean canDo) {
        setPreferenceBooleanValue(PreferencesWrapper.BACKUP_PREFIX + cap, canDo);
    }

    // DTMF

    public boolean useSipInfoDtmf() {
        return (getPreferenceIntegerValue(SipConfigManager.DTMF_MODE) == SipConfigManager.DTMF_MODE_INFO);
    }

    public boolean forceDtmfInBand() {
        return (getPreferenceIntegerValue(SipConfigManager.DTMF_MODE) == SipConfigManager.DTMF_MODE_INBAND);
    }

    public boolean forceDtmfRTP() {
        return (getPreferenceIntegerValue(SipConfigManager.DTMF_MODE) == SipConfigManager.DTMF_MODE_RTP);
    }

    // Codecs

    /**
     * Get the codec priority
     *
     * @param codecName codec name formated in the pjsip format (the
     *            corresponding pref is
     *            codec_{{lower(codecName)}}_{{codecFreq}})
     * @param defaultValue the default value if the pref is not found MUST be
     *            casteable as Integer/short
     * @return the priority of the codec as defined in preferences
     */
    public short getCodecPriority(String codecName, String type, String defaultValue) {
        String key = SipConfigManager.getCodecKey(codecName, type);
        if (key != null) {
            String val = getPreferenceStringValue(key, defaultValue);
            if (!TextUtils.isEmpty(val)) {
                try {
                    return (short) Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    LogUtils.e(TAG, "Impossible to parse " + val);
                }
            }
        }
        return (short) Integer.parseInt(defaultValue);
    }

    /**
     * Set the priority for the codec for a given bandwidth type
     *
     * @param codecName the name of the codec as announced by codec
     * @param type bandwidth type <br/>
     *            For now, valid constants are :
     *            {@link SipConfigManager#CODEC_NB} and
     *            {@link SipConfigManager#CODEC_WB}
     * @param newValue Short value for preference as a string.
     */
    public void setCodecPriority(String codecName, String type, String newValue) {
        String key = SipConfigManager.getCodecKey(codecName, type);
        if (key != null) {
            setPreferenceStringValue(key, newValue);
        }
        // TODO : else raise error
    }

//    public static File getRecordsFolder(Context ctxt) {
//        return PreferencesWrapper.getRecordsFolder(ctxt);
//    }
}
