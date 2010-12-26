package com.kamosoft.happycontacts.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
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
import com.kamosoft.happycontacts.contacts.ContactProxyFactory;
import com.kamosoft.happycontacts.contacts.PhoneContact;

public class DataManager
    extends Activity
{
    private Button backupDbButton;

    private Button restoreDbButton;

    public static final String dbPath = "/data/com.kamosoft.happycontacts/databases/happy_contacts";

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
                new AlertDialog.Builder( DataManager.this ).setMessage( R.string.data_backup_check )
                    .setPositiveButton( R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                            if ( isExternalStorageAvail() )
                            {
                                Log.i( "importing database from external storage, and resetting database" );
                                new ExportDatabaseTask().execute();
                            }
                            else
                            {
                                Toast.makeText( DataManager.this, R.string.data_external_unavailable,
                                                Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } ).setNegativeButton( R.string.no, new DialogInterface.OnClickListener()
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
                new AlertDialog.Builder( DataManager.this ).setMessage( R.string.data_restore_check )
                    .setPositiveButton( R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface arg0, int arg1 )
                        {
                            if ( isExternalStorageAvail() )
                            {
                                Log.i( "importing database from external storage, and resetting database" );
                                new ImportDatabaseTask().execute();
                            }
                            else
                            {
                                Toast.makeText( DataManager.this, R.string.data_external_unavailable,
                                                Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } ).setNegativeButton( R.string.no, new DialogInterface.OnClickListener()
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
            dialog.setMessage( getString( R.string.data_exporting ) );
            dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Boolean doInBackground( final Void... args )
        {

            File dbFile = new File( Environment.getDataDirectory() + dbPath );

            File exportDir = new File( Environment.getExternalStorageDirectory(), "happycontacts_db_backup" );
            if ( !exportDir.exists() )
            {
                exportDir.mkdirs();
            }
            File file = new File( exportDir, dbFile.getName() );
            Log.i( "Starting copy " + dbFile.getPath() + " to " + file.getPath() );
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
            // sleep momentarily so that database reset stuff has time to take place (else Main reloads too fast)
            SystemClock.sleep( 500 );
            if ( dialog.isShowing() )
            {
                dialog.dismiss();
            }
            HappyContactsPreferences.backToMain( DataManager.this );

            if ( success )
            {
                Toast.makeText( DataManager.this, R.string.data_backup_ok, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Toast.makeText( DataManager.this, R.string.data_backup_ko, Toast.LENGTH_SHORT ).show();
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
            dialog.setMessage( getString( R.string.data_restoring ) );
            dialog.show();
        }

        // could pass the params used here in AsyncTask<String, Void, String> - but not being re-used
        @Override
        protected String doInBackground( final Void... args )
        {

            File dbBackupFile = new File( Environment.getExternalStorageDirectory()
                + "/happycontacts_db_backup/happy_contacts" );
            if ( !dbBackupFile.exists() )
            {
                return getString( R.string.data_backupfile_notfound );
            }
            else if ( !dbBackupFile.canRead() )
            {
                return getString( R.string.data_backupfile_notreadable );
            }

            File dbFile = new File( Environment.getDataDirectory() + dbPath );
            if ( dbFile.exists() )
            {
                dbFile.delete();
            }

            try
            {
                dbFile.createNewFile();
                FileUtil.copyFile( dbBackupFile, dbFile );
                DbAdapter db = new DbAdapter( DataManager.this );
                db.resetDbConnection();

                /* apply the fixes */
                fixContactsIds();

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
            // sleep momentarily so that database reset stuff has time to take place (else Main reloads too fast)
            SystemClock.sleep( 500 );
            if ( dialog.isShowing() )
            {
                dialog.dismiss();
            }
            HappyContactsPreferences.backToMain( DataManager.this );
            if ( errMsg == null )
            {
                Toast.makeText( DataManager.this, R.string.data_restore_ok, Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Toast.makeText( DataManager.this, getString( R.string.data_restore_ok, errMsg ), Toast.LENGTH_SHORT )
                    .show();
            }
        }
    }

    /**
     * Convenience dry method for checkings the contacts ids
     * @param idIndex
     * @param contactIdIndex
     * @param contactNameIndex
     * @param cursor
     * @param toBeDeleted
     * @param toBeUpdated
     * @param phoneContactsName
     * @param logTag
     */
    private void checkIds( int idIndex, int contactIdIndex, int contactNameIndex, Cursor cursor,
                           ArrayList<Long> toBeDeleted, ArrayList<PhoneContact> toBeUpdated,
                           HashMap<String, PhoneContact> phoneContactsName, String logTag )
    {
        while ( cursor.moveToNext() )
        {
            Long id = cursor.getLong( idIndex );
            String contactName = cursor.getString( contactNameIndex );
            Long contactId = cursor.getLong( contactIdIndex );
            if ( contactId == null )
            {
                Log.v( logTag + ": Skipping empty id entry" );
                continue;
            }
            PhoneContact phoneContact = phoneContactsName.get( contactName );
            if ( phoneContact == null )
            {
                /* contact not in phone contacts!!, need to delete it */
                Log.e( logTag + ": Contact " + contactName + ", id="+contactId+" not found in contact phones, delete it !" );
                toBeDeleted.add( id );
                continue;
            }
            if ( phoneContact.id.longValue() == contactId.longValue() )
            {
                /* ids are the same it's OK */
                Log.v( logTag + ": Contact id="+contactId+" for " + contactName + " are OK both side" );
                continue;
            }
            /* contacts ids are different, need to update the black list with the new id */
            Log.v( logTag + ": " + contactName + " Contact ids are different: id="+contactId+", phoneId="+phoneContact.id+", need to update" );
            toBeUpdated.add( phoneContact );
        }
        cursor.close();
    }

    /**
     * After restore, the contacts ids may have change, need to scan all the tables
     * which contains contact ids et check them.
     */
    private void fixContactsIds()
    {
        ArrayList<PhoneContact> phoneContacts = ContactProxyFactory.create().loadPhoneContacts( this );
        HashMap<String, PhoneContact> phoneContactsName = new HashMap<String, PhoneContact>();
        for ( PhoneContact phoneContact : phoneContacts )
        {
            if ( phoneContactsName.containsKey( phoneContact.name ) )
            {
                /* argh multiple contact with same names...
                 * no solution here, for now we skip the fix step */
                Log.v( "Multiple contact with same name, the fix step is skipped" );
                return;
            }
            phoneContactsName.put( phoneContact.name, phoneContact );
        }
        DbAdapter db = new DbAdapter( this );
        db.open( false );

        ArrayList<Long> toBeDeleted = new ArrayList<Long>();
        ArrayList<PhoneContact> toBeUpdated = new ArrayList<PhoneContact>();

        Log.i( "Start fixing blacklist" );
        /* fix black list */
        Cursor cursor = db.fetchAllBlackList();
        int contactNameIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.BlackList.CONTACT_NAME );
        int contactIdIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.BlackList.CONTACT_ID );
        int idIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.BlackList.ID );

        checkIds( idIndex, contactIdIndex, contactNameIndex, cursor, toBeDeleted, toBeUpdated, phoneContactsName,
                  "Fix Blacklist" );

        for ( Long id : toBeDeleted )
        {
            if ( !db.deleteBlackList( id ) )
            {
                Log.e( "Fix blacklist: Unable to delete it !" );
            }
        }
        for ( PhoneContact phoneContact : toBeUpdated )
        {
            db.fixBlackListId( phoneContact );
        }
        Log.i( "Blacklist fixed" );

        /* clear the lists */
        toBeDeleted.clear();
        toBeUpdated.clear();

        /* fix White list */
        Log.i( "Start fixing whitelist" );
        cursor = db.fetchAllWhiteList();
        idIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.ID );
        contactIdIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.CONTACT_ID );
        contactNameIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.WhiteList.CONTACT_NAME );

        checkIds( idIndex, contactIdIndex, contactNameIndex, cursor, toBeDeleted, toBeUpdated, phoneContactsName,
                  "Fix White list" );
        for ( Long id : toBeDeleted )
        {
            if ( !db.deleteWhiteList( id ) )
            {
                Log.e( "Fix whitelist: Unable to delete it !" );
            }
        }
        for ( PhoneContact phoneContact : toBeUpdated )
        {
            db.fixWhiteListId( phoneContact );
        }
        Log.i( "Whitelist fixed" );

        /* clear the lists */
        toBeDeleted.clear();
        toBeUpdated.clear();

        /* fix birthday list */
        Log.i( "Start fixing birthday" );
        cursor = db.fetchAllBirthdays();
        idIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.ID );
        contactIdIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_ID );
        contactNameIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.Birthday.CONTACT_NAME );

        checkIds( idIndex, contactIdIndex, contactNameIndex, cursor, toBeDeleted, toBeUpdated, phoneContactsName,
                  "Fix Birthday" );
        for ( Long id : toBeDeleted )
        {
            if ( !db.deleteBirthday( id ) )
            {
                Log.e( "Fix Birthday: Unable to delete it !" );
            }
        }
        for ( PhoneContact phoneContact : toBeUpdated )
        {
            db.fixBirthdayId( phoneContact );
        }
        Log.i( "Birthday fixed" );

        /* clear the lists */
        toBeDeleted.clear();
        toBeUpdated.clear();

        /* fix Events list: the events will be regenerated automatically, so no need to fix them, juste delete all */
        db.deleteNextEvents();

        /* fix SyncResult */
        Log.i( "Start fixing SyncResult" );
        cursor = db.fetchAllSyncResults();
        idIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.ID );
        contactIdIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.CONTACT_ID );
        contactNameIndex = cursor.getColumnIndexOrThrow( HappyContactsDb.SyncResult.CONTACT_NAME );

        checkIds( idIndex, contactIdIndex, contactNameIndex, cursor, toBeDeleted, toBeUpdated, phoneContactsName,
                  "Fix SyncResult" );
        for ( Long id : toBeDeleted )
        {
            if ( !db.deleteSyncResult( id ) )
            {
                Log.e( "Fix SyncResult: Unable to delete it !" );
            }
        }
        for ( PhoneContact phoneContact : toBeUpdated )
        {
            db.fixSyncResultId( phoneContact );
        }
        Log.i( "SyncResult fixed" );

        db.close();
        Log.i( "All fixes are done" );
    }
}
