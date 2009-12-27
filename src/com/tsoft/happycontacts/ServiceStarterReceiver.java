/**
 * 
 */
package com.tsoft.happycontacts;

import com.tsoft.happycontacts.alarm.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Lance le service {@link AlarmSetterService}
 * @author tom
 * 
 */
public class ServiceStarterReceiver
    extends BroadcastReceiver
{

  /**
   * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
   *      android.content.Intent)
   */
  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (Log.DEBUG)
    {
      Log.v("ServiceStarterReceiver: start onReceive()");
    }
    AlarmController.startAlarm(context);
    if (Log.DEBUG)
    {
      Log.v("ServiceStarterReceiver: end onReceive()");
    }
  }

}
