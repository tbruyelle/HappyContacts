/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.PickContactsListActivity;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @since 10 mai 2010
 * @version $Id$
 */
public class SocialUserDialog
    extends Dialog
    implements Constants, android.view.View.OnClickListener
{
    private FacebookActivity mFacebookActivity;

    private SocialNetworkUser mUser;

    private DbAdapter mDb;

    public SocialUserDialog( FacebookActivity facebookActivity, SocialNetworkUser user, DbAdapter db )
    {
        super( facebookActivity );
        mFacebookActivity = facebookActivity;
        mUser = user;
        mDb = db;
        setContentView( R.layout.socialuserdialog );
        setTitle( R.string.socialuserdialog_title );
        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = getWindow().getAttributes().height;
        getWindow().setAttributes( lp );

        TextView userNameText = (TextView) findViewById( R.id.user_name );
        userNameText.setText( user.name );

        //        TextView contactNameText = (TextView) findViewById( R.id.contact_name );
        //        contactNameText.setVisibility( View.VISIBLE );
        //        contactNameText.setText( user.getContactName() );

        TextView birthdayText = (TextView) findViewById( R.id.birthday_date );
        ImageView iconSync = (ImageView) findViewById( R.id.icon_sync );
        if ( user.birthday == null )
        {
            iconSync.setImageResource( R.drawable.sync_ko );
            birthdayText.setText( R.string.unknow_birthday );
        }
        else
        {
            birthdayText.setText( user.birthday );

            if ( user.getContactName() != null )
            {
                iconSync.setImageResource( R.drawable.sync_ok );
                TextView linkedTo = (TextView) findViewById( R.id.linked_to );
                linkedTo.setVisibility( View.VISIBLE );
                linkedTo.setText( facebookActivity.getString( R.string.linked_to, user.getContactName() ) );
            }
            else
            {
                iconSync.setImageResource( R.drawable.sync_not_found );
            }
        }

        Button linkContact = (Button) findViewById( R.id.link_contact );
        linkContact.setOnClickListener( this );
        Button unlinkContact = (Button) findViewById( R.id.unlink_contact );
        if ( user.getContactName() != null )
        {
            /* contact linked */
            unlinkContact.setOnClickListener( this );
        }
        else
        {
            /* contact not linked, its not possible to unlink */
            unlinkContact.setEnabled( false );
        }
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick( View view )
    {
        switch ( view.getId() )
        {
            case R.id.link_contact:
                Intent intent = new Intent( mFacebookActivity, PickContactsListActivity.class );
                intent.putExtra( NEXT_ACTIVITY_INTENT_KEY, FacebookActivity.class );
                String label = mFacebookActivity.getString( R.string.pick_contact_to_link, mUser.name );
                intent.putExtra( PICK_CONTACT_LABEL_INTENT_KEY, label );
                intent.putExtra( CALLED_FOR_RESULT_INTENT_KEY, true );
                mFacebookActivity.startActivityForResult( intent, FacebookActivity.PICK_CONTACT_ACTIVITY_RESULT );
                dismiss();
                return;

            case R.id.unlink_contact:
                mUser.setContactId( null );
                mUser.setContactName( null );
                mDb.updateSyncResult( mUser );
                mFacebookActivity.fillList();
                dismiss();
                return;
        }

    }

}
