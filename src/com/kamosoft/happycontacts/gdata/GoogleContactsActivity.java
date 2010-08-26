/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.gdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.xml.atom.AtomParser;
import com.google.api.data.contacts.v3.GoogleContacts;
import com.google.api.data.contacts.v3.atom.GoogleContactsAtom;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.facebook.ConfirmStoreDialog;
import com.kamosoft.happycontacts.facebook.SocialUserArrayAdapter;
import com.kamosoft.happycontacts.model.SocialNetworkUser;
import com.kamosoft.happycontacts.sync.StoreAsyncTask;
import com.kamosoft.happycontacts.sync.SyncStorer;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 25 mai 2010
 * @version $Id$
 */
public class GoogleContactsActivity
    extends ListActivity
    implements Constants, android.content.DialogInterface.OnClickListener, SyncStorer
{
    private static final int START_SYNC_MENU_ID = Menu.FIRST;

    private static final int STORE_SYNC_MENU_ID = START_SYNC_MENU_ID + 1;

    private static final int DELETEALL_MENU_ID = STORE_SYNC_MENU_ID + 1;

    private static final int DELETEALL_DIALOG_ID = 1;

    private static final int CONFIRM_STORE_DIALOG_ID = 2;

    private ProgressDialog mProgressDialog;

    enum AuthType {
        OAUTH, ACCOUNT_MANAGER, CLIENT_LOGIN
    }

    private static AuthType AUTH_TYPE = AuthType.OAUTH;

    private GoogleTransport transport = new GoogleTransport();

    private SocialUserArrayAdapter mArrayAdapter;

    private static boolean isTemporary;

    private static OAuthCredentialsResponse credentials;

    private TextView mSyncCounter;

    private DbAdapter mDb;

    private ArrayList<SocialNetworkUser> mUserList;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactsActivity: onCreate()  start" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.gcontactlist );
        mSyncCounter = (TextView) findViewById( R.id.sync_google_counter );

        mDb = new DbAdapter( this );
        mDb.open( false );

        transport.setVersionHeader( GoogleContacts.VERSION );
        AtomParser atomParser = new AtomParser();
        atomParser.namespaceDictionary = GoogleContactsAtom.NAMESPACE_DICTIONARY;
        transport.addParser( atomParser );
        transport.applicationName = APP_NAME;
        HttpTransport.setLowLevelHttpTransport( ApacheHttpTransport.INSTANCE );
        Intent intent = getIntent();
        if ( Intent.ACTION_SEND.equals( intent.getAction() ) )
        {
            if ( Log.DEBUG )
            {
                Log.v( "Intent.ACTION_SEND.equals( intent.getAction() )" );
            }
            sendData = new SendData( intent, getContentResolver() );
        }
        else if ( Intent.ACTION_MAIN.equals( intent.getAction() ) )
        {
            if ( Log.DEBUG )
            {
                Log.v( "Intent.ACTION_MAIN.equals( intent.getAction() )" );
            }
            sendData = null;
        }
        gotAccount( false );
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactsActivity: onCreate() stop" );
        }
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

    /**
     * @return the mProgressDialog
     */
    public ProgressDialog getProgressDialog()
    {
        return mProgressDialog;
    }

    private void gotAccount( boolean tokenExpired )
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactsActivity:  gotAccount( " + tokenExpired + " ) start" );
        }
        switch ( AUTH_TYPE )
        {
            case OAUTH:
                try
                {
                    boolean isViewAction = Intent.ACTION_VIEW.equals( getIntent().getAction() );
                    if ( tokenExpired && !isTemporary && credentials != null )
                    {
                        if ( Log.DEBUG )
                        {
                            Log.v( "( tokenExpired && !isTemporary && credentials != null )" );
                        }
                        GoogleOAuthGetAccessToken.revokeAccessToken( createOAuthParameters() );
                        credentials = null;
                    }
                    if ( tokenExpired || !isViewAction && ( isTemporary || credentials == null ) )
                    {
                        if ( Log.DEBUG )
                        {
                            Log.v( "( tokenExpired || !isViewAction && ( isTemporary || credentials == null ) )" );
                        }
                        GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
                        temporaryToken.signer = createOAuthSigner();
                        temporaryToken.consumerKey = "anonymous";
                        temporaryToken.scope = GoogleContacts.ROOT_URL;
                        temporaryToken.displayName = "HappyContacts";
                        temporaryToken.callback = "happycontacts:///";
                        isTemporary = true;
                        credentials = temporaryToken.execute();
                        GoogleOAuthAuthorizeTemporaryTokenUrl authorizeUrl = new GoogleOAuthAuthorizeTemporaryTokenUrl();
                        authorizeUrl.temporaryToken = credentials.token;
                        Intent webIntent = new Intent( Intent.ACTION_VIEW );
                        webIntent.setData( Uri.parse( authorizeUrl.build() ) );
                        webIntent.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
                        startActivity( webIntent );
                    }
                    else
                    {
                        if ( isViewAction )
                        {
                            if ( Log.DEBUG )
                            {
                                Log.v( "isViewAction" );
                            }
                            Uri uri = this.getIntent().getData();
                            if ( Log.DEBUG )
                            {
                                Log.v( "intent uri=" + uri.toString() );
                            }
                            OAuthCallbackUrl callbackUrl = new OAuthCallbackUrl( uri.toString() );
                            GoogleOAuthGetAccessToken accessToken = new GoogleOAuthGetAccessToken();
                            accessToken.temporaryToken = callbackUrl.token;
                            accessToken.verifier = callbackUrl.verifier;
                            accessToken.signer = createOAuthSigner();
                            accessToken.consumerKey = "anonymous";
                            isTemporary = false;
                            if ( Log.DEBUG )
                            {
                                Log.v( "accessToken = " + accessToken.toString() );
                            }
                            credentials = accessToken.execute();
                            if ( Log.DEBUG )
                            {
                                Log.v( "credentials = " + credentials.toString() );
                            }
                            createOAuthParameters().signRequestsUsingAuthorizationHeader( transport );
                        }
                        authenticated();
                    }
                }
                catch ( IOException e )
                {
                    Log.e( "IOException during gotAccount " + e.getMessage(), e );
                    handleException( e );
                }
                if ( Log.DEBUG )
                {
                    Log.v( "GoogleContactsActivity:  gotAccount( " + tokenExpired + " ) stop" );
                }
                return;
        }
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
        if ( Log.DEBUG )
        {
            Log.v( "start authenticated" );
        }
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
            fileList();
        }
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "end authenticated" );
        }
    }

    private void handleException( Exception e )
    {
        if ( Log.DEBUG )
        {
            Log.v( "start handleException e=" + e.getMessage() );
        }
        e.printStackTrace();
        if ( e instanceof HttpResponseException )
        {
            int statusCode = ( (HttpResponseException) e ).response.statusCode;
            if ( statusCode == 401 || statusCode == 403 )
            {
                if ( Log.DEBUG )
                {
                    Log.v( "handleException : restart gotAccount" );
                }
                gotAccount( true );
            }
            return;
        }
    }

    public void finishSync( List<SocialNetworkUser> users )
    {
        if ( Log.DEBUG )
        {
            Log.d( "GoogleContactsSync : finishSync start" );
        }
        if ( users == null )
        {
            Toast.makeText( this, "Authorization has expired !", Toast.LENGTH_LONG ).show();
            mProgressDialog.dismiss();
            gotAccount( true );
        }
        else if ( users.isEmpty() )
        {
            Toast.makeText( this, "No google contacts found !", Toast.LENGTH_SHORT ).show();
        }
        else
        {
            /* record results in database */
            mDb.insertGoogleSyncResults( users );
            fillList();
        }
        mProgressDialog.dismiss();
        if ( Log.DEBUG )
        {
            Log.d( "GoogleContactsSync : finishSync end" );
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, START_SYNC_MENU_ID, 0, R.string.start_sync ).setIcon( R.drawable.googlecontacts );
        menu.add( 0, STORE_SYNC_MENU_ID, 0, R.string.store_birthdays ).setIcon( R.drawable.ic_menu_save );
        menu.add( 0, DELETEALL_MENU_ID, 0, R.string.deleteall ).setIcon( R.drawable.ic_menu_delete );
        return true;
    }

    /**
     * 
     */
    private void sync()
    {
        if ( Log.DEBUG )
        {
            Log.v( "GoogleContactsActivity: Start sync" );
        }
        mProgressDialog = ProgressDialog.show( this, "", this.getString( R.string.loading_googlecontacts ), true );
        new GoogleContactSync( this, transport ).execute();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch ( item.getItemId() )
        {
            case START_SYNC_MENU_ID:
                sync();
                return true;
            case STORE_SYNC_MENU_ID:
                Cursor cursor = mDb.fetchAllBirthdays();
                boolean existingBirthdays = cursor.getCount() > 0;
                cursor.close();
                if ( existingBirthdays )
                {
                    showDialog( CONFIRM_STORE_DIALOG_ID );
                }
                else
                {
                    store( false );
                }
                return true;
            case DELETEALL_MENU_ID:
                showDialog( DELETEALL_DIALOG_ID );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * @see com.kamosoft.happycontacts.sync.SyncStorer#store(boolean)
     */
    public void store( boolean update )
    {
        if ( mUserList != null && !mUserList.isEmpty() )
        {
            mProgressDialog = ProgressDialog.show( this, "", getString( R.string.inserting_birthdays, 0 ), true );
            new StoreAsyncTask( update, this, mUserList, mDb, false, mProgressDialog ).execute();
        }
        else
        {
            Toast.makeText( this, R.string.no_syncresults, Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog( int id )
    {
        switch ( id )
        {
            case DELETEALL_DIALOG_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setMessage( R.string.confirm_deleteall ).setCancelable( false ).setPositiveButton( R.string.ok,
                                                                                                           this )
                    .setNegativeButton( R.string.cancel, this );
                return builder.create();

            case CONFIRM_STORE_DIALOG_ID:
                return new ConfirmStoreDialog( this, this );

        }
        return null;
    }

    /**
     * 
     */
    public void fillList()
    {
        mUserList = mDb.fetchGoogleSyncResults();
        mSyncCounter.setText( String.valueOf( mUserList.size() ) );

        mArrayAdapter = new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, mUserList );
        setListAdapter( mArrayAdapter );
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    public void onClick( DialogInterface dialog, int which )
    {
        switch ( which )
        {
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_POSITIVE:
                mDb.deleteGoogleSyncResults();
                fillList();
                return;
        }
    }

}
