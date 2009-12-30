/**
 * 
 */
package com.kamosoft.happycontacts.model;

import java.io.Serializable;

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

  private Long feastId;

  private String lastWishYear;

  public ContactFeast(String contactName, Long feastId, String lastWishYear)
  {
    this.contactName = contactName;
    this.feastId = feastId;
    this.setLastWishYear(lastWishYear);
  }

  public String getContactName()
  {
    return contactName;
  }

  public void setContactName(String contactName)
  {
    this.contactName = contactName;
  }

  public Long getFeastId()
  {
    return feastId;
  }

  public void setFeastId(Long feastId)
  {
    this.feastId = feastId;
  }

  /**
   * @param lastWishYear the lastWishYear to set
   */
  public void setLastWishYear(String lastWishYear)
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

}
