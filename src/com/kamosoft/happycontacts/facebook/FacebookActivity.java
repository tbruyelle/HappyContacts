/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.birthday.BirthdayActivity;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 26 mars 2010
 * @version $Id$
 */
public class FacebookActivity
    extends ListActivity
    implements Constants, OnClickListener
{
    private static final int ACTIVITY_LOGIN = 1;

    private DbAdapter mDb;

    private SocialUserArrayAdapter mArrayAdapter;

    private ArrayList<SocialNetworkUser> mUserList;

    private Button mStoreButton;

    private ProgressDialog mProgressDialog;

    /**
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );

        if ( resultCode != Activity.RESULT_OK )
        {
            return;
        }

        switch ( requestCode )
        {
            case ACTIVITY_LOGIN:
                fillList();
                break;
        }
    }

    /**
     * 
     */
    private void fillList()
    {
        mUserList = mDb.fetchAllSyncResults();

        mArrayAdapter = new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, mUserList );
        setListAdapter( mArrayAdapter );
    }

    public ProgressDialog getProgressDialog()
    {
        return this.mProgressDialog;
    }

    public void finishSync( List<SocialNetworkUser> users )
    {
        if ( users == null || users.isEmpty() )
        {
            Toast.makeText( this, "No Facebook friends found !", Toast.LENGTH_SHORT );
        }
        else
        {
            /* record results in database */            
            mDb.insertSyncResults( users );
            mProgressDialog.dismiss();
            fillList();
        }
    }

    /**
     * 
     */
    private void sync()
    {
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: Start sync" );
        }
        mProgressDialog = ProgressDialog.show( this, "", this.getString( R.string.loading_friends ), true );
        new FacebookContactSync( this ).execute();
    }

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: onCreate()  start" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.facebook_sync );

        Button syncButton = (Button) findViewById( R.id.start_sync );
        syncButton.setOnClickListener( this );

        mStoreButton = (Button) findViewById( R.id.store_birthdays );
        mStoreButton.setOnClickListener( this );

        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: onCreate() end" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: start onResume" );
        }
        super.onResume();

        mDb.open( false );

        if ( isLoggedIn() )
        {
            if ( Log.DEBUG )
            {
                Log.v( "FaceBookActivity: onCreate user logged" );
            }
            fillList();
        }
        else
        {
            if ( Log.DEBUG )
            {
                Log.v( "FaceBookActivity: onCreate start login" );
            }
            startActivityForResult( new Intent( this, FacebookLoginActivity.class ), ACTIVITY_LOGIN );
        }
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onStop" );
        }
    }

    /**
     * @return
     */
    private boolean isLoggedIn()
    {
        SharedPreferences settings = this.getSharedPreferences( APP_NAME, 0 );
        String session_key = settings.getString( "session_key", null );
        String secret = settings.getString( "secret", null );
        String uid = settings.getString( "uid", null );

        return session_key != null && secret != null && uid != null;
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick( View view )
    {
        switch ( view.getId() )
        {
            case R.id.start_sync:
                sync();
                return;
            case R.id.store_birthdays:
                store();
                return;
        }
    }

    /**
     * 
     */
    private void store()
    {
        if ( mUserList != null && !mUserList.isEmpty() )
        {
            for ( SocialNetworkUser user : mUserList )
            {
                if ( user.birthday == null || user.getContactName() == null )
                {
                    /* we don't store if no birthday */
                    continue;
                }
                /* facebook birthday date has format MMMM dd, yyyy or MMMM dd */
                String birthday = null, birthyear = null;
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
                        Log.e( "unable to parse date " + user.birthday );
                        continue;
                    }
                }
                if ( !mDb.insertBirthday( user.getContactId(), user.getContactName(), birthday, birthyear ) )
                {
                    Log.e( "Error while inserting birthday " + user.toString() );
                }
            }
            Intent intent = new Intent( this, BirthdayActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivity( intent );
        }
        else
        {
            Toast.makeText( this, R.string.no_birthday, Toast.LENGTH_SHORT ).show();
        }
    }
}
