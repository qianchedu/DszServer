package com.dsz.service;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

/**
 * 图片处理工厂
 */
public class JpegFactory implements Camera.PreviewCallback, JpegProvider {
    
    private int mWidth;
    private int mHeight;
    private int mQuality;
    private ByteArrayOutputStream mJpegOutputStream;
    private byte[] mJpegData;

    /**
     * 图片进行出来
     * @param width     ：图片的宽
     * @param height    ：图片的高
     * @param quality   ：质量
     */
    public JpegFactory(int width, int height, int quality) {
        mWidth = width;
        mHeight = height;
        mQuality = quality;
        mJpegData = null;
        mJpegOutputStream = new ByteArrayOutputStream();
    }
    
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
   
    public int getWidth() {
        return mWidth;
    }
    
    public int getHeight() {
        return mHeight;
    }
    
    public void setQuality(int quality) {
        mQuality = quality;
    }
    
    public int getQuality() {
        return mQuality;
    }
    
    //将yuv格式的转化为jpeg
    public void onPreviewFrame(byte[] data, Camera camera) {

//        byte[] datas = rotateYUV420Degree90(data,mWidth,mHeight);



        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
        mJpegOutputStream.reset();
        yuvImage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), mQuality, mJpegOutputStream);
        mJpegData = mJpegOutputStream.toByteArray();
        
        synchronized (mJpegOutputStream) {
            mJpegOutputStream.notifyAll();
        }
        
    }

    /**
     * 后来加的 旋转图片    但是客户端接收的时候出现问题  所以就每用到了
     * @param data             相机回传过来的数据
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
    
    public byte[] getNewJpeg() throws InterruptedException {
        synchronized (mJpegOutputStream) {
            mJpegOutputStream.wait();
        }
        
        return mJpegData;
    }
    
    public byte[] getJpeg() {
        return mJpegData;
    }

}
