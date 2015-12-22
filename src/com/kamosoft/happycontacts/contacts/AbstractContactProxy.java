/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

/**
 * @since 12 juil. 2010
 * @version $Id$
 */
public abstract class AbstractContactProxy
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
}
