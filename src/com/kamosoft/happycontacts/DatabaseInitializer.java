package com.kamosoft.happycontacts;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Threaded database initializer to avoid latence when db create or update
 * @author Tom
 *
 * @since 31 déc. 2009
 * @version $Id$
 */
public class DatabaseInitializer
    extends AsyncTask<String, Integer, Integer>
{
    private SQLiteDatabase mDb;

    private ProgressDialog mProgressDialog;

    public DatabaseInitializer( SQLiteDatabase db, ProgressDialog progressDialog )
    {
        super();
        mDb = db;
        mProgressDialog = progressDialog;
    }

    /**
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Integer doInBackground( String... sqlStatements )
    {
        int count = sqlStatements.length;
        SQLiteDatabase db = mDb;
        for ( int i = 0; i < count; i++ )
        {
            db.execSQL( sqlStatements[i] );
            publishProgress( (int) ( ( i / (float) count ) * 100 ) );
        }
        return 0;
    }

    /**
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute( Integer result )
    {
        mProgressDialog.dismiss();
    }

    /**
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate( Integer... values )
    {
        mProgressDialog.setProgress( values[0].intValue() );
    }

    /**
     * @see java.lang.Thread#run()
     */
    //    @Override
    //    public void run()
    //    {
    //        try
    //        {
    //            DbAdapter dbAdapter = new DbAdapter( mContext );
    //            /* a simple call to open() proceed to db create or upgrade */
    //            dbAdapter.open( true );
    //            dbAdapter.close();
    //        }
    //        finally
    //        {
    //            mProgressDialog.dismiss();
    //        }
    //    }

}
