/**
 * 
 */
package com.tsoft.happycontacts.alarm;

import com.tsoft.happycontacts.DayMatcherService;
import com.tsoft.happycontacts.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Launch contact search for current date.
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
     * direct call a service to minimize time
     */
    Intent startDayMatcher = new Intent(context, DayMatcherService.class);
    context.startService(startDayMatcher);

    if (Log.DEBUG)
    {
      Log.v("AlarmReceiver: stop onReceive()");
    }
  }
}
