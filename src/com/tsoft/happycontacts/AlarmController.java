/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * @author tom
 *
 */
public class AlarmController
{
    /** default alarm to 9AM */
    public static int DEFAULT_ALARM_HOUR = 9;

    public static int DEFAULT_ALARM_MINUTE = 0;

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
        SharedPreferences prefs = context.getSharedPreferences( HappyContactsPreferences.appName, 0 );
        int hour = prefs.getInt( "alarmHour", DEFAULT_ALARM_HOUR );
        int minute = prefs.getInt( "alarmMinute", DEFAULT_ALARM_MINUTE );
        calendar.set( Calendar.HOUR_OF_DAY, hour );
        calendar.set( Calendar.MINUTE, minute );
        calendar.set( Calendar.SECOND, 0 );

        if ( calendar.getTimeInMillis() <= currentTimeMillis )
        {
            /* temps deja passe, on incremente d'un jour */
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
