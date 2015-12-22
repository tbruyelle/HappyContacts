/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.contacts;

/**
 * @since 3 mai 2010
 * @version $Id$
 */
public final class PhoneContact
    implements Comparable<PhoneContact>
{

    public PhoneContact( Long id, String name )
    {
        this.id = id;
        this.name = name;
    }

    public Long id;

    public String name;

    public int compareTo( PhoneContact another )
    {
        return name.compareTo( another.name );
    }
}
