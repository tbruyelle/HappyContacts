/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.sync;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.DateFormatConstants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.birthday.BirthdayActivity;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 14 juin 2010
 * @version $Id$
 */
public class StoreAsyncTask
    extends AsyncTask<Void, Integer, Void>
    implements Constants, DateFormatConstants
{
    public static final SimpleDateFormat FB_birthdayFull = new SimpleDateFormat( "MM/dd/yyyy", Locale.ENGLISH );

    public static final SimpleDateFormat FB_birthdaySmall = new SimpleDateFormat( "MM/dd", Locale.ENGLISH );

    public static final SimpleDateFormat GoogleContact_birthdayFull = new SimpleDateFormat( "yyyy-MM-dd",
                                                                                            Locale.ENGLISH );

    public static final SimpleDateFormat GoogleContact_birthdaySmall = new SimpleDateFormat( "--MM-dd", Locale.ENGLISH );

    private int counter;

    private boolean update;

    private ArrayList<SocialNetworkUser> mUserList;

    private DbAdapter mDb;

    private Context mContext;

    private ProgressDialog mProgressDialog;

    private boolean mFromFacebook;

    public StoreAsyncTask( boolean update, Context context, ArrayList<SocialNetworkUser> users, DbAdapter db,
                           boolean fromFacebook, ProgressDialog progressDialog )
    {
        this.update = update;
        mContext = context;
        this.counter = 0;
        mUserList = users;
        mDb = db;
        mFromFacebook = fromFacebook;
        mProgressDialog = progressDialog;
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Void doInBackground( Void... voids )
    {
        for ( SocialNetworkUser user : mUserList )
        {
            if ( user.birthday == null || user.getContactName() == null )
            {
                /* we don't store if no birthday, not linked to a contact or has already a birthday*/
                continue;
            }
            boolean hasBirthday = mDb.hasBirthday( user.getContactId() );
            if ( !update && hasBirthday )
            {
                /* we don't store if update option is off and if contact has already a birthday */
                continue;
            }
            publishProgress( ++counter );
            String birthday = null, birthyear = null;
            if ( mFromFacebook )
            {
                /* facebook birthday date has format MM/dd/YYYY or MM/dd */
                try
                {
                    Date date = FB_birthdayFull.parse( user.birthday );
                    birthday = dayDateFormat.format( date );
                    birthyear = yearDateFormat.format( date );
                }
                catch ( ParseException e )
                {
                    try
                    {
                        Date date = FB_birthdaySmall.parse( user.birthday );
                        birthday = dayDateFormat.format( date );
                        birthyear = null;
                    }
                    catch ( ParseException e1 )
                    {
                        Log.e( "unable to parse date " + user.birthday + " for user " + user.toString() );
                        continue;
                    }
                }
            }
            else
            {
                /* format google YYYY-mm-dd */
                try
                {
                    Date date = GoogleContact_birthdayFull.parse( user.birthday );
                    birthday = dayDateFormat.format( date );
                    birthyear = yearDateFormat.format( date );
                }
                catch ( ParseException e )
                {
                    try
                    {
                        Date date = GoogleContact_birthdaySmall.parse( user.birthday );
                        birthday = dayDateFormat.format( date );
                        birthyear = null;
                    }
                    catch ( ParseException e1 )
                    {
                        Log.e( "unable to parse date " + user.birthday + " for user " + user.toString() );
                        continue;
                    }
                }
            }
            if ( hasBirthday && update )
            {
                if ( !mDb.updateBirthday( user.getContactId(), user.getContactName(), birthday, birthyear ) )
                {
                    Log.e( "Error while updating birthday " + user.toString() );
                }
            }
            else
            {
                if ( mDb.insertBirthday( user.getContactId(), user.getContactName(), birthday, birthyear ) == -1 )
                {
                    Log.e( "Error while inserting birthday " + user.toString() );
                }
            }
        }
        return null;
    }

    /**
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate( Integer... values )
    {
        mProgressDialog.setMessage( mContext.getString( R.string.inserting_birthdays, values[0] ) );
    }

    /**
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute( Void voids )
    {
        mProgressDialog.dismiss();
        Toast.makeText( mContext, mContext.getString( R.string.inserting_birthdays_done, counter ), Toast.LENGTH_SHORT )
            .show();
        Intent intent = new Intent( mContext, BirthdayActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        mContext.startActivity( intent );
    }
}
