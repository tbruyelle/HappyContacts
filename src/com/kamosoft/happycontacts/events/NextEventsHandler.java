/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts.events;

import java.util.LinkedHashMap;

import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 * created 21 nov. 2010
 * @since 
 * @version $Id$
 */
public interface NextEventsHandler
{
    void finishRetrieveNextEvents( LinkedHashMap<String, ContactFeasts> results );
}
