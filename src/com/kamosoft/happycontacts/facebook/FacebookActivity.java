/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.facebook.SessionEvents.AuthListener;
import com.kamosoft.happycontacts.facebook.SessionEvents.LogoutListener;
import com.kamosoft.happycontacts.model.SocialNetworkUser;
import com.kamosoft.happycontacts.sync.StoreAsyncTask;
import com.kamosoft.happycontacts.sync.SyncStorer;

/**
 * @since 26 mars 2010
 * @version $Id$
 */
public class FacebookActivity
    extends ListActivity
    implements Constants, android.content.DialogInterface.OnClickListener, SyncStorer
{
    private static final String FACEBOOK_APP_ID = "105507002814035";

    //    private static final String FACEBOOK_API_KEY = "8e9a98e18c9e1c174e6c8904d9ed350e";
    //
    //    private static final String FAsCEBOOK_SECRET_API_KEY = "6ad543258350e403b907878a8f4b5308";
    //
    //    private static final String FACEBOOK_API_NAME = "HappyContacts";

    private static final int START_SYNC_MENU_ID = Menu.FIRST;

    private static final int LOGOUT_MENU_ID = START_SYNC_MENU_ID + 1;

    private static final int STORE_SYNC_MENU_ID = LOGOUT_MENU_ID + 1;

    private static final int DELETEALL_MENU_ID = STORE_SYNC_MENU_ID + 1;

    private static final int DELETEALL_DIALOG_ID = 1;

    private static final int CONFIRM_STORE_DIALOG_ID = 2;

    private static final int LOGIN_ACTIVITY_RESULT = 1;

    public static final int PICK_CONTACT_ACTIVITY_RESULT = 2;

    private DbAdapter mDb;

    private SocialUserArrayAdapter mArrayAdapter;

    private ArrayList<SocialNetworkUser> mUserList;

    private ProgressDialog mProgressDialog;

    private SocialNetworkUser mCurrentUser;

    private int mCurrentPosition;

    private TextView mSyncCounter;

    private Facebook mFacebook;

    private Handler mHandler;

    private String[] mPermissions = new String[] { "friends_birthday" };

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

        mDb = new DbAdapter( this );

        mSyncCounter = (TextView) findViewById( R.id.sync_counter );

        mDb.open( false );

        mFacebook = new Facebook( FACEBOOK_APP_ID );
        mHandler = new Handler();

        Button syncButton = (Button) findViewById( R.id.start_sync_button );
        syncButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                sync();
            }
        } );

        SessionStore.restore( mFacebook, this );
        SessionListener sessionListener = new SessionListener();
        SessionEvents.addAuthListener( sessionListener );
        SessionEvents.addLogoutListener( sessionListener );

        if ( mFacebook.isSessionValid() )
        {
            if ( Log.DEBUG )
            {
                Log.v( "FaceBookActivity: onResume user logged" );
            }
            fillList();
        }
        else
        {
            if ( Log.DEBUG )
            {
                Log.v( "FaceBookActivity: onResume start login" );
            }
            mFacebook.authorize( this, mPermissions, new LoginDialogListener() );
        }

        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: onCreate() end" );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        SocialNetworkUser user = mArrayAdapter.getItem( position );
        if ( user.birthday == null )
        {
            /* nothing todo if no birthday */
            Toast.makeText( this, R.string.friend_no_birthday, Toast.LENGTH_SHORT ).show();
            return;
        }
        mCurrentUser = user;
        mCurrentPosition = position;
        new SocialUserDialog( this, user, mDb ).show();
    }

    /**
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: start onActivityResult" );
        }
        super.onActivityResult( requestCode, resultCode, data );
        mFacebook.authorizeCallback( requestCode, resultCode, data );

        if ( resultCode == Activity.RESULT_CANCELED )
        {
            return;
        }

        if ( resultCode != Activity.RESULT_OK )
        {
            Toast.makeText( this, R.string.facebook_login_error, Toast.LENGTH_SHORT );
            this.finish();
            return;
        }

        switch ( requestCode )
        {
            case LOGIN_ACTIVITY_RESULT:
                fillList();
                break;

            case PICK_CONTACT_ACTIVITY_RESULT:
                Toast.makeText( this,
                                getString( R.string.link_contact_done, mCurrentUser.name,
                                           data.getStringExtra( CONTACTNAME_INTENT_KEY ) ), Toast.LENGTH_LONG ).show();
                setIntent( data );
                break;
        }
        if ( Log.DEBUG )
        {
            Log.v( "FaceBookActivity: end onActivityResult" );
        }
    }

    public Facebook getFacebook()
    {
        return mFacebook;
    }

    /**
     * 
     */
    public void fillList()
    {
        /* check intent contactId to update contact link and then scroll to the right item */
        if ( getIntent().hasExtra( CONTACTID_INTENT_KEY ) )
        {
            long id = getIntent().getLongExtra( CONTACTID_INTENT_KEY, 0 );
            String contactName = getIntent().getStringExtra( CONTACTNAME_INTENT_KEY );
            if ( contactName != null && mCurrentUser != null )
            {
                mCurrentUser.setContactId( id );
                mCurrentUser.setContactName( contactName );
                mDb.updateSyncResult( mCurrentUser );
                getIntent().removeExtra( CONTACTNAME_INTENT_KEY );
            }
        }
        mUserList = mDb.fetchFacebookSyncResults();
        mSyncCounter.setText( String.valueOf( mUserList.size() ) );

        mArrayAdapter = new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, mUserList );
        setListAdapter( mArrayAdapter );
        if ( mCurrentPosition > 0 )
        {
            getListView().setSelection( mCurrentPosition );
        }
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
            mDb.insertFacebookSyncResults( users );
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

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: start onStop" );
        }
        super.onStop();
        //        if ( mDb != null )
        //        {
        //            mDb.close();
        //        }
        if ( Log.DEBUG )
        {
            Log.v( "BirthdayActivity: end onStop" );
        }
    }

    /**
     * @see com.kamosoft.happycontacts.sync.SyncStorer#store(boolean)
     */
    public void store( boolean update )
    {
        if ( mUserList != null && !mUserList.isEmpty() )
        {
            mProgressDialog = ProgressDialog.show( this, "", getString( R.string.inserting_birthdays, 0 ), true );
            new StoreAsyncTask( update, this, mUserList, mDb, true, mProgressDialog ).execute();
        }
        else
        {
            Toast.makeText( this, R.string.no_syncresults, Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, START_SYNC_MENU_ID, 0, R.string.start_sync ).setIcon( R.drawable.fb );
        menu.add( 0, LOGOUT_MENU_ID, 0, R.string.logout ).setIcon( R.drawable.ic_menu_close_clear_cancel );
        menu.add( 0, STORE_SYNC_MENU_ID, 0, R.string.store_birthdays ).setIcon( R.drawable.ic_menu_save );
        menu.add( 0, DELETEALL_MENU_ID, 0, R.string.deleteall ).setIcon( R.drawable.ic_menu_delete );
        return true;
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
            case LOGOUT_MENU_ID:
                logout();
                return true;
            case DELETEALL_MENU_ID:
                showDialog( DELETEALL_DIALOG_ID );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * 
     */
    private void logout()
    {
        if ( Log.DEBUG )
        {
            Log.d( "Start logout" );
        }
        SessionEvents.onLogoutBegin();
        AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner( mFacebook );
        asyncRunner.logout( this, new LogoutRequestListener() );
        if ( Log.DEBUG )
        {
            Log.d( "end logout" );
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
                builder.setMessage( R.string.confirm_deleteall ).setCancelable( false )
                    .setPositiveButton( R.string.ok, this ).setNegativeButton( R.string.cancel, this );
                return builder.create();

            case CONFIRM_STORE_DIALOG_ID:
                return new ConfirmStoreDialog( this, this );

        }
        return null;
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
                mDb.deleteFacebookSyncResults();
                fillList();
                return;
        }
    }

    private class SessionListener
        implements AuthListener, LogoutListener
    {
        public void onAuthSucceed()
        {
            Log.d( "You have logged in! " );
            Toast.makeText( FacebookActivity.this, FacebookActivity.this.getText( R.string.login_thankyou ),
                            Toast.LENGTH_SHORT ).show();
            SessionStore.save( mFacebook, FacebookActivity.this );
        }

        public void onAuthFail( String error )
        {
            Log.e( "Login Failed: " + error );
            Toast.makeText( FacebookActivity.this, FacebookActivity.this.getText( R.string.login_ko ),
                            Toast.LENGTH_SHORT ).show();
            FacebookActivity.this.finish();
        }

        public void onLogoutBegin()
        {
            Log.d( "Logging out..." );
            FacebookActivity.this.mProgressDialog = ProgressDialog.show( FacebookActivity.this, "",
                                                                         FacebookActivity.this
                                                                             .getText( R.string.logging_out ), true );
        }

        public void onLogoutFinish()
        {
            Log.d( "You have logged out! " );
            SessionStore.clear( FacebookActivity.this );
            FacebookActivity.this.mProgressDialog.dismiss();
            FacebookActivity.this.finish();
            Toast.makeText( FacebookActivity.this, FacebookActivity.this.getText( R.string.logout_ok ),
                            Toast.LENGTH_SHORT ).show();
        }
    }

    private class LogoutRequestListener
        extends BaseRequestListener
    {
        /* (non-Javadoc)
         * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onComplete(java.lang.String, java.lang.Object)
         */
        @Override
        public void onComplete( String response, Object state )
        {
         // callback should be run in the original thread, 
            // not the background thread
            mHandler.post( new Runnable()
            {
                public void run()
                {
                    SessionEvents.onLogoutFinish();
                }
            } );
        }
    }

    private final class LoginDialogListener
        implements DialogListener
    {
        public void onComplete( Bundle values )
        {
            SessionEvents.onLoginSuccess();
            fillList();
        }

        public void onFacebookError( FacebookError error )
        {
            SessionEvents.onLoginError( error.getMessage() );
        }

        public void onError( DialogError error )
        {
            SessionEvents.onLoginError( error.getMessage() );
        }

        public void onCancel()
        {
            SessionEvents.onLoginError( "Action Canceled" );
        }
    }
}
