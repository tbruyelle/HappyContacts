package com.kamosoft.utils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * @author tom
 *
 */
public class ProgressDialogHandler
    extends Handler
{
    private ProgressDialog mProgressDialog;

    private int mLastPercent;

    private final Runnable shower = new Runnable()
    {
        @Override
        public void run()
        {
            mProgressDialog.show();
        }
    };

    private final Runnable dissmisser = new Runnable()
    {
        @Override
        public void run()
        {
            mProgressDialog.dismiss();
        }
    };

    public ProgressDialogHandler( ProgressDialog progressDialog )
    {
        super();
        mProgressDialog = progressDialog;
        mLastPercent = 0;
    }

    public void updateProgress( int currentCount, int totalCount )
    {
        int percent = (int) ( ( currentCount / (float) totalCount ) * 100 );
        updateProgress( percent );
    }

    public void updateProgress( int percent )
    {
        if ( percent > mLastPercent )
        {
            Message msg = obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt( "percent", percent );
            msg.setData( bundle );
            sendMessage( msg );
            mLastPercent = percent;
        }
    }

    public void startProgress()
    {
        post( shower );
    }

    public void stopProgress()
    {
        post( dissmisser );
    }

    @Override
    public void handleMessage( Message msg )
    {
        int percent = msg.getData().getInt( "percent" );
        if ( percent < 0 )
        {
            mProgressDialog.dismiss();
        }
        mProgressDialog.setProgress( percent );
        if ( percent >= 100 )
        {
            mProgressDialog.dismiss();
        }
    }
}
