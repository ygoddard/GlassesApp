package sdk.everysight.examples.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by eran on 15/02/2017.
 */

public class BarometerReceiver extends BroadcastReceiver
{
    public static final String INTENT_NAME = "evs.intent.BarometerEvent";
    public static final String EXTRA_PRESSURE = "evs.intent.BarometerEvent.pressure";
    public static final String EXTRA_ALTITUDE = "evs.intent.BarometerEvent.altitude";
    private final Context mContext;
    private final IBarometerCallback mCallback;

    public interface IBarometerCallback
    {
        void onBarometerData(float pressureMbr,float altitudeMeter);
    }
    public BarometerReceiver(Context context,IBarometerCallback callback)
    {
        mCallback = callback;
        mContext = context;
    }
    @Override
    public void onReceive(Context context, Intent intent)
    {
        float pressure = intent.getExtras().getFloat(EXTRA_PRESSURE);
        float altitude = intent.getExtras().getFloat(EXTRA_ALTITUDE);
        mCallback.onBarometerData(pressure,altitude);
    }

    public void register()
    {
        mContext.registerReceiver(this,new IntentFilter(INTENT_NAME));
    }
    public void unregister()
    {
        mContext.unregisterReceiver(this);
    }

}
