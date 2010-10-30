/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
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
    private static final int dayLimit = 15;

    private DbAdapter mDb;

    private SectionedAdapter mSectionedAdapter;

    private TextView mEventCounter;

    private LinkedHashMap<String, ContactFeasts> mEventsPerDate;

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
        mEventCounter = (TextView) findViewById( R.id.nextevents_counter );

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
        if ( mSectionedAdapter == null )
        {
            new NextEventsAsyncTask( this, dayLimit, mDb ).execute();
        }
        else
        {
            setListAdapter( mSectionedAdapter );
        }
        if ( Log.DEBUG )
        {
            Log.v( "NextEventsActivity: end onResume" );
        }
    }

    private void displayEvents()
    {
        mSectionedAdapter = new EventSectionedAdapter( this );
        int nbEvents = 0;
        if ( mEventsPerDate != null && !mEventsPerDate.isEmpty() )
        {
            for ( Map.Entry<String, ContactFeasts> entry : mEventsPerDate.entrySet() )
            {
                String date = entry.getKey();
                ContactFeasts contactFeasts = entry.getValue();
                ArrayList<ContactFeast> contacts = new ArrayList<ContactFeast>();
                for ( Map.Entry<Long, ContactFeast> contactEntry : contactFeasts.getContactList().entrySet() )
                {
                    if ( Log.DEBUG )
                    {
                        Log.d( "Event retrieved " + entry.toString() );
                    }
                    contacts.add( contactEntry.getValue() );
                }
                if ( !contacts.isEmpty() )
                {
                    nbEvents += contacts.size();
                    EventArrayAdapter eventArrayAdapter = new EventArrayAdapter( this, R.layout.event_element, contacts );
                    mSectionedAdapter.addSection( date, eventArrayAdapter );
                }
            }
        }
        mEventCounter.setText( String.valueOf( nbEvents ) );
        setListAdapter( mSectionedAdapter );
    }

    public void finishRetrieveNextEvents( LinkedHashMap<String, ContactFeasts> results )
    {
        mEventsPerDate = results;
        displayEvents();
    }

}
