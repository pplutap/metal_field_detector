package com.eri.mfd;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.eri.mfd.views.MagneticMap;

public class FormMapMagnetic extends Activity   implements SensorEventListener {
    
	private MagneticMap mMagneticMap;
	private SensorManager sensorManager = null;
	
	Timer mValueTimer = null;
	double mMagneticUT = 0;
	Double mMagneticUTLock = new Double(0);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.form_map_magnetic);

	    mMagneticUT = 0;
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    mMagneticMap = (MagneticMap) findViewById(R.id.viewMagnetometrMap);
	    
	    mValueTimer = new Timer();        	
	    mValueTimer.schedule(new TimerTask() { public void run() 
		{
			threadHandler.sendEmptyMessage(0); 
		}},100, 100);  
	    
	}

    @Override
    protected void onPause() {        
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
   }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    { 
    	mMagneticMap.Calibrate();
		
		return true;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

	float[] aValues = new float[3];
	float[] alValues = new float[3];
	float[] aOldValues = new float[3];
	float[] aMoveValues = new float[3];
	boolean mMoveValuesInitialized = false;
	float[] mValues = new float[3];
	float[] oValues = new float[3];
	private final float NOISE = (float) 0.1;
    float mSpeed;
    
  
    private static final boolean ADAPTIVE_ACCEL_FILTER = true;
    float lastAccel[] = new float[3];
    float accelFilter[] = new float[3];

    public float norm(float x,float y,float z)
    {
    	return (float)Math.sqrt((float)(x*x+y*y+z*z));
    }
    
    double clamp(double v, double min, double max)
    {
        if(v > max)
            return max;
        else if(v < min)
            return min;
        else
            return v;
    }
    
    public void onAccelerometerChanged(float accelX, float accelY, float accelZ) {
     
        float updateFreq = 30; 
        float cutOffFreq = 0.9f;
        float RC = 1.0f / cutOffFreq;
        float dt = 1.0f / updateFreq;
        float filterConstant = RC / (dt + RC);
        float alpha = filterConstant; 
        float kAccelerometerMinStep = 0.033f;
        float kAccelerometerNoiseAttenuation = 3.0f;

        if(ADAPTIVE_ACCEL_FILTER)
        {
            double d = clamp(Math.abs(norm(accelFilter[0], accelFilter[1], accelFilter[2]) - norm(accelX, accelY, accelZ)) / kAccelerometerMinStep - 1.0f, 0.0f, 1.0f);
            alpha = (float)(d * filterConstant / kAccelerometerNoiseAttenuation + (1.0f - d) * filterConstant);
        }

        accelFilter[0] = (float) (alpha * (accelFilter[0] + accelX - lastAccel[0]));
        accelFilter[1] = (float) (alpha * (accelFilter[1] + accelY - lastAccel[1]));
        accelFilter[2] = (float) (alpha * (accelFilter[2] + accelZ - lastAccel[2]));

        lastAccel[0] = accelX;
        lastAccel[1] = accelY;
        lastAccel[2] = accelZ;
    }
	
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


	        onAccelerometerChanged(aValues[0],aValues[1],aValues[2]);
	        	/*
	        if (event.sensor.getType () == Sensor.TYPE_ACCELEROMETER)
	        {
//		        aMoveValues[0] = aOldValues[0] - aValues[0];
//		        aMoveValues[1] = aOldValues[1] - aValues[1];
//		        aMoveValues[2] = aOldValues[2] - aValues[2];
	        	aMoveValues[0] += aValues[0];
		        aMoveValues[1] += aValues[1];
		        aMoveValues[2] += aValues[2];
		        //mSpeed = (float)Math.sqrt(aMoveValues[0]*aMoveValues[0]+aMoveValues[1]*aMoveValues[1]+aMoveValues[2]*aMoveValues[2]);
		        aOldValues[0] = aValues[0];
		        aOldValues[1] = aValues[1];
		        aOldValues[2] = aValues[2];
//		        if (aMoveValues[0] < NOISE) aMoveValues[0] = 0;
//		        if (aMoveValues[1] < NOISE) aMoveValues[1] = 0;
//		        if (aMoveValues[2] < NOISE) aMoveValues[2] = 0;
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
	        
	        float[] inVec = new float[4];
	        float[] outVec = new float[4]; 
	        inVec[0] = 0;
	        inVec[1] = (float)lMagneticUT;
	        inVec[2] = 0;
	        inVec[3] = 1;

	        //Matrix.setIdentityM(R, 0);
	        //Matrix.rotateM(matrix, 0, angle, axisX, axisY, axisZ);
	        //Matrix.multiplyMV(outVec, 0, R, 0, inVec, 0);

	        mMagneticMap.addMovementValue(accelFilter[0],accelFilter[1],accelFilter[2]);
	       
	       
	        
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
	    	        mMagneticMap.addMagneticValue((int)lMagneticUT);
	    	        mMagneticMap.invalidate();
	        	}
	    }
	};
		        		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	 //   getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}
	

}