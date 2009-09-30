package com.tsoft.happycontacts;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;

public class TestAppActivity
    extends ListActivity
{
  private static final int DAY_MENU_ID = Menu.FIRST;

  private String[] projection = new String[] { People._ID, People.NAME, People.DISPLAY_NAME };

  private DbAdapter mDb;

  private Cursor c;

  private final Calendar calendar = Calendar.getInstance();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.testapp);
    mDb = new DbAdapter(this);
    mDb.open();
    fillList();
  }

  private void fillList()
  {
    c = managedQuery(People.CONTENT_URI, projection, null, null, People.NAME + " ASC");
    startManagingCursor(c);
    String[] from = new String[] { People.NAME };
    int[] to = new int[] { android.R.id.text1 };
    SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
        android.R.layout.simple_list_item_2, c, from, to);
    setListAdapter(simpleCursorAdapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id)
  {
    super.onListItemClick(l, v, position, id);
    Long contactId = c.getLong(c.getColumnIndexOrThrow(People._ID));
    if (mDb.isBlackListed(contactId))
    {
      Toast.makeText(this, R.string.toast_contact_blacklisted, Toast.LENGTH_SHORT).show();
      return;
    }
    String contactName = c.getString(c.getColumnIndexOrThrow(People.NAME));
    notifyEvent(contactId, contactName);
    // new ReminderDialog(this, contactId, contactName).show();
  }

  /**
   * @param contactId
   * @param contactName
   */
  private void notifyEvent(Long contactId, String contactName)
  {
    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    Intent intent = new Intent(this, ReminderPopupActivity.class);
    intent.putExtra(People.NAME, contactName);
    intent.putExtra(People._ID, contactId);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
        Intent.FLAG_ACTIVITY_NEW_TASK);

    // The ticker text, this uses a formatted string so our message could be
    // localized
    String tickerText = getString(R.string.notif_new_event, contactName);

    // construct the Notification object.
    Notification notif = new Notification(R.drawable.smile, tickerText, System.currentTimeMillis());

    // Set the info for the views that show in the notification panel.
    notif.setLatestEventInfo(this, getText(R.string.app_name), tickerText, contentIntent);

    // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
    // then vibrate for 500ms.
    // notif.vibrate = new long[] { 100, 250, 100, 500};

    // Note that we use R.layout.incoming_message_panel as the ID for
    // the notification. It could be any integer you want, but we use
    // the convention of using a resource id for a string related to
    // the notification. It will always be a unique number within your
    // application.
    nm.notify(R.string.app_name, notif);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    menu.add(0, DAY_MENU_ID, 0, R.string.enter_date);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item)
  {
    switch (item.getItemId())
    {
    case DAY_MENU_ID:
      displayDateForm();
      return true;
    }

    return super.onMenuItemSelected(featureId, item);
  }

  /**
   * 
   */
  private void displayDateForm()
  {

    new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
    {
      @Override
      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
      {
        // TODO faire la bonne conversion
        Cursor c = mDb.fetchNamesForDay("01/01");
        do
        {
          String contactName = c.getString(c.getColumnIndexOrThrow(HappyContactsDb.Feast.NAME));
          Toast.makeText(TestAppActivity.this, contactName, Toast.LENGTH_SHORT).show();
        }
        while (c.moveToNext());
      }
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
        .get(Calendar.DAY_OF_MONTH)).show();

  }
}