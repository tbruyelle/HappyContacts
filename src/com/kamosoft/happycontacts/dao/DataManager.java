package com.kamosoft.happycontacts.dao;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kamosoft.happycontacts.HappyContactsPreferences;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;

public class DataManager
    extends Activity
{
    private Button backupDbButton;

    private Button restoreDbButton;

    @Override
    public void onCreate( final Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.data_manager );

        backupDbButton = (Button) findViewById( R.id.backupDb );
        backupDbButton.setOnClickListener( new OnClickListener()
        {
            public void onClick( final View v )
            {
                Log.i( "exporting database to external storage" );
                new AlertDialog.Builder( DataManager.this )
                    .setMessage( "Are you sure (this will overwrite any existing backup data)?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                            if ( isExternalStorageAvail() )
                            {
                                Log.i( "importing database from external storage, and resetting database" );
                                new ExportDatabaseTask().execute();
                                DataManager.this.startActivity( new Intent( DataManager.this,
                                                                            HappyContactsPreferences.class ) );
                            }
                            else
                            {
                                Toast.makeText( DataManager.this,
                                                "External storage is not available, unable to export data.",
                                                Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } ).setNegativeButton( "No", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                        }
                    } ).show();
            }
        } );

        restoreDbButton = (Button) findViewById( R.id.restoreDb );
        restoreDbButton.setOnClickListener( new OnClickListener()
        {
            public void onClick( final View v )
            {
                new AlertDialog.Builder( DataManager.this )
                    .setMessage( "Are you sure (this will overwrite existing current data)?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                            if ( isExternalStorageAvail() )
                            {
                                Log.i( "importing database from external storage, and resetting database" );
                                new ImportDatabaseTask().execute();
                                // sleep momentarily so that database reset stuff has time to take place (else Main reloads too fast)
                                SystemClock.sleep( 500 );
                                DataManager.this.startActivity( new Intent( DataManager.this,
                                                                            HappyContactsPreferences.class ) );
                            }
                            else
                            {
                                Toast.makeText( DataManager.this,
                                                "External storage is not available, unable to export data.",
                                                Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } ).setNegativeButton( "No", new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                        }
                    } ).show();
            }
        } );
    }

    private boolean isExternalStorageAvail()
    {
        return Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED );
    }

    private class ExportDatabaseTask
        extends AsyncTask<Void, Void, Boolean>
    {
        private final ProgressDialog dialog = new ProgressDialog( DataManager.this );

        // can use UI thread here
        @Override
        protected void onPreExecute()
        {
            dialog.setMessage( "Exporting database..." );
            dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Boolean doInBackground( final Void... args )
        {

            File dbFile = new File( Environment.getDataDirectory() + "/data/com.totsp.database/databases/sample.db" );

            File exportDir = new File( Environment.getExternalStorageDirectory(), "databasedata" );
            if ( !exportDir.exists() )
            {
                exportDir.mkdirs();
            }
            File file = new File( exportDir, dbFile.getName() );

            try
            {
                file.createNewFile();
                FileUtil.copyFile( dbFile, file );
                return true;
            }
            catch ( IOException e )
            {
                Log.e( e.getMessage(), e );
                return false;
            }
        }

        // can use UI thread here
        @Override
        protected void onPostExecute( final Boolean success )
        {
            if ( dialog.isShowing() )
            {
                dialog.dismiss();
            }
            if ( success )
            {
                Toast.makeText( DataManager.this, "Export successful!", Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Toast.makeText( DataManager.this, "Export failed", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private class ImportDatabaseTask
        extends AsyncTask<Void, Void, String>
    {
        private final ProgressDialog dialog = new ProgressDialog( DataManager.this );

        @Override
        protected void onPreExecute()
        {
            dialog.setMessage( "Importing database..." );
            dialog.show();
        }

        // could pass the params used here in AsyncTask<String, Void, String> - but not being re-used
        @Override
        protected String doInBackground( final Void... args )
        {

            File dbBackupFile = new File( Environment.getExternalStorageDirectory() + "/databasedata/sample.db" );
            if ( !dbBackupFile.exists() )
            {
                return "Database backup file does not exist, cannot import.";
            }
            else if ( !dbBackupFile.canRead() )
            {
                return "Database backup file exists, but is not readable, cannot import.";
            }

            File dbFile = new File( Environment.getDataDirectory() + "/data/com.totsp.database/databases/sample.db" );
            if ( dbFile.exists() )
            {
                dbFile.delete();
            }

            try
            {
                dbFile.createNewFile();
                FileUtil.copyFile( dbBackupFile, dbFile );
                //FIXME DataManager.this.application.getDataHelper().resetDbConnection();
                return null;
            }
            catch ( IOException e )
            {
                Log.e( e.getMessage(), e );
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute( final String errMsg )
        {
            if ( dialog.isShowing() )
            {
                dialog.dismiss();
            }
            if ( errMsg == null )
            {
                Toast.makeText( DataManager.this, "Import successful!", Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Toast.makeText( DataManager.this, "Import failed - " + errMsg, Toast.LENGTH_SHORT ).show();
            }
        }
    }
}
