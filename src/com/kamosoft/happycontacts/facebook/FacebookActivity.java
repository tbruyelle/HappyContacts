/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
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
    implements Constants
{
    private static final int ACTIVITY_LOGIN = 1;

    private String[] mProjection = new String[] { People._ID, People.NAME };

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
                sync();
                break;
        }
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
            ArrayList<SocialNetworkUser> userList = api.getUserInfo( api.getFriends() );
            if ( Log.DEBUG )
            {
                Log.v( "End getUserInfo" );
            }
            if ( userList != null && !userList.isEmpty() )
            {
                Cursor contacts =
                    AndroidUtils.avoidEmptyName( this.getContentResolver().query( People.CONTENT_URI, mProjection,
                                                                                  null, null, People.NAME + " ASC" ) );
                if ( Log.DEBUG )
                {
                    Log.v( "Start matching with contacts" );
                }
                for ( SocialNetworkUser user : userList )
                {
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

                SocialUserArrayAdapter adapter =
                    new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, userList );

                setListAdapter( adapter );
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
            Log.v( "onCreate FaceBookActivity start" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.facebook_sync );

        if ( isLoggedIn() )
        {
            if ( Log.DEBUG )
            {
                Log.v( "onCreate FaceBookActivity user logged" );
            }
            sync();
        }
        else
        {
            if ( Log.DEBUG )
            {
                Log.v( "onCreate FaceBookActivity start login" );
            }
            startActivityForResult( new Intent( this, FacebookLoginActivity.class ), ACTIVITY_LOGIN );
        }
        if ( Log.DEBUG )
        {
            Log.v( "onCreate FaceBookActivity end" );
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
}
