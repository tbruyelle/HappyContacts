/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.blacklist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * 
 */
public class BlackListActivity
    extends ListActivity
    implements DialogInterface.OnClickListener
{
    private static final int DELETEALL_MENU_ID = Menu.FIRST;

    private static final int DELETEALL_DIALOG_ID = 1;

    private DbAdapter mDb;

    private Cursor mCursorBlakListed;

    private Long mBlackListId;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.blacklist );
        mDb = new DbAdapter( this );
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: start onResume" );
        }
        super.onResume();
        mDb.open( false );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: end onResume" );
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
        if ( Log.DEBUG )
        {
            /* display only in debug mode to avoid user to see them
             * we display only the black listed setted direclty by the user */
            mCursorBlakListed = mDb.fetchAllBlackList();
        }
        else
        {
            mCursorBlakListed = mDb.fetchAllTimeBlackListed();
        }       
        startManagingCursor( mCursorBlakListed );
        String[] from =
            new String[] { HappyContactsDb.BlackList.CONTACT_NAME, HappyContactsDb.BlackList.LAST_WISH_DATE };
        int[] to = { R.id.contact_name, R.id.last_wish_date };
        setListAdapter( new SimpleCursorAdapter( this, R.layout.blacklist_element, mCursorBlakListed, from, to ) );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mBlackListId =
            mCursorBlakListed.getLong( mCursorBlakListed.getColumnIndexOrThrow( HappyContactsDb.BlackList.ID ) );
        String name =
            mCursorBlakListed.getString( mCursorBlakListed.getColumnIndexOrThrow( HappyContactsDb.BlackList.CONTACT_NAME ) );
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( getString( R.string.confirm_delete_blacklist, name ) ).setCancelable( false ).setPositiveButton(
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
                mDb.deleteBlackList( mBlackListId );
                fillList();
                return;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_NEUTRAL:
                mDb.deleteAllBlackList();
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
