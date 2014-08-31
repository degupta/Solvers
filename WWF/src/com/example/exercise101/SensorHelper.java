package com.example.exercise101;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorHelper implements SensorEventListener
{
	public static final int SHAKE_THRESHOLD = 800;
	public static final int ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Context mContext;
	private int type;
	private long lastUpdate, lastTimeAccepted;
	private float lastX, lastY, lastZ;
	ICallback callback;
	
	public SensorHelper(Context context, int _type, ICallback _callback)
	{
		this.mContext = context;
		this.type = _type;
		this.mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
	    this.mSensor = this.mSensorManager.getDefaultSensor(type);
	    this.callback = _callback;
	}
	 
	protected void onResume() 
	{
	    this.mSensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	 
	protected void onPause() 
	{
	    this.mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		switch(this.type)
		{
			case Sensor.TYPE_ACCELEROMETER:
				if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
					return;
				long curTime = System.currentTimeMillis();
			    // only allow one update every 100ms.
			    if ((curTime - lastUpdate) > 100 && curTime - lastTimeAccepted > 3000) 
			    {
			    	long diffTime = (curTime - lastUpdate);
			    	lastUpdate = curTime;
			      
					float x = event.values[SensorManager.DATA_X];
				    float y = event.values[SensorManager.DATA_Y];
				    float z = event.values[SensorManager.DATA_Z];
				    
				    float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

					if (speed > SHAKE_THRESHOLD)
					{
						callback.callback(null);
						lastTimeAccepted = curTime;
					}
					lastX = x;
					lastY = y;
					lastZ = z;
			    }
				break;
		}
	}

}
