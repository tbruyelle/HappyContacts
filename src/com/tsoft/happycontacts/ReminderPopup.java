/**
 * 
 */
package com.tsoft.happycontacts;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.tsoft.happycontacts.dao.DbAdapter;

/**
 * @author tom
 * 
 */
public class ReminderPopup
    extends Dialog
{
  private String TAG = getClass().getSimpleName();
  private DbAdapter mDb;

  /**
   * @param context
   */
  public ReminderPopup(final Context context, final Long contactId, final String contactName)
  {
    super(context);
    setContentView(R.layout.reminder);

    // Have the system blur any windows behind this one.
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

    mDb = new DbAdapter(context);
    mDb.open();

    setTitle(context.getString(R.string.happyfeast, contactName));

    Button callButton = (Button) findViewById(R.id.call_button);
    callButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // Afficher les données du contacts
        Uri displayContactUri = ContentUris
            .withAppendedId(People.CONTENT_URI, contactId.intValue());
        Intent intent = new Intent(Intent.ACTION_VIEW, displayContactUri);
        context.startActivity(intent);
        exit();
      }
    });

    Button laterButton = (Button) findViewById(R.id.later_button);
    laterButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // TODO reminder!
        Toast.makeText(context, R.string.toast_later, Toast.LENGTH_SHORT).show();
        exit();
      }
    });

    Button neverButton = (Button) findViewById(R.id.never_button);
    neverButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        long res = mDb.insertBlackList(contactId.longValue(), contactName);
        if (res < 1)
        {
          Log.e(TAG, "Error insertBlackList " + res);
        }
        Toast.makeText(context, context.getString(R.string.toast_blacklisted, contactName),
            Toast.LENGTH_SHORT).show();
        exit();
      }
    });

    Button exitButton = (Button) findViewById(R.id.exit_button);
    exitButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        exit();
      }
    });
  }

  private void exit()
  {
    // FIXME ne pas faire si boutton later
    NotificationManager nm = (NotificationManager) getContext().getSystemService(
        Activity.NOTIFICATION_SERVICE);
    nm.cancel(R.string.app_name);
    // FIXME voir aussi cancel() qui fait appel onCancelListener()
    dismiss();
  }

  @Override
  protected void onStop()
  {
    if (mDb != null)
    {
      mDb.close();
    }
  }
}
