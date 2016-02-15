/*
 *  Copyright (c) 2012 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc.videoengine;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import com.crte.sipstackhome.utils.log.LogUtils;

import org.webrtc.videoengine.camera.CameraUtilsWrapper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dalvik.system.DexClassLoader;

/**
 * 视频设备捕获信息
 */
@TargetApi(5)
@SuppressWarnings("deprecation")
public class VideoCaptureDeviceInfoAndroid {
    Context context;

    private final static String TAG = "WEBRTC";

    // 获得Android所有可用摄像头的可用和能力信息
    public class AndroidVideoCaptureDevice {
        public AndroidVideoCaptureDevice() {
            frontCameraType = FrontFacingCameraType.None;
            index = 0;
        }

        public String deviceUniqueName;
        /**
         * 保存捕捉参数
         */
        public CaptureCapabilityAndroid captureCapabilies[];
        public FrontFacingCameraType frontCameraType;

        // 定位相机中的描述 android.hardware.Camera.CameraInfo.Orientation
        public int orientation;
        // 使用的摄像头索引
        public int index;
        public CaptureCapabilityAndroid bestCapability;
    }

    /**
     * 前置摄像头类型
     */
    public enum FrontFacingCameraType {
        None, // 不是前置摄像头
        GalaxyS, // Galaxy S 前置摄像头
        HTCEvo, // HTC Evo 前置摄像头
        Android23, // Android 2.3 前置摄像头
    }

    String currentDeviceUniqueId;
    int id;
    List<AndroidVideoCaptureDevice> deviceList;

    private CameraUtilsWrapper cameraUtils;

    /**
     * 创建Android 的视频捕获设备信息
     *
     * @param in_id
     * @param in_context
     * @return
     */
    public static VideoCaptureDeviceInfoAndroid CreateVideoCaptureDeviceInfoAndroid(int in_id, Context in_context) {
        Log.d(TAG, String.format(Locale.US, "VideoCaptureDeviceInfoAndroid"));

        VideoCaptureDeviceInfoAndroid self = new VideoCaptureDeviceInfoAndroid(in_id, in_context);
        if (self != null && self.Init() == 0) {
            return self;
        } else {
            Log.d(TAG, "Failed to create VideoCaptureDeviceInfoAndroid.");
        }
        return null;
    }

    private VideoCaptureDeviceInfoAndroid(int in_id, Context in_context) {
        id = in_id;
        context = in_context;
        deviceList = new ArrayList<AndroidVideoCaptureDevice>();
        cameraUtils = CameraUtilsWrapper.getInstance();
    }

    private int Init() {
        // 填充可用的摄像头和其能力的设备列表。
        try {
            cameraUtils.Init(this, deviceList);
        } catch (Exception ex) {
            Log.e(TAG, "无法初始化 VideoCaptureDeviceInfo ex" + ex.getLocalizedMessage());
            return -1;
        }
        VerifyCapabilities();
        return 0;
    }

    // 添加当前设备的捕获功能
    public void AddDeviceInfo(AndroidVideoCaptureDevice newDevice, Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes(); // 获得支持的预览大小

        List<Integer> frameRates = parameters.getSupportedPreviewFrameRates(); // 获取支持的预览帧速率
        int maxFPS = 0;
        if (sizes == null) {
            newDevice.captureCapabilies = new CaptureCapabilityAndroid[0];
            return;
        }
        if (frameRates != null) {
            for (Integer frameRate : frameRates) {
                if (frameRate > maxFPS) {
                    maxFPS = frameRate;
                }
            }
        } else {
            maxFPS = 15;
        }

        newDevice.captureCapabilies = new CaptureCapabilityAndroid[sizes.size()];
        newDevice.bestCapability = new CaptureCapabilityAndroid();
        int bestBandwidth = 0;

        for (int i = 0; i < sizes.size(); ++i) {
            Size s = sizes.get(i);
            newDevice.captureCapabilies[i] = new CaptureCapabilityAndroid();
            newDevice.captureCapabilies[i].height = s.height;
            newDevice.captureCapabilies[i].width = s.width;
            newDevice.captureCapabilies[i].maxFPS = maxFPS;
            Log.v(TAG, "VideoCaptureDeviceInfo " + "maxFPS:" + maxFPS + " width:" + s.width + " height:" + s.height);

            // 使用H.264公式来估算带宽需求
            int currentBandwidth = (int) (s.width * s.height * maxFPS * 0.07);
            Log.v(TAG, "VideoCaptureDeviceInfo 当前宽带:" + currentBandwidth);
            int maxBestBandwidth = 1000000; // 最大宽带
            // 找到一个带宽小于1兆的
            if (bestBandwidth == 0 || (currentBandwidth < bestBandwidth && currentBandwidth >= maxBestBandwidth)) {
                newDevice.bestCapability.width = s.width;
                newDevice.bestCapability.height = s.height;
                newDevice.bestCapability.maxFPS = maxFPS;
                bestBandwidth = currentBandwidth;
            } else if (currentBandwidth < maxBestBandwidth) {
                if (s.width > newDevice.bestCapability.width || s.height > newDevice.bestCapability.height || bestBandwidth > maxBestBandwidth) {
                    if (s.height != s.width) {
                        newDevice.bestCapability.width = s.width;
                        newDevice.bestCapability.height = s.height;
                        newDevice.bestCapability.maxFPS = maxFPS;
                        bestBandwidth = currentBandwidth;
                    }
                }

            }
        }

        Log.d(TAG, "最佳分辨率 " + newDevice.bestCapability.width + " x " + newDevice.bestCapability.height);
    }

