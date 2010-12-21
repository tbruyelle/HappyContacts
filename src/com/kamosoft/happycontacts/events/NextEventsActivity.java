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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.HappyContactsPreferences;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;
import com.kamosoft.happycontacts.widget.HappyContactsWidget;
import com.kamosoft.utils.DateUtils;

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
                /* return to the nearest position */
                getListView().setSelection( info.position > 0 ? info.position - 1 : 0 );
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
                String when = entry.getKey();
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
                    mSectionedAdapter.addSection( DateUtils.getDateLabel( this, when ), eventArrayAdapter );
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

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if ( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
            && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
        {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown( keyCode, event );
    }

    @Override
    public void onBackPressed()
    {
        // This will be called either automatically for you on 2.0
        // or later, or by the code above on earlier versions of the
        // platform.
        HappyContactsPreferences.backToMain( this );
        return;
    }
}
