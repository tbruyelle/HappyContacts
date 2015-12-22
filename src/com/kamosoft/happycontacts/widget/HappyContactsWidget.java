package com.kamosoft.happycontacts.widget;

import java.util.LinkedHashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.events.NextEventsActivity;
import com.kamosoft.happycontacts.events.NextEventsAsyncTask;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;
import com.kamosoft.utils.DateUtils;

/**
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

        private static final short MAX_DAY_DISPLAYED = 3;

        /**
         * @see android.app.Service#onStart(android.content.Intent, int)
         */
        @Override
        public void onStart( Intent intent, int startId )
        {
            super.onStart( intent, startId );

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews rootViews = new RemoteViews( this.getPackageName(), R.layout.appwidget );

            // remove old views from previous call
            rootViews.removeAllViews( R.id.widget_events_list );
            // add the loading message
            rootViews.setViewVisibility( R.id.widget_event_counter, View.VISIBLE );
            rootViews.setTextViewText( R.id.widget_event_counter, getString( R.string.loading ) );

            /*
             * init and open database
             */
            DbAdapter db = new DbAdapter( this );
            db.open( true );

            LinkedHashMap<String, ContactFeasts> eventsPerDate = db.fetchTodayNextEvents();
            if ( eventsPerDate == null )
            {
                Log.d( "Events not found in db" );
                eventsPerDate = NextEventsAsyncTask.lookForNextEvents( this );
            }
            Log.d( "Events looked" );

            // Create an Intent to launch NextEventsActivity
            Intent widgetIntent = new Intent( this, NextEventsActivity.class );
            PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, widgetIntent, 0 );
            // attach an on-click listener          
            rootViews.setOnClickPendingIntent( R.id.widget_root, pendingIntent );

            // get the sooner events
            short dayDisplayed = 0;
            short eventsMoreThanMax = 0;
            for ( Map.Entry<String, ContactFeasts> entry : eventsPerDate.entrySet() )
            {
                ContactFeasts contactFeasts = entry.getValue();
                if ( contactFeasts != null && contactFeasts.getContactList().isEmpty() )
                {
                    continue;
                }

                if ( dayDisplayed >= MAX_DAY_DISPLAYED )
                {
                    eventsMoreThanMax += contactFeasts.getContactList().size();
                    continue;
                }
                dayDisplayed++;

                String eventWhen = entry.getKey();
                Log.d( "Events found at " + eventWhen );

                if ( dayDisplayed > 1 )
                {
                    /* add a divider */
                    RemoteViews divider = new RemoteViews( this.getPackageName(), R.layout.divider_dark );
                    rootViews.addView( R.id.widget_events_list, divider );
                }

                //View eventElementLayout = layoutInflater.inflate( R.layout.event_element, null );
                RemoteViews eventElementLayout = new RemoteViews( this.getPackageName(), R.layout.appwidget_element );

                eventElementLayout.setTextViewText( R.id.sooner_event_date, DateUtils.getDateLabel( this, eventWhen ) );

                StringBuilder sb = new StringBuilder();
                for ( Map.Entry<Long, ContactFeast> entry2 : contactFeasts.getContactList().entrySet() )
                {
                    if ( sb.length() > 0 )
                    {
                        sb.append( ", " );
                    }
                    sb.append( entry2.getValue().getContactName() );
                }
                eventElementLayout.setTextViewText( R.id.sooner_events, sb.toString() );
                rootViews.addView( R.id.widget_events_list, eventElementLayout );
            }
            if ( dayDisplayed == 0 )
            {
                RemoteViews eventElementLayout = new RemoteViews( this.getPackageName(), R.layout.appwidget_element );
                eventElementLayout.setTextViewText( R.id.sooner_event_date, getString( R.string.no_nextevents ) );
                eventElementLayout.setTextViewText( R.id.sooner_events, null );
                rootViews.addView( R.id.widget_events_list, eventElementLayout );
            }
            //           
            rootViews.setViewVisibility( R.id.widget_event_counter, View.GONE );
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName( this, HappyContactsWidget.class );
            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            manager.updateAppWidget( thisWidget, rootViews );
            db.close();
            /* stop the service */
            this.stopSelf();
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
