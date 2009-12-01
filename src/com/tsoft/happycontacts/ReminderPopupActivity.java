/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 * 
 */
public class ReminderPopupActivity
    extends Activity
{
  private DbAdapter mDb;

  private Iterator<Map.Entry<Long, String>> contacts;

  private boolean keepNotif = false;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: start onCreate()");
    }
    setContentView(R.layout.reminder);

    // Have the system blur any windows behind this one.
    //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
    //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

    mDb = new DbAdapter(this);
    mDb.open();

    /* boucle sur les contacts a qui il fait souhaiter la fete */
    ContactFeast contactFeast = DayMatcherService.testDayMatch(this);
    /* boucle sur les contacts a qui il fait souhaiter la fete */
    contacts = contactFeast.getContactList().entrySet().iterator();

    nextOrExit();
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: end onCreate()");
    }
  }

  /**
   * Boucle sur les contacts a afficher
   */
  private void nextOrExit()
  {
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: nextOrExit ?");
    }
    if (!contacts.hasNext())
    {
      // plus de contact a traiter on sort
      exit();
      return;
    }
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: next");
    }
    Map.Entry<Long, String> contact = contacts.next();
    if (contact == null)
    {
      // plus de contact a traiter on sort
      exit();
      return;
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
    setTitle(getString(R.string.happyfeast, contactName));

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
        startActivity(intent);
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
        Toast.makeText(ReminderPopupActivity.this, R.string.toast_later, Toast.LENGTH_SHORT).show();
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
          Log.e("Error insertBlackList " + res);
        }
        Toast.makeText(ReminderPopupActivity.this,
            getString(R.string.toast_blacklisted, contactName), Toast.LENGTH_SHORT).show();
        nextOrExit();
      }
    });
  }

  private void exit()
  {
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: start exit");
    }
    if (!keepNotif)
    {
      NotificationManager nm = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
      nm.cancel(R.string.app_name);
    }
    if (mDb != null)
    {
      mDb.close();
    }
    finish();
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: end exit");
    }
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    if (Log.DEBUG)
    {
      Log.v("ReminderPopup: start onStop");
    }
    if (mDb != null)
    {
      mDb.close();
    }
    if (Log.DEBUG)
    {
      Log.v("ReminderPopup: end onStop");
    }
  }
}
