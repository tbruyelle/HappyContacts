/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.model.SocialNetworkUser;
import com.nloko.simplyfacebook.net.FacebookRestClient;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 26 mars 2010
 * @version $Id$
 */
public class FacebookActivity
    extends Activity
    implements Constants
{
    private static final int ACTIVITY_LOGIN = 1;

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
        FacebookRestClient client =
            new FacebookRestClient( FACEBOOK_API_KEY, this.getSharedPreferences( APP_NAME, 0 ).getString( "uid",
                                                                                                             null ),
                                    this.getSharedPreferences( APP_NAME, 0 ).getString( "session_key", null ),
                                    this.getSharedPreferences( APP_NAME, 0 ).getString( "secret", null ) );

        FacebookApi api = new FacebookApi( client );
        try
        {
            List<SocialNetworkUser> userList = api.getUserInfo( api.getFriends() );
            if ( userList != null && !userList.isEmpty() )
            {
                SocialNetworkUser fbUser = userList.get( 0 );
                TextView text = (TextView) findViewById( R.id.facebook_sync );
                text.setText( "user0 : " + fbUser.uid + ", " + fbUser.firstName + ", " + fbUser.lastName + ", "
                    + fbUser.name + ", " + fbUser.birthday );
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

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.facebook_sync );
        startActivityForResult( new Intent( this, FacebookLoginActivity.class ), ACTIVITY_LOGIN );
    }
}
