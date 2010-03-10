/**
 * Copyright (C) 2010 Kamosoft
 */
package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
    implements Constants, OnItemClickListener, TextWatcher
{
    private DbAdapter mDb;

    private String mContactName;

    private Long mContactId;

    private Cursor mCursor;

    private EditText mEditText;

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

        mEditText = (EditText) findViewById( R.id.autocomplete );
        mEditText.addTextChangedListener( this );

        mDb = new DbAdapter( this );
        mDb.open( false );

        fillList( null );
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

    private void fillList( String text )
    {
        if ( text == null || text.length() == 0 )
        {
            mCursor = mDb.fetchAllNameDay();
        }
        else
        {
            mCursor = mDb.fetchNameDayLike( text );
        }

        String[] from = { HappyContactsDb.Feast.NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter simpleAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to );
        setListAdapter( simpleAdapter );
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
        Intent intent = new Intent( this, WhiteListActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }

    /**
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick( AdapterView<?> adapterView, View view, int position, long id )
    {
        onNameDayClick( position );
    }

    /**
     * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
     */
    @Override
    public void afterTextChanged( Editable arg0 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
     */
    @Override
    public void beforeTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
     */
    @Override
    public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        String text = mEditText.getText().toString();
        fillList( text );
    }
}
