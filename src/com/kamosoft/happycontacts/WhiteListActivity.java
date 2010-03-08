package com.kamosoft.happycontacts;

import android.app.ListActivity;
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

public class WhiteListActivity
    extends ListActivity
    implements OnClickListener
{
    private DbAdapter mDb;

    private Cursor mCursor;

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
        String[] from = new String[] { HappyContactsDb.WhiteList.CONTACT_NAME };
        int[] to = new int[] { android.R.id.text1 };
        setListAdapter( new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to ) );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        Long whiteListId = mCursor.getLong( mCursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.ID ) );
        mDb.deleteWhiteList( whiteListId );
        fillList();
    }

    @Override
    public void onClick( View v )
    {
        startActivity( new Intent( this, PickContactsListActivity.class ) );
        //        EditText editText = (EditText) findViewById( R.id.name_whitelist );
        //        String contactName = editText.getText().toString();
        //        if ( Log.DEBUG )
        //        {
        //            Log.v( "WhitekListActivity: start onClick editText=" + contactName );
        //        }

        //        if ( contactName != null && contactName.length() > 0 )
        //        {
        //            /* insert the white list */
        //            mDb.insertWhiteList( contactName );
        //            /* hide the keyboard */
        //            InputMethodManager inputManager = (InputMethodManager) this.getSystemService( Context.INPUT_METHOD_SERVICE );
        //            inputManager.hideSoftInputFromWindow( this.getCurrentFocus().getWindowToken(),
        //                                                  InputMethodManager.HIDE_NOT_ALWAYS );
        //            /* refill the list */
        //            fillList();
        //            Toast.makeText( this, getString( R.string.whitelist_added, contactName ), Toast.LENGTH_SHORT ).show();
        //        }
        //        else
        //        {
        //            Toast.makeText( this, R.string.empty_name, Toast.LENGTH_SHORT ).show();
        //        }
        //        if ( Log.DEBUG )
        //        {
        //            Log.v( "WhitekListActivity: end onClick" );
        //        }
    }

}
