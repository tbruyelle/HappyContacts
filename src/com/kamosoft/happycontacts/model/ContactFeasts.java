/**
 * 
 */
package com.kamosoft.happycontacts.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 *
 */
public class ContactFeasts
    implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -6905950633473003602L;

  /**
   * key=contactId
   * value=contact name
   */
  private Map<Long, ContactFeast> contactList;

  private String day;

  public ContactFeasts(String day)
  {
    this.setDay(day);
  }

  public void addContact(Long id, ContactFeast contactFeast)
  {
    if (contactList == null)
    {
      contactList = new HashMap<Long, ContactFeast>();
    }
    contactList.put(id, contactFeast);
  }

  public ContactFeast getContact(Long id)
  {
    if (contactList == null)
    {
      return null;
    }
    return contactList.get(id);
  }

  public Map<Long, ContactFeast> getContactList()
  {
    if (contactList == null)
    {
      return Collections.<Long, ContactFeast> emptyMap();
    }
    return contactList;
  }

  public void setContactList(Map<Long, ContactFeast> contactList)
  {
    this.contactList = contactList;
  }

  /**
   * @param day the day to set
   */
  public void setDay(String day)
  {
    this.day = day;
  }

  /**
   * @return the day
   */
  public String getDay()
  {
    return day;
  }
}
