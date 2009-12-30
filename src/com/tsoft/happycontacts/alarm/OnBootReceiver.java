package com.tsoft.happycontacts.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tsoft.happycontacts.Constants;
import com.tsoft.happycontacts.Log;

/**
 * Reset the alarm if necessary
 * @author tom
 *
 * @since 29 déc. 2009
 */
public class OnBootReceiver
    extends BroadcastReceiver
    implements Constants
{
    /**
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( Log.DEBUG )
        {
            Log.v( "OnBootReceiver: start onReceive()" );
        }
        SharedPreferences prefs = context.getSharedPreferences( APP_NAME, 0 );
        if ( prefs.getBoolean( PREF_ALARM_STATUS, true ) )
        {
            AlarmController.startAlarm( context );
        }
        if ( Log.DEBUG )
        {
            Log.v( "OnBootReceiver: end onReceive()" );
        }

    }
}
