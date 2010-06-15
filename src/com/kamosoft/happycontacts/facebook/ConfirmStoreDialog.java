/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.sync.SyncStorer;

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
    private SyncStorer mSyncStorer;

    /**
     * @param context
     */
    public ConfirmStoreDialog( Context context, SyncStorer syncStorer )
    {
        super( context );

        mSyncStorer = syncStorer;
        setMessage( context.getString( R.string.confirm_store_question ) );

        setButton( BUTTON_POSITIVE, context.getString( R.string.yes ), this );
        setButton( BUTTON_NEUTRAL, context.getString( R.string.no ), this );
        setButton( BUTTON_NEGATIVE, context.getString( R.string.cancel ), this );
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    public void onClick( DialogInterface dialog, int which )
    {
        switch ( which )
        {
            case BUTTON_POSITIVE:
                mSyncStorer.store( true );
                this.dismiss();
                return;

            case BUTTON_NEUTRAL:
                mSyncStorer.store( false );
                this.dismiss();
                return;

            case BUTTON_NEGATIVE:
                this.dismiss();
                return;
        }

    }

}
