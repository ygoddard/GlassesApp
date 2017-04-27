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


package sdk.everysight.examples.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everysight.activities.managers.EvsPopupManager;
import com.everysight.activities.managers.EvsServiceInterfaceManager;
import com.everysight.base.EvsContext;
import com.everysight.carousel.EvsCarouselActivity;
import com.everysight.common.carouselm.CarouselBehavior;
import com.everysight.common.carouselm.ItemInfo;
import com.everysight.common.carouselm.OnCarouselItemClickListener;
import com.everysight.notifications.EvsAlertNotification;
import com.everysight.notifications.EvsNotification;
import com.everysight.notifications.EvsToast;

import java.util.ArrayList;

import sdk.everysight.examples.gps.R;

/*
This is a standard Android Location service example. No difference what-so-ever.
The only thing you should keep in mind is the source of the GPS data.
The source depends on Glasses global system configuration.
It can be either from on-board glasses GPS or paired phone location service.
*/
public class MainActivity extends EvsCarouselActivity implements LocationListener
{
	private final String TAG = "MainActivity";
	private TextView mCxtCenterLable = null;
	private TextView mMenuLable = null;
	private LocationManager mLocationManager = null;
	private ArrayList<ItemInfo> mMainMenu = null;
	private EvsPopupManager mPopupManager;
	private CarouselBehavior mMainCarouselBehavior;
	private BarometerReceiver mBarometerReceiver = new BarometerReceiver(this, new BarometerReceiver.IBarometerCallback()
	{
		@Override
		public void onBarometerData(float pressureMbr, float altitudeMeter)
		{
			Log.i(TAG,"Got barometer data: " + pressureMbr + ", " + altitudeMeter);
		}
	});


	/******************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

		mCxtCenterLable = (TextView) this.findViewById(R.id.centerLabel);
		mMenuLable = (TextView) this.findViewById(R.id.menuLabel);

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


		initGps();
		initMainMenu();


	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mBarometerReceiver.register();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mBarometerReceiver.unregister();
	}

	private void initMainMenu()
	{
		mMainCarouselBehavior = new CarouselBehavior();
		mMainCarouselBehavior.setSelectionAnimation(CarouselBehavior.CarouselSelectionAnimationType.fullAnimation);
		mMainCarouselBehavior.setOpeningAnimationType(CarouselBehavior.CarouselOpeningAnimationType.applicationMenu);

		mMainMenu = new ArrayList<>();
		ItemInfo item = new ItemInfoExe("Popup", getResources().getDrawable(R.drawable.ic_games))
		{
			@Override
			public void execute()
			{
				showPopup();
			}
		};
		mMainMenu.add(item);

		item = new ItemInfoExe("Toast", getResources().getDrawable(R.drawable.ic_apps))
		{
			@Override
			public void execute()
			{
				EvsToast.show(MainActivity.this,"This is a toast");
			}
		};
		mMainMenu.add(item);
		item = new ItemInfoExe("Audio", getResources().getDrawable(R.drawable.ic_sms))
		{
			@Override
			public void execute()
			{
				StartAudio();
			}
		};
		mMainMenu.add(item);

		item = new ItemInfoExe("Close", getResources().getDrawable(R.drawable.ic_close_app))
		{
			@Override
			public void execute()
			{
				onBackPressed();
			}
		};
		mMainMenu.add(item);
	}

	private void StartAudio()
	{
		final AudioProcessor audio = new AudioProcessor();
		audio.start();
		mMenuLable.setText("Speak now!!");
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				audio.close();
				MainActivity.this.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mMenuLable.setText("Tap to open menu");
					}
				});
			}
		},2000);
	}


	private void initGps()
	{
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager == null)
		{
			Log.e(TAG, "No GPS LocationManager is available?");
			mCxtCenterLable.setText("No location manager");
			return;
		}

		String provider = mLocationManager.getBestProvider(new Criteria(), false);
		if (provider == null)
		{
			Log.e(TAG, "No GPS provider?");
			mCxtCenterLable.setText("No GPS provider");
			return;
		}

		// lets get the last known location
		Location location = mLocationManager.getLastKnownLocation(provider);
		if(location != null)
		{
			mCxtCenterLable.setText("Lat: " + String.format("%.2f", location.getLatitude()) + ", Lon: " + String.format("%.2f", location.getLongitude()));
		}

		// register for updates - get GPS point as soon as it is available
		mLocationManager.requestLocationUpdates(provider, 0, 0, this);
	}

	/******************************************************************/
	@Override
	public void onDestroy()
	{
		// clean up once we're done
		super.onDestroy();
		if(mLocationManager != null)
		{
			mLocationManager.removeUpdates(this);
		}
	}

	@Override
	protected void onDownCompleted()
	{
		super.onDownCompleted();
		if(!isCarouselOpened())
		{
			mMenuLable.setVisibility(View.VISIBLE);
		}
	}

	/******************************************************************/
	@Override
	public void onLocationChanged(Location location)
	{
		mCxtCenterLable.setText(location.getTime() + "\n" + location.getLatitude() + "\n" + location.getLongitude());
	}

	/******************************************************************/
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}

	/******************************************************************/
	@Override
	public void onProviderEnabled(String provider)
	{

	}

	/******************************************************************/
	@Override
	public void onProviderDisabled(String provider)
	{

	}

	@Override
	public void onTap()
	{
		super.onTap();
		if(!isCarouselOpened())
		{
			openMainMenu();
			return;
		}

	}

	private void openMainMenu()
	{
		mMenuLable.setVisibility(View.GONE);
		openMenuCarousel(true, mMainMenu, (ViewGroup) findViewById(R.id.SelectSettingsCarousel), new OnCarouselItemClickListener()
		{
			@Override
			public void onItemClick(int i, ItemInfo itemInfo)
			{
				((ItemInfoExe)itemInfo).execute();
				closeMenuCarousel();
				mMenuLable.setVisibility(View.VISIBLE);
			}
		},mMainCarouselBehavior);
	}
	private void showPopup()
	{
		if(mPopupManager!=null)
		{
			//create the popup notification
			EvsNotification notif = new EvsAlertNotification()
					.setTapAction(this,R.drawable.ic_launcher,null,null)
					.setTitle("I'm a popup")
					.setMessage("Swipe down to dismiss");

			//ask Everysight OS to show the popup
			mPopupManager.notify(notif);
		}
	}
}
