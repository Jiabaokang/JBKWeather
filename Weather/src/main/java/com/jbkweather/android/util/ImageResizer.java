package com.jbkweather.android.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * Created by sunny on 2017/7/17.
 * 实现图片压缩工具类
 */

public class ImageResizer {

    private static final String TAG = "ImageResizer";

    public ImageResizer() {
    }

    /**
     * 压缩资源图片
     * @param res
     * @param resId
     * @param rewWidth
     * @param rewheight
     * @return
     */
    public Bitmap decodeSampledBitmapResource(Resources res,int resId,int rewWidth,int rewheight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);

        options.inSampleSize = calculateInSampleSize(options,rewWidth,rewheight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res,resId,options);
    }

    /**
     * 压缩文件图片
     * @param fd
     * @param rewWidth
     * @param rewHeight
     * @return
     */
    public Bitmap decodeSampleeBitmapFromFileDescriptor(FileDescriptor fd,int rewWidth,int rewHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize = calculateInSampleSize(options,rewWidth,rewHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }


    //计算采样率
    public int calculateInSampleSize(BitmapFactory.Options options, int rewWidth, int rewheight) {

        if(rewWidth == 0 || rewheight==0){
            return 1;
        }

        final int width = options.outWidth;
        final int height = options.outHeight;

        int inSampleSize =  1;
        if (height > rewheight || width > rewWidth){
            final int halfHeight = height / 2;
            final int halFWidth = width / 2;
            while((halfHeight / inSampleSize) >= rewheight && (halFWidth / inSampleSize) >= rewWidth){
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "calculateInSampleSize: ==" + inSampleSize);
        return inSampleSize;

    }
}
