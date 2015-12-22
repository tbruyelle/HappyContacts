/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
import com.kamosoft.happycontacts.contacts.PhoneContact;
import com.kamosoft.happycontacts.model.SocialNetworkUser;
import com.kamosoft.utils.AndroidUtils;

/**
 * @since 3 mai 2010
 * @version $Id$
 */
public class FacebookContactSync
    extends AsyncTask<Void, String, List<SocialNetworkUser>>
    implements Constants
{
    private FacebookActivity mFacebookActivity;

    public FacebookContactSync( FacebookActivity context )
    {
        mFacebookActivity = context;
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected List<SocialNetworkUser> doInBackground( Void... voids )
    {
        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: Start doInBackground" );
        }
        ArrayList<SocialNetworkUser> users = new ArrayList<SocialNetworkUser>();
        try
        {
            Bundle parameters = new Bundle();
            parameters.putString( "fields", "id,name,birthday" );

            String response = mFacebookActivity.getFacebook().request( "me/friends", parameters );

            Log.d( "Response: " + response.toString() );
            JSONObject json = Util.parseJson( response );

            JSONArray data = json.getJSONArray( "data" );

            for ( int i = 0, size = data.length(); i < size; i++ )
            {
                JSONObject friend = data.getJSONObject( i );
                SocialNetworkUser user = new SocialNetworkUser();
                user.uid = friend.getString( "id" );
                user.name = friend.getString( "name" );
                if ( friend.has( "birthday" ) )
                {
                    user.birthday = friend.getString( "birthday" );
                }
                users.add( user );
            }

            if ( Log.DEBUG )
            {
                Log.v( "FacebookContactSync: End getUserInfo" );
            }
        }
        catch ( JSONException e )
        {
            Log.e( e.getMessage(), e );
        }
        catch ( FacebookError e )
        {
            Log.e( e.getMessage(), e );
        }
        catch ( MalformedURLException e )
        {
            Log.e( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            Log.e( e.getMessage(), e );
        }

        if ( users == null || users.isEmpty() )
        {
            Log.e( "FacebookContactSync: no facebook friends found" );
            return users;
        }

        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: Start loading contacts phones" );
        }
        ArrayList<PhoneContact> phoneContacts = ContactUtils.loadPhoneContacts( mFacebookActivity );
        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: end loading contacts phones" );
        }

        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: Start matching with contacts" );
        }
        for ( SocialNetworkUser user : users )
        {
            if ( user == null )
            {
                if ( Log.DEBUG )
                {
                    Log.d( "FacebookContactSync: skipping user null" );
                }
                continue;
            }
            if ( user.birthday == null || user.name == null )
            {
                if ( Log.DEBUG )
                {
                    Log.d( "FacebookContactSync: skipping user " + user.name + " no birthday provided" );
                }
                continue;
            }
            if ( Log.DEBUG )
            {
                Log.d( "FacebookContactSync: searching contact for user " + user.name );
            }
            publishProgress( mFacebookActivity.getString( R.string.sync_friends, user.name ) );

            String friendName = AndroidUtils.replaceAccents( user.name );
            for ( PhoneContact phoneContact : phoneContacts )
            {
                if ( phoneContact == null || phoneContact.name == null || phoneContact.name.length() == 0 )
                {
                    continue;
                }
                if ( nameMatch( AndroidUtils.replaceAccents( phoneContact.name ), friendName ) )
                {
                    if ( Log.DEBUG )
                    {
                        Log.d( "FacebookContactSync: *** " + phoneContact.name + " match with " + user.name + " ***" );
                    }
                    /* user FB trouv� dans les contacts */
                    user.setContactId( phoneContact.id );
                    user.setContactName( phoneContact.name );
                    break;
                }
            }
        }
        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: Stop matching with contacts" );
        }
        publishProgress( mFacebookActivity.getString( R.string.inserting_results ) );
        return users;
    }

    /**
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute( List<SocialNetworkUser> result )
    {
        mFacebookActivity.finishSync( result );
    }

    /**
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate( String... values )
    {
        if ( Log.DEBUG )
        {
            Log.v( "FacebookContactSync: " + values[0] );
        }
        mFacebookActivity.getProgressDialog().setMessage( values[0] );
    }

    private boolean nameMatch( String contactName, String userName )
    {
        if ( contactName == null || contactName.length() == 0 || userName == null || userName.length() == 0 )
        {
            return false;
        }
        String[] contactSubNames = contactName.split( " " );
        String[] userSubNames = userName.split( " " );
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
}
