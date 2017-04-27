/*
 * This work contains files distributed in Android, such files Copyright (C) 2016 The Android Open Source Project
 *
 * and are Licensed under the Apache License, Version 2.0 (the "License"); you may not use these files except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
*/

package sdk.everysight.examples.los;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.everysight.base.EvsBaseActivity;
import com.everysight.utilities.EvsTimer;
import com.everysight.utilities.SensorOrientationUtils;

import java.text.DecimalFormat;

/*
This is a standard Android sensors example, with translation to the glasses Axis
*/
public class MainActivity extends EvsBaseActivity implements SensorEventListener
{
	private final String TAG = "LosSample";
    private final String EMPTY_INDICATION = "NA";
	private TextView mYaw,mPitch,mRoll;
	private boolean mIsLosActive = false;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private float[] mLosAngles = null;
    private Sensor mQuaternion;
    private EvsTimer mUiUpdateTimer = null;
    private static final int UI_TIMER_PERIOD = 30;
    private DecimalFormat mFormat;
    private ImageView mCube;

    /******************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

		mYaw = (TextView) this.findViewById(R.id.Yaw);
		mPitch = (TextView) this.findViewById(R.id.Pitch);
		mRoll = (TextView) this.findViewById(R.id.Roll);
        mCube = (ImageView) this.findViewById(R.id.Cube);
        mCube.setVisibility(View.GONE);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "LosSample");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mQuaternion = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mFormat = new DecimalFormat("#.##");

        mUiUpdateTimer = new EvsTimer(new EvsTimer.IEvsTimerCallback()
        {
            @Override
            public void onTick(long l)
            {
                updateLosUI();
            }
        },UI_TIMER_PERIOD, false);
        updateLosUI();
	}

    @Override
    protected void onResume()
    {
        super.onResume();
        mWakeLock.acquire();
    }

    @Override
	public void onTap()
	{
		super.onTap();
		if(mIsLosActive)
		{
			deactivateLos();
		}
		else
		{
			activateLos();
		}
	}

	private void activateLos()
	{
		if(mIsLosActive)
		{
			return;
		}
        mCube.setRotation(0);
        mCube.setVisibility(View.VISIBLE);
		//get reference location from LocationManager (see GPS_Example)
		//in order to correct the magnetic variation
		//SensorOrientationUtils.setReferenceLocation(currentLocation);
        mSensorManager.registerListener(this, mQuaternion, SensorManager.SENSOR_DELAY_FASTEST);
        mUiUpdateTimer.start();
        mIsLosActive = true;
	}

    // NOTE:
    // You should call the UI rendering just when you must, performance-wise
    // This sample illustrates the mechanism WITHOUT taking the performance into consideration
    private void updateLosUI()
    {
        if(mLosAngles==null)
        {
            mYaw.setText("YAW\n"+EMPTY_INDICATION);
            mPitch.setText("PITCH\n"+EMPTY_INDICATION);
            mRoll.setText("ROLL\n" + EMPTY_INDICATION);
            return;
        }

        float[] losAngles = mLosAngles.clone();
        mYaw.setText("YAW\n"+mFormat.format(Math.toDegrees(losAngles[0])));
        mPitch.setText("PITCH\n"+mFormat.format(Math.toDegrees(losAngles[1])));
        double roll = Math.toDegrees(losAngles[2]);
        mRoll.setText("ROLL\n" + mFormat.format(roll));
        mCube.setRotation(-(float)roll);

    }


    private void deactivateLos()
	{
		if(!mIsLosActive)
		{
			return;
		}
        mCube.setVisibility(View.GONE);
        mSensorManager.unregisterListener(this);
        mUiUpdateTimer.stop();
        mLosAngles = null;
        updateLosUI();
        mIsLosActive = false;

	}

	@Override
	protected void onPause()
	{
		super.onPause();
        mWakeLock.release();
		deactivateLos();

	}

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event == null)
        {
            return;
        }

        int sensor_type = event.sensor.getType();
        if(sensor_type != Sensor.TYPE_ROTATION_VECTOR)
        {
            return;
        }

        float[] quaternion = event.values.clone();//Quarernion is [x,y,z,w]
        mLosAngles = SensorOrientationUtils.QuaternionToAngles(quaternion);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
