/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

public interface IContactProxy
{
    Uri getContentUri();

    Bitmap loadContactPhoto( Context context, Long contactId );

    ArrayList<PhoneContact> loadPhoneContacts( Context context );

    Cursor doQuery( Context context, String where );

    String getIdColumn();

    String getNameColumn();

    ArrayList<String> getContactPhones( Context context, Long contactId );

    ArrayList<String> getContactEmails( Context context, Long contactId );
}
