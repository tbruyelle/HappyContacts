/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;

public class ContactUtils
{
    public static Uri getContentUri()
    {
        return ContactProxyFactory.create().getContentUri();
    }

    public static ArrayList<PhoneContact> loadPhoneContacts( Context context )
    {
        return ContactProxyFactory.create().loadPhoneContacts( context );
    }
}
