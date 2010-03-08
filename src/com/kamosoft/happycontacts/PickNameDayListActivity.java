/**
 * Copyright (C) 2010 Kamosoft
 */
package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * created 8 mars 2010
 */
public class PickNameDayListActivity
    extends ListActivity
    implements Constants
{
    private DbAdapter mDb;

    private String mContactName;

    private Long mContactId;

    private Cursor mCursor;

    private AutoCompleteTextView mAutoCompleteTextView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.namedaylist );

        mAutoCompleteTextView = (AutoCompleteTextView) findViewById( R.id.autocomplete );
        mAutoCompleteTextView.setThreshold( 1 );
        mAutoCompleteTextView.setCompletionHint( getString( R.string.pick_nameday, mContactName ) );

        mContactId = getIntent().getExtras().getLong( CONTACTID_INTENT_KEY );
        mContactName = getIntent().getExtras().getString( CONTACTID_INTENT_KEY );
        mDb = new DbAdapter( this );
        mDb.open( true );

        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: start onCreate" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "PickNameDayListActivity: end onStop" );
        }
    }

    private void fillList()
    {
        mCursor = mDb.fetchAllNameDay();

        String[] from = { HappyContactsDb.Feast.NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter simpleAdapter = new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1,
                                                                     mCursor, from, to );
        setListAdapter( simpleAdapter );
        SimpleCursorAdapter dropDowCursorAdapter = new SimpleCursorAdapter(
                                                                            this,
                                                                            android.R.layout.simple_dropdown_item_1line,
                                                                            mCursor, from, to );
        mAutoCompleteTextView.setAdapter( dropDowCursorAdapter );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        // TODO Auto-generated method stub
        super.onListItemClick( l, v, position, id );
    }

}
