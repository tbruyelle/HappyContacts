/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.birthday;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;

/**
 * @since 28 avr. 2010
 * @version $Id$
 */
public class PickBirthdayActivity
    extends Activity
    implements Constants, OnClickListener
{
    private DbAdapter mDb;

    private String mContactName;

    private Long mContactId;

    private boolean mUpdate;

    private DatePicker mDatePicker;

    private CheckBox mIgnoreBirthyear;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickBirthdayActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.pickbirthday );

        mDb = new DbAdapter( this );
        mDb.open( false );

        mContactId = getIntent().getExtras().getLong( CONTACTID_INTENT_KEY );
        mContactName = getIntent().getExtras().getString( CONTACTNAME_INTENT_KEY );

        /* is it an update or an insert ? */
        String[] dayYear = mDb.getBirthday( mContactId );
        mUpdate = dayYear != null;

        mDatePicker = (DatePicker) findViewById( R.id.birthday_picker );
        TextView textView = (TextView) findViewById( R.id.pick_birthday_label );

        mIgnoreBirthyear = (CheckBox) findViewById( R.id.ignore_birthyear );

        if ( mUpdate )
        {
            /* contact has already a birthday, this is an update */
            String birthdayDate = dayYear[0];
            String birthdayYear = dayYear[1];
            mIgnoreBirthyear.setChecked( birthdayYear == null );
            textView.setText( getString( R.string.pick_birthday_update_label, mContactName ) );
            int year =
                birthdayYear == null ? Calendar.getInstance().get( Calendar.YEAR ) : Integer.parseInt( birthdayYear );
            int month = Integer.parseInt( birthdayDate.subSequence( 3, 5 ).toString() );
            int day = Integer.parseInt( birthdayDate.subSequence( 0, 2 ).toString() );
            mDatePicker.updateDate( year, month - 1, day );
        }
        else
        {
            /* contact has no birthday, this is an insert */
            textView.setText( getString( R.string.pick_birthday_label, mContactName ) );
        }

        Button button = (Button) findViewById( R.id.ok );
        button.setOnClickListener( this );

        if ( Log.DEBUG )
        {
            Log.v( "PickBirthdayActivity: start onCreate" );
        }
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick( View view )
    {
        mDatePicker.clearFocus();
        String month = String.valueOf( mDatePicker.getMonth() + 1 );
        if ( month.length() == 1 )
        {
            month = "0" + month;
        }
        String day = String.valueOf( mDatePicker.getDayOfMonth() );
        if ( day.length() == 1 )
        {
            day = "0" + day;
        }
        String birthday = day + "/" + month;
        String birthyear = mIgnoreBirthyear.isChecked() ? null : String.valueOf( mDatePicker.getYear() );
        if ( mUpdate )
        {
            if ( !mDb.updateBirthday( mContactId, mContactName, birthday, birthyear ) )
            {
                String error = "Error while updating birthday for " + mContactName + ", " + birthday + ", " + birthyear;
                Log.e( error );
                Toast.makeText( this, error, Toast.LENGTH_SHORT ).show();
                finishError();
            }
            else
            {
                Toast.makeText( this, R.string.birthday_updated, Toast.LENGTH_SHORT ).show();
                finishSuccess( mContactId );
            }
        }
        else
        {
            if ( mDb.insertBirthday( mContactId, mContactName, birthday, birthyear ) < 0 )
            {
                String error =
                    "Error while inserting birthday for " + mContactName + ", " + birthday + ", " + birthyear;
                Log.e( error );
                Toast.makeText( this, error, Toast.LENGTH_SHORT ).show();
                finishError();
            }
            else
            {
                Toast.makeText( this, R.string.birthday_added, Toast.LENGTH_SHORT ).show();
                finishSuccess( mContactId );
            }
        }
    }

    private void finishSuccess( long contactId )
    {
        Intent intent = new Intent( this, BirthdayActivity.class );
        intent.putExtra( CONTACTID_INTENT_KEY, contactId );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }

    private void finishError()
    {
        Intent intent = new Intent( this, BirthdayActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickBirthdayActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "PickBirthdayActivity: end onStop" );
        }
    }
}
