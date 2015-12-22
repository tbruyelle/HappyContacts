/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.gdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.os.AsyncTask;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
import com.kamosoft.happycontacts.contacts.PhoneContact;
import com.kamosoft.happycontacts.model.SocialNetworkUser;
import com.kamosoft.utils.AndroidUtils;

/**
 * @since 14 juin 2010
 * @version $Id$
 */
public class GoogleContactSync
    extends AsyncTask<Void, String, List<SocialNetworkUser>>
    implements Constants
{
    private GoogleContactsActivity mContext;

    private GoogleTransport mGoogleTransport;

    public GoogleContactSync( GoogleContactsActivity context, GoogleTransport googleTransport )
    {
        mGoogleTransport = googleTransport;
        mContext = context;
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected List<SocialNetworkUser> doInBackground( Void... params )
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactSync: Start getContacts" );
        }
        HttpRequest request = mGoogleTransport.buildGetRequest();
        request.setUrl( "https://www.google.com/m8/feeds/contacts/default/full" );
        GoogleContactsHandler handler = new GoogleContactsHandler();
        try
        {
            /* loop on results */
            while ( true )
            {
                HttpResponse response = request.execute();

                SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                SAXParser parser = saxFactory.newSAXParser();

                parser.parse( response.getContent(), handler );

                if ( handler.getNextResultUrl() == null )
                {
                    break;
                }
                request.setUrl( handler.getNextResultUrl() );
            }
        }
        catch ( IOException e )
        {
            Log.e( "IOException", e );
            return null;
        }
        catch ( SAXException e )
        {
            Log.e( "SaxException", e );
            return null;
        }
        catch ( ParserConfigurationException e )
        {
            Log.e( "ParserConfigurationException", e );
            return null;
        }
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactSync: Stop getContacts" );
        }
        if ( handler.getGoogleContacts().size() > 0 )
        {
            syncWithPhoneContacts( handler.getGoogleContacts() );
        }
        return handler.getGoogleContacts();
    }

    /**
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute( List<SocialNetworkUser> result )
    {
        if ( Log.DEBUG )
        {
            Log.d( "GoogleContactSync onPostExecute" );
        }
        mContext.finishSync( result );
    }

    /**
     * 
     */
    private void syncWithPhoneContacts( ArrayList<SocialNetworkUser> users )
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactSync: Start syncContact" );
        }
        ArrayList<PhoneContact> phoneContacts = ContactUtils.loadPhoneContacts( mContext );

        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactsActivity: Start matching with contacts" );
        }
        for ( SocialNetworkUser user : users )
        {
            if ( user == null )
            {
                if ( Log.DEBUG )
                {
                    Log.d( "GoogleContactsActivity: skipping google contact null" );
                }
                continue;
            }
            if ( Log.DEBUG )
            {
                Log.d( "GoogleContactsActivity: searching contact for user " + user.name );
            }
            publishProgress( mContext.getString( R.string.sync_friends, user.name ) );

            for ( PhoneContact phoneContact : phoneContacts )
            {
                if ( phoneContact == null || phoneContact.name == null || phoneContact.name.length() == 0 )
                {
                    if ( Log.DEBUG )
                    {
                        Log.d( "GoogleContactsActivity: skipping phone contact null" );
                    }
                    continue;
                }
                String phoneContactName = AndroidUtils.replaceAccents( phoneContact.name ).toUpperCase();
                String userName = AndroidUtils.replaceAccents( user.name ).toUpperCase();
                if ( phoneContactName.equals( userName ) )
                {
                    if ( Log.DEBUG )
                    {
                        Log.d( "GoogleContactsActivity: *** " + phoneContact.name + " match with " + user.name + " ***" );
                    }
                    /* user google trouve dans les contacts */
                    user.setContactId( phoneContact.id );
                    user.setContactName( phoneContact.name );
                    break;
                }
            }
            if ( user.getContactId() == null )
            {
                /* contact not found */
                Log.e( user.toString() + " not found!" );
            }
        }
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactSync: stop syncContact" );
        }
    }

    /**
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate( String... values )
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactSync: " + values[0] );
        }
        mContext.getProgressDialog().setMessage( values[0] );
    }

}
