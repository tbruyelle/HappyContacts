/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;
import com.kamosoft.happycontacts.facebook.FacebookActivity;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 23 avr. 2010
 * @version $Id$
 */
public class BirthdayActivity
    extends ListActivity
    implements OnClickListener, android.content.DialogInterface.OnClickListener
{
    private DbAdapter mDb;

    private Cursor mCursor;

    private SimpleCursorAdapter mCursorAdapter;

    private Long mBirthdayId;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.birthday_list );

        Button addBirthday = (Button) findViewById( R.id.add_birthday );
        addBirthday.setOnClickListener( this );
        Button syncFacebook = (Button) findViewById( R.id.sync_facebook );
        syncFacebook.setOnClickListener( this );

        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: start onResume" );
        }
        super.onResume();

        mDb.open( false );

        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onResume" );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mBirthdayId = mCursor.getLong( mCursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.ID ) );
        String name = mCursor.getString( mCursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_NAME ) );
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.confirm_delete_birthday, name ) ).setCancelable( false ).setPositiveButton(
                                                                                                                            R.string.ok,
                                                                                                                            this ).setNegativeButton(
                                                                                                                                                      R.string.cancel,
                                                                                                                                                      this );
        builder.create().show();
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    @Override
    public void onClick( DialogInterface dialog, int which )
    {
        switch ( which )
        {
            case DialogInterface.BUTTON_POSITIVE:
                mDb.deleteBlackList( mBirthdayId );
                fillList();
                return;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
        }
    }

    /**
     * 
     */
    private void fillList()
    {
        mCursor = mDb.fetchAllBirthdays();
        if ( Log.DEBUG )
        {
            Log.d( "BirthdayActivity: retrieveing " + mCursor.getCount() + " birthdays from db" );
        }
        startManagingCursor( mCursor );
        if ( mCursorAdapter == null )
        {
            String[] from =
                new String[] { HappyContactsDb.Birthday.CONTACT_NAME, HappyContactsDb.Birthday.BIRTHDAY_DATE,
                    HappyContactsDb.Birthday.BIRTHDAY_YEAR };
            int[] to = { R.id.birthday_contact_name, R.id.birthday_birthday_date, R.id.birthday_birthday_year };
            mCursorAdapter = new SimpleCursorAdapter( this, R.layout.birthday_element, mCursor, from, to );
            setListAdapter( mCursorAdapter );
        }
        else
        {
            mCursorAdapter.changeCursor( mCursor );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onStop" );
        }
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick( View view )
    {
        switch ( view.getId() )
        {
            case R.id.add_birthday:
                /* TODO */
                return;
            case R.id.sync_facebook:
                startActivity( new Intent( this, FacebookActivity.class ) );
                return;
        }
    }

}
