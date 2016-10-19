package com.eri.mfd;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eri.mfd.views.CameraPreview;
import com.eri.mfd.views.DrawView;

public class FormCamera extends Activity implements SensorEventListener {
	 
	CameraPreview cv;
	DrawView dv;
	FrameLayout alParent;
	boolean mLight = false;
	
	Timer mValueTimer = null;
	double mMagneticUT = 0;
	Double mMagneticUTLock = new Double(0);
	private SensorManager sensorManager = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
	    mMagneticUT = 0;
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    mValueTimer = new Timer();        	
	    mValueTimer.schedule(new TimerTask() { public void run() 
		{
			threadHandler.sendEmptyMessage(0); 
		}},100, 100);  
	    
    }
    
    public void Load(){
        Camera c = getCameraInstance();
        
        if (c != null){
            alParent = new FrameLayout(this);
            alParent.setLayoutParams(new LayoutParams(
            		LayoutParams.FILL_PARENT,
            		LayoutParams.FILL_PARENT));
            
            cv = new CameraPreview(this,c);
            alParent.addView(cv);
            
            dv = new DrawView(this);
            alParent.addView(dv);
             
            setContentView(alParent);
        }

        else {
        	Toast toast = Toast.makeText(getApplicationContext(), 
        			"Urz¹dzenie wymaga wbudowanej kamery.", Toast.LENGTH_SHORT);
        	toast.show();
        	finish();
        }
    }
    

    public static Camera getCameraInstance(){
        Camera c = null;
        
        try {
            c = Camera.open();
        }
        catch (Exception e){  
        	e.printStackTrace();
        }
        return c; 
    }
    
    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }
    
    @Override
    protected void onPause() {
    	sensorManager.unregisterListener(this);
        super.onPause();
        
        if (cv != null){
        	cv.onPause();
        	cv = null;
        }
    }
    
    @Override 
    protected void onResume(){
    	super.onResume();
    	
    	Load();
    	
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);        
    } 
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    { 
    	int action = event.getAction();    	   
    	if ((action==MotionEvent.ACTION_DOWN)&&(cv != null))
    	{
    		mLight = !mLight;
    		cv.setLight(mLight);
    	}
    	return true;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

	float[] aValues = new float[3];
	float[] mValues = new float[3];
	float[] oValues = new float[3];
    
    public void onSensorChanged(SensorEvent event) {
		
        synchronized (this) {
        	switch (event.sensor.getType ()){
            case Sensor.TYPE_ACCELEROMETER:
                aValues = event.values.clone ();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = event.values.clone ();
                break;
            case Sensor.TYPE_ORIENTATION: 
                oValues = event.values.clone(); 
                break; 
        }


        	/*
        if (event.sensor.getType () == Sensor.TYPE_ACCELEROMETER)
        {
//	        aMoveValues[0] = aOldValues[0] - aValues[0];
//	        aMoveValues[1] = aOldValues[1] - aValues[1];
//	        aMoveValues[2] = aOldValues[2] - aValues[2];
        	aMoveValues[0] += aValues[0];
	        aMoveValues[1] += aValues[1];
	        aMoveValues[2] += aValues[2];
	        //mSpeed = (float)Math.sqrt(aMoveValues[0]*aMoveValues[0]+aMoveValues[1]*aMoveValues[1]+aMoveValues[2]*aMoveValues[2]);
	        aOldValues[0] = aValues[0];
	        aOldValues[1] = aValues[1];
	        aOldValues[2] = aValues[2];
//	        if (aMoveValues[0] < NOISE) aMoveValues[0] = 0;
//	        if (aMoveValues[1] < NOISE) aMoveValues[1] = 0;
//	        if (aMoveValues[2] < NOISE) aMoveValues[2] = 0;
	        if (mMoveValuesInitialized)
	        	mMagneticMap.addMovementValue(aMoveValues[0], aMoveValues[1], aMoveValues[2]);       
	        mMoveValuesInitialized = true;
	        
	        //mMagneticMap.mDebug = 
	        mDBG.setText(Html.fromHtml(	
	        		String.valueOf(aValues[0])+"<br/>"+
	        		String.valueOf(aValues[1])+"<br/>"+
	        		String.valueOf(aValues[2])+"<br/>"+
	        		String.valueOf( (int)(mSpeed) )
	        		));
	        mMagneticMap.invalidate();
        }
*/
        float[] R = new float[16];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrix (R, null, aValues, mValues);
        SensorManager.getOrientation (R, orientationValues);


        orientationValues[0] = (float)Math.toDegrees (orientationValues[0]);
        orientationValues[1] = (float)Math.toDegrees (orientationValues[1]);
        orientationValues[2] = (float)Math.toDegrees (orientationValues[2]);
        
        double lMagneticUT = Math.sqrt(mValues[0]*mValues[0] + mValues[1]*mValues[1] + mValues[2]*mValues[2]);
        
        if (dv != null)
        { 
        	dv.setRotationMatrix(R);
        	dv.setDirection(orientationValues[0]);
        }
        
        
         
        synchronized(mMagneticUTLock)
        {
        	mMagneticUT = lMagneticUT;
        }
          
      }
  }
    
	private Handler threadHandler = new Handler() 
	{
	    public void handleMessage(android.os.Message msg) 
	    {
	        	if (msg.what == 0)
	        	{
	        		double lMagneticUT;
	    	        synchronized(mMagneticUTLock)
	    	        {
	    	        	lMagneticUT = mMagneticUT;
	    	        }
	    	        if (dv!=null)
	    	        {
	    	        	dv.addMagneticValue((int)lMagneticUT);
	    	        	dv.invalidate();
	    	        }
	    	        //mMagneticMap.invalidate();
	        	}
	    }
	};
}
