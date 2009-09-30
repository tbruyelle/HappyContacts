/**
 * 
 */
package com.tsoft.happycontacts.dao;

import java.io.IOException;

import android.app.ProgressDialog;
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
      ProgressDialog progressDialog = ProgressDialog.show(mCtx, "", mCtx
          .getString(R.string.loading_data));
      try
      {
        // get file content
        String sqlCode = AndroidUtils.getFileContent(mCtx.getResources(), R.raw.db_create);
        // execute code
        for (String sqlStatements : sqlCode.split(";"))
        {
          db.execSQL(sqlStatements);
        }
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
        progressDialog.hide();
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
      ProgressDialog progressDialog = ProgressDialog.show(mCtx, "", mCtx
          .getString(R.string.updating_data));
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
          + ", which will destroy all old data");
      try
      {
        // get file content
        String sqlCode = AndroidUtils.getFileContent(mCtx.getResources(), R.raw.db_create);
        // execute code
        for (String sqlStatements : sqlCode.split(";"))
        {
          db.execSQL(sqlStatements);
        }
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
        progressDialog.hide();
      }
      onCreate(db);
    }
  }

  /**
   * Constructor - takes the context to allow the database to be opened/created
   * 
   * @param ctx
   *          the Context within which to work
   */
  public DbAdapter(Context ctx)
  {
    this.mCtx = ctx;
  }

  /**
   * Open the notes database. If it cannot be opened, try to create a new
   * instance of the database. If it cannot be created, throw an exception to
   * signal the failure
   * 
   * @return this (self reference, allowing this to be chained in an
   *         initialization call)
   * @throws SQLException
   *           if the database could be neither opened or created
   */
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

  public Cursor fetchNamesForDay(String day)
  {
    Cursor mCursor =

    mDb.query(HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.NAME,
        HappyContactsDb.Feast.LAST_WISH_YEAR }, HappyContactsDb.Feast.DAY + "='" + day + "'", null,
        null, null, null, null);
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

  /**
   * Delete the note with the given rowId
   * 
   * @param rowId
   *          id of note to delete
   * @return true if deleted, false otherwise
   */
  public boolean deleteBlackList(long id)
  {

    return mDb.delete(HappyContactsDb.BlackList.TABLE_NAME,
        HappyContactsDb.BlackList.ID + "=" + id, null) > 0;
  }

  /**
   * Return a Cursor over the list of all notes in the database
   * 
   * @return Cursor over all notes
   */
  public Cursor fetchAllBlackList()
  {

    return mDb.query(HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS, null,
        null, null, null, null);
  }

  /**
   * Return a Cursor positioned at the note that matches the given rowId
   * 
   * @param rowId
   *          id of note to retrieve
   * @return Cursor positioned to matching note, if found
   * @throws SQLException
   *           if note could not be found/retrieved
   */
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

  /**
   * Update the note using the details provided. The note to be updated is
   * specified using the rowId, and it is altered to use the title and body
   * values passed in
   * 
   * @param rowId
   *          id of note to update
   * @param title
   *          value to set note title to
   * @param body
   *          value to set note body to
   * @return true if the note was successfully updated, false otherwise
   */
  // public boolean updateNote(long rowId, String title, String body)
  // {
  // ContentValues args = new ContentValues();
  // args.put(KEY_TITLE, title);
  // args.put(KEY_BODY, body);
  //
  // return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
  // }
}
