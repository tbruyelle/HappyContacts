/**
 * 
 */
package com.tsoft.happycontacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts.People;

/**
 * @author tom
 * 
 */
public class ReminderPopupActivity
    extends Activity
{
  private Long contactId;
  private String contactName;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    contactName = intent.getExtras().getString(People.NAME);
    contactId = intent.getExtras().getLong(People._ID);
    showDialog(0);
  }

  @Override
  protected Dialog onCreateDialog(int id)
  {
    ReminderPopup reminderDialog = new ReminderPopup(this, contactId, contactName);
    reminderDialog.show();
    reminderDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
    {
      @Override
      public void onDismiss(DialogInterface dialog)
      {
        ReminderPopupActivity.this.finish();
      }
    });
    return reminderDialog;
  }

}
