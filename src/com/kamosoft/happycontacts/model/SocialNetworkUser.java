/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.model;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 21 avr. 2010
 * @version $Id$
 */
public final class SocialNetworkUser
{

    public String uid;

    public String lastName;

    public String firstName;

    public String name;

    public String picUrl;

    public String birthday;

    private Long contactId;

    private String contactName;

    /**
     * @param contactId the contactId to set
     */
    public void setContactId( Long contactId )
    {
        this.contactId = contactId;
    }

    /**
     * @return the contactId
     */
    public Long getContactId()
    {
        return contactId;
    }

    /**
     * @param contactName the contactName to set
     */
    public void setContactName( String contactName )
    {
        this.contactName = contactName;
    }

    /**
     * @return the contactName
     */
    public String getContactName()
    {
        return contactName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name + " - " + birthday + " - " + contactName;
    }

}
