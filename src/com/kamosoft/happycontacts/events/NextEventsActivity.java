/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
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
    implements DateFormatConstants, Constants, NextEventsHandler
{
    private static final int REFRESH_MENU_ID = Menu.FIRST;

    private DbAdapter mDb;

    private SectionedAdapter mSectionedAdapter;

    private TextView mEventCounter;

    private LinkedHashMap<String, ContactFeasts> mEventsPerDate;

    private ContactFeast mSelectedContactFeast;

    private QuickAction mQuickAction;

    private ActionItem mDisplayAction;

    private ActionItem mAddToBlackListAction;

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

        mDisplayAction = new ActionItem( getResources().getDrawable( R.drawable.ic_button_contacts ) );
        mDisplayAction.setTitle( getString( R.string.action_displayContact ) );
        mDisplayAction.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                mQuickAction.dismiss();
                if ( mSelectedContactFeast == null )
                {
                    Log.e( "no selected contact feast found " );
                }
                Uri displayContactUri = ContentUris.withAppendedId( ContactUtils.getContentUri(),
                                                                    mSelectedContactFeast.getContactId() );
                Intent intent = new Intent( Intent.ACTION_VIEW, displayContactUri );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );                
            }
        } );
        mAddToBlackListAction = new ActionItem( getResources().getDrawable( R.drawable.ic_menu_close_clear_cancel ) );
        mAddToBlackListAction.setTitle( getString( R.string.action_addToBlackList ) );
        mAddToBlackListAction.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                mQuickAction.dismiss();
                if ( mSelectedContactFeast == null )
                {
                    Log.e( "no selected contact feast found " );
                    return;
                }
                long contactId = mSelectedContactFeast.getContactId();
                String contactName = mSelectedContactFeast.getContactName();
                if ( !mDb.insertBlackList( contactId, contactName, null ) )
                {
                    Toast.makeText( NextEventsActivity.this, R.string.error_db, Toast.LENGTH_SHORT ).show();
                    return;
                }
                mDb.deleteNextEvent( contactId );
                fillList();
                Toast.makeText( NextEventsActivity.this, getString( R.string.toast_blacklisted, contactName ),
                                Toast.LENGTH_LONG ).show();                
            }
        } );

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
        mEventsPerDate = mDb.fetchTodayNextEvents();
        if ( mEventsPerDate == null )
        {
            new NextEventsAsyncTask( this, this ).execute();
        }
        else
        {
            displayEvents();
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        mSelectedContactFeast = (ContactFeast) mSectionedAdapter.getItem( position );
        mQuickAction = new QuickAction( v );

        mQuickAction.addActionItem( mDisplayAction );
        mQuickAction.addActionItem( mAddToBlackListAction );

        mQuickAction.show();

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
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

    /**
     * @see com.kamosoft.happycontacts.events.NextEventsHandler#finishRetrieveNextEvents(java.util.LinkedHashMap)
     */
    public void finishRetrieveNextEvents( LinkedHashMap<String, ContactFeasts> results )
    {
        mEventsPerDate = results;
        displayEvents();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, REFRESH_MENU_ID, 0, R.string.refresh ).setIcon( R.drawable.ic_menu_refresh );
        return true;
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case REFRESH_MENU_ID:
                new NextEventsAsyncTask( this, this ).execute();
                return true;

        }
        return super.onMenuItemSelected( featureId, item );
    }
}
