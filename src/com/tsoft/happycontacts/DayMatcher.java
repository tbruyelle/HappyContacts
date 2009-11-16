/**
 * 
 */
package com.tsoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts.People;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;
import com.tsoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 *
 */
public class DayMatcher
{
  public static ContactFeast testDayMatch(Context context)
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
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
    Date date = new Date();
    String day = dateFormat.format(date);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    String year = yearFormat.format(date);
    Cursor c = mDb.fetchNamesForDay(day, year);
    List<String> names = new ArrayList<String>();
    do
    {
      String name = c.getString(c.getColumnIndexOrThrow(HappyContactsDb.Feast.NAME));
      names.add(name.toUpperCase());
    }
    while (c.moveToNext());

    if (Log.DEBUG)
    {
      Log.v("DayMatcher: today " + day + " feast : " + names.toString());
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
              Log.v("DayMatcher: today contact feast found for " + contactName);
            }
          }
        }
      }
    }

    if (Log.DEBUG)
    {
      Log.v("DayMatcher: end testDayMatch()");
    }
    return contactFeastToday;
  }

}
