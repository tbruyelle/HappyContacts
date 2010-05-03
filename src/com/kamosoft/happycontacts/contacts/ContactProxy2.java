/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.kamosoft.happycontacts.Log;

public class ContactProxy2
    extends ContactProxy
    implements IContactProxy
{
    protected PhoneContact createFromCursor( Cursor cursor )
    {
        if ( cursor == null || cursor.isClosed() )
        {
            return null;
        }

        Long id = cursor.getLong( cursor.getColumnIndex( ContactsContract.Contacts._ID ) );
        String name = cursor.getString( cursor.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
        return new PhoneContact( id, name );
    }

    protected Cursor doQuery( Context context )
    {
        if ( context == null )
        {
            return null;
        }

        Log.d( "ContactProxy2: Querying database for contacts.." );

        return context.getContentResolver().query( ContactsContract.Contacts.CONTENT_URI, null, null, null, null );
    }

    public Uri getContentUri()
    {
        return ContactsContract.Contacts.CONTENT_URI;
    }

}
