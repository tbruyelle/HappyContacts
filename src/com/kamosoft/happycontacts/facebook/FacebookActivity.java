/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
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
import com.kamosoft.utils.AndroidUtils;
import com.nloko.simplyfacebook.net.FacebookRestClient;

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

    private String[] mProjection = new String[] { People._ID, People.NAME };

    private DbAdapter mDb;

    private SocialUserArrayAdapter mArrayAdapter;

    private ArrayList<SocialNetworkUser> mUserList;

    private Button mStoreButton;

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

    /**
     * 
     */
    private void sync()
    {
        if ( Log.DEBUG )
        {
            Log.v( "Start sync" );
        }
        FacebookRestClient client =
            new FacebookRestClient( FACEBOOK_API_KEY,
                                    this.getSharedPreferences( APP_NAME, 0 ).getString( "uid", null ),
                                    this.getSharedPreferences( APP_NAME, 0 ).getString( "session_key", null ),
                                    this.getSharedPreferences( APP_NAME, 0 ).getString( "secret", null ) );

        FacebookApi api = new FacebookApi( client );
        try
        {
            if ( Log.DEBUG )
            {
                Log.v( "Start getUserInfo" );
            }
            mUserList = api.getUserInfo( api.getFriends() );
            if ( Log.DEBUG )
            {
                Log.v( "End getUserInfo" );
            }
            if ( mUserList != null && !mUserList.isEmpty() )
            {
                Cursor contacts =
                    AndroidUtils.avoidEmptyName( this.getContentResolver().query( People.CONTENT_URI, mProjection,
                                                                                  null, null, People.NAME + " ASC" ) );
                if ( Log.DEBUG )
                {
                    Log.v( "Start matching with contacts" );
                }
                for ( SocialNetworkUser user : mUserList )
                {
                    if ( user.birthday == null )
                    {
                        if ( Log.DEBUG )
                        {
                            Log.d( "skpping user " + user.name + " no birthday provided" );
                        }
                        continue;
                    }
                    if ( Log.DEBUG )
                    {
                        Log.d( "searching contact for user " + user.name );
                    }
                    while ( contacts.moveToNext() )
                    {
                        String contactName = contacts.getString( contacts.getColumnIndexOrThrow( People.NAME ) );
                        if ( nameMatch( contactName, user.name ) )
                        {
                            if ( Log.DEBUG )
                            {
                                Log.d( "*** " + contactName + " match with " + user.name + " ***" );
                            }
                            /* user FB trouvé dans les contacts */
                            user.setContactId( contacts.getLong( contacts.getColumnIndexOrThrow( People._ID ) ) );
                            user.setContactName( contactName );
                            break;
                        }
                    }
                    contacts.moveToFirst();
                }
                if ( Log.DEBUG )
                {
                    Log.v( "Stop matching with contacts" );
                }
                /* record results in database */
                mDb.insertSyncResults( mUserList );

                fillList();
            }
            else
            {
                Toast.makeText( this, "No friends found !", Toast.LENGTH_SHORT ).show();
            }
        }
        catch ( Exception e )
        {
            Toast.makeText( this, e.getMessage(), Toast.LENGTH_LONG ).show();
        }
    }

    private boolean nameMatch( String contactName, String userName )
    {
        if ( contactName == null || contactName.length() == 0 || userName == null || userName.length() == 0 )
        {
            return false;
        }
        String[] contactSubNames = AndroidUtils.replaceAccents( contactName ).split( " " );
        String[] userSubNames = AndroidUtils.replaceAccents( userName ).split( " " );
        boolean subNameFound = false;
        for ( String userSubName : userSubNames )
        {
            for ( String contactSubName : contactSubNames )
            {
                if ( userSubName.equalsIgnoreCase( contactSubName ) )
                {
                    subNameFound = true;
                    break;
                }
            }
            if ( !subNameFound )
            {
                return false;
            }
            subNameFound = false;
        }
        return true;
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
