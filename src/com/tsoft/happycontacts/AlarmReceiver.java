/**
 * 
 */
package com.tsoft.happycontacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tsoft.happycontacts.model.ContactFeast;

/**
 * Lance la recherche dans les contacts par rapport a la date du jour
 * @author tom
 *
 */
public class AlarmReceiver
    extends BroadcastReceiver
{
  /**
   * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
   */
  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (Log.DEBUG)
    {
      Log.v("AlarmReceiver: start onReceive()");
    }

    /*
     * Look for names matching today date
     */
    ContactFeast contactFeastToday = DayMatcher.testDayMatch(context);

    if (!contactFeastToday.getContactList().isEmpty())
    {
      /* lancer les notify event */
      Notifier.notifyEvent(context);
    }
    else
    {
      if (Log.DEBUG)
      {
        Log.v("AlarmReceiver: no feast contact today");
      }
    }

    if (Log.DEBUG)
    {
      Log.v("AlarmReceiver: stop onReceive()");
    }
  }
}
