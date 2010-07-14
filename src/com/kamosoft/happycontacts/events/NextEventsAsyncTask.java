/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.text.format.DateFormat;

import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.DayMatcherService;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * @author tom
 *
 */
public class NextEventsAsyncTask
    extends AsyncTask<Void, String, EventSectionedAdapter>
    implements DateFormatConstants
{
    private NextEventsActivity mActivity;

    private int mDayLimit;

    private DbAdapter mDb;

    public NextEventsAsyncTask( NextEventsActivity activity, int dayLimit, DbAdapter db )
    {
        mActivity = activity;
        mDayLimit = dayLimit;
        mDb = db;
    }

    @Override
    protected void onPostExecute( EventSectionedAdapter result )
    {
        mActivity.finishRetrieveNextEvents( result );
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected EventSectionedAdapter doInBackground( Void... params )
    {
        EventSectionedAdapter sectionedAdapter = new EventSectionedAdapter( mActivity );

        HashMap<Date, ContactFeasts> eventsPerDate = new HashMap<Date, ContactFeasts>();
        Calendar calendar = Calendar.getInstance();
        int inc = 0;
        int nbEvents = 0;
        while ( inc++ < mDayLimit )
        {
            ContactFeasts contactFeasts = new ContactFeasts();
            Date date = calendar.getTime();
            String dayDate = dayDateFormat.format( date ), fullDate = fullDateFormat.format( date );
            if ( Log.DEBUG )
            {
                Log.d( "Retrieving events for " + fullDate );
            }

            DayMatcherService.checkNameDays( mActivity, mDb, contactFeasts, dayDate, fullDate );
            DayMatcherService.checkBirthdays( mActivity, mDb, contactFeasts, dayDate, fullDate );

            eventsPerDate.put( calendar.getTime(), contactFeasts );
            ArrayList<ContactFeast> contacts = new ArrayList<ContactFeast>();
            for ( Map.Entry<Long, ContactFeast> entry : contactFeasts.getContactList().entrySet() )
            {
                if ( Log.DEBUG )
                {
                    Log.d( "Event retrieved " + entry.toString() );
                }
                contacts.add( entry.getValue() );
               
            }
            if ( !contacts.isEmpty() )
            {
                nbEvents += contacts.size();
                EventArrayAdapter eventArrayAdapter = new EventArrayAdapter( mActivity, R.layout.event_element,
                                                                             contacts );
                sectionedAdapter.addSection( DateFormat.getDateFormat( mActivity ).format( date ), eventArrayAdapter );
            }

            calendar.add( Calendar.DAY_OF_YEAR, 1 );
        }
        sectionedAdapter.setNbEvents( nbEvents );
        return sectionedAdapter;
    }

}
