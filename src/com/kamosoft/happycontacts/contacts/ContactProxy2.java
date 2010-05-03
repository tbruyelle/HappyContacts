/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.kamosoft.happycontacts.R;

public class ContactProxy2
    extends ContactProxy
    implements IContactProxy
{
    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getIdColumnIndex()
     */
    @Override
    public String getIdColumn()
    {
        return ContactsContract.Contacts._ID;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getNameColumnIndex()
     */
    @Override
    public String getNameColumn()
    {
        return ContactsContract.Contacts.DISPLAY_NAME;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.ContactProxy#loadContactPhoto(android.content.Context, java.lang.Long)
     */
    @Override
    public Bitmap loadContactPhoto( Context context, Long contactId )
    {
        Uri uri = ContentUris.withAppendedId( getContentUri(), contactId );

        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream( context.getContentResolver(), uri );

        if ( input == null )
        {
            return BitmapFactory.decodeResource( context.getResources(), R.drawable.nophoto );
        }

        return BitmapFactory.decodeStream( input );
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.ContactProxy#getContentUri()
     */
    @Override
    public Uri getContentUri()
    {
        return ContactsContract.Contacts.CONTENT_URI;
    }

}
