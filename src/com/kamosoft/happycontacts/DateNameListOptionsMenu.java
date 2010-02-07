/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.text.format.DateFormat;
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
    protected static final int BACK_MENU_ID = Menu.FIRST;

    protected static final int DAY_MENU_ID = BACK_MENU_ID + 1;

    protected static final int NAME_MENU_ID = DAY_MENU_ID + 1;

    private static final int NAME_FORM_DIALOG_ID = 1;

    private static final int DATE_FORM_DIALOG_ID = 2;

    protected int mYear;

    protected int mMonthOfYear;

    protected int mDayOfMonth;

    protected java.text.DateFormat mDateFormat;

    /**
     * formatted date according to used locale
     */
    protected String mDateTitle;

    protected abstract void fillList();

    protected void updateDate( String day )
    {
        Calendar calendar = Calendar.getInstance();
        if ( day != null )
        {
            calendar.set( Calendar.MONTH, Integer.valueOf( day.substring( 3, 5 ) ) - 1 );
            calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( day.substring( 0, 2 ) ) );
        }
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
        mDateFormat = DateFormat.getDateFormat( this );
        mDateTitle = mDateFormat.format( calendar.getTime() );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, BACK_MENU_ID, 0, R.string.back_to_main ).setIcon( R.drawable.ic_menu_home );
        menu.add( 0, NAME_MENU_ID, 0, R.string.enter_name ).setIcon( R.drawable.ic_menu_edit );
        menu.add( 0, DAY_MENU_ID, 0, R.string.enter_date ).setIcon( R.drawable.ic_menu_today );
        return true;
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case BACK_MENU_ID:
                Intent intent = new Intent( this, HappyContactsPreferences.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity( intent );
                break;

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
                DatePickerDialog datePickerDialog = new DatePickerDialog( this,
                                                                          new DatePickerDialog.OnDateSetListener()
                                                                          {
                                                                              public void onDateSet( DatePicker view,
                                                                                                     int year,
                                                                                                     int monthOfYear,
                                                                                                     int dayOfMonth )
                                                                              {
                                                                                  Calendar cal = Calendar.getInstance();
                                                                                  cal.set( year, monthOfYear,
                                                                                           dayOfMonth );
                                                                                  SimpleDateFormat dateFormat = new SimpleDateFormat(
                                                                                                                                      DAY_FORMAT );
                                                                                  String day = dateFormat.format( cal
                                                                                      .getTime() );
                                                                                  Intent intent = new Intent(
                                                                                                              DateNameListOptionsMenu.this,
                                                                                                              NameListActivity.class );
                                                                                  intent
                                                                                      .putExtra( DATE_INTENT_KEY, day );
                                                                                  startActivity( intent );
                                                                              }
                                                                          }, 0, 0, 0 );
                datePickerDialog.setTitle( R.string.enter_date );
                return datePickerDialog;
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
