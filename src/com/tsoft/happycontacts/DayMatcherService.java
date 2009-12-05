/**
 * 
 */
package com.tsoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.Contacts.People;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;
import com.tsoft.happycontacts.model.ContactFeast;
import com.tsoft.happycontacts.model.ContactFeasts;

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
    ContactFeasts contactFeastToday = DayMatcherService.testDayMatch(getApplicationContext());

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
  public static ContactFeasts testDayMatch(Context context)
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
  public static ContactFeasts testDayMatch(Context context, String day, String year)
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
    ContactFeasts contactFeastToday = new ContactFeasts(day);

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

    Map<String, ContactFeast> names = new HashMap<String, ContactFeast>();
    do
    {
      String name = c.getString(c.getColumnIndexOrThrow(HappyContactsDb.Feast.NAME));
      Long feastId = c.getLong(c.getColumnIndexOrThrow(HappyContactsDb.Feast.ID));
      ContactFeast contactFeast = new ContactFeast(name, feastId, null);
      names.put(name.toUpperCase(), contactFeast);
      if (Log.DEBUG)
      {
        Log.v("DayMatcher: day " + day + " feast : " + contactFeast.getContactName());
      }
    }
    while (c.moveToNext());
    c.close();

    /*
     * now we have to scan contacts
     */
    c = context.getContentResolver().query(People.CONTENT_URI, projection, null, null,
        People.NAME + " ASC");
    if (c != null)
    {
      while (c.moveToNext())
      {
        Long contactId = c.getLong(c.getColumnIndexOrThrow(People._ID));
        String contactName = c.getString(c.getColumnIndexOrThrow(People.NAME));

        if (mDb.isBlackListed(contactId, year))
        {
          if (Log.DEBUG)
          {
            Log.v("DayMatcher: already wished this year " + contactName + " is ignored");
          }
          continue;
        }

        for (String subName : contactName.split(" "))
        {
          String subNameUpper = subName.toUpperCase();
          if (names.containsKey(subNameUpper))
          {
            /* find one !! */
            if (Log.DEBUG)
            {
              Log.v("DayMatcher: day contact feast found for " + contactName);
            }
            ContactFeast contactFeast = names.get(subNameUpper);
            // set the real contact name
            contactFeast.setContactName(contactName);
            contactFeastToday.addContact(contactId, contactFeast);
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
