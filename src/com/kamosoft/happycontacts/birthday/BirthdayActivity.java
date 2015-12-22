/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.birthday;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.PickContactsListActivity;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;
import com.kamosoft.happycontacts.facebook.FacebookActivity;
import com.kamosoft.happycontacts.gdata.GoogleContactsActivity;

/**
 * @since 23 avr. 2010
 * @version $Id$
 */
public class BirthdayActivity
    extends ListActivity
    implements android.content.DialogInterface.OnClickListener, Constants
{
    private static final int ADD_BIRTHDAY_MENU_ID = Menu.FIRST;

    private static final int SYNC_FACEBOOK_MENU_ID = ADD_BIRTHDAY_MENU_ID + 1;

    private static final int SYNC_GOOGLE_CONTACTS_ID = SYNC_FACEBOOK_MENU_ID + 1;

    private static final int DELETEALL_MENU_ID = SYNC_GOOGLE_CONTACTS_ID + 1;

    private static final int DELETEALL_DIALOG_ID = 1;

    private static final int EDIT_CONTEXT_MENU_ID = 1;

    private static final int DELETE_CONTEXT_MENU_ID = 2;

    private DbAdapter mDb;

    private Cursor mCursor;

    private SimpleCursorAdapter mCursorAdapter;

    private TextView mBirthdayCounter;

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

        mBirthdayCounter = (TextView) findViewById( R.id.birthday_counter );

        mDb = new DbAdapter( this );

        registerForContextMenu( getListView() );

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
        /* check intent contactId to scroll to the right item after update or delete */
        if ( getIntent().hasExtra( CONTACTID_INTENT_KEY ) )
        {
            long id = getIntent().getLongExtra( CONTACTID_INTENT_KEY, 0 );
            int position = getPositionFromContactId( id );
            getListView().setSelection( position > 0 ? position - 1 : 0 );
            getIntent().removeExtra( CONTACTID_INTENT_KEY );
        }
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onResume" );
        }
    }

    private int getPositionFromContactId( long id )
    {
        mCursor.moveToFirst();
        do
        {
            Long cursorId = mCursor.getLong( mCursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_ID ) );
            if ( cursorId.longValue() == id )
            {
                return mCursor.getPosition();
            }
        }
        while ( mCursor.moveToNext() );
        return 0;
    }

    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
    {
        super.onCreateContextMenu( menu, v, menuInfo );
        menu.add( 0, EDIT_CONTEXT_MENU_ID, 0, getString( R.string.update ) );
        menu.add( 0, DELETE_CONTEXT_MENU_ID, 0, getString( R.string.delete ) );
    }

    @Override
    public boolean onContextItemSelected( MenuItem item )
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch ( item.getItemId() )
        {
            case EDIT_CONTEXT_MENU_ID:
                mCursor.moveToPosition( info.position );
                Long contactId = mCursor.getLong( mCursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_ID ) );
                String contactName =
                    mCursor.getString( mCursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_NAME ) );

                Intent intent = new Intent( this, PickBirthdayActivity.class );
                intent.putExtra( CONTACTID_INTENT_KEY, contactId );
                intent.putExtra( CONTACTNAME_INTENT_KEY, contactName );
                startActivity( intent );
                return true;

            case DELETE_CONTEXT_MENU_ID:
                if ( !mDb.deleteBirthday( info.id ) )
                {
                    Log.e( "Error while deleting item " + info.id );
                }
                else
                {
                    Toast.makeText( this, R.string.birthday_deleted, Toast.LENGTH_SHORT ).show();
                }
                fillList();
                return true;
            default:
                return super.onContextItemSelected( item );
        }
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    public void onClick( DialogInterface dialog, int which )
    {
        switch ( which )
        {
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_POSITIVE:
                mDb.deleteAllBirthday();
                fillList();
                return;
        }
    }

    /**
     * 
     */
    private void fillList()
    {
        mCursor = mDb.fetchAllBirthdays();
        startManagingCursor( mCursor );
        mBirthdayCounter.setText( String.valueOf( mCursor.getCount() ) );
        if ( Log.DEBUG )
        {
            Log.d( "BirthdayActivity: retrieveing " + mCursor.getCount() + " birthdays from db" );
        }
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

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, SYNC_GOOGLE_CONTACTS_ID, 0, R.string.sync_googlecontacts ).setIcon( R.drawable.googlecontacts );
        menu.add( 0, SYNC_FACEBOOK_MENU_ID, 0, R.string.sync_facebook ).setIcon( R.drawable.fb );        
        menu.add( 0, ADD_BIRTHDAY_MENU_ID, 0, R.string.add_birthday ).setIcon( R.drawable.ic_menu_add );
        menu.add( 0, DELETEALL_MENU_ID, 0, R.string.deleteall ).setIcon( R.drawable.ic_menu_delete );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case ADD_BIRTHDAY_MENU_ID:
                Intent intent = new Intent( this, PickContactsListActivity.class );
                intent.putExtra( NEXT_ACTIVITY_INTENT_KEY, PickBirthdayActivity.class );
                intent.putExtra( PICK_CONTACT_LABEL_INTENT_KEY, getString( R.string.pick_contact_birthday ) );
                startActivity( intent );
                return true;

            case SYNC_FACEBOOK_MENU_ID:
                startActivity( new Intent( this, FacebookActivity.class ) );
                return true;

            case SYNC_GOOGLE_CONTACTS_ID:
                startActivity( new Intent( this, GoogleContactsActivity.class ) );
                return true;

            case DELETEALL_MENU_ID:
                showDialog( DELETEALL_DIALOG_ID );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog( int id )
    {
        switch ( id )
        {
            case DELETEALL_DIALOG_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setMessage( R.string.confirm_deleteall ).setCancelable( false ).setPositiveButton( R.string.ok,
                                                                                                           this ).setNegativeButton(
                                                                                                                                     R.string.cancel,
                                                                                                                                     this );
                return builder.create();
        }
        return null;
    }
}
