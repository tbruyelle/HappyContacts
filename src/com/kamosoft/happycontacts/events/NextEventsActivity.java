/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
import com.kamosoft.happycontacts.widget.HappyContactsWidget;

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

    private static final int DISPLAY_CONTACT_CONTEXT_MENU = 1;

    private static final int BLACKLIST_CONTEXT_MENU = 2;

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
        registerForContextMenu( getListView() );
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

    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
    {
        super.onCreateContextMenu( menu, v, menuInfo );
        menu.add( 0, DISPLAY_CONTACT_CONTEXT_MENU, 0, getString( R.string.action_displayContact ) );
        menu.add( 0, BLACKLIST_CONTEXT_MENU, 0, getString( R.string.action_addToBlackList ) );
    }

    @Override
    public boolean onContextItemSelected( MenuItem item )
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ContactFeast selectedContactFeast = (ContactFeast) mSectionedAdapter.getItem( info.position );
        if ( selectedContactFeast == null )
        {
            Log.e( "no selected contact feast found " );
            return false;
        }
        switch ( item.getItemId() )
        {
            case DISPLAY_CONTACT_CONTEXT_MENU:
                Uri displayContactUri = ContentUris.withAppendedId( ContactUtils.getContentUri(),
                                                                    selectedContactFeast.getContactId() );
                Intent intent = new Intent( Intent.ACTION_VIEW, displayContactUri );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
                return true;

            case BLACKLIST_CONTEXT_MENU:
                long contactId = selectedContactFeast.getContactId();
                String contactName = selectedContactFeast.getContactName();
                if ( !mDb.insertBlackList( contactId, contactName, null ) )
                {
                    Toast.makeText( NextEventsActivity.this, R.string.error_db, Toast.LENGTH_SHORT ).show();
                    return false;
                }
                mDb.deleteNextEvent( contactId );
                fillList();
                Toast.makeText( NextEventsActivity.this, getString( R.string.toast_blacklisted, contactName ),
                                Toast.LENGTH_LONG ).show();

                return true;
            default:
                return super.onContextItemSelected( item );
        }
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

        /* update the widget */
        this.startService( new Intent( this, HappyContactsWidget.UpdateService.class ) );
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
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case REFRESH_MENU_ID:
                new NextEventsAsyncTask( this, this ).execute();
                return true;

        }
        return super.onOptionsItemSelected( item );
    }
}
