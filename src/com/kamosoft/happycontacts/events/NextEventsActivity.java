/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;

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

    private SectionedAdapter mSectionedAdapter;

    private ProgressDialog mProgressDialog;

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
            getProgressDialog().show();
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

    public void finishRetrieveNextEvents( SectionedAdapter adapter )
    {
        getProgressDialog().dismiss();
        setListAdapter( adapter );
    }

    public ProgressDialog getProgressDialog()
    {
        if ( mProgressDialog == null )
        {
            mProgressDialog = ProgressDialog.show( this, "", getString( R.string.retrieving_events, 0 ), true );
        }
        return mProgressDialog;
    }
}
