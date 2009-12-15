package com.tsoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;
import com.tsoft.happycontacts.model.ContactFeast;
import com.tsoft.happycontacts.model.ContactFeasts;

/**
 * @author tom
 *
 */
public class TestAppActivity
    extends ListActivity
{
    private static final int DAY_MENU_ID = Menu.FIRST;

    private static final int TEST_MENU_ID = Menu.FIRST + 1;

    private DbAdapter mDb;

    private SimpleCursorAdapter simpleCursorAdapter;

    private Cursor mCursorNamesForDay;

    private String day;

    private String currentYear = String.valueOf( Calendar.getInstance().get( Calendar.YEAR ) );

    private int mYear;

    private int mMonthOfYear;

    private int mDayOfMonth;

    private final Calendar calendar = Calendar.getInstance();

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.testapp );
        mDb = new DbAdapter( this );
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: start onResume" );
        }
        super.onResume();
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );

        mDb.open( true );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: end onStop" );
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if ( Log.DEBUG )
        {
            Log.v( "TestAppActivity: start onRestart" );
        }
    }

    private void fillList()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM" );
        Date date = new Date();
        day = dateFormat.format( date );
        setTitle( getApplicationContext().getString( R.string.test_app_title, day ) );
        mCursorNamesForDay = mDb.fetchNamesForDay( day );
        startManagingCursor( mCursorNamesForDay );

        startManagingCursor( mCursorNamesForDay );
        String[] from = new String[] { HappyContactsDb.Feast.NAME };
        int[] to = new int[] { android.R.id.text1 };
        simpleCursorAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursorNamesForDay, from, to );
        setListAdapter( simpleCursorAdapter );
    }

    //  @Override
    //  protected void onListItemClick(ListView l, View v, int position, long id)
    //  {
    //    super.onListItemClick(l, v, position, id);
    //    Long contactId = c.getLong(c.getColumnIndexOrThrow(People._ID));
    //    if (mDb.isBlackListed(contactId))
    //    {
    //      Toast.makeText(this, R.string.toast_contact_blacklisted, Toast.LENGTH_SHORT).show();
    //      return;
    //    }
    //    String contactName = c.getString(c.getColumnIndexOrThrow(People.NAME));
    //    notifyEvent(contactId, contactName);
    //    // new ReminderDialog(this, contactId, contactName).show();
    //  }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, DAY_MENU_ID, 0, R.string.enter_date ).setIcon( R.drawable.ic_menu_today );
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
            case TEST_MENU_ID:
                /*
                 * Look for names matching today date
                 */
                ContactFeasts contactFeastToday =
                    DayMatcherService.testDayMatch( getApplicationContext(), day, currentYear );

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
     * 
     */
    private void displayDateForm()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog( this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
            {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                Calendar cal = Calendar.getInstance();
                cal.set( year, monthOfYear, dayOfMonth );
                SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM" );
                day = dateFormat.format( cal.getTime() );
                mCursorNamesForDay = mDb.fetchNamesForDay( day );
                startManagingCursor( mCursorNamesForDay );
                simpleCursorAdapter.changeCursor( mCursorNamesForDay );

                setTitle( getApplicationContext().getString( R.string.test_app_title, day ) );
            }
        }, mYear, mMonthOfYear, mDayOfMonth );
        datePickerDialog.show();
    }
}