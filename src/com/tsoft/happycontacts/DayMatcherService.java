/**
 * 
 */
package com.tsoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.Contacts.People;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;
import com.tsoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 *
 */
public class DayMatcherService
    extends Service
{
  @Override
  public void onCreate()
  {
    if (Log.DEBUG)
    {
      Log.v("DayMatcher: start onCreate()");
    }

    /*
     * Look for names matching today date
     */
    ContactFeast contactFeastToday = DayMatcherService.testDayMatch(getApplicationContext());

    if (!contactFeastToday.getContactList().isEmpty())
    {
      /* lancer les notify event */
      Notifier.notifyEvent(getApplicationContext());
    }

    if (Log.DEBUG)
    {
      Log.v("DayMatcher: end onCreate()");
    }
    stopSelf();
  }

  /**
   * search for today date
   * @param context
   * @return
   */
  public static ContactFeast testDayMatch(Context context)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
    Date date = new Date();
    String day = dateFormat.format(date);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    String year = yearFormat.format(date);
    return testDayMatch(context, day, year);
  }

  /**
   * search for defined date
   * @param context
   * @param day
   * @param year
   * @return
   */
  public static ContactFeast testDayMatch(Context context, String day, String year)
  {
    String[] projection = new String[] { People._ID, People.NAME, People.DISPLAY_NAME };
    if (Log.DEBUG)
    {
      Log.v("DayMatcher: start testDayMatch()");
    }

    /*
     * init and open database
     */
    DbAdapter mDb = new DbAdapter(context);
    mDb.open();

    /*
     * Look for names matching today date
     */
    ContactFeast contactFeastToday = new ContactFeast();

    Cursor c = mDb.fetchNamesForDay(day, year);
    if (c.getCount() == 0)
    {
      if (Log.DEBUG)
      {
        Log.v("DayMatcher: day " + day + " no feast found");
      }
      mDb.close();
      return contactFeastToday;
    }
    if (Log.DEBUG)
    {
      Log.v("DayMatcher: found " + c.getCount() + " feast(s) for today");
    }

    ArrayList<String> names = new ArrayList<String>();
    do
    {
      String name = c.getString(c.getColumnIndexOrThrow(HappyContactsDb.Feast.NAME));
      names.add(name.toUpperCase());
    }
    while (c.moveToNext());
    c.close();

    if (Log.DEBUG)
    {
      Log.v("DayMatcher: day " + day + " feast : " + names.toString());
    }
    /*
     * now we have to scan contacts
     */
    c = context.getContentResolver().query(People.CONTENT_URI, projection, null, null,
        People.NAME + " ASC");
    if (c != null)
    {
      while (c.moveToNext())
      {
        String contactName = c.getString(c.getColumnIndexOrThrow(People.NAME));
        for (String subName : contactName.split(" "))
        {
          if (names.contains(subName.toUpperCase()))
          {
            /* find one !! */
            Long contactId = c.getLong(c.getColumnIndexOrThrow(People._ID));
            contactFeastToday.addContact(contactId, contactName);
            if (Log.DEBUG)
            {
              Log.v("DayMatcher: day contact feast found for " + contactName);
            }
          }
        }
        c.close();
      }
    }
    if (Log.DEBUG)
    {
      if (contactFeastToday.getContactList().isEmpty())
      {
        Log.v("DayMatcher: no matching contact found");
      }
    }
    mDb.close();
    if (Log.DEBUG)
    {
      Log.v("DayMatcher: end testDayMatch()");
    }
    return contactFeastToday;
  }

  /**
   * @see android.app.Service#onBind(android.content.Intent)
   */
  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }
}
