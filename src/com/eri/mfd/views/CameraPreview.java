package com.eri.mfd.views;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public void setLight(boolean AOn)
    {
    	if (mCamera == null) return;
    	if (AOn)
    	{
    		Parameters p = mCamera.getParameters();
    		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
    		mCamera.setParameters(p);
    	}else
    	{
    		Parameters p = mCamera.getParameters();
    		p.setFlashMode(Parameters.FLASH_MODE_OFF);
    		mCamera.setParameters(p);
    	}
    }
    
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        
        Camera.Parameters p = mCamera.getParameters();
        p.set("orientation", "portrait");
        p.set("rotation", 90);
        mCamera.setParameters(p);

        mHolder = getHolder();
        mHolder.addCallback(this);
       
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
       
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CameraView", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
       
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
     
          return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
        
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("CameraView", "Error starting camera preview: " + e.getMessage());
        }
    }

    public void onPause() {
    	mCamera.release();
    	mCamera = null;
    }
}