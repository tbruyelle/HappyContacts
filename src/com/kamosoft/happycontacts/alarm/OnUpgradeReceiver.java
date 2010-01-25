/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;

/**
 * Triggered when application is being upgraded in order to reset the alarm if necessary
 * @author tom
 * 
 */
public class OnUpgradeReceiver
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
            Log.v( "OnUpgradeReceiver: start onReceive()" );
        }
        SharedPreferences prefs = context.getSharedPreferences( APP_NAME, 0 );
        if ( prefs.getBoolean( PREF_ALARM_STATUS, true ) )
        {
            AlarmController.startAlarm( context );
        }
        if ( Log.DEBUG )
        {
            Log.v( "OnUpgradeReceiver: end onReceive()" );
        }
    }

}
