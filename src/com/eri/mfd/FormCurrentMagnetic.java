package com.eri.mfd;

import java.util.Timer;
import java.util.TimerTask;

import com.eri.mfd.views.MagneticHistory;
import com.eri.mfd.views.MagneticValue;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.TextView;

public class FormCurrentMagnetic extends Activity   implements SensorEventListener{

   	private TextView magneticX;
    private TextView magneticY;
    private TextView magneticZ;
    private TextView magneticAbs;
    private MagneticHistory mMagneticHistory;
    private MagneticValue mMagneticValue;
    private SensorManager sensorManager = null;
    
    Timer mValueTimer = null;
    double mMagneticUT = 0;
    Double mMagneticUTLock = new Double(0);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_current_magnetic);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
       
        mMagneticHistory = (MagneticHistory) findViewById(R.id.viewMagnetometrHistory);
	    mMagneticValue = (MagneticValue) findViewById(R.id.viewMagnetometrValue);
        
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
        // Unregister the listener
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

        float[] R = new float[16];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrix (R, null, aValues, mValues);
        SensorManager.getOrientation (R, orientationValues);

        orientationValues[0] = (float)Math.toDegrees (orientationValues[0]);
        orientationValues[1] = (float)Math.toDegrees (orientationValues[1]);
        orientationValues[2] = (float)Math.toDegrees (orientationValues[2]);
        
        double lMagneticUT = Math.sqrt(mValues[0]*mValues[0] + mValues[1]*mValues[1] + mValues[2]*mValues[2]);
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
        		mMagneticValue.setMagneticValue((int)lMagneticUT);
        		mMagneticHistory.addMagneticValue((int)lMagneticUT);
        	}
    }
};
	        		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	 //   getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}
}