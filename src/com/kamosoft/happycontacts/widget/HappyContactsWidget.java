/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts.widget;

import java.util.LinkedHashMap;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.events.NextEventsAsyncTask;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 * created 21 nov. 2010
 * @since 
 * @version $Id$
 */
public class HappyContactsWidget
    extends AppWidgetProvider
{
    /**
     * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
     */
    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        context.startService( new Intent( context, UpdateService.class ) );
    }

    public static class UpdateService
        extends Service
    {

        /**
         * @see android.app.Service#onStart(android.content.Intent, int)
         */
        @Override
        public void onStart( Intent intent, int startId )
        {
            super.onStart( intent, startId );
            LinkedHashMap<String, ContactFeasts> eventsPerDate = NextEventsAsyncTask.lookForNextEvents( this, 5 );
            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews( this.getPackageName(), R.layout.appwidget );
            views.setTextViewText( R.id.nextevents_counter, String.valueOf( eventsPerDate.size() ) );
            
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName( this, HappyContactsWidget.class );
            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            manager.updateAppWidget( thisWidget, views );
        }

        /**
         * @see android.app.Service#onBind(android.content.Intent)
         */
        @Override
        public IBinder onBind( Intent intent )
        {
            // dont need to bind this service
            return null;
        }
    }

    /**
      * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context, int[])
      */
    @Override
    public void onDeleted( Context context, int[] appWidgetIds )
    {
        super.onDeleted( context, appWidgetIds );
    }

    /**
     * @see android.appwidget.AppWidgetProvider#onDisabled(android.content.Context)
     */
    @Override
    public void onDisabled( Context context )
    {
        super.onDisabled( context );
    }

    /**
     * @see android.appwidget.AppWidgetProvider#onEnabled(android.content.Context)
     */
    @Override
    public void onEnabled( Context context )
    {
        super.onEnabled( context );
    }
}
