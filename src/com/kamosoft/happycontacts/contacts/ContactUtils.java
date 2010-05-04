/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

public class ContactUtils
{
    private static IContactProxy singleInstance;

    private static IContactProxy getInstance()
    {
        if ( singleInstance == null )
        {
            singleInstance = ContactProxyFactory.create();
        }
        return singleInstance;
    }

    public static ArrayList<String> getContactPhones( Context context, Long contactId )
    {
        return getInstance().getContactPhones( context, contactId );
    }

    public static ArrayList<String> getContactEmails( Context context, Long contactId )
    {
        return getInstance().getContactEmails( context, contactId );
    }

    public static Bitmap loadContactPhoto( Context context, Long contactId )
    {
        return getInstance().loadContactPhoto( context, contactId );
    }

    public static Uri getContentUri()
    {
        return getInstance().getContentUri();
    }

    public static ArrayList<PhoneContact> loadPhoneContacts( Context context )
    {
        return getInstance().loadPhoneContacts( context );
    }

    public static Cursor doQuery( Context context, String where )
    {
        return getInstance().doQuery( context, where );
    }

    public static String getIdColumn()
    {
        return getInstance().getIdColumn();
    }

    public static String getNameColumn()
    {
        return getInstance().getNameColumn();
    }
}
