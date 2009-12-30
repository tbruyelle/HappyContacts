package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * Display name list for a date
 * @author tom
 *
 */
public class NameListActivity
    extends ListActivity
    implements Constants
{
    private static final int DAY_MENU_ID = Menu.FIRST;

    private static final int NAME_MENU_ID = DAY_MENU_ID + 1;

    private static final int TEST_MENU_ID = NAME_MENU_ID + 1;

    private DbAdapter mDb;

    private SimpleCursorAdapter mCursorAdapter;

    private Cursor mCursorNamesForDay;

    private String mDay;

    private String mDate;

    private int mYear;

    private int mMonthOfYear;

    private int mDayOfMonth;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.testapp );
        
        TextView emptyText = (TextView)findViewById( android.R.id.empty );
        emptyText.setText( getString( R.string.no_feast ) );
        
        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "NameListActivity: end onCreate" );
        }
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
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.MONTH, Integer.valueOf( mDay.substring( 3, 5 ) ) );
        calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( mDay.substring( 0, 2 ) ) );

        SimpleDateFormat fullDateFormat = new SimpleDateFormat( FULL_DATE_FORMAT );
        mDate = fullDateFormat.format( calendar.getTime() );
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );

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

    private void fillList()
    {
        setTitle( getString( R.string.name_list_title, mDay ) );
        mCursorNamesForDay = mDb.fetchNamesForDay( mDay );
        startManagingCursor( mCursorNamesForDay );

        if ( mCursorAdapter == null )
        {
            String[] from = new String[] { HappyContactsDb.Feast.NAME };
            int[] to = new int[] { R.id.element };
            mCursorAdapter = new SimpleCursorAdapter( this, R.layout.testapp_element, mCursorNamesForDay, from, to );
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
        intent.putExtra( NAME_INTENT_KEY,
                         mCursorNamesForDay.getString( mCursorNamesForDay.getColumnIndex( HappyContactsDb.Feast.NAME ) ) );
        startActivity( intent );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, DAY_MENU_ID, 0, R.string.enter_date ).setIcon( R.drawable.ic_menu_today );
        menu.add( 0, NAME_MENU_ID, 0, R.string.enter_name ).setIcon( android.R.drawable.ic_menu_edit );
        menu.add( 0, TEST_MENU_ID, 0, R.string.check_contacts ).setIcon( R.drawable.ic_menu_allfriends );
        return true;
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case DAY_MENU_ID:
                displayDateForm();
                return true;
            case NAME_MENU_ID:
                displayNameForm();
                return true;
            case TEST_MENU_ID:
                /*
                 * Look for names matching today date
                 */
                ContactFeasts contactFeastToday = DayMatcherService.testDayMatch( getApplicationContext(), mDay, mDate );

                if ( !contactFeastToday.getContactList().isEmpty() )
                {
                    Notifier.notifyEvent( getApplicationContext() );
                    StringBuilder sb = new StringBuilder();
                    if ( contactFeastToday.getContactList().size() > 1 )
                    {
                        sb.append( this.getString( R.string.toast_contact_list ) );
                    }
                    else
                    {
                        sb.append( this.getString( R.string.toast_contact_one ) );
                    }
                    for ( Map.Entry<Long, ContactFeast> mapEntry : contactFeastToday.getContactList().entrySet() )
                    {
                        sb.append( mapEntry.getValue().getContactName() );
                        sb.append( "\n" );
                    }
                    Toast.makeText( this, sb.toString(), Toast.LENGTH_LONG ).show();
                }
                else
                {
                    Toast.makeText( this, R.string.toast_no_contact, Toast.LENGTH_SHORT ).show();
                }
                return true;
        }

        return super.onMenuItemSelected( featureId, item );
    }

    /**
     * FIXME faire du showdialog
     * display 
     */
    private void displayNameForm()
    {
        new EnterNameDialog( this ).show();
    }

    /**
     * FIXME faire du showdialog
     * display a datepicker
     */
    private void displayDateForm()
    {
        new DatePickerDialog( this, new DatePickerDialog.OnDateSetListener()
        {
            public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
            {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                Calendar cal = Calendar.getInstance();
                cal.set( year, monthOfYear, dayOfMonth );
                SimpleDateFormat dateFormat = new SimpleDateFormat( DAY_FORMAT );
                mDay = dateFormat.format( cal.getTime() );
                fillList();
                NameListActivity.this.getListView().scrollTo( 0, 0 );
            }
        }, mYear, mMonthOfYear, mDayOfMonth ).show();
    }
}