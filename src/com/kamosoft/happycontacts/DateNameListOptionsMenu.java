/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 8 janv. 2010
 * @version $Id$
 */
public abstract class DateNameListOptionsMenu
    extends ListActivity
    implements Constants
{
    protected static final int DAY_MENU_ID = Menu.FIRST;

    protected static final int NAME_MENU_ID = DAY_MENU_ID + 1;

    protected static final int TEST_MENU_ID = NAME_MENU_ID + 1;

    private static final int NAME_FORM_DIALOG_ID = 1;

    private static final int DATE_FORM_DIALOG_ID = 2;

    protected int mYear;

    protected int mMonthOfYear;

    protected int mDayOfMonth;

    protected String mDay;

    protected abstract void fillList();

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, NAME_MENU_ID, 0, R.string.enter_name ).setIcon( android.R.drawable.ic_menu_edit );
        return true;
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case DAY_MENU_ID:
                showDialog( DATE_FORM_DIALOG_ID );
                return true;

            case NAME_MENU_ID:
                showDialog( NAME_FORM_DIALOG_ID );
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
            case NAME_FORM_DIALOG_ID:
                return new EnterNameDialog( this );

            case DATE_FORM_DIALOG_ID:
                return new DatePickerDialog( this, new DatePickerDialog.OnDateSetListener()
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
                    }
                }, 0, 0, 0 );
        }
        return null;
    }

    /**
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog( int id, Dialog dialog )
    {
        super.onPrepareDialog( id, dialog );
        switch ( id )
        {
            case DATE_FORM_DIALOG_ID:
                DatePickerDialog datePickerDialog = (DatePickerDialog) dialog;
                datePickerDialog.updateDate( mYear, mMonthOfYear, mDayOfMonth );
                break;
        }
    }
}
