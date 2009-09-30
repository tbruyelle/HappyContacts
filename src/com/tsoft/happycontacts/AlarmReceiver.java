/**
 * 
 */
package com.tsoft.happycontacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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
    Toast.makeText(context, "Yeah alarm received", Toast.LENGTH_SHORT).show();
    if (Log.DEBUG)
    {
      Log.v("AlarmReceiver: start onReceive()");
    }
  }

}
