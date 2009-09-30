/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

/**
 * Met en place l'alarme toutes les 24h 
 * Vérifie déjà si aujourd'hui un des contacts match
 * 
 * @author tom
 * 
 */
public class AlarmSetterService
    extends Service
{

  public static void startAlarm(Context context)
  {
    Intent intent = new Intent(context, AlarmSetterService.class);
    context.startService(intent);
  }

  public static void cancelAlarm(Context context)
  {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    alarmManager.cancel(AlarmSetterService.getPendingIntent(context, 0));
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

  private static PendingIntent getPendingIntent(Context context, int flag)
  {
    return PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), flag);
  }

  @Override
  public void onStart(Intent intent, int startId)
  {
    if (Log.DEBUG)
    {
      Log.v("AlarmSetterService: start onStart()");
    }

    PendingIntent pendingIntent = getPendingIntent(this, 0);

    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    /*
     * alarme toutes les 24h
     * TODO: gérer l'heure à laquelle le service doit etre lancé.
     */
    //    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (5 * 1000),
    //    /*(24 * 60 * 60 * 1000)*/3000, pendingIntent);
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.SECOND, 30);
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    if (Log.DEBUG)
    {
      Log.v("AlarmSetterService: end onStart()");
    }
  }

  @Override
  public void onDestroy()
  {
    if (Log.DEBUG)
    {
      Log.v("AlarmSetterService: start onDestroy()");
    }
    super.onDestroy();
    if (Log.DEBUG)
    {
      Log.v("AlarmSetterService: end onDestroy()");
    }
  }

  /**
   * @see android.app.Service#onBind(android.content.Intent)
   */
  @Override
  public IBinder onBind(Intent arg0)
  {
    if (Log.DEBUG)
    {
      Log.v("AlarmSetterService: onBind()");
    }
    return null;
  }

}
