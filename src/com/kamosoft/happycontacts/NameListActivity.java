/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * Display name list for a date
 * @author tom
 *
 */
public class NameListActivity
    extends DateNameListOptionsMenu
    implements View.OnClickListener, DialogInterface.OnClickListener
{
    private static final int UPDATE_CONTEXT_MENU = 1;

    private static final int CLONE_CONTEXT_MENU = 2;

    private static final int DELETE_CONTEXT_MENU = 3;

    private static final int ADD_MENU_ID = NAME_MENU_ID + 1;

    private DbAdapter mDb;

    private SimpleCursorAdapter mCursorAdapter;

    private Cursor mCursorNamesForDay;

    private String mDay;

    private SimpleDateFormat df = new SimpleDateFormat( "dd/MM" );

    private Long mNameDayId;

    public void onClick( View v )
    {
        switch ( v.getId() )
        {
            case R.id.dateback:
                updateDate( mDay, -1 );
                fillList();
                break;

            case R.id.datenext:
                updateDate( mDay, 1 );
                fillList();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, ADD_MENU_ID, 0, R.string.add ).setIcon( R.drawable.ic_menu_add );
        return true;
    }

    /**
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case ADD_MENU_ID:
                new UpdateNameDayDialog( this, null, null, mDay, mDb, false ).show();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.namelist );

        Button dateBackButton = (Button) findViewById( R.id.dateback );
        dateBackButton.setOnClickListener( this );
        Button dateNextButton = (Button) findViewById( R.id.datenext );
        dateNextButton.setOnClickListener( this );

        mDb = new DbAdapter( this );
        registerForContextMenu( getListView() );
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onCreate" );
        }
    }

    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
    {
        super.onCreateContextMenu( menu, v, menuInfo );
        menu.add( 0, UPDATE_CONTEXT_MENU, 0, getString( R.string.fix_nameday ) );
        menu.add( 0, CLONE_CONTEXT_MENU, 0, getString( R.string.clone_nameday ) );
        menu.add( 0, DELETE_CONTEXT_MENU, 0, getString( R.string.delete_nameday ) );
    }

    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected( MenuItem item )
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch ( item.getItemId() )
        {
            case UPDATE_CONTEXT_MENU:
                mCursorNamesForDay.moveToPosition( info.position );
                Long id = mCursorNamesForDay.getLong( mCursorNamesForDay
                    .getColumnIndexOrThrow( HappyContactsDb.Feast.ID ) );
                String nameDay = mCursorNamesForDay.getString( mCursorNamesForDay
                    .getColumnIndexOrThrow( HappyContactsDb.Feast.NAME ) );
                new UpdateNameDayDialog( this, id, nameDay, mDay, mDb, true ).show();
                return true;

            case CLONE_CONTEXT_MENU:
                mCursorNamesForDay.moveToPosition( info.position );
                id = mCursorNamesForDay.getLong( mCursorNamesForDay.getColumnIndexOrThrow( HappyContactsDb.Feast.ID ) );
                nameDay = mCursorNamesForDay.getString( mCursorNamesForDay
                    .getColumnIndexOrThrow( HappyContactsDb.Feast.NAME ) );
                new UpdateNameDayDialog( this, id, nameDay, mDay, mDb, false ).show();
                return true;

            case DELETE_CONTEXT_MENU:

                mCursorNamesForDay.moveToPosition( info.position );
                mNameDayId = mCursorNamesForDay.getLong( mCursorNamesForDay
                    .getColumnIndexOrThrow( HappyContactsDb.Feast.ID ) );
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setMessage( R.string.confirm_delete ).setCancelable( false )
                    .setPositiveButton( R.string.ok, this ).setNegativeButton( R.string.cancel, this );
                builder.create().show();

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
            case DialogInterface.BUTTON_POSITIVE:
                if ( mNameDayId != null && mDb.deleteNameDay( mNameDayId ) )
                {
                    Toast.makeText( this, R.string.success_delete_nameday, Toast.LENGTH_SHORT ).show();
                    fillList();
                }
                else
                {
                    Log.e( "Unable to delete nameday " + mNameDayId );
                    Toast.makeText( this, R.string.error_delete_nameday, Toast.LENGTH_SHORT ).show();
                }
                return;

            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
        }
    }

    protected void updateDate( String day, int nb )
    {
        Calendar calendar = Calendar.getInstance();
        if ( day != null )
        {
            calendar.set( Calendar.MONTH, Integer.valueOf( day.substring( 3, 5 ) ) - 1 );
            calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( day.substring( 0, 2 ) ) );
            if ( nb != 0 )
            {
                calendar.add( Calendar.DAY_OF_MONTH, nb );
                mDay = df.format( calendar.getTime() );
            }
        }
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
        mDateFormat = DateFormat.getDateFormat( this );
        mDateTitle = mDateFormat.format( calendar.getTime() );
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onResume" );
        }
        super.onResume();

        mDay = getIntent().getExtras().getString( DATE_INTENT_KEY );
        updateDate( mDay );

        mDb.open( true );
        fillList();

        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onStop" );
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onRestart" );
        }
    }

    public void fillList()
    {
        setTitle( getString( R.string.name_list_title, mDateTitle ) );
        mCursorNamesForDay = mDb.fetchNamesForDay( mDay );
        startManagingCursor( mCursorNamesForDay );

        if ( mCursorAdapter == null )
        {
            String[] from = new String[] { HappyContactsDb.Feast.NAME };
            int[] to = new int[] { R.id.element };
            mCursorAdapter = new SimpleCursorAdapter( this, R.layout.datename_element, mCursorNamesForDay, from, to );
            setListAdapter( mCursorAdapter );
        }
        else
        {
            mCursorAdapter.changeCursor( mCursorNamesForDay );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mCursorNamesForDay.moveToPosition( position );
        Intent intent = new Intent( this, DateListActivity.class );
        intent
            .putExtra( NAME_INTENT_KEY,
                       mCursorNamesForDay.getString( mCursorNamesForDay.getColumnIndex( HappyContactsDb.Feast.NAME ) ) );
        startActivity( intent );
    }

}