/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.gdata;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.auth.oauth.OAuthCallbackUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
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
import com.google.api.data.picasa.v2.PicasaWebAlbums;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.facebook.SocialUserArrayAdapter;

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

    private static boolean isTemporary;

    private static OAuthCredentialsResponse credentials;

    private static final int MENU_ADD = 0;

    private static final int MENU_ACCOUNTS = 1;

    private static final int REQUEST_AUTHENTICATE = 0;

    private static final int REQUEST_ADD_ACCOUNT = 1;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        transport.setVersionHeader( GoogleContacts.VERSION );
        AtomParser atomParser = new AtomParser();
        atomParser.namespaceDictionary = GoogleContactsAtom.NAMESPACE_DICTIONARY;
        transport.addParser( atomParser );
        transport.applicationName = APP_NAME;
        setContentView( R.layout.gcontactlist );

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

    @Override
    protected Dialog onCreateDialog( int id )
    {
        switch ( id )
        {
            case DIALOG_ACCOUNTS:
                switch ( AUTH_TYPE )
                {
                    //                    case ACCOUNT_MANAGER:
                    //                        AlertDialog.Builder builder = new AlertDialog.Builder( this );
                    //                        builder.setTitle( "Select a Google account" );
                    //                        final AccountManager manager = AccountManager.get( this );
                    //                        final Account[] accounts = manager.getAccountsByType( "com.google" );
                    //                        final int size = accounts.length;
                    //                        String[] names = new String[size];
                    //                        for ( int i = 0; i < size; i++ )
                    //                        {
                    //                            names[i] = accounts[i].name;
                    //                        }
                    //                        // names[size] = "New Account";
                    //                        builder.setItems( names, new DialogInterface.OnClickListener()
                    //                        {
                    //                            @Override
                    //                            public void onClick( DialogInterface dialog, int which )
                    //                            {
                    //                                if ( which == size )
                    //                                {
                    //                                    addAccount( manager );
                    //                                }
                    //                                else
                    //                                {
                    //                                    gotAccount( manager, accounts[which] );
                    //                                }
                    //                            }
                    //                        } );
                    //                        return builder.create();
                    case CLIENT_LOGIN:
                        final Dialog clientLoginDialog = new Dialog( this );
                        clientLoginDialog.setContentView( R.layout.clientlogin );
                        clientLoginDialog.setTitle( "Sign in with your Google Account" );
                        Button signInButton = (Button) clientLoginDialog.findViewById( R.id.SignIn );
                        final EditText password = (EditText) clientLoginDialog.findViewById( R.id.Password );
                        password.setTransformationMethod( PasswordTransformationMethod.getInstance() );
                        signInButton.setOnClickListener( new View.OnClickListener()
                        {
                            @Override
                            public void onClick( View v )
                            {
                                clientLoginDialog.dismiss();
                                ClientLogin authenticator = new ClientLogin();
                                authenticator.authTokenType = PicasaWebAlbums.AUTH_TOKEN_TYPE;
                                EditText username = (EditText) clientLoginDialog.findViewById( R.id.Email );
                                authenticator.username = username.getText().toString();
                                authenticator.password = password.getText().toString();
                                try
                                {
                                    authenticator.authenticate().setAuthorizationHeader( transport );
                                    authenticated();
                                }
                                catch ( IOException e )
                                {
                                    handleException( e );
                                }
                            }
                        } );
                        return clientLoginDialog;
                }
        }
        return null;
    }

    private void gotAccount( boolean tokenExpired )
    {
        switch ( AUTH_TYPE )
        {
            //            case ACCOUNT_MANAGER:
            //                SharedPreferences settings = getSharedPreferences( APP_NAME, 0 );
            //                String accountName = settings.getString( "accountName", null );
            //                if ( accountName != null )
            //                {
            //                    AccountManager manager = AccountManager.get( this );
            //                    Account[] accounts = manager.getAccountsByType( "com.google" );
            //                    int size = accounts.length;
            //                    for ( int i = 0; i < size; i++ )
            //                    {
            //                        Account account = accounts[i];
            //                        if ( accountName.equals( account.name ) )
            //                        {
            //                            if ( tokenExpired )
            //                            {
            //                                manager.invalidateAuthToken( "com.google", this.authToken );
            //                            }
            //                            gotAccount( manager, account );
            //                            return;
            //                        }
            //                    }
            //                }
            //                break;
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

    //    private void addAccount( AccountManager manager )
    //    {
    //        // TODO: test!
    //        try
    //        {
    //            Bundle bundle =
    //                manager.addAccount( "google.com", PicasaWebAlbums.AUTH_TOKEN_TYPE, null, null, this, null, null ).getResult();
    //            if ( bundle.containsKey( AccountManager.KEY_INTENT ) )
    //            {
    //                Intent intent = bundle.getParcelable( AccountManager.KEY_INTENT );
    //                int flags = intent.getFlags();
    //                flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
    //                intent.setFlags( flags );
    //                startActivityForResult( intent, REQUEST_ADD_ACCOUNT );
    //            }
    //            else
    //            {
    //                addAccountResult( bundle );
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            handleException( e );
    //        }
    //    }

    private void addAccountResult( Bundle bundle )
    {
        // TODO: test!
        String authToken = null;// = bundle.getString( AccountManager.KEY_AUTHTOKEN );
        String accountName = null;// = bundle.getString( AccountManager.KEY_ACCOUNT_NAME );
        SharedPreferences settings = getSharedPreferences( APP_NAME, 0 );
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( "accountName", accountName );
        editor.commit();
        authenticatedClientLogin( authToken );
    }

    //    private void gotAccount( AccountManager manager, Account account )
    //    {
    //        SharedPreferences settings = getSharedPreferences( APP_NAME, 0 );
    //        SharedPreferences.Editor editor = settings.edit();
    //        editor.putString( "accountName", account.name );
    //        editor.commit();
    //        try
    //        {
    //            Bundle bundle =
    //                manager.getAuthToken( account, PicasaWebAlbums.AUTH_TOKEN_TYPE, true, null, null ).getResult();
    //            if ( bundle.containsKey( AccountManager.KEY_INTENT ) )
    //            {
    //                Intent intent = bundle.getParcelable( AccountManager.KEY_INTENT );
    //                int flags = intent.getFlags();
    //                flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
    //                intent.setFlags( flags );
    //                startActivityForResult( intent, REQUEST_AUTHENTICATE );
    //            }
    //            else if ( bundle.containsKey( AccountManager.KEY_AUTHTOKEN ) )
    //            {
    //                authenticatedClientLogin( bundle.getString( AccountManager.KEY_AUTHTOKEN ) );
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            handleException( e );
    //            return;
    //        }
    //    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        switch ( requestCode )
        {
            case REQUEST_AUTHENTICATE:
                if ( resultCode == RESULT_OK )
                {
                    gotAccount( false );
                }
                else
                {
                    showDialog( DIALOG_ACCOUNTS );
                }
                break;
            case REQUEST_ADD_ACCOUNT:
                // TODO: test!
                if ( resultCode == RESULT_OK )
                {
                    addAccountResult( data.getExtras() );
                }
                else
                {
                    showDialog( DIALOG_ACCOUNTS );
                }
        }
    }

    private void authenticatedClientLogin( String authToken )
    {
        this.authToken = authToken;
        transport.setClientLoginToken( authToken );
        authenticated();
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
        Intent intent = getIntent();
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
        ArrayList<String> names = new ArrayList<String>();

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
        setListAdapter( new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, handler.getGoogleContacts() ) );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        menu.add( 0, MENU_ADD, 0, "New album" );
        if ( AUTH_TYPE != AuthType.OAUTH )
        {
            menu.add( 0, MENU_ACCOUNTS, 0, "Switch Account" );
        }
        return true;
    }

    private void setLogging( boolean logging )
    {
        Logger.getLogger( "com.google.api.client" ).setLevel( logging ? Level.CONFIG : Level.OFF );
        SharedPreferences settings = getSharedPreferences( APP_NAME, 0 );
        boolean currentSetting = settings.getBoolean( "logging", false );
        if ( currentSetting != logging )
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean( "logging", logging );
            editor.commit();
        }
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
        SharedPreferences settings = getSharedPreferences( APP_NAME, 0 );
        if ( settings.getBoolean( "logging", false ) )
        {
            if ( e instanceof HttpResponseException )
            {
                try
                {
                    Log.e( ( (HttpResponseException) e ).response.parseAsString() );
                }
                catch ( IOException parseException )
                {
                    parseException.printStackTrace();
                }
            }
            Log.e( e.getMessage(), e );
        }
    }
}
