/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Iterator;
import java.util.Map;

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
import com.tsoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 * 
 */
public class ReminderPopup
    extends Dialog
{
  private String TAG = getClass().getSimpleName();
  private DbAdapter mDb;
  private Context mContext;
  private Iterator<Map.Entry<Long, String>> contacts;
  private boolean keepNotif = false;

  /**
   * @param context
   */
  public ReminderPopup(final Context context, final ContactFeast contactFeast)
  {
    super(context);
    mContext = context;
    setContentView(R.layout.reminder);

    // Have the system blur any windows behind this one.
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

    mDb = new DbAdapter(context);
    mDb.open();

    /* boucle sur les contacts a qui il fait souhaiter la fete */
    contacts = contactFeast.getContactList().entrySet().iterator();

    nextOrExit();
  }

  /**
   * 
   */
  private void nextOrExit()
  {
    Map.Entry<Long, String> contact = contacts.next();
    if (contact == null)
    {
      // plus de contact a traiter on sort
      exit();
    }
    setContentForContact(contact.getKey(), contact.getValue());

    Button exitButton = (Button) findViewById(R.id.nottoday_button);
    exitButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        nextOrExit();
      }
    });
  }

  private void setContentForContact(final Long contactId, final String contactName)
  {
    setTitle(mContext.getString(R.string.happyfeast, contactName));

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
        //FIXME mDb.updateContactFeast(feastId, year)
        mContext.startActivity(intent);
        nextOrExit();
      }
    });

    Button laterButton = (Button) findViewById(R.id.later_button);
    laterButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // on laisse la notification en place si clique sur later
        Toast.makeText(mContext, R.string.toast_later, Toast.LENGTH_SHORT).show();
        keepNotif = true;
        nextOrExit();
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
        Toast.makeText(mContext, mContext.getString(R.string.toast_blacklisted, contactName),
            Toast.LENGTH_SHORT).show();
        exit();
      }
    });
  }

  private void exit()
  {
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
