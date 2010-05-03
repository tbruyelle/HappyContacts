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
    public static ArrayList<String> getContactPhones( Context context, Long contactId )
    {
        return ContactProxyFactory.create().getContactPhones( context, contactId );
    }

    public static ArrayList<String> getContactEmails( Context context, Long contactId )
    {
        return ContactProxyFactory.create().getContactEmails( context, contactId );
    }

    public static Bitmap loadContactPhoto( Context context, Long contactId )
    {
        return ContactProxyFactory.create().loadContactPhoto( context, contactId );
    }

    public static Uri getContentUri()
    {
        return ContactProxyFactory.create().getContentUri();
    }

    public static ArrayList<PhoneContact> loadPhoneContacts( Context context )
    {
        return ContactProxyFactory.create().loadPhoneContacts( context );
    }

    public static Cursor doQuery( Context context, String where )
    {
        return ContactProxyFactory.create().doQuery( context, where );
    }

    public static String getIdColumn()
    {
        return ContactProxyFactory.create().getIdColumn();
    }

    public static String getNameColumn()
    {
        return ContactProxyFactory.create().getNameColumn();
    }
}
