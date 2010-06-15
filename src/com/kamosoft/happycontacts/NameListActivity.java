/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * Display name list for a date
 * @author tom
 *
 */
public class NameListActivity
    extends DateNameListOptionsMenu
    implements OnClickListener
{
    private DbAdapter mDb;

    private SimpleCursorAdapter mCursorAdapter;

    private Cursor mCursorNamesForDay;

    private String mDay;

    private SimpleDateFormat df = new SimpleDateFormat( "dd/MM" );

    public void onClick( View v )
    {
        switch ( v.getId() )
        {
            case R.id.dateback:
                updateDate( mDay, -1 );
                fillList();
                break;

            case R.id.datenext:
                updateDate( mDay, 1 );
                fillList();
                break;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.namelist );

        Button dateBackButton = (Button) findViewById( R.id.dateback );
        dateBackButton.setOnClickListener( this );
        Button dateNextButton = (Button) findViewById( R.id.datenext );
        dateNextButton.setOnClickListener( this );

        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onCreate" );
        }
    }

    protected void updateDate( String day, int nb )
    {
        Calendar calendar = Calendar.getInstance();
        if ( day != null )
        {
            calendar.set( Calendar.MONTH, Integer.valueOf( day.substring( 3, 5 ) ) - 1 );
            calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( day.substring( 0, 2 ) ) );
            if ( nb != 0 )
            {
                calendar.add( Calendar.DAY_OF_MONTH, nb );
                mDay = df.format( calendar.getTime() );
            }
        }
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
        mDateFormat = DateFormat.getDateFormat( this );
        mDateTitle = mDateFormat.format( calendar.getTime() );
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onResume" );
        }
        super.onResume();

        mDay = getIntent().getExtras().getString( DATE_INTENT_KEY );
        updateDate( mDay );

        mDb.open( true );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onStop" );
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onRestart" );
        }
    }

    protected void fillList()
    {
        setTitle( getString( R.string.name_list_title, mDateTitle ) );
        mCursorNamesForDay = mDb.fetchNamesForDay( mDay );
        startManagingCursor( mCursorNamesForDay );

        if ( mCursorAdapter == null )
        {
            String[] from = new String[] { HappyContactsDb.Feast.NAME };
            int[] to = new int[] { R.id.element };
            mCursorAdapter = new SimpleCursorAdapter( this, R.layout.datename_element, mCursorNamesForDay, from, to );
            setListAdapter( mCursorAdapter );
        }
        else
        {
            mCursorAdapter.changeCursor( mCursorNamesForDay );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mCursorNamesForDay.moveToPosition( position );
        Intent intent = new Intent( this, DateListActivity.class );
        intent.putExtra( NAME_INTENT_KEY, mCursorNamesForDay.getString( mCursorNamesForDay
            .getColumnIndex( HappyContactsDb.Feast.NAME ) ) );
        startActivity( intent );
    }

}