/**
 * 
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * @author tom
 */
public class ReminderPopupActivity
    extends Activity
{
    private DbAdapter mDb;

    private Iterator<Map.Entry<Long, ContactFeast>> contacts;

    private boolean keepNotif = false;

    private String mDate;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: start onCreate()" );
        }
        setContentView( R.layout.reminder );

        // Have the system blur any windows behind this one.
        //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        mDb = new DbAdapter( this );

        /* boucle sur les contacts a qui il fait souhaiter la fete */
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM" );
        Date date = new Date();
        String day = dateFormat.format( date );
        SimpleDateFormat fullDateFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        mDate = fullDateFormat.format( date );

        ContactFeasts contactFeasts = DayMatcherService.testDayMatch( this, day, mDate );
        /* boucle sur les contacts a qui il fait souhaiter la fete */
        contacts = contactFeasts.getContactList().entrySet().iterator();

        nextOrExit();
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: end onCreate()" );
        }
    }

    /**
     * Boucle sur les contacts a afficher
     */
    private void nextOrExit()
    {
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: nextOrExit ?" );
        }
        if ( !contacts.hasNext() )
        {
            // plus de contact a traiter on sort
            exit();
            return;
        }
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: next" );
        }
        Map.Entry<Long, ContactFeast> contact = contacts.next();
        if ( contact == null )
        {
            // plus de contact a traiter on sort
            exit();
            return;
        }
        setContentForContact( contact.getKey(), contact.getValue() );
    }

    private void setContentForContact( final Long contactId, final ContactFeast contactFeast )
    {
        setTitle( R.string.happyfeast );

        TextView contactNameTextView = (TextView) findViewById( R.id.contact_name );
        contactNameTextView.setText( contactFeast.getContactName() );

        Uri contactUri = ContentUris.withAppendedId( People.CONTENT_URI, contactId );
        Bitmap photo = People.loadContactPhoto( this, contactUri, R.drawable.smile, null );
        ImageView imageView = (ImageView) findViewById( R.id.contact_photo );
        imageView.setBackgroundResource( android.R.drawable.picture_frame );
        imageView.setImageBitmap( photo );

        Button callButton = (Button) findViewById( R.id.call_button );
        callButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                // Afficher les données du contacts
                boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), mDate );
                if ( !res )
                {
                    Log.e( "Error insertBlackList with year " + mDate );
                }

                Uri displayContactUri = ContentUris.withAppendedId( People.CONTENT_URI, contactId.intValue() );
                Intent intent = new Intent( Intent.ACTION_VIEW, displayContactUri );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
                nextOrExit();
            }
        } );

        Button laterButton = (Button) findViewById( R.id.later_button );
        laterButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                // on laisse la notification en place si clique sur later
                Toast.makeText( ReminderPopupActivity.this, R.string.toast_later, Toast.LENGTH_LONG ).show();
                keepNotif = true;
                nextOrExit();
            }
        } );

        Button neverButton = (Button) findViewById( R.id.never_button );
        neverButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), null );
                if ( !res )
                {
                    Log.e( "Error insertBlackList" );
                }
                else
                {
                    Toast.makeText( ReminderPopupActivity.this,
                                    getString( R.string.toast_blacklisted, contactFeast.getContactName() ),
                                    Toast.LENGTH_LONG ).show();
                }
                nextOrExit();
            }
        } );

        Button exitButton = (Button) findViewById( R.id.nottoday_button );
        exitButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                boolean res = mDb.updateContactFeast( contactId.longValue(), contactFeast.getContactName(), mDate );
                nextOrExit();
                if ( !res )
                {
                    Log.e( "Error insertBlackList with year " + mDate );
                }
            }
        } );
    }

    //  @Override
    //  protected void onActivityResult(int requestCode, int resultCode, Intent data)
    //  {
    //    if (Log.DEBUG)
    //    {
    //      Log.v("ReminderPopupActivity: onActivityResult start");
    //    }
    //    super.onActivityResult(requestCode, resultCode, data);
    //    nextOrExit();
    //  }

    private void exit()
    {
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: start exit" );
        }
        if ( !keepNotif )
        {
            NotificationManager nm = (NotificationManager) getSystemService( Activity.NOTIFICATION_SERVICE );
            nm.cancel( R.string.app_name );
            Toast.makeText( ReminderPopupActivity.this, R.string.toast_finish, Toast.LENGTH_LONG ).show();
        }
        finish();
    }

    @Override
    protected void onPause()
    {
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: onPause()" );
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: onResume()" );
        }
        super.onResume();
        mDb.open( false );
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: start onStop" );
        }
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "ReminderPopupActivity: end onStop" );
        }
    }
}
