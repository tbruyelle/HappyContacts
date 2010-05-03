/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;

import com.kamosoft.happycontacts.Log;

@SuppressWarnings( "deprecation" )
public class ContactProxy
    implements IContactProxy
{
    protected Cursor doQuery( Context context )
    {
        if ( context == null )
        {
            return null;
        }

        Log.d( "ContactProxy: Querying database for contacts.." );

        return context.getContentResolver().query( People.CONTENT_URI,
                                                   new String[] { People._ID, People.NAME, People.PRIMARY_PHONE_ID },
                                                   null, null, null );
    }

    public Uri getContentUri()
    {
        return People.CONTENT_URI;
    }

    protected PhoneContact createFromCursor( Cursor cursor )
    {
        if ( cursor == null || cursor.isClosed() )
        {
            return null;
        }

        Long id = cursor.getLong( cursor.getColumnIndex( People._ID ) );
        String name = cursor.getString( cursor.getColumnIndex( People.NAME ) );
        if ( name == null || name.length() == 0 )
        {
            return null;
        }
        return new PhoneContact( id, name );
    }

    public ArrayList<PhoneContact> loadPhoneContacts( Context context )
    {
        ArrayList<PhoneContact> phoneContacts = new ArrayList<PhoneContact>();
        Cursor cursor = doQuery( context );
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

}
