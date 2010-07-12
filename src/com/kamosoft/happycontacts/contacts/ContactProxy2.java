/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.kamosoft.happycontacts.R;

public class ContactProxy2
    extends AbstractContactProxy
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
     * @see com.kamosoft.happycontacts.contacts.ContactProxy#loadContactPhoto(android.content.Context,
     *      java.lang.Long)
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

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getContactEmails(android.content.Context,
     *      java.lang.Long)
     */
    @Override
    public ArrayList<String> getContactEmails( Context context, Long contactId )
    {
        ArrayList<String> emails = new ArrayList<String>();

        /* have to retrieve emails */
        Cursor c =
            context.getContentResolver().query( ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId,
                                                null, null );
        while ( c.moveToNext() )
        {
            String data = c.getString( c.getColumnIndex( ContactsContract.CommonDataKinds.Email.DATA ) );
            // int kind = c.getInt( c.getColumnIndex(
            // ContactsContract.CommonDataKinds.Email.KIND ) );
            //
            // if ( kind == Contacts.KIND_EMAIL )
            // {
            emails.add( data );
            // }
        }

        c.close();
        return emails;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getContactPhones(android.content.Context,
     *      java.lang.Long)
     */
    @Override
    public ArrayList<String> getContactPhones( Context context, Long contactId )
    {
        ArrayList<String> phones = new ArrayList<String>();
        Cursor c =
            context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                                                null, null );
        while ( c.moveToNext() )
        {
            String phone = c.getString( c.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ) );
            phones.add( phone );
        }

        c.close();
        return phones;
    }

}
