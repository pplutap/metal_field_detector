package com.eri.mfd;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.View;

public class FormMain extends Activity  implements SensorEventListener{

	    private SensorManager sensorManager = null;
	    
	    Timer mValueTimer = null;
	    double mMagneticUT = 0;
	    Double mMagneticUTLock = new Double(0);
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.form_main);

	        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	        
		    mValueTimer = new Timer();        	
		    mValueTimer.schedule(new TimerTask() { public void run() 
			{
				threadHandler.sendEmptyMessage(0); 
			}},100, 100);
	        
	        // Register magnetic sensor
	        sensorManager.registerListener(this,
	                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
	                SensorManager.SENSOR_DELAY_NORMAL);
	    }

	    @Override
	    protected void onPause() {
	        // Unregister the listener
	        sensorManager.unregisterListener(this);
	        super.onPause();
	    }

	    @Override
	    protected void onStop() {
	        // Unregister the listener
	        sensorManager.unregisterListener(this);
	        super.onStop();
	    }

	    @Override
	    protected void onResume() {
	        super.onResume();

	        // Register magnetic sensor
	        sensorManager.registerListener(this,
	                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
	                SensorManager.SENSOR_DELAY_GAME);
	        
	        sensorManager.registerListener(this,
	                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	                SensorManager.SENSOR_DELAY_GAME);
	    }

	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        // Ignoring this for now

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

	        float[] R = new float[16];
	        float[] orientationValues = new float[3];

	        SensorManager.getRotationMatrix (R, null, aValues, mValues);
	        SensorManager.getOrientation (R, orientationValues);

	        orientationValues[0] = (float)Math.toDegrees (orientationValues[0]);
	        orientationValues[1] = (float)Math.toDegrees (orientationValues[1]);
	        orientationValues[2] = (float)Math.toDegrees (orientationValues[2]);
	        
	        //Absolute uT value read from the magnotomiter
	        double lMagneticUT = Math.sqrt(mValues[0]*mValues[0] + mValues[1]*mValues[1] + mValues[2]*mValues[2]);
	        synchronized(mMagneticUTLock)
	        {
	        	mMagneticUT = lMagneticUT;
	        }
	      }
	  }
	    
	public void DoMapMeasurement(View v)
	{
		Intent lIntent = new Intent(this, FormMapMagnetic.class);
 		startActivity(lIntent);
	}
	
	public void DoSimpleMeasurement(View v)
	{
		Intent lIntent = new Intent(this, FormCurrentMagnetic.class);
 		startActivity(lIntent);
	}
	
	public void DoCamera(View v)
	{
		Intent lIntent = new Intent(this, FormCamera.class);
 		startActivity(lIntent);
	}
	
	public void DoInformation(View v)
	{
		Spanned lHtml = Html.fromHtml(
				"Detektor pola magnetycznego<br/>wersja 1.0"
				);
		
		new AlertDialog.Builder(this)
    	.setTitle("Informacja o programie")
    	.setCancelable(false)
    	.setMessage(lHtml)
    	.setIcon(R.drawable.ic_launcher)
    	.setNeutralButton("Zamknij",
    	new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,
    	int which) { 		
    	}
    	}).show();
	}
	
	public void DoZakoncz(View v)
	{
	  finish();	
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
	        	}
        }
    };
		        		

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     //   getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
