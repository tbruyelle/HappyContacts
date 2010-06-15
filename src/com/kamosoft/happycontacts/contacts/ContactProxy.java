/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

import com.kamosoft.happycontacts.R;

@SuppressWarnings( "deprecation" )
public class ContactProxy
    implements IContactProxy
{
    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#doQuery(android.content.Context, java.lang.String, java.lang.String)
     */
    public Cursor doQuery( Context context, String where )
    {
        if ( context == null )
        {
            return null;
        }
        return context.getContentResolver().query( getContentUri(), new String[] { getIdColumn(), getNameColumn() },
                                                   where, null, getNameColumn() + " ASC" );
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getContentUri()
     */
    public Uri getContentUri()
    {
        return People.CONTENT_URI;
    }

    /**
     * @param cursor
     * @return
     */
    protected PhoneContact createFromCursor( Cursor cursor )
    {
        if ( cursor == null || cursor.isClosed() )
        {
            return null;
        }

        Long id = cursor.getLong( cursor.getColumnIndex( getIdColumn() ) );
        String name = cursor.getString( cursor.getColumnIndex( getNameColumn() ) );
        if ( name == null || name.length() == 0 )
        {
            return null;
        }
        return new PhoneContact( id, name );
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#loadPhoneContacts(android.content.Context)
     */
    public ArrayList<PhoneContact> loadPhoneContacts( Context context )
    {
        ArrayList<PhoneContact> phoneContacts = new ArrayList<PhoneContact>();
        Cursor cursor = doQuery( context, null );
        if ( cursor == null )
        {
            return phoneContacts;
        }

        while ( cursor.moveToNext() )
        {
            PhoneContact contact = createFromCursor( cursor );
            if ( contact == null )
            {
                continue;
            }
            phoneContacts.add( contact );
        }

        cursor.close();
        return phoneContacts;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getIdColumn()
     */
    public String getIdColumn()
    {
        return People._ID;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getNameColumn()
     */
    public String getNameColumn()
    {
        return People.NAME;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#loadContactPhoto(android.content.Context, java.lang.Long)
     */
    public Bitmap loadContactPhoto( Context context, Long contactId )
    {
        Uri contactUri = ContentUris.withAppendedId( getContentUri(), contactId );
        return People.loadContactPhoto( context, contactUri, R.drawable.nophoto, null );
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getContactEmails(android.content.Context, java.lang.Long)
     */
    public ArrayList<String> getContactEmails( Context context, Long contactId )
    {
        ArrayList<String> emails = new ArrayList<String>();

        /* have to retrieve emails */
        Cursor c =
            context.getContentResolver().query( Contacts.ContactMethods.CONTENT_URI, null,
                                                Contacts.Phones.PERSON_ID + "=" + contactId, null, null );
        while ( c.moveToNext() )
        {
            String data = c.getString( c.getColumnIndex( Contacts.ContactMethodsColumns.DATA ) );
            int kind = c.getInt( c.getColumnIndex( Contacts.ContactMethodsColumns.KIND ) );

            if ( kind == Contacts.KIND_EMAIL )
            {
                emails.add( data );
            }
        }

        c.close();
        return emails;
    }

    /**
     * @see com.kamosoft.happycontacts.contacts.IContactProxy#getContactPhones(android.content.Context, java.lang.Long)
     */
    public ArrayList<String> getContactPhones( Context context, Long contactId )
    {
        ArrayList<String> phones = new ArrayList<String>();
        Cursor c =
            context.getContentResolver().query( Contacts.Phones.CONTENT_URI, null,
                                                Contacts.Phones.PERSON_ID + "=" + contactId, null, null );
        while ( c.moveToNext() )
        {
            String phone = c.getString( c.getColumnIndex( Contacts.PhonesColumns.NUMBER ) );
            phones.add( phone );
        }

        c.close();
        return phones;
    }
}