    // 针对不同相机的设置
    // Function that make sure device specific capabilities are
    // in the capability list.
    // Ie Galaxy S supports CIF but does not list CIF as a supported capability.
    // Motorola Droid Camera does not work with frame rate above 15fps.
    // http://code.google.com/p/android/issues/detail?id=5514#c0
    private void VerifyCapabilities() {
        // Nexus S or Galaxy S
        if (android.os.Build.DEVICE.equals("GT-I9000") || android.os.Build.DEVICE.equals("crespo")) {
            CaptureCapabilityAndroid specificCapability = new CaptureCapabilityAndroid();
            specificCapability.width = 352;
            specificCapability.height = 288;
            specificCapability.maxFPS = 15;
            AddDeviceSpecificCapability(specificCapability);

            specificCapability = new CaptureCapabilityAndroid();
            specificCapability.width = 176;
            specificCapability.height = 144;
            specificCapability.maxFPS = 15;
            AddDeviceSpecificCapability(specificCapability);

            specificCapability = new CaptureCapabilityAndroid();
            specificCapability.width = 320;
            specificCapability.height = 240;
            specificCapability.maxFPS = 15;
            AddDeviceSpecificCapability(specificCapability);
        }
        // Motorola Milestone Camera server does not work at 30fps
        // even though it reports that it can
        if (android.os.Build.MANUFACTURER.equals("motorola") && android.os.Build.DEVICE.equals("umts_sholes")) {
            for (AndroidVideoCaptureDevice device : deviceList) {
                for (CaptureCapabilityAndroid capability : device.captureCapabilies) {
                    capability.maxFPS = 15;
                }
            }
        }
    }

    /**
     * 添加设备功能
     *
     * @param specificCapability
     */
    private void AddDeviceSpecificCapability(CaptureCapabilityAndroid specificCapability) {
        for (AndroidVideoCaptureDevice device : deviceList) {
            boolean foundCapability = false;
            for (CaptureCapabilityAndroid capability : device.captureCapabilies) {
                if (capability.width == specificCapability.width && capability.height == specificCapability.height) {
                    foundCapability = true;
                    break;
                }
            }

            // 3R : galaxy S CIF only on rear
            if (android.os.Build.DEVICE.equals("GT-I9000") && (device.frontCameraType == FrontFacingCameraType.GalaxyS ||
                    device.frontCameraType == FrontFacingCameraType.Android23) && specificCapability.width == 352 && specificCapability.height == 288) {
                foundCapability = true;
            }

            if (foundCapability == false) {
                CaptureCapabilityAndroid newCaptureCapabilies[] = new CaptureCapabilityAndroid[device.captureCapabilies.length + 1];
                for (int i = 0; i < device.captureCapabilies.length; ++i) {
                    newCaptureCapabilies[i + 1] = device.captureCapabilies[i];
                }
                newCaptureCapabilies[0] = specificCapability;
                device.captureCapabilies = newCaptureCapabilies;
            }
        }
    }

    // 返回所支持的捕获装置的数量
    public int NumberOfDevices() {
        return deviceList.size();
    }

    /**
     * 获取设备唯一名称
     *
     * @param deviceNumber
     * @return
     */
    public String GetDeviceUniqueName(int deviceNumber) {
        if (deviceNumber < 0 || deviceNumber >= deviceList.size()) {
            return null;
        }
        return deviceList.get(deviceNumber).deviceUniqueName;
    }

    /**
     * 获取性能列表
     * @param deviceUniqueId
     * @return
     */
    public CaptureCapabilityAndroid[] GetCapabilityArray(String deviceUniqueId) {
        for (AndroidVideoCaptureDevice device : deviceList) {
            if (device.deviceUniqueName.equals(deviceUniqueId)) {
                return (CaptureCapabilityAndroid[]) device.captureCapabilies;
            }
        }
        return null;
    }

    /**
     * 获得最佳性能
     * @param deviceUniqueId
     * @return
     */
    public CaptureCapabilityAndroid GetBestCapability(String deviceUniqueId) {
        for (AndroidVideoCaptureDevice device : deviceList) {
            if (device.deviceUniqueName.equals(deviceUniqueId)) {
                return (CaptureCapabilityAndroid) device.bestCapability;
            }
        }
        return null;

    }

