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


package sdk.everysight.examples.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.everysight.activities.managers.EvsPopupManager;
import com.everysight.activities.managers.EvsServiceInterfaceManager;
import com.everysight.base.EvsBaseActivity;
import com.everysight.base.EvsContext;
import com.everysight.notifications.EvsAlertNotification;
import com.everysight.notifications.EvsNotification;
import com.everysight.notifications.EvsToast;

/**
 * Simple Activity for demonstrating basic capabilities
 * - Gestures (tap, back, forward swipes
 * - EvsToast
 * - Popup
 */
public class       SimpleActivity extends EvsBaseActivity
{
	private static final String CLICK_INTENT = "com.everysight.sample.click";
	private static final String TAG = "SimpleActivity";

	private TextView mCxtCenterLabel = null;
	private ImageView mPrevButton = null;
	private ImageView mNextButton = null;
	private int mCounter = 0;
	private EvsPopupManager mPopupManager;

	/**
	 * BroadcastReceiver to receive the popup intent when popup is tapped
	 */
	private BroadcastReceiver mClickReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			EvsToast.show(SimpleActivity.this,"Popup clicked");
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

//		mCxtCenterLabel = (TextView) this.findViewById(R.id.centerLable);
		mPrevButton = (ImageView) this.findViewById(R.id.prevButton);
		mNextButton = (ImageView) this.findViewById(R.id.nextButton);
		//get the evs popup service
		final EvsPopupManager popupManager = (EvsPopupManager)getEvsContext().getSystemService(EvsContext.POPUP_SERVICE_EVS);
		//wait for the service to bind
		popupManager.registerForServiceStateChanges(new EvsServiceInterfaceManager.IServiceStateListener()
		{
			@Override
			public void onServiceConnected()
			{
				//mark the service as connected (binded)
				mPopupManager = popupManager;
			}
		});
		//register the popup receiver
		registerReceiver(mClickReceiver,new IntentFilter(CLICK_INTENT));

	}

	/******************************************************************/
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(mClickReceiver);
	}

	/******************************************************************/
	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume : We are running again!");
	}

	/******************************************************************/
	@Override
	public void onPause()
	{
		super.onPause();
	}

	/******************************************************************/
	@Override
	public void onTap()
	{
		super.onTap();

		mCounter++;
//		mCxtCenterLabel.setText("אין יאוש בעולם כלל" );
	}

	/******************************************************************/
	@Override
	public void onUp()
	{
		super.onUp();
	}

	/******************************************************************/
	@Override
	public void onDown()
	{
		//the default behaviour of down is to close the activity
		super.onDown();
	}

	/******************************************************************/
//	@Override
//	public void onForward()
//	{
//		super.onForward();
//		animateClick(mNextButton,false);
//		EvsToast.show(this,"Forward swipe");
//	}



//	@Override
//	public void onBackward()
//	{
//		super.onBackward();
//		animateClick(mPrevButton,true);
//		if(mPopupManager!=null)
//		{
//			//show a popup message upon backward swipe
//
//			//create the callback intent
//			PendingIntent onClick = PendingIntent.getBroadcast(this, 0, new Intent(CLICK_INTENT), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
//
//			//create the popup notification
//			EvsNotification notif = new EvsAlertNotification()
//					.setTapAction(this,R.drawable.ic_launcher,null,onClick)
//					.setTitle("Back swipe")
//					.setMessage("Tap me");
//
//			//ask Everysight OS to show the popup
//			mPopupManager.notify(notif);
//		}
//	}

//
//	private void animateClick(View v,boolean isBack)
//	{
//		Animation a;
//		if(isBack)
//		{
//			a = AnimationUtils.loadAnimation(this, R.anim.click_bk);
//		}
//		else
//		{
//			a = AnimationUtils.loadAnimation(this, R.anim.click_fw);
//		}
//		a.reset();
//		v.clearAnimation();
//		v.startAnimation(a);
//	}

}
