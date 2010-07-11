/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;

import com.kamosoft.happycontacts.dao.DbAdapter;
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

        Calendar calendar = Calendar.getInstance();
        int inc = 0;
        while ( inc < dayLimit )
        {
            calendar.add( Calendar.DAY_OF_YEAR, inc );
            ContactFeasts contactFeasts = new ContactFeasts();
            DayMatcherService.checkNameDays( this, mDb, contactFeasts, dayDateFormat.format( calendar.getTime() ),
                                             fullDateFormat.format( calendar.getTime() ) );
            eventsPerDate.put( calendar.getTime(), contactFeasts );

            /* TODO voir http://github.com/commonsguy/cw-advandroid/tree/master/ListView/Sections/ pour les sections dans les listes */
        }

    }

}
