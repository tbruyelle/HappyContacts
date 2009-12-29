/**
 * 
 */
package com.tsoft.happycontacts;

import com.tsoft.happycontacts.alarm.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Launch service {@link AlarmSetterService}
 * @author tom
 * 
 */
public class OnInstallReceiver
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
      Log.v("OnInstallReceiver: start onReceive()");
    }
    AlarmController.startAlarm(context);
    if (Log.DEBUG)
    {
      Log.v("OnInstallReceiver: end onReceive()");
    }
  }

}
