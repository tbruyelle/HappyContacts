/**
 * 
 */
package com.tsoft.happycontacts.dao;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tsoft.happycontacts.R;
import com.tsoft.utils.AndroidUtils;

/**
 * @author tom
 * 
 */
public class DbAdapter
{
  private static final String TAG = "DbAdapter";

  private DatabaseHelper mDbHelper;

  private SQLiteDatabase mDb;

  private final Context mCtx;

  private static class DatabaseHelper
      extends SQLiteOpenHelper
  {
    private final Context mCtx;

    DatabaseHelper(Context context)
    {
      super(context, HappyContactsDb.DATABASE_NAME, null, HappyContactsDb.DATABASE_VERSION);
      mCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
      //      ProgressDialog progressDialog = ProgressDialog.show(mCtx, "", mCtx
      //          .getString(R.string.loading_data));
      Log.i(TAG, "Creating database start...");
      try
      {
        // get file content
        String sqlCode = AndroidUtils.getFileContent(mCtx.getResources(), R.raw.db_create);
        // execute code
        for (String sqlStatements : sqlCode.split(";"))
        {
          db.execSQL(sqlStatements);
        }
        Log.i(TAG, "Creating database done.");
      }
      catch (IOException e)
      {
        // Should never happen!
        Log.e(TAG, "Error reading sql file " + e.getMessage(), e);
        throw new RuntimeException(e);
      }
      catch (SQLException e)
      {
        Log.e(TAG, "Error executing sql code " + e.getMessage(), e);
        throw new RuntimeException(e);
      }
      finally
      {
        //progressDialog.hide();
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
      //      ProgressDialog progressDialog = ProgressDialog.show(mCtx, "", mCtx
      //          .getString(R.string.updating_data));
      Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
          + ", which will destroy all old data");
      try
      {
        // get file content
        String sqlCode = AndroidUtils.getFileContent(mCtx.getResources(), R.raw.db_update);
        // execute code
        for (String sqlStatements : sqlCode.split(";"))
        {
          db.execSQL(sqlStatements);

        }
        Log.i(TAG, "Updating database done.");
      }
      catch (IOException e)
      {
        // Should never happen!
        Log.e(TAG, "Error reading sql file " + e.getMessage());
        throw new RuntimeException(e);
      }
      catch (SQLException e)
      {
        Log.e(TAG, "Error executing sql code " + e.getMessage());
        throw new RuntimeException(e);
      }
      finally
      {
        //        progressDialog.hide();
      }
      onCreate(db);
    }
  }

  public DbAdapter(Context ctx)
  {
    this.mCtx = ctx;
  }

  public DbAdapter open() throws SQLException
  {
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    return this;
  }

  public void close()
  {
    mDbHelper.close();
  }

  /**
   * Retourne les lignes pour le jour donné et qui n'ont pas encore ete souhaite
   * @param day format dd/MM
   * @param year format YYYY
   * @return
   */
  public Cursor fetchNamesForDay(String day, String year)
  {
    Cursor mCursor =

    mDb.query(HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
        HappyContactsDb.Feast.NAME, HappyContactsDb.Feast.LAST_WISH_YEAR },
        HappyContactsDb.Feast.DAY + "='" + day + "' and " + HappyContactsDb.Feast.LAST_WISH_YEAR
            + " != '" + year + "'", null, null, null, null, null);
    if (mCursor != null)
    {
      mCursor.moveToFirst();
    }
    return mCursor;
  }

  public Cursor fetchDayForName(String name)
  {
    Cursor mCursor =

    mDb.query(HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.DAY },
        HappyContactsDb.Feast.NAME + " like " + name, null, null, null, null, null);
    if (mCursor != null)
    {
      mCursor.moveToFirst();
    }
    return mCursor;
  }

  public long insertBlackList(long contactId, String contactName)
  {
    ContentValues initialValues = new ContentValues();
    initialValues.put(HappyContactsDb.BlackList.CONTACT_ID, contactId);
    initialValues.put(HappyContactsDb.BlackList.CONTACT_NAME, contactName);

    return mDb.insert(HappyContactsDb.BlackList.TABLE_NAME, null, initialValues);
  }

  public boolean deleteBlackList(long id)
  {
    return mDb.delete(HappyContactsDb.BlackList.TABLE_NAME,
        HappyContactsDb.BlackList.ID + "=" + id, null) > 0;
  }

  public Cursor fetchAllBlackList()
  {

    return mDb.query(HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS, null,
        null, null, null, null);
  }

  public Cursor fetchBlackList(long contactId) throws SQLException
  {

    Cursor mCursor =

    mDb.query(HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
        HappyContactsDb.BlackList.CONTACT_ID + "=" + contactId, null, null, null, null, null);
    if (mCursor != null)
    {
      mCursor.moveToFirst();
    }
    return mCursor;
  }

  public boolean isBlackListed(Long contactId) throws SQLException
  {
    Cursor c = fetchBlackList(contactId);
    if (c == null || c.getCount() == 0)
    {
      return false;
    }
    return true;
  }

  public boolean updateContactFeast(Long feastId, String year)
  {
    ContentValues args = new ContentValues();
    args.put(HappyContactsDb.Feast.LAST_WISH_YEAR, year);
    return mDb.update(HappyContactsDb.Feast.TABLE_NAME, args, HappyContactsDb.Feast.ID + "="
        + feastId, null) > 0;
  }
}
