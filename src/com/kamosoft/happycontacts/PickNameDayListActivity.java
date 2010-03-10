/**
 * Copyright (C) 2010 Kamosoft
 */
package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * After select the contact in PickContactListActivity, the user has to select a nameday from this Activity
 * @author tom
 * created 8 mars 2010
 */
public class PickNameDayListActivity
    extends ListActivity
    implements Constants, OnItemClickListener
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

        mContactId = getIntent().getExtras().getLong( CONTACTID_INTENT_KEY );
        mContactName = getIntent().getExtras().getString( CONTACTNAME_INTENT_KEY );

        TextView pickNameDayTextView = (TextView) findViewById( R.id.pick_nameday );
        pickNameDayTextView.setText( getString( R.string.pick_nameday, mContactName ) );

        mAutoCompleteTextView = (AutoCompleteTextView) findViewById( R.id.autocomplete );
        mAutoCompleteTextView.setThreshold( 1 );
        mAutoCompleteTextView.setOnItemClickListener( this );

        mDb = new DbAdapter( this );
        mDb.open( false );

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
        SimpleCursorAdapter simpleAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to );
        setListAdapter( simpleAdapter );
        SimpleCursorAdapter dropDowCursorAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_dropdown_item_1line, mCursor, from, to );
        mAutoCompleteTextView.setAdapter( dropDowCursorAdapter );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        onNameDayClick( position );
    }

    private void onNameDayClick( int position )
    {
        mCursor.moveToPosition( position );
        String nameDay = mCursor.getString( mCursor.getColumnIndex( HappyContactsDb.Feast.NAME ) );
        mDb.insertWhiteList( mContactId, mContactName, nameDay );
        Toast.makeText( this, getString( R.string.whitelist_added, mContactName, nameDay ), Toast.LENGTH_SHORT ).show();
        startActivity( new Intent( this, WhiteListActivity.class ) );
    }

    /**
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick( AdapterView<?> adapterView, View view, int position, long id )
    {
        onNameDayClick( position );
    }
}
