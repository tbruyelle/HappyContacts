/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;

public interface IContactProxy
{
    Uri getContentUri();

    ArrayList<PhoneContact> loadPhoneContacts( Context context );
}
