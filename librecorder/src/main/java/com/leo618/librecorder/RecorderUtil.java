package com.leo618.librecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.Surface;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * function:工具类
 *
 * <p>
 * Created by Leo on 2017/11/29.
 */
@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored", "WeakerAccess"})
public class RecorderUtil {

    //校验是否具有摄像头使用权限
  /*package*/
    static boolean hasCameraPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            RecorderLog.d("hasPermissions: API version < M, returning true by default");
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    //检查RecorderConfig是否有配置
  /*package*/
    static void checkRecorderConfig(RecorderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("请配置RecorderConfig.");
        }
    }

    //获取可用的所有摄像头 int[2]
  /*package*/
    static int[] findAvailableCameras() {
        int               CAMERA_FRONT = -1;
        int               CAMERA_BACK  = -1;
        Camera.CameraInfo info         = new Camera.CameraInfo();
        int               cameraNum    = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_BACK = info.facing;
                    break;
            }
        }
        return new int[]{CAMERA_FRONT, CAMERA_BACK};
    }

    /**
     * 设置摄像头的角度
     *
     * @param screenRotation 当前屏幕角度: activity.getWindowManager().getDefaultDisplay().getRotation()
     * @param cameraId       当前摄像头id
     * @param camera         摄像头
     */
  /*package*/
    static int setCameraDisplayOrientation(int screenRotation, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        switch (screenRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return result;
    }

    //获取最佳的预览宽高
   /*package*/
    static int[] getBestPreviewWH(int previewWidth, int previewHeight, List<Camera.Size> sizeList) {
        RecorderLog.d("getBestPreviewWH before: " + previewHeight + "x" + previewWidth);
        //分辨率升序排序
        Collections.sort(sizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.height * lhs.width - rhs.height * rhs.width;
            }
        });
        StringBuilder sizeStr = new StringBuilder();
        for (Camera.Size size : sizeList) {
            sizeStr.append(size.height).append("x").append(size.width).append(",");
        }
        RecorderLog.d("List<Camera.Size> : " + sizeStr.toString());
        boolean haveSize = false;
        if (sizeList.size() > 1) {
            for (Camera.Size cur : sizeList) {
                if (cur.width >= previewWidth && cur.height >= previewHeight) {
                    previewWidth = cur.width;
                    previewHeight = cur.height;
                    haveSize = true;
                    break;
                }
            }
            if (!haveSize) {// 如果未找到和屏幕适配的size则取最大一个
                previewWidth = sizeList.get(sizeList.size() - 1).width;
                previewHeight = sizeList.get(sizeList.size() - 1).height;
                RecorderLog.d("未找到和屏幕适配的size则取最大一个");
            }
        } else {
            RecorderLog.d("手机支持的分辨率不足，获取系统预设的最佳值");
            CamcorderProfile cameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            previewWidth = cameraProfile.videoFrameWidth;
            previewHeight = cameraProfile.videoFrameHeight;
        }
        RecorderLog.d("getBestPreviewWH after: " + previewHeight + "x" + previewWidth);
        return new int[]{previewWidth, previewHeight};
    }

    //获取最佳的视频捕捉画面宽高
  /*package*/
    static int[] getBestVideoWH(Camera.Size previewSize, List<Camera.Size> sizeList) {
        int previewWidth  = previewSize.width;
        int previewHeight = previewSize.height;
        RecorderLog.d("getBestVideoWH before: " + previewHeight + "x" + previewWidth);
        //分辨率升序排序
        Collections.sort(sizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.height * lhs.width - rhs.height * rhs.width;
            }
        });
        StringBuilder sizeStr = new StringBuilder();
        for (Camera.Size size : sizeList) {
            sizeStr.append(size.height).append("x").append(size.width).append(",");
        }
        RecorderLog.d("List<Camera.Size> : " + sizeStr.toString());
        boolean haveSize = false;
        if (sizeList.size() > 1) {
            for (Camera.Size cur : sizeList) {
                if (cur.width >= previewWidth && cur.height >= previewHeight) {
                    previewWidth = cur.width;
                    previewHeight = cur.height;
                    haveSize = true;
                    break;
                }
            }
            if (!haveSize) {// 如果未找到和屏幕适配的size则取最大一个
                previewWidth = sizeList.get(sizeList.size() - 1).width;
                previewHeight = sizeList.get(sizeList.size() - 1).height;
                RecorderLog.d("未找到和屏幕适配的size则取最大一个");
            }
        } else {
            RecorderLog.d("手机支持的分辨率不足，获取系统预设的最佳值");
            CamcorderProfile cameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            previewWidth = cameraProfile.videoFrameWidth;
            previewHeight = cameraProfile.videoFrameHeight;
        }
        RecorderLog.d("getBestVideoWH after: " + previewHeight + "x" + previewWidth);
        return new int[]{previewWidth, previewHeight};
    }

    //创建文件 如果不存在的话
    public static void createFileIfNotExist(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {
        Bitmap bitmap;
        // 获取视频的缩略图
        //参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
        // 其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        return bitmap;
    }

    /**
     * 格式化秒为单位的时间为00:00:00
     */
    @SuppressLint("DefaultLocale")
    public static String formatDuration(long time) {
        return String.format("%02d", time / 3600) + ":" + String.format("%02d", time / 60) + ":" + String.format("%02d", time % 60);
    }
}
