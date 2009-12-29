/**
 * 
 */
package com.tsoft.happycontacts.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tsoft.happycontacts.Constants;
import com.tsoft.happycontacts.Log;

/**
 * @author tom
 *
 */
public class AlarmController
    implements Constants
{

    public static void startAlarm( Context context )
    {
        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: start startAlarm()" );
        }

        if ( isAlarmUp( context ) )
        {
            cancelAlarm( context );
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );

        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = calendar.getTimeInMillis();
        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: currentTimeMillis=" + currentTimeMillis );
        }
        SharedPreferences prefs = context.getSharedPreferences( APP_NAME, 0 );
        int hour = prefs.getInt( PREF_ALARM_HOUR, DEFAULT_ALARM_HOUR );
        int minute = prefs.getInt( PREF_ALARM_MINUTE, DEFAULT_ALARM_MINUTE );
        calendar.set( Calendar.HOUR_OF_DAY, hour );
        calendar.set( Calendar.MINUTE, minute );
        calendar.set( Calendar.SECOND, 0 );

        if ( calendar.getTimeInMillis() <= currentTimeMillis )
        {
            /* time elapsed, add a day */
            calendar.add( Calendar.DAY_OF_MONTH, 1 );
        }

        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: set alarm to " + calendar.getTime() );
        }

        alarmManager.set( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent( context, 0 ) );

        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: end startAlarm()" );
        }
    }

    /**
     * Cancel the alarm
     * @param context
     */
    public static void cancelAlarm( Context context )
    {
        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: start cancelAlarm()" );
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        alarmManager.cancel( getPendingIntent( context, 0 ) );
        PendingIntent pendingIntent = getPendingIntent( context, PendingIntent.FLAG_CANCEL_CURRENT );
        pendingIntent.cancel();
        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: end cancelAlarm()" );
        }
    }

    /**
     * @param context
     * @return true if Alarm is up
     */
    public static boolean isAlarmUp( Context context )
    {
        PendingIntent pendingIntent = getPendingIntent( context, PendingIntent.FLAG_NO_CREATE );
        return ( pendingIntent != null );
    }

    /**
     * Retrieve the PendingIntent that will start AlarmReceiver.
     * @param context
     * @param flag
     * @return
     */
    private static PendingIntent getPendingIntent( Context context, int flag )
    {
        return PendingIntent.getBroadcast( context, 0, new Intent( context, AlarmReceiver.class ), flag );
    }
}
