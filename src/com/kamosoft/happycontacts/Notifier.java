/**
 * 
 */
package com.kamosoft.happycontacts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Display notification from list of contacts.
 * @author tom
 *
 */
public class Notifier
{
    /**
     * @param contactId
     * @param contactName
     */
    public static void notifyEvent( Context context )
    {
        NotificationManager nm = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

        Intent intent = new Intent( context, ReminderPopupActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        //intent.putExtra("contactFeastToday", contactFeastToday);
        PendingIntent contentIntent = PendingIntent.getActivity( context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );

        // The ticker text, this uses a formatted string so our message could be
        // localized
        String tickerText = null;
        //    if (contactFeastToday.getContactList().size() == 1)
        //    {
        //      /* si un seul on prends le premier nom */
        //      String contactName = contactFeastToday.getContactList().entrySet().iterator().next()
        //          .getValue();
        //      tickerText = context.getString(R.string.notif_new_event, contactName);
        //    }
        //    else
        //    {
        //      tickerText = context.getString(R.string.notif_new_event_multi);
        //    }
        tickerText = context.getString( R.string.notif_one_or_more );

        // construct the Notification object.
        Notification notif = new Notification( R.drawable.notif, tickerText, System.currentTimeMillis() );

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo( context, context.getText( R.string.app_name ), tickerText, contentIntent );

        // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
        // then vibrate for 500ms.
        // notif.vibrate = new long[] { 100, 250, 100, 500};

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification. It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification. It will always be a unique number within your
        // application.
        nm.notify( R.string.app_name, notif );
    }

}
