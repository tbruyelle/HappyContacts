/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.kamosoft.happycontacts.R;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 20 mai 2010
 * @version $Id$
 */
public class ConfirmStoreDialog
    extends AlertDialog
    implements DialogInterface.OnClickListener
{
    private FacebookActivity mFacebookActivity;

    /**
     * @param context
     */
    public ConfirmStoreDialog( FacebookActivity facebookActivity )
    {
        super( facebookActivity );

        mFacebookActivity = facebookActivity;
        setMessage( mFacebookActivity.getString( R.string.confirm_store_question ) );

        setButton( BUTTON_POSITIVE, mFacebookActivity.getString( R.string.yes ), this );
        setButton( BUTTON_NEUTRAL, mFacebookActivity.getString( R.string.no ), this );
        setButton( BUTTON_NEGATIVE, mFacebookActivity.getString( R.string.cancel ), this );
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    @Override
    public void onClick( DialogInterface dialog, int which )
    {
        switch ( which )
        {
            case BUTTON_POSITIVE:
                mFacebookActivity.store( true );
                this.dismiss();
                return;

            case BUTTON_NEUTRAL:
                mFacebookActivity.store( false );
                this.dismiss();
                return;

            case BUTTON_NEGATIVE:
                this.dismiss();
                return;
        }

    }

}
