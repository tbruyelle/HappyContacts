/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.kamosoft.happycontacts.Constants;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 * created 18 janv. 2011
 * @since 
 * @version $Id$
 */
public class AlarmService
    extends Service
    implements Constants
{
    public static void start( Context context )
    {
        context.startService( new Intent( context, AlarmService.class ) );
    }

    /**
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public void onStart( Intent intent, int startId )
    {
        super.onStart( intent, startId );
        SharedPreferences prefs = this.getSharedPreferences( APP_NAME, 0 );
        if ( prefs.getBoolean( PREF_ALARM_STATUS, true ) )
        {
            AlarmController.startAlarm( this );
        }
    }

    /**
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind( Intent intent )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
