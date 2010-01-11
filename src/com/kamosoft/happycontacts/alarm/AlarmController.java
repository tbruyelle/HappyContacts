/**
 * 
 */
package com.kamosoft.happycontacts.alarm;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;

/**
 * @author tom
 *
 */
public class AlarmController
    implements Constants
{
    /*
     * Wake lock code took from http://github.com/commonsguy/cw-advandroid/tree/master/SystemServices/Alarm/src/com/commonsware/android/syssvc/alarm/
     */
    private static final String LOCK_NAME_STATIC = "com.kamosoft.happycontacts.LOCK";

    private static PowerManager.WakeLock lockStatic = null;

    public static void acquireStaticLock( Context context )
    {
        getLock( context ).acquire();
    }

    synchronized private static PowerManager.WakeLock getLock( Context context )
    {
        if ( lockStatic == null )
        {
            PowerManager mgr = (PowerManager) context.getSystemService( Context.POWER_SERVICE );

            lockStatic = mgr.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC );
            lockStatic.setReferenceCounted( true );
        }

        return ( lockStatic );
    }

    /*
     * end code from commonsware
     */

    public static void releaseStaticLock( Context context )
    {
        getLock( context ).release();
    }

    public static void startAlarm( Context context )
    {
        if ( Log.DEBUG )
        {
            Log.v( "AlarmController: start startAlarm()" );
        }

        // no need if use PendingIntent.FLAG_CANCEL_CURRENT
        //        if ( isAlarmUp( context ) )
        //        {
        //            cancelAlarm( context );
        //        }

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

        alarmManager.set( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                          getPendingIntent( context, PendingIntent.FLAG_CANCEL_CURRENT ) );

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
        alarmManager.cancel( getPendingIntent( context, PendingIntent.FLAG_CANCEL_CURRENT ) );
        //        PendingIntent pendingIntent = getPendingIntent( context, PendingIntent.FLAG_CANCEL_CURRENT );
        //        pendingIntent.cancel();
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
