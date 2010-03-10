/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * List of contacts linked with a nameday (usefull for contact with nickname)
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 9 mars 2010
 * @version $Id$
 */
public class WhiteListActivity
    extends ListActivity
    implements OnClickListener, DialogInterface.OnClickListener
{
    private static final int DELETEALL_MENU_ID = Menu.FIRST;

    private static final int DELETEALL_DIALOG_ID = 1;

    private DbAdapter mDb;

    private Cursor mCursor;

    private Long mWhiteListId;

    private String mContactName;

    private String mNameDay;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "WhitekListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );

        setContentView( R.layout.whitelist );
        mDb = new DbAdapter( this );
        Button addButton = (Button) findViewById( R.id.add_whitelist );
        addButton.setOnClickListener( this );
        if ( Log.DEBUG )
        {
            Log.v( "WhitekListActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "WhitekListActivity: start onResume" );
        }
        super.onResume();
        mDb.open( false );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "WhiteListActivity: end onResume" );
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

    private void fillList()
    {
        mCursor = mDb.fetchAllWhiteList();

        startManagingCursor( mCursor );
        String[] from = new String[] { HappyContactsDb.WhiteList.CONTACT_NAME, HappyContactsDb.WhiteList.NAME_DAY };
        int[] to = new int[] { R.id.contact_name, R.id.nameday };
        setListAdapter( new SimpleCursorAdapter( this, R.layout.whitelist_element, mCursor, from, to ) );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mWhiteListId = mCursor.getLong( mCursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.ID ) );
        mContactName = mCursor.getString( mCursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.CONTACT_NAME ) );
        mNameDay = mCursor.getString( mCursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.NAME_DAY ) );

        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.confirm_delete_whitelist, mContactName, mNameDay ) ).setCancelable(
                                                                                                                    false ).setPositiveButton(
                                                                                                                                               R.string.ok,
                                                                                                                                               this ).setNegativeButton(
                                                                                                                                                                         R.string.cancel,
                                                                                                                                                                         this );
        builder.create().show();
    }

    @Override
    public void onClick( View v )
    {
        startActivity( new Intent( this, PickContactsListActivity.class ) );

        //            /* hide the keyboard */
        //            InputMethodManager inputManager = (InputMethodManager) this.getSystemService( Context.INPUT_METHOD_SERVICE );
        //            inputManager.hideSoftInputFromWindow( this.getCurrentFocus().getWindowToken(),
        //                                                  InputMethodManager.HIDE_NOT_ALWAYS );

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
                mDb.deleteWhiteList( mWhiteListId );
                fillList();
                return;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_NEUTRAL:
                mDb.deleteAllWhiteList();
                fillList();
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, DELETEALL_MENU_ID, 0, R.string.deleteall ).setIcon( R.drawable.ic_menu_delete );
        return true;
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case DELETEALL_MENU_ID:
                showDialog( DELETEALL_DIALOG_ID );
                return true;

        }
        return super.onMenuItemSelected( featureId, item );
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
                builder.setMessage( R.string.confirm_deleteall ).setCancelable( false ).setNeutralButton( R.string.ok,
                                                                                                          this ).setNegativeButton(
                                                                                                                                    R.string.cancel,
                                                                                                                                    this );
                return builder.create();
        }
        return null;
    }
}
