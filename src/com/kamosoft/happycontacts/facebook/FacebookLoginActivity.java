/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLDecoder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.nloko.simplyfacebook.net.login.FacebookLogin;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 26 mars 2010
 * @version $Id$
 */
public class FacebookLoginActivity
    extends Activity
{
    private final FacebookLogin login = new FacebookLogin();

    private final int AUTH_DIALOG = 0;

    private ProgressDialog authDialog = null;

    private WebView webview;

    private static class ChromeClient
        extends WebChromeClient
    {
        private final WeakReference<Activity> mActivity;

        public ChromeClient( Activity activity )
        {
            super();
            mActivity = new WeakReference<Activity>( activity );
        }

        @Override
        public void onProgressChanged( WebView view, int newProgress )
        {
            super.onProgressChanged( view, newProgress );
            setProgress( newProgress );
        }

        private void setProgress( int progress )
        {
            Activity activity = mActivity.get();
            if ( activity != null )
            {
                activity.setProgress( progress * 100 );
                activity.setProgressBarIndeterminateVisibility( progress < 100 );
            }
        }
    }

    private static class FacebookClient
        extends WebViewClient
    {
        private final WeakReference<FacebookLoginActivity> mActivity;

        public FacebookClient( FacebookLoginActivity activity )
        {
            super();
            mActivity = new WeakReference<FacebookLoginActivity>( activity );
        }

        @Override
        public void onPageFinished( WebView view, String url )
        {
            super.onPageFinished( view, url );
            FacebookLoginActivity activity = mActivity.get();
            if ( activity != null )
            {
                Dialog dialog = activity.authDialog;
                if ( dialog != null && dialog.isShowing() )
                {
                    activity.dismissDialog( activity.AUTH_DIALOG );
                }
            }
        }

        @Override
        public void onReceivedError( WebView view, int errorCode, String description, String failingUrl )
        {
            super.onReceivedError( view, errorCode, description, failingUrl );
            String msg = String.format( "URL %s failed to load with error %d %s", failingUrl, errorCode, description );

            FacebookLoginActivity activity = mActivity.get();
            if ( activity != null )
            {
                Log.e( msg );
                Toast.makeText( activity.getApplicationContext(), msg, Toast.LENGTH_LONG ).show();

                Dialog dialog = activity.authDialog;
                if ( dialog != null && dialog.isShowing() )
                {
                    activity.removeDialog( activity.AUTH_DIALOG );
                }

                activity.setResult( Activity.RESULT_CANCELED );
                activity.finish();
            }
        }

        @Override
        public void onPageStarted( WebView view, String url, Bitmap favicon )
        {
            super.onPageStarted( view, url, favicon );
            FacebookLoginActivity activity = mActivity.get();
            if ( activity == null )
            {
                return;
            }

            FacebookLogin login = activity.login;
            if ( login == null )
            {
                return;
            }

            if ( !url.equals( login.getFullLoginUrl() ) )
            {
                activity.showDialog( activity.AUTH_DIALOG );
            }

            try
            {
                Log.d( url );
                Log.d( login.getNextUrl().getPath() );

                URL page = new URL( URLDecoder.decode( url ).trim() );

                if ( page.getPath().equals( login.getNextUrl().getPath() ) )
                {
                    login.setResponseFromExternalBrowser( page );
                    Toast.makeText( activity.getApplicationContext(), R.string.login_thankyou, Toast.LENGTH_LONG ).show();

                    if ( login.isLoggedIn() )
                    {
                        SharedPreferences prefs = activity.getSharedPreferences( Constants.APP_NAME, 0 );
                        Editor editor = prefs.edit();
                        editor.putString( "session_key", login.getSessionKey() );

                        editor.putString( "secret", login.getSecret() );
                        editor.putString( "uid", login.getUid() );
                        editor.commit();
                    }

                    activity.setResult( Activity.RESULT_OK );
                    activity.finish();
                }
                else if ( page.getPath().equals( login.getCancelUrl().getPath() ) )
                {
                    activity.setResult( Activity.RESULT_CANCELED );
                    activity.finish();
                }
            }
            catch ( Exception ex )
            {
                Toast.makeText( activity.getApplicationContext(), R.string.facebooklogin_urlError, Toast.LENGTH_LONG ).show();
                Log.e("Exception during FaceBookLoginActivity.onPageStart()");
                Log.e(android.util.Log.getStackTraceString( ex ));                
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading( WebView view, String url )
        {
            return false;
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_PROGRESS );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.facebook_login );

        login.setAPIKey( Constants.FACEBOOK_API_KEY );

        webview = (WebView) findViewById( R.id.webview );
        webview.setWebChromeClient( new ChromeClient( this ) );
        webview.setWebViewClient( new FacebookClient( this ) );
        webview.getSettings().setJavaScriptEnabled( true );
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d( login.getFullLoginUrl() );
        webview.loadUrl( login.getFullLoginUrl() );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        webview.stopLoading();
    }

    @Override
    protected void onDestroy()
    {
        Log.d( "onDestroy" );
        super.onDestroy();
        // allow proper GC
        authDialog = null;
        webview = null;
    }

    @Override
    protected void finalize()
        throws Throwable
    {
        super.finalize();
        Log.d( "FINALIZED" );
    }

    @Override
    protected Dialog onCreateDialog( int id )
    {
        switch ( id )
        {
            case AUTH_DIALOG:
                authDialog = new ProgressDialog( this );
                authDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
                authDialog.setMessage( getString( R.string.login_authorization ) );
                authDialog.setCancelable( true );
                return authDialog;
        }

        return super.onCreateDialog( id );
    }
}
