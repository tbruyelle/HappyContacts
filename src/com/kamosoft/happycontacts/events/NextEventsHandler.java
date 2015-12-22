package com.kamosoft.happycontacts.events;

import java.util.LinkedHashMap;

import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * created 21 nov. 2010
 * @since 
 * @version $Id$
 */
public interface NextEventsHandler
{
    void finishRetrieveNextEvents( LinkedHashMap<String, ContactFeasts> results );
}
