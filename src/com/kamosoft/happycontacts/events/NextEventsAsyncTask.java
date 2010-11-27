/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateFormat;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.DayMatcherService;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * @author tom
 * 
 */
public class NextEventsAsyncTask
    extends AsyncTask<Void, String, LinkedHashMap<String, ContactFeasts>>
    implements DateFormatConstants
{
    private NextEventsHandler mHandler;

    private Context mContext;

    private final ProgressDialog mDialog;

    public NextEventsAsyncTask( NextEventsHandler handler, Context context )
    {
        mHandler = handler;
        mContext = context;
        mDialog = new ProgressDialog( mContext );
    }

    @Override
    protected void onPreExecute()
    {
        if ( mDialog != null )
        {
            mDialog.setMessage( mContext.getString( R.string.retrieving_events, 0 ) );
            mDialog.show();
        }
    }

    @Override
    protected void onPostExecute( LinkedHashMap<String, ContactFeasts> result )
    {
        if ( mDialog != null && mDialog.isShowing() )
        {
            mDialog.dismiss();
        }
        mHandler.finishRetrieveNextEvents( result );
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected LinkedHashMap<String, ContactFeasts> doInBackground( Void... params )
    {

        return lookForNextEvents( mContext );
    }

    public static LinkedHashMap<String, ContactFeasts> lookForNextEvents( Context context )
    {
        /*
         * init and open database
         */
        DbAdapter db = new DbAdapter( context );
        db.open( true );

        LinkedHashMap<String, ContactFeasts> eventsPerDate = new LinkedHashMap<String, ContactFeasts>();
        Calendar calendar = Calendar.getInstance();
        int inc = 0;
        while ( inc++ < Constants.MAX_DAY_EVENTS )
        {
            ContactFeasts contactFeasts = new ContactFeasts();
            Date date = calendar.getTime();
            String dayDate = dayDateFormat.format( date ), fullDate = fullDateFormat.format( date );
            if ( Log.DEBUG )
            {
                Log.d( "Retrieving events for " + fullDate );
            }

            DayMatcherService.checkNameDays( context, db, contactFeasts, dayDate, fullDate );
            DayMatcherService.checkBirthdays( context, db, contactFeasts, dayDate, fullDate );
            if ( Log.DEBUG )
            {
                Log.d( "Found " + contactFeasts.getContactList().size() + " events to wish for " + dayDate );
            }
            eventsPerDate.put( DateFormat.getDateFormat( context ).format( date ), contactFeasts );
            calendar.add( Calendar.DAY_OF_YEAR, 1 );
        }
        /* recording results in database */
        db.insertNextEvents( eventsPerDate );
        db.close();
        return eventsPerDate;
    }

    //    /**
    //     * @see android.os.AsyncTask#doInBackground(Params[])
    //     */
    //    @Override
    //    protected EventSectionedAdapter doInBackground( Void... params )
    //    {
    //        EventSectionedAdapter sectionedAdapter = new EventSectionedAdapter( mActivity );
    //
    //        HashMap<Date, ContactFeasts> eventsPerDate = new HashMap<Date, ContactFeasts>();
    //        Calendar calendar = Calendar.getInstance();
    //        int inc = 0;
    //        int nbEvents = 0;
    //        while ( inc++ < mDayLimit )
    //        {
    //            ContactFeasts contactFeasts = new ContactFeasts();
    //            Date date = calendar.getTime();
    //            String dayDate = dayDateFormat.format( date ), fullDate = fullDateFormat.format( date );
    //            if ( Log.DEBUG )
    //            {
    //                Log.d( "Retrieving events for " + fullDate );
    //            }
    //
    //            DayMatcherService.checkNameDays( mActivity, mDb, contactFeasts, dayDate, fullDate );
    //            DayMatcherService.checkBirthdays( mActivity, mDb, contactFeasts, dayDate, fullDate );
    //
    //            eventsPerDate.put( calendar.getTime(), contactFeasts );
    //            ArrayList<ContactFeast> contacts = new ArrayList<ContactFeast>();
    //            for ( Map.Entry<Long, ContactFeast> entry : contactFeasts.getContactList().entrySet() )
    //            {
    //                if ( Log.DEBUG )
    //                {
    //                    Log.d( "Event retrieved " + entry.toString() );
    //                }
    //                contacts.add( entry.getValue() );
    //
    //            }
    //            if ( !contacts.isEmpty() )
    //            {
    //                nbEvents += contacts.size();
    //                EventArrayAdapter eventArrayAdapter = new EventArrayAdapter( mActivity, R.layout.event_element,
    //                                                                             contacts );
    //                sectionedAdapter.addSection( DateFormat.getDateFormat( mActivity ).format( date ), eventArrayAdapter );
    //            }
    //
    //            calendar.add( Calendar.DAY_OF_YEAR, 1 );
    //        }
    //        sectionedAdapter.setNbEvents( nbEvents );
    //        return sectionedAdapter;
    //    }

}
