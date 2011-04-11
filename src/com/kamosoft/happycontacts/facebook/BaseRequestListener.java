package com.kamosoft.happycontacts.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.kamosoft.happycontacts.Log;

/**
 * Skeleton base class for RequestListeners, providing default error 
 * handling. Applications should handle these error conditions.
 *
 */
public abstract class BaseRequestListener
    implements RequestListener
{

    public void onFacebookError( FacebookError e, Object state )
    {
        Log.e( e.getMessage() );
        e.printStackTrace();
    }

    public void onFileNotFoundException( FileNotFoundException e, Object state )
    {
        Log.e( e.getMessage() );
        e.printStackTrace();
    }

    public void onIOException( IOException e, Object state )
    {
        Log.e( e.getMessage() );
        e.printStackTrace();
    }

    public void onMalformedURLException( MalformedURLException e, Object state )
    {
        Log.e( e.getMessage() );
        e.printStackTrace();
    }

}
