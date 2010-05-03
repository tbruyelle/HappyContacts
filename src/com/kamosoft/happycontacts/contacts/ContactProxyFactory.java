/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

import com.kamosoft.utils.AndroidUtils;

public class ContactProxyFactory
{
    public static IContactProxy create()
    {
        if ( AndroidUtils.determineOsVersion() >= 5 )
        {
            return new ContactProxy2();
        }
        return new ContactProxy();
    }
}
