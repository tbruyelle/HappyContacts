/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.gdata;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.auth.oauth.OAuthCallbackUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.xml.atom.AtomParser;
import com.google.api.data.contacts.v3.GoogleContacts;
import com.google.api.data.contacts.v3.atom.GoogleContactsAtom;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.contacts.ContactUtils;
import com.kamosoft.happycontacts.contacts.PhoneContact;
import com.kamosoft.happycontacts.facebook.SocialUserArrayAdapter;
import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 25 mai 2010
 * @version $Id$
 */
public class GoogleContactsActivity
    extends ListActivity
    implements Constants
{
    enum AuthType
    {
        OAUTH, ACCOUNT_MANAGER, CLIENT_LOGIN
    }

    private static AuthType AUTH_TYPE = AuthType.OAUTH;

    private static final int DIALOG_ACCOUNTS = 0;

    private GoogleTransport transport = new GoogleTransport();

    private String authToken;

    private String postLink;

    private SocialUserArrayAdapter mArrayAdapter;

    private static boolean isTemporary;

    private static OAuthCredentialsResponse credentials;

    private static final int MENU_ADD = 0;

    private static final int MENU_ACCOUNTS = 1;

    private TextView mSyncCounter;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.gcontactlist );
        mSyncCounter = (TextView) findViewById( R.id.sync_google_counter );

        transport.setVersionHeader( GoogleContacts.VERSION );
        AtomParser atomParser = new AtomParser();
        atomParser.namespaceDictionary = GoogleContactsAtom.NAMESPACE_DICTIONARY;
        transport.addParser( atomParser );
        transport.applicationName = APP_NAME;
        HttpTransport.setLowLevelHttpTransport( ApacheHttpTransport.INSTANCE );
        Intent intent = getIntent();
        if ( Intent.ACTION_SEND.equals( intent.getAction() ) )
        {
            sendData = new SendData( intent, getContentResolver() );
        }
        else if ( Intent.ACTION_MAIN.equals( getIntent().getAction() ) )
        {
            sendData = null;
        }
        gotAccount( false );
    }

    private static OAuthHmacSigner createOAuthSigner()
    {
        OAuthHmacSigner result = new OAuthHmacSigner();
        if ( credentials != null )
        {
            result.tokenSharedSecret = credentials.tokenSecret;
        }
        result.clientSharedSecret = "anonymous";
        return result;
    }

    private static OAuthParameters createOAuthParameters()
    {
        OAuthParameters authorizer = new OAuthParameters();
        authorizer.consumerKey = "anonymous";
        authorizer.signer = createOAuthSigner();
        authorizer.token = credentials.token;
        return authorizer;
    }

    private void gotAccount( boolean tokenExpired )
    {
        switch ( AUTH_TYPE )
        {
            case OAUTH:
                try
                {
                    boolean isViewAction = Intent.ACTION_VIEW.equals( getIntent().getAction() );
                    if ( tokenExpired && !isTemporary && credentials != null )
                    {
                        GoogleOAuthGetAccessToken.revokeAccessToken( createOAuthParameters() );
                        credentials = null;
                    }
                    if ( tokenExpired || !isViewAction && ( isTemporary || credentials == null ) )
                    {
                        GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
                        temporaryToken.signer = createOAuthSigner();
                        temporaryToken.consumerKey = "anonymous";
                        temporaryToken.scope = GoogleContacts.ROOT_URL;
                        temporaryToken.displayName = "HappyContacts";
                        temporaryToken.callback = "happycontacts:///";
                        isTemporary = true;
                        credentials = temporaryToken.execute();
                        GoogleOAuthAuthorizeTemporaryTokenUrl authorizeUrl =
                            new GoogleOAuthAuthorizeTemporaryTokenUrl();
                        authorizeUrl.temporaryToken = credentials.token;
                        Intent webIntent = new Intent( Intent.ACTION_VIEW );
                        webIntent.setData( Uri.parse( authorizeUrl.build() ) );
                        startActivity( webIntent );
                    }
                    else
                    {
                        if ( isViewAction )
                        {
                            Uri uri = this.getIntent().getData();
                            OAuthCallbackUrl callbackUrl = new OAuthCallbackUrl( uri.toString() );
                            GoogleOAuthGetAccessToken accessToken = new GoogleOAuthGetAccessToken();
                            accessToken.temporaryToken = callbackUrl.token;
                            accessToken.verifier = callbackUrl.verifier;
                            accessToken.signer = createOAuthSigner();
                            accessToken.consumerKey = "anonymous";
                            isTemporary = false;
                            credentials = accessToken.execute();
                            createOAuthParameters().signRequestsUsingAuthorizationHeader( transport );
                        }
                        authenticated();
                    }
                }
                catch ( IOException e )
                {
                    handleException( e );
                }
                return;
        }
        showDialog( DIALOG_ACCOUNTS );
    }

    static class SendData
    {
        String fileName;

        Uri uri;

        String contentType;

        long contentLength;

        SendData( Intent intent, ContentResolver contentResolver )
        {
            Bundle extras = intent.getExtras();
            if ( extras.containsKey( Intent.EXTRA_STREAM ) )
            {
                Uri uri = this.uri = (Uri) extras.getParcelable( Intent.EXTRA_STREAM );
                String scheme = uri.getScheme();
                if ( scheme.equals( "content" ) )
                {
                    Cursor cursor = contentResolver.query( uri, null, null, null, null );
                    cursor.moveToFirst();
                    this.fileName = cursor.getString( cursor.getColumnIndexOrThrow( Images.Media.DISPLAY_NAME ) );
                    this.contentType = intent.getType();
                    this.contentLength = cursor.getLong( cursor.getColumnIndexOrThrow( Images.Media.SIZE ) );
                }
            }
        }
    }

    static SendData sendData;

    private void authenticated()
    {
        if ( sendData != null )
        {
            try
            {
                if ( sendData.fileName != null )
                {
                    boolean success = false;
                    try
                    {
                        HttpRequest request = transport.buildPostRequest();
                        request.url = new GenericUrl( GoogleContacts.ROOT_URL );
                        GoogleHeaders.setSlug( request.headers, sendData.fileName );
                        InputStreamContent content = new InputStreamContent();
                        content.inputStream = getContentResolver().openInputStream( sendData.uri );
                        content.type = sendData.contentType;
                        content.length = sendData.contentLength;
                        request.content = content;
                        request.execute().ignore();
                        success = true;
                    }
                    catch ( IOException e )
                    {
                        handleException( e );
                    }
                    setListAdapter( new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1,
                                                              new String[] { success ? "OK" : "ERROR" } ) );
                }
            }
            finally
            {
                sendData = null;
            }
        }
        else
        {
            retrieveContacts();
        }
    }

    private void retrieveContacts()
    {
        HttpRequest request = transport.buildGetRequest();
        //TODO mettre une variable et escapé le @       
        request.setUrl( "https://www.google.com/m8/feeds/contacts/thomas.bruyelle%40gmail.com/full" );
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
            handleException( e );
        }
        catch ( SAXException e )
        {
            Log.e( "SaxException", e );
        }
        catch ( ParserConfigurationException e )
        {
            Log.e( "ParserConfigurationException", e );
        }
        if ( handler.getGoogleContacts().size() > 0 )
        {
            syncWithPhoneContacts( handler.getGoogleContacts() );
            mSyncCounter.setText( String.valueOf( handler.getGoogleContacts().size() ) );
        }

        mArrayAdapter = new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, handler.getGoogleContacts() );
        setListAdapter( mArrayAdapter );
    }

    /**
     * 
     */
    private void syncWithPhoneContacts( ArrayList<SocialNetworkUser> users )
    {
        ArrayList<PhoneContact> phoneContacts = ContactUtils.loadPhoneContacts( this );

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
            //TODO publishProgress( mContext.getString( R.string.sync_friends, user.name ) );

            //String friendName = AndroidUtils.replaceAccents( user.name );
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
                if ( phoneContact.name.equals( user.name ) )
                {
                    if ( Log.DEBUG )
                    {
                        Log.d( "GoogleContactsActivity: *** " + phoneContact.name + " match with " + user.name + " ***" );
                    }
                    /* user google trouvé dans les contacts */
                    user.setContactId( phoneContact.id );
                    user.setContactName( phoneContact.name );
                    break;
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        /* todo mettre l'enregistrement */
        menu.add( 0, MENU_ADD, 0, "New album" );
        if ( AUTH_TYPE != AuthType.OAUTH )
        {
            menu.add( 0, MENU_ACCOUNTS, 0, "Switch Account" );
        }
        return true;
    }

    private void handleException( Exception e )
    {
        e.printStackTrace();
        if ( e instanceof HttpResponseException )
        {
            int statusCode = ( (HttpResponseException) e ).response.statusCode;
            if ( statusCode == 401 || statusCode == 403 )
            {
                gotAccount( true );
            }
            return;
        }
    }
}
