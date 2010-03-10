/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.kamosoft.utils.AndroidUtils;

/**
 * Allow user to select a contact in order to link it with a nameday (usefull for nickname)
 * @author tom
 * created 11 janv. 2010
 */
public class PickContactsListActivity
    extends ListActivity
    implements Constants, OnItemClickListener, TextWatcher
{
    private Cursor mCursor;

    private EditText mEditText;

    private String[] mProjection = new String[] { People._ID, People.NAME };

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contactlist );
        mEditText = (EditText) findViewById( R.id.autocomplete );
        mEditText.addTextChangedListener( this );
        fillList( null );

        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: end onCreate" );
        }
    }

    private void fillList( String text )
    {
        if ( text == null || text.length() == 0 )
        {
            mCursor =
                AndroidUtils.avoidEmptyName( this.getContentResolver().query( People.CONTENT_URI, mProjection, null,
                                                                              null, People.NAME + " ASC" ) );
        }
        else
        {
            mCursor =
                getContentResolver().query( People.CONTENT_URI, mProjection, People.NAME + " like \"" + text + "%\"",
                                            null, People.NAME + " ASC" );
        }

        String[] from = { People.NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter simpleAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to );
        setListAdapter( simpleAdapter );     
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        onContactClick( position );
    }

    private void onContactClick( int position )
    {
        mCursor.moveToPosition( position );
        Intent intent = new Intent( this, PickNameDayListActivity.class );
        intent.putExtra( CONTACTID_INTENT_KEY, mCursor.getLong( mCursor.getColumnIndex( People._ID ) ) );
        intent.putExtra( CONTACTNAME_INTENT_KEY, mCursor.getString( mCursor.getColumnIndex( People.NAME ) ) );
        startActivity( intent );
    }

    /**
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick( AdapterView<?> adapterView, View view, int position, long id )
    {
        onContactClick( position );
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
