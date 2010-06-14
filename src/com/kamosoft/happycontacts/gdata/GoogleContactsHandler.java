/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.gdata;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 14 juin 2010
 * @version $Id$
 */
public class GoogleContactsHandler
    extends DefaultHandler
{
    private ArrayList<SocialNetworkUser> socialNetworkUsers;

    private String currentContact;

    private boolean inFullName;

    private String currentBirthday;

    private String nextResultUrl;

    public GoogleContactsHandler()
    {
        socialNetworkUsers = new ArrayList<SocialNetworkUser>();
    }

    public String getNextResultUrl()
    {
        return nextResultUrl;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument()
        throws SAXException
    {
        nextResultUrl = null;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument()
        throws SAXException
    {

    }

    public ArrayList<SocialNetworkUser> getGoogleContacts()
    {
        return socialNetworkUsers;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        if ( localName.equals( "link" ) )
        {
            if ( attributes.getValue( "rel" ).equals( "next" ) )
            {
                nextResultUrl = attributes.getValue( "href" );
            }
        }
        else if ( localName.equals( "fullName" ) )
        {
            inFullName = true;
        }
        else if ( localName.equals( "birthday" ) )
        {
            currentBirthday = attributes.getValue( "when" );
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        if ( inFullName )
        {
            currentContact = new String( ch, start, length );
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( localName.equals( "entry" ) )
        {
            if ( currentContact != null )
            {
                SocialNetworkUser user = new SocialNetworkUser();
                user.name = currentContact;
                user.birthday = currentBirthday;
                socialNetworkUsers.add( user );
            }
            currentBirthday = null;
            currentContact = null;
        }
        else if ( localName.equals( "fullName" ) )
        {
            inFullName = false;
        }
    }

}
