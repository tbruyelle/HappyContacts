/**
 * 
 */
package com.tsoft.happycontacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tsoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 * 
 */
public class ReminderPopupActivity
    extends Activity
{
  private ContactFeast contactFeast;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    contactFeast = DayMatcher.testDayMatch(getApplicationContext());
    showDialog(0);
  }

  @Override
  protected Dialog onCreateDialog(int id)
  {
    ReminderPopup reminderDialog = new ReminderPopup(this, contactFeast);
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
