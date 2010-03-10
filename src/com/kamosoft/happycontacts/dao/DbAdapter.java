/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.dao;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.utils.AndroidUtils;
import com.kamosoft.utils.ProgressDialogHandler;

/**
 * @author tom
 * 
 */
public class DbAdapter
{
    private DatabaseHelper mDbHelper;

    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static class DatabaseHelper
        extends SQLiteOpenHelper
    {
        private final Context mContext;

        /**
         * Handler for updating a progress dialog while 
         * creating or updating the database
         */
        private ProgressDialogHandler mHandler;

        DatabaseHelper( Context context, ProgressDialogHandler handler )
        {
            super( context, HappyContactsDb.DATABASE_NAME, null, HappyContactsDb.DATABASE_VERSION );
            mContext = context;
            mHandler = handler;
        }

        public boolean needUpgrade()
        {
            SQLiteDatabase db = mContext.openOrCreateDatabase( HappyContactsDb.DATABASE_NAME, 0, null );
            boolean needUpgrade = HappyContactsDb.DATABASE_VERSION > db.getVersion();
            db.close();
            return needUpgrade;
        }

        @Override
        public void onCreate( SQLiteDatabase db )
        {
            Log.v( "Creating database start..." );

            try
            {
                /* get file content */
                String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_create );
                /* parsing sql */
                String[] sqlStatements = sqlCode.split( ";" );
                int nbStatements = sqlStatements.length;
                ProgressDialogHandler handler = mHandler;
                /* execute code */
                for ( int i = 0; i < nbStatements; i++ )
                {
                    db.execSQL( sqlStatements[i] );

                    /* update handler */
                    handler.updateProgress( i, nbStatements );
                }
                /* send last message to the handler */
                handler.updateProgress( 100 );
                Log.v( "Creating database done..." );
            }
            catch ( IOException e )
            {
                // Should never happen!
                Log.e( "Error reading sql file " + e.getMessage(), e );
                throw new RuntimeException( e );
            }
            catch ( SQLException e )
            {
                Log.e( "Error executing sql code " + e.getMessage(), e );
                throw new RuntimeException( e );
            }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
        {
            Log.v( "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data" );
            try
            {
                /* get file content */
                String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_update );
                /* execute code */
                for ( String sqlStatements : sqlCode.split( ";" ) )
                {
                    db.execSQL( sqlStatements );
                }
                Log.v( "Updating database done." );
            }
            catch ( IOException e )
            {
                // Should never happen!
                Log.e( "Error reading sql file " + e.getMessage() );
                throw new RuntimeException( e );
            }
            catch ( SQLException e )
            {
                Log.e( "Error executing sql code " + e.getMessage() );
                throw new RuntimeException( e );
            }
            onCreate( db );
        }
    }

    public DbAdapter( Context ctx )
    {
        mCtx = ctx;
        mDbHelper = new DatabaseHelper( mCtx, null );
    }

    /**
     * Create or update the database in a thread, in order to allow to display a progress bar
     * @param context
     * @param handler
     * @param checkUpgrade
     */
    public static void createOrUpdate( final Context context, final ProgressDialogHandler handler )
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                /* a simple call to getReadableDatabase() proceed to db create or upgrade */
                DatabaseHelper db = new DatabaseHelper( context, handler );
                if ( !db.needUpgrade() )
                {
                    /* no need to upgrade */
                    return;
                }
                handler.startProgress();
                db.getReadableDatabase();
                db.close();
                handler.stopProgress();
            }
        };
        thread.start();
    }

    public DbAdapter open( boolean readOnly )
        throws SQLException
    {
        mDb = readOnly ? mDbHelper.getReadableDatabase() : mDbHelper.getWritableDatabase();
        return this;
    }

    public boolean needUpgrade()
    {
        return mDbHelper.needUpgrade();
    }

    public void close()
    {
        mDbHelper.close();
    }

    /**
     * Return the names for a given day
     * @param day format dd/MM
     * @return
     */
    public Cursor fetchAllNameDay()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start fetchAllNameDay()" );
        }
        /* use order by name */
        Cursor cursor =
            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
                HappyContactsDb.Feast.NAME }, null, null, null, null, HappyContactsDb.Feast.NAME );

        if ( cursor != null )
        {
            cursor = avoidDuplicate( cursor, HappyContactsDb.Feast.ID, HappyContactsDb.Feast.NAME );
        }
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end fetchAllNameDay()" );
        }
        return cursor;
    }

    public Cursor fetchNameDayLike( String constraint )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start fetchNameDayLike()" );
        }
        /* use order by name */
        Cursor cursor =
            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
                HappyContactsDb.Feast.NAME }, HappyContactsDb.Feast.NAME + " like \"" + constraint + "%\"", null, null,
                       null, HappyContactsDb.Feast.NAME );

        if ( cursor != null )
        {
            cursor = avoidDuplicate( cursor, HappyContactsDb.Feast.ID, HappyContactsDb.Feast.NAME );
        }
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end fetchNameDayLike()" );
        }
        return cursor;
    }

    /**
     * Return the names for a given day
     * @param day format dd/MM
     * @return
     */
    public Cursor fetchNamesForDay( String day )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start fetchNameForDay()" );
        }
        /* use order by name */
        Cursor cursor =
            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
                HappyContactsDb.Feast.NAME }, HappyContactsDb.Feast.DAY + "='" + day + "'", null, null, null,
                       HappyContactsDb.Feast.NAME );

        if ( cursor != null )
        {
            cursor = avoidDuplicate( cursor, HappyContactsDb.Feast.ID, HappyContactsDb.Feast.NAME );
        }
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end fetchNameForDay()" );
        }
        return cursor;
    }

    /**
     * Returns the days for a given name
     * @param name
     * @return
     */
    public Cursor fetchDayForName( String name )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start fetchDayForName()" );
        }
        /* 
         * order by date 
         * tips use substr() to have month then day
         */
        Cursor cursor =
            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
                HappyContactsDb.Feast.DAY }, HappyContactsDb.Feast.NAME + " like '" + name + "'", null, null, null,
                       "substr(" + HappyContactsDb.Feast.DAY + ",4,2)||substr(" + HappyContactsDb.Feast.DAY + ",1,2)" );
        if ( cursor != null )
        {
            cursor = avoidDuplicate( cursor, HappyContactsDb.Feast.ID, HappyContactsDb.Feast.DAY );
        }
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end fetchDayForName()" );
        }
        return cursor;
    }

    /**
     * use a matrixCursor to avoid duplicate names (due to multiple source for the database)
     * @param cursor
     * @return
     */
    private Cursor avoidDuplicate( Cursor cursor, String idColumnName, String columnName )
    {
        return cursor;
//        ArrayList<String> columns = new ArrayList<String>();
//        MatrixCursor matrixCursor = new MatrixCursor( new String[] { idColumnName, columnName } );
//        int columnIndex = cursor.getColumnIndex( columnName );
//        int idColumnIndex = cursor.getColumnIndex( idColumnName );
//        cursor.moveToFirst();
//        do
//        {
//            String columnValue = cursor.getString( columnIndex );
//            if ( columns.contains( columnValue ) )
//            {
//                continue;
//            }
//            Long id = cursor.getLong( idColumnIndex );
//            matrixCursor.newRow().add( id ).add( columnValue );
//            columns.add( columnValue );
//        }
//        while ( cursor.moveToNext() );
//        cursor.close();
//        return matrixCursor;
    }

    /**
     * @param contactId
     * @param contactName
     * @param date
     * @return
     */
    private boolean insertBlackList( long contactId, String contactName, String date )
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put( HappyContactsDb.BlackList.CONTACT_ID, contactId );
        initialValues.put( HappyContactsDb.BlackList.CONTACT_NAME, contactName );
        if ( date != null )
        {
            initialValues.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
        }

        return mDb.insert( HappyContactsDb.BlackList.TABLE_NAME, null, initialValues ) > 0;
    }

    public boolean deleteBlackList( long id )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call deleteBlackList()" );
        }
        return mDb.delete( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.ID + "=" + id, null ) > 0;
    }

    public boolean deleteAllBlackList()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call deleteAllBlackList()" );
        }
        return mDb.delete( HappyContactsDb.BlackList.TABLE_NAME, null, null ) > 0;
    }

    public boolean deleteAllWhiteList()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call deleteAllWhiteList()" );
        }
        return mDb.delete( HappyContactsDb.WhiteList.TABLE_NAME, null, null ) > 0;
    }

    /**
     * @return all lines
     */
    public Cursor fetchAllBlackList()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call fetchAllBlackList()" );
        }
        return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS, null, null, null,
                          null, null );
    }

    public boolean insertWhiteList( Long contactId, String contactName, String nameDay )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call insertWhiteList(" + contactId + ", " + contactName + "," + nameDay + ") " );
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put( HappyContactsDb.WhiteList.CONTACT_ID, contactId );
        initialValues.put( HappyContactsDb.WhiteList.CONTACT_NAME, contactName );
        initialValues.put( HappyContactsDb.WhiteList.NAME_DAY, nameDay );
        return mDb.insert( HappyContactsDb.WhiteList.TABLE_NAME, null, initialValues ) > 0;
    }

    public boolean deleteWhiteList( long id )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call deleteWhiteList()" );
        }
        return mDb.delete( HappyContactsDb.WhiteList.TABLE_NAME, HappyContactsDb.WhiteList.ID + "=" + id, null ) > 0;
    }

    /**
     * @return all lines
     */
    public Cursor fetchAllWhiteList()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call fetchAllWhiteList()" );
        }
        return mDb.query( HappyContactsDb.WhiteList.TABLE_NAME, HappyContactsDb.WhiteList.COLUMNS, null, null, null,
                          null, null );
    }

    /**
     * @return only the lines with lastWishDate=null, meaning the contact is black listed all the time.
     */
    public Cursor fetchAllTimeBlackListed()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: call fetchAllTimeBlackListed()" );
        }
        return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                          HappyContactsDb.BlackList.LAST_WISH_DATE + " = null", null, null, null, null );
    }

    public Cursor fetchBlackList( long contactId )
        throws SQLException
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start fetchBlackList()" );
        }
        Cursor mCursor =
            mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                       HappyContactsDb.BlackList.CONTACT_ID + "=" + contactId, null, null, null, null, null );
        if ( mCursor != null )
        {
            mCursor.moveToFirst();
        }
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end fetchBlackList()" );
        }
        return mCursor;
    }

    public boolean isBlackListed( long contactId, String date )
        throws SQLException
    {
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: start isBlackListed()" );
        }
        Cursor c = fetchBlackList( contactId );
        if ( c == null )
        {
            return false;
        }
        if ( c.getCount() == 0 )
        {
            c.close();
            return false;
        }
        if ( date != null )
        {
            /* check if its black listed for this year only */
            String lastWishedDate = c.getString( c.getColumnIndexOrThrow( HappyContactsDb.BlackList.LAST_WISH_DATE ) );
            c.close();
            return ( lastWishedDate == null || lastWishedDate.equals( date ) );
        }
        c.close();
        if ( Log.DEBUG )
        {
            Log.v( "DbAdapter: end isBlackListed()" );
        }
        return true;
    }

    public boolean updateContactFeast( long contactId, String contactName, String date )
    {
        if ( Log.DEBUG )
        {
            Log.v( "Dbadapter: call updateContactFeast for contact " + contactName + " with date " + date );
        }
        if ( isBlackListed( contactId, null ) )
        {
            ContentValues args = new ContentValues();
            args.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
            return mDb.update( HappyContactsDb.BlackList.TABLE_NAME, args, HappyContactsDb.BlackList.CONTACT_ID + "="
                + contactId, null ) > 0;
        }
        else
        {
            return insertBlackList( contactId, contactName, date );
        }
    }
}
