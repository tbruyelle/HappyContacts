package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * Display date list for a name
 * @author Tom
 *
 * @since 30 déc. 2009
 */
public class DateListActivity
    extends ListActivity
    implements Constants
{
    private DbAdapter mDb;

    private SimpleCursorAdapter mCursorAdapter;

    private Cursor mCursorDaysForName;

    private String mName;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.testapp );
        mDb = new DbAdapter( this );

        mName = getIntent().getExtras().getString( NAME_INTENT_KEY );

        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onResume" );
        }
        super.onResume();
        mDb.open( true );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onStop" );
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onRestart" );
        }
    }

    /**
     * 
     */
    private void fillList()
    {
        setTitle( getString( R.string.date_list_title, mName ) );
        mCursorDaysForName = mDb.fetchDayForName( mName );
        startManagingCursor( mCursorDaysForName );

        if ( mCursorAdapter == null )
        {
            String[] from = new String[] { HappyContactsDb.Feast.DAY };
            int[] to = new int[] { R.id.element };
            mCursorAdapter = new SimpleCursorAdapter( this, R.layout.testapp_element, mCursorDaysForName, from, to );
            setListAdapter( mCursorAdapter );
        }
        else
        {
            mCursorAdapter.changeCursor( mCursorDaysForName );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mCursorDaysForName.moveToPosition( position );
        Intent intent = new Intent( this, NameListActivity.class );
        intent.putExtra( DATE_INTENT_KEY,
                         mCursorDaysForName.getString( mCursorDaysForName.getColumnIndex( HappyContactsDb.Feast.DAY ) ) );
        startActivity( intent );
    }

}
