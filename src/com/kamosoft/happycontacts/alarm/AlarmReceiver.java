/**
 * 
 */
package com.kamosoft.happycontacts.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kamosoft.happycontacts.DayMatcherService;
import com.kamosoft.happycontacts.Log;

/**
 * Launch contact search for current date.
 * @author tom
 *
 */
public class AlarmReceiver
    extends BroadcastReceiver
{
    /**
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( Log.DEBUG )
        {
            Log.v( "AlarmReceiver: start onReceive()" );
        }
        /*
         * acquire the wake lock
         */
        AlarmController.acquireStaticLock( context );

        /*
         * direct call a service to minimize time
         */
        Intent startDayMatcher = new Intent( context, DayMatcherService.class );
        context.startService( startDayMatcher );

        if ( Log.DEBUG )
        {
            Log.v( "AlarmReceiver: stop onReceive()" );
        }
    }
}
