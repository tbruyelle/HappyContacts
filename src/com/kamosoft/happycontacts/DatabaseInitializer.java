package com.kamosoft.happycontacts;

import android.app.ProgressDialog;
import android.content.Context;

import com.kamosoft.happycontacts.dao.DbAdapter;

/**
 * Threaded database initializer to avoid latence when db create or update
 * @author Tom
 *
 * @since 31 déc. 2009
 * @version $Id$
 */
public class DatabaseInitializer
    extends Thread
{
    private Context mContext;

    private ProgressDialog mProgressDialog;

    public DatabaseInitializer( Context context, ProgressDialog progressDialog )
    {
        super();
        mContext = context;
        mProgressDialog = progressDialog;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run()
    {
        try
        {
            DbAdapter dbAdapter = new DbAdapter( mContext );
            /* a simple call to open() proceed to db create or upgrade */
            dbAdapter.open( true );
            dbAdapter.close();
        }
        finally
        {
            mProgressDialog.dismiss();
        }
    }

}
