/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmController
{
  public static void startAlarm(Context context)
  {
    if (Log.DEBUG)
    {
      Log.v("AlarmSetter: start startAlarm()");
    }

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    /*
     * alarme toutes les 24h
     * TODO: gérer l'heure à laquelle le service doit etre lancé.
     */
    //    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (5 * 1000),
    //    /*(24 * 60 * 60 * 1000)*/3000, pendingIntent);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 30);
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent(context,
        0));

    if (Log.DEBUG)
    {
      Log.v("AlarmSetter: end startAlarm()");
    }
  }

  /**
   * Cancel the alarm
   * @param context
   */
  public static void cancelAlarm(Context context)
  {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(getPendingIntent(context, 0));
    PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT);
    pendingIntent.cancel();
  }

  /**
   * @param happyContactsPreferences
   * @return true if Alarm is up
   */
  public static boolean isAlarmUp(Context context)
  {
    PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_NO_CREATE);
    return (pendingIntent != null);
  }

  /**
   * Retrieve the PendingIntent that will start AlarmReceiver.
   * @param context
   * @param flag
   * @return
   */
  private static PendingIntent getPendingIntent(Context context, int flag)
  {
    return PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), flag);
  }
}
