/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.blacklist;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.utils.AndroidUtils;

/**
 * Allow user to select a contact in order to link it with a nameday (usefull for nickname)
 * TODO : element full click checks the checlkbox via onListItemClicl
 * TODO : keep trace of checked elements in a list
 * @author tom
 * created 11 janv. 2010
 */
public class PickContactsBlackListActivity
    extends ListActivity
    implements TextWatcher
{
    private Cursor mCursor;

    private EditText mEditText;

    private DbAdapter mDb;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickContactsListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.blacklist_contactlist );
        mEditText = (EditText) findViewById( R.id.autocomplete );
        mEditText.addTextChangedListener( this );

        mDb = new DbAdapter( this );
        mDb.open( true );

        fillList( null );

        if ( Log.DEBUG )
        {
            Log.v( "PickContactsListActivity: end onCreate" );
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if ( mDb != null )
        {
            mDb.cleanup();
        }
    }

    private void fillList( String text )
    {
        if ( text == null || text.length() == 0 )
        {
            mCursor = filterOnlyNotBlacklisted( ContactUtils.doQuery( this, ContactUtils.getNameColumn()
                + " is not null" ) );
        }
        else
        {
            mCursor = filterOnlyNotBlacklisted( ContactUtils.doQuery( this, ContactUtils.getNameColumn() + " like \""
                + text + "%\"" ) );
        }

        startManagingCursor( mCursor );
        String[] from = { ContactUtils.getNameColumn() };
        int[] to = { R.id.contact_name };
        SimpleCursorAdapter simpleAdapter = new SimpleCursorAdapter( this, R.layout.blacklist_contact_element, mCursor,
                                                                     from, to );
        setListAdapter( simpleAdapter );
    }

    private Cursor filterOnlyNotBlacklisted( Cursor contactCursor )
    {
        MatrixCursor matrixCursor = new MatrixCursor( contactCursor.getColumnNames() );
        int contactIdColumn = contactCursor.getColumnIndexOrThrow( ContactUtils.getIdColumn() );
        int contactNameColumn = contactCursor.getColumnIndexOrThrow( ContactUtils.getNameColumn() );
        while ( contactCursor.moveToNext() )
        {
            Long contactId = contactCursor.getLong( contactIdColumn );
            if ( mDb.isReallyBlackListed( contactId ) )
            {
                continue;
            }
            String contactName = contactCursor.getString( contactNameColumn );
            matrixCursor.newRow().add( contactId ).add( contactName );
        }
        contactCursor.close();
        return matrixCursor;
    }

    public void onClear( View view )
    {
        mEditText.getText().clear();
        AndroidUtils.hideKeyboard( this, mEditText );
    }

    public void onHide( View view )
    {
        AndroidUtils.hideKeyboard( this, mEditText );
    }

    public void onDone( View view )
    {
        ListView lv = getListView();
        int listItemCount = lv.getChildCount();
        DbAdapter db = new DbAdapter( this );
        db.open( false );
        int blackListed = 0;
        for ( int i = 0; i < listItemCount; i++ )
        {
            CheckBox cbox = (CheckBox) ( (View) lv.getChildAt( i ) ).findViewById( R.id.contact_checkbox );
            if ( cbox.isChecked() )
            {
                mCursor.moveToPosition( i );
                Long contactId = mCursor.getLong( mCursor.getColumnIndex( ContactUtils.getIdColumn() ) );
                String contactName = mCursor.getString( mCursor.getColumnIndex( ContactUtils.getNameColumn() ) );
                if ( !db.isReallyBlackListed( contactId ) )
                {
                    Log.i( "Blacklisting " + contactName );
                    db.insertBlackList( contactId, contactName, null );
                    blackListed++;
                }
            }
        }
        db.cleanup();
        if ( blackListed > 0 )
        {
            Toast
                .makeText( this, getString( R.string.blacklisted, Integer.valueOf( blackListed ) ), Toast.LENGTH_SHORT )
                .show();
        }
        setResult( Activity.RESULT_OK );
        finish();
    }

    /**
     * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
     */
    public void afterTextChanged( Editable arg0 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
     */
    public void beforeTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
     */
    public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        String text = mEditText.getText().toString();
        fillList( text );
    }

}
