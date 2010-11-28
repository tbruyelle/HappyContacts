/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts.widget;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.events.EventSectionedAdapter;
import com.kamosoft.happycontacts.events.NextEventsAsyncTask;
import com.kamosoft.happycontacts.model.ContactFeast;
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
            /*
             * init and open database
             */
            DbAdapter db = new DbAdapter( this );
            db.open( true );

            LinkedHashMap<String, ContactFeasts> eventsPerDate = db.fetchTodayNextEvents();
            if ( eventsPerDate == null )
            {
                eventsPerDate = NextEventsAsyncTask.lookForNextEvents( this );
            }
            Log.d( "Events looked" );
            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews( this.getPackageName(), R.layout.appwidget );

            //display the events number
            //views.setTextViewText( R.id.nextevents_counter, String.valueOf( eventsPerDate.size() ) );

            // get the sooner events
            boolean eventFound = false;
            for ( Map.Entry<String, ContactFeasts> entry : eventsPerDate.entrySet() )
            {
                ContactFeasts contactFeasts = entry.getValue();
                if ( contactFeasts != null && contactFeasts.getContactList().isEmpty() )
                {
                    continue;
                }
                eventFound = true;
                String eventDate = entry.getKey();
                Log.d( "Events found at " + eventDate );                
                try
                {
                    views
                        .setTextViewText( R.id.sooner_event_date, EventSectionedAdapter.getDateLabel( this, eventDate ) );
                }
                catch ( ParseException e )
                {
                    Log.e( "Error parsing date " + eventDate );
                    views.setTextViewText( R.id.sooner_event_date, eventDate );
                }
                StringBuilder sb = new StringBuilder();
                for ( Map.Entry<Long, ContactFeast> entry2 : contactFeasts.getContactList().entrySet() )
                {
                    if ( sb.length() > 0 )
                    {
                        sb.append( ", " );
                    }
                    sb.append( entry2.getValue().getContactName() );
                }
                views.setTextViewText( R.id.sooner_events, sb.toString() );
                break;
            }
            if ( !eventFound )
            {
                views.setTextViewText( R.id.sooner_event_date,getString( R.string.no_nextevents ));
                views.setTextViewText( R.id.sooner_events, null );                
            }

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
