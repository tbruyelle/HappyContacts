/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author tom
 *
 */
public class ContactFeast
    implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 5984664253829892060L;

    private String contactName;

    private Long contactId;

    private ArrayList<String> phones;

    private ArrayList<String> emails;

    private String lastWishYear;

    public ContactFeast( String contactName, String lastWishYear )
    {
        this.contactName = contactName;
        this.setLastWishYear( lastWishYear );
    }

    public boolean isContactable()
    {
        return ( hasEmail() || hasPhone() );
    }

    public boolean hasEmail()
    {
        return getEmails() != null && !getEmails().isEmpty();
    }

    public boolean hasPhone()
    {
        return getPhones() != null && !getPhones().isEmpty();
    }

    public void addPhone( String phone )
    {
        if ( phones == null )
        {
            phones = new ArrayList<String>();
        }
        phones.add( phone );
    }

    public void addEmail( String email )
    {
        if ( emails == null )
        {
            emails = new ArrayList<String>();
        }
        emails.add( email );
    }

    public ArrayList<String> getPhones()
    {
        return phones;
    }

    public void setPhones( ArrayList<String> phones )
    {
        this.phones = phones;
    }

    public ArrayList<String> getEmails()
    {
        return emails;
    }

    public void setEmails( ArrayList<String> emails )
    {
        this.emails = emails;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName( String contactName )
    {
        this.contactName = contactName;
    }

    /**
     * @param lastWishYear the lastWishYear to set
     */
    public void setLastWishYear( String lastWishYear )
    {
        this.lastWishYear = lastWishYear;
    }

    /**
     * @return the lastWishYear
     */
    public String getLastWishYear()
    {
        return lastWishYear;
    }

    public void setContactId( Long contactId )
    {
        this.contactId = contactId;
    }

    public Long getContactId()
    {
        return contactId;
    }

}
