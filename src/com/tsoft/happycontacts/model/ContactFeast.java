/**
 * 
 */
package com.tsoft.happycontacts.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
  private static final long serialVersionUID = -6905950633473003602L;

  /**
   * key=contactId
   * value=contact name
   */
  private Map<Long, String> contactList;

  public ContactFeast()
  {
  }

  public void addContact(Long id, String name)
  {
    if (contactList == null)
    {
      contactList = new HashMap<Long, String>();
    }
    contactList.put(id, name);
  }

  public String getContact(Long id)
  {
    if (contactList == null)
    {
      return null;
    }
    return contactList.get(id);
  }

  public Map<Long, String> getContactList()
  {
    if (contactList == null)
    {
      return Collections.<Long, String> emptyMap();
    }
    return contactList;
  }

  public void setContactList(Map<Long, String> contactList)
  {
    this.contactList = contactList;
  }
}