    // 返回锁描述的相机返回位置 android.hardware.Camera.CameraInfo.orientation
    public int GetOrientation(String deviceUniqueId) {
        for (AndroidVideoCaptureDevice device : deviceList) {
            if (device.deviceUniqueName.equals(deviceUniqueId)) {
                return device.orientation;
            }
        }
        return -1;
    }

    // 返回 VideoCaptureAndroid实例
    public VideoCaptureAndroid AllocateCamera(int id, long context, String deviceUniqueId) {
        try {
            Log.d(TAG, "分配摄像头 " + deviceUniqueId);

            Camera camera = null;
            AndroidVideoCaptureDevice deviceToUse = null;
            for (AndroidVideoCaptureDevice device : deviceList) {
                if (device.deviceUniqueName.equals(deviceUniqueId)) {
                    // 找到想要的摄像头
                    deviceToUse = device;
                    switch (device.frontCameraType) {
                        case GalaxyS:
                            camera = AllocateGalaxySFrontCamera();
                            break;
                        case HTCEvo:
                            camera = AllocateEVOFrontFacingCamera();
                            break;
                        default:
                            camera = cameraUtils.openCamera(device.index);
                    }
                }
            }

            if (camera == null) {
                return null;
            }
            Log.v(TAG, "AllocateCamera - creating VideoCaptureAndroid");

            return new VideoCaptureAndroid(id, context, camera, deviceToUse);

        } catch (Exception ex) {
            Log.e(TAG, "AllocateCamera Failed to open camera- ex " +
                    ex.getLocalizedMessage());
        }
        return null;
    }

    // 搜索前置摄像头设备，这是设备特定的代码
    public Camera.Parameters
    SearchOldFrontFacingCameras(AndroidVideoCaptureDevice newDevice) throws SecurityException, IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        // 检查打开相机设备的标识
        // Returns null on X10 and 1 on Samsung Galaxy S.
        Camera camera = Camera.open(); // 试图获得一个摄像头实例
        Camera.Parameters parameters = camera.getParameters(); // 获得相机参数实例
        String cameraId = parameters.get("camera-id");
        LogUtils.d(TAG, "camera-id: " + cameraId);
        if (cameraId != null && cameraId.equals("1")) {
            // 这有可能是一个Samsung Galaxy S的前置摄像头
            try {
                parameters.set("camera-id", 2);
                camera.setParameters(parameters);
                parameters = camera.getParameters();
                newDevice.frontCameraType = FrontFacingCameraType.GalaxyS;
                newDevice.orientation = 0;
                camera.release();
                return parameters;
            } catch (Exception ex) {
                Log.e(TAG, "初始化摄像头出错 - ex " + ex.getLocalizedMessage());
            }
        }
        camera.release();

        // 检查Evo的前置摄像头
        File file = new File("/system/framework/com.htc.hardware.twinCamDevice.jar");
        boolean exists = file.exists();
        if (!exists) {
            file = new File("/system/framework/com.sprint.hardware.twinCamDevice.jar");
            exists = file.exists();
        }
        if (exists) {
            newDevice.frontCameraType = FrontFacingCameraType.HTCEvo;
            newDevice.orientation = 0;
            Camera evCamera = AllocateEVOFrontFacingCamera();
            parameters = evCamera.getParameters();
            evCamera.release();
            return parameters;
        }
        return null;
    }

    // 返回一个HTC前置摄像头
    // 调用者负责在完成后释放它
    private Camera AllocateEVOFrontFacingCamera() throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String classPath = null;
        File file = new File("/system/framework/com.htc.hardware.twinCamDevice.jar");
        classPath = "com.htc.hardware.twinCamDevice.FrontFacingCamera";
        boolean exists = file.exists();
        if (!exists) {
            file = new File("/system/framework/com.sprint.hardware.twinCamDevice.jar");
            classPath = "com.sprint.hardware.twinCamDevice.FrontFacingCamera";
            exists = file.exists();
        }
        if (!exists) {
            return null;
        }

        String dexOutputDir = "";
        if (context != null) {
            dexOutputDir = context.getFilesDir().getAbsolutePath();
            File mFilesDir = new File(dexOutputDir, "dexfiles");
            if (!mFilesDir.exists()) {
                // Log.e("*WEBRTCN*", "Directory doesn't exists");
                if (!mFilesDir.mkdirs()) {
                    // Log.e("*WEBRTCN*", "Unable to create files directory");
                }
            }
        }

        dexOutputDir += "/dexfiles";

        DexClassLoader loader = new DexClassLoader(file.getAbsolutePath(), dexOutputDir, null, ClassLoader.getSystemClassLoader());

        Method method = ((ClassLoader) loader).loadClass(classPath).getDeclaredMethod("getFrontFacingCamera", (Class[]) null);
        Camera camera = (Camera) method.invoke((Object[]) null, (Object[]) null);
        return camera;
    }

    // 返回一个Galaxy S前置摄像头
    // 调用者负责在完成后释放它
    private Camera AllocateGalaxySFrontCamera() {
        Camera camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        parameters.set("camera-id", 2);
        camera.setParameters(parameters);
        return camera;
    }

}
