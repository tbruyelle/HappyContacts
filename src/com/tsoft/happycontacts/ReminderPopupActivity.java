/**
 * 
 */
package com.tsoft.happycontacts;

import java.util.Calendar;
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
import com.tsoft.happycontacts.model.ContactFeasts;

/**
 * @author tom
 * FIXME this activity musn't appear in recent task display
 */
public class ReminderPopupActivity
    extends Activity
{
  private DbAdapter mDb;

  private Iterator<Map.Entry<Long, ContactFeast>> contacts;

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
    ContactFeasts contactFeasts = DayMatcherService.testDayMatch(this);
    /* boucle sur les contacts a qui il fait souhaiter la fete */
    contacts = contactFeasts.getContactList().entrySet().iterator();

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
    Map.Entry<Long, ContactFeast> contact = contacts.next();
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

  private void setContentForContact(final Long contactId, final ContactFeast contactFeast)
  {
    setTitle(getString(R.string.happyfeast, contactFeast.getContactName()));

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
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        mDb.updateContactFeast(contactId.longValue(), contactFeast.getContactName(), year);
        startActivityForResult(intent, 0);
      }
    });

    Button laterButton = (Button) findViewById(R.id.later_button);
    laterButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // on laisse la notification en place si clique sur later
        Toast.makeText(ReminderPopupActivity.this, R.string.toast_later, Toast.LENGTH_LONG).show();
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
        long res = mDb.updateContactFeast(contactId.longValue(), contactFeast.getContactName(),
            null);
        if (res < 1)
        {
          Log.e("Error insertBlackList " + res);
        }
        Toast.makeText(ReminderPopupActivity.this,
            getString(R.string.toast_blacklisted, contactFeast.getContactName()),
            Toast.LENGTH_SHORT).show();
        nextOrExit();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: onActivityResult start");
    }
    super.onActivityResult(requestCode, resultCode, data);
    nextOrExit();
  }

  private void exit()
  {
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: start exit");
    }
    Toast.makeText(ReminderPopupActivity.this, R.string.toast_finish, Toast.LENGTH_LONG).show();
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
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: start onStop");
    }
    if (mDb != null)
    {
      mDb.close();
    }
    if (Log.DEBUG)
    {
      Log.v("ReminderPopupActivity: end onStop");
    }
  }
}
