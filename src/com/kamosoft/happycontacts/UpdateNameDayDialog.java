/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kamosoft.happycontacts.dao.DbAdapter;

/**
 *
 * @since 28 avr. 2010
 * @version $Id$
 */
public class UpdateNameDayDialog
    extends Dialog
    implements Constants
{
    private DbAdapter mDb;

    private Long mNameDayId;

    private DatePicker mDatePicker;

    private EditText mNameDayEditText;

    private NameListActivity mNameListActivity;

    private boolean mUpdate;

    public UpdateNameDayDialog( NameListActivity activity, Long nameDayId, String nameDay, String date, DbAdapter db,
                                boolean update )
    {
        super( activity );
        mUpdate = update;
        mNameListActivity = activity;
        mDb = db;
        mNameDayId = nameDayId;
        setContentView( R.layout.updatenamedaydialog );

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = getWindow().getAttributes().height;
        getWindow().setAttributes( lp );

        LinearLayout nameDayLayout = (LinearLayout) findViewById( R.id.nameday_layout );
        mNameDayEditText = (EditText) findViewById( R.id.nameday );
        if ( nameDay != null )
        {
            mNameDayEditText.setText( nameDay );
        }
        if ( mUpdate )
        {
            setTitle( activity.getString( R.string.updatenameday, nameDay ) );
            nameDayLayout.setVisibility( View.GONE );
        }
        else
        {
            setTitle( R.string.addnameday );
            nameDayLayout.setVisibility( View.VISIBLE );
        }

        Button okButton = (Button) findViewById( R.id.ok_button );
        okButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                onSubmit( v );
            }
        } );
        Button cancelButton = (Button) findViewById( R.id.cancel_button );
        cancelButton.setOnClickListener( new View.OnClickListener()
        {

            @Override
            public void onClick( View v )
            {
                onCancel( v );
            }

        } );

        mDatePicker = (DatePicker) findViewById( R.id.date );
        if ( date != null )
        {
            int month = Integer.parseInt( date.subSequence( 3, 5 ).toString() );
            int day = Integer.parseInt( date.subSequence( 0, 2 ).toString() );
            mDatePicker.updateDate( mDatePicker.getYear(), month - 1, day );
        }
    }

    public void onSubmit( View view )
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
        String date = day + "/" + month;

        String name = mNameDayEditText.getText().toString();
        if ( name == null || name.length() == 0 )
        {
            Toast.makeText( mNameListActivity, R.string.empty_name, Toast.LENGTH_SHORT ).show();
            return;
        }

        if ( mUpdate )
        {
            if ( mDb.existsNameDay( name, date ) )
            {
                Toast.makeText( mNameListActivity, R.string.already_exists, Toast.LENGTH_SHORT ).show();
                return;
            }
            if ( mDb.updateNameDay( mNameDayId, date ) )
            {
                Log.i( name + " with id " + mNameDayId + " updated to date " + date );
                Toast.makeText( getContext(), R.string.success_update_nameday, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Log.e( "Unable to update " + name + " with id " + mNameDayId );
                Toast.makeText( getContext(), R.string.error_update_nameday, Toast.LENGTH_SHORT ).show();
                dismiss();
            }
        }
        else
        {
            if ( mDb.existsNameDay( name, date ) )
            {
                Toast.makeText( mNameListActivity, R.string.already_exists, Toast.LENGTH_SHORT ).show();
                return;
            }
            if ( mDb.insertNameDay( name, date ) )
            {
                Log.i( name + " added to date " + date );
                Toast.makeText( getContext(), R.string.success_add_nameday, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Log.e( "Unable to add " + name + " with date " + date );
                Toast.makeText( getContext(), R.string.error_add_nameday, Toast.LENGTH_SHORT ).show();
                dismiss();
            }
        }

        mNameListActivity.fillList();
        dismiss();
    }

    public void onCancel( View view )
    {
        dismiss();
    }
}
