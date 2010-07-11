/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.DayMatcherService;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * Display the upcoming events for the next 30 days
 * 
 * @author tom
 * 
 */
public class NextEventsActivity
    extends ListActivity
    implements DateFormatConstants, Constants
{
    private static final int dayLimit = 10;

    private DbAdapter mDb;

    private HashMap<Date, ContactFeasts> eventsPerDate;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if ( Log.DEBUG )
        {
            Log.v( "NextEventsActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.nextevents );
        eventsPerDate = new HashMap<Date, ContactFeasts>();
        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "NextEventsActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "NextEventsActivity: start onResume" );
        }
        super.onResume();
        mDb.open( true );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "NextEventsActivity: end onResume" );
        }
    }

    private void fillList()
    {
        SectionedAdapter sectionedAdapter = new SectionedAdapter()
        {
            @Override
            protected View getHeaderView( String caption, int index, View convertView, ViewGroup parent )
            {
                TextView result = (TextView) convertView;

                if ( convertView == null )
                {
                    result = (TextView) getLayoutInflater().inflate( R.layout.event_header, null );
                }

                result.setText( caption );

                return ( result );
            }
        };
        Calendar calendar = Calendar.getInstance();
        int inc = 0;
        while ( inc < dayLimit )
        {
            inc++;
            ContactFeasts contactFeasts = new ContactFeasts();
            String dayDate = dayDateFormat.format( calendar.getTime() ), fullDate = fullDateFormat.format( calendar
                .getTime() );
            if ( Log.DEBUG )
            {
                Log.d( "Retrieving events for " + fullDate );
            }

            DayMatcherService.checkNameDays( this, mDb, contactFeasts, dayDate, fullDate );

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
                EventArrayAdapter eventArrayAdapter = new EventArrayAdapter( this, R.layout.event_element, contacts );
                sectionedAdapter.addSection( fullDate, eventArrayAdapter );
            }

            calendar.add( Calendar.DAY_OF_YEAR, 1 );
        }
        setListAdapter( sectionedAdapter );
    }
}
