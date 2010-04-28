/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author tom
 *
 * @since 29 déc. 2009
 * @version $Id$
 */
public interface Constants
{
    public static final String FACEBOOK_API_KEY = "8e9a98e18c9e1c174e6c8904d9ed350e";

    public static final String FACEBOOK_SECRET_API_KEY = "6ad543258350e403b907878a8f4b5308";

    public static final String FACEBOOK_API_NAME = "HappyContacts";

    public static final String APP_NAME = "com.tsoft.HappyContacts";

    public static final String DAY_FORMAT = "dd/MM";

    public static final String FULL_DATE_FORMAT = "dd/MM/yyyy";

    /** default alarm to 9AM */
    public static int DEFAULT_ALARM_HOUR = 9;

    public static int DEFAULT_ALARM_MINUTE = 0;

    public static final String PREF_ALARM_STATUS = "alarmStatus";

    public static final String PREF_ALARM_MINUTE = "alarmMinute";

    public static final String PREF_ALARM_HOUR = "alarmHour";

    public static final String PREF_FIRST_RUN = "firstRun";

    public static final String PREF_MAIL_SUBJECT_TEMPLATE = "mailSubjectTemplate";

    public static final String PREF_MAIL_BODY_TEMPLATE = "mailBodyTemplate";

    public static final String PREF_SMS_BODY_TEMPLATE = "smsSubjectTemplate";

    /* intents data keys */
    public static final String NAME_INTENT_KEY = "name";

    public static final String DATE_INTENT_KEY = "date";

    public static final String CONTACTNAME_INTENT_KEY = "contactName";

    public static final String CONTACTID_INTENT_KEY = "contactId";

    public static final String NEXT_ACTIVITY_INTENT_KEY = "nextActivity";

    public static final String PICK_CONTACT_LABEL_INTENT_KEY = "pickContactLabel";
    public static final String PICK_NAMEDAY_LABEL_INTENT_KEY = "pickNameDayLabel";

    /* contact methods in the string array 'contactmethods_items' */
    public static final int CALL_ITEM_INDEX = 0;

    public static final int SMS_ITEM_INDEX = 1;

    public static final int EMAIL_ITEM_INDEX = 2;

    public static final String CONTACTFEAST_INTENT_KEY = "ContactFeasts";

    /* date formats */
    public static final SimpleDateFormat FB_birthdayFull = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

    public static final SimpleDateFormat FB_birthdaySmall = new SimpleDateFormat( "MMMM dd", Locale.ENGLISH );

    public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat( DAY_FORMAT );

    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat( FULL_DATE_FORMAT );

    public static final SimpleDateFormat yearDateFormat = new SimpleDateFormat( "yyyy" );

}
