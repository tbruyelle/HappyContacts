/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

/**
 * @author tom
 *
 * @since 29 d�c. 2009
 * @version $Id$
 */
public interface Constants
{
    public static final String APP_NAME = "com.tsoft.HappyContacts";

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

    public static final String PREF_MAIL_BIRTHDAY_SUBJECT_TEMPLATE = "mailBirthdaySubjectTemplate";

    public static final String PREF_MAIL_BIRTHDAY_BODY_TEMPLATE = "mailBirthdayBodyTemplate";

    public static final String PREF_SMS_BIRTHDAY_BODY_TEMPLATE = "smsBirthdaySubjectTemplate";

    public static final String PREF_DISABLE_NAMEDAY = "nameDayStatus";

    public static final String BDAY_HINT = "BDAY";

    /* intents data keys */
    public static final String NAME_INTENT_KEY = "name";

    public static final String DATE_INTENT_KEY = "date";

    public static final String CONTACTNAME_INTENT_KEY = "contactName";

    public static final String CONTACTID_INTENT_KEY = "contactId";

    public static final String NEXT_ACTIVITY_INTENT_KEY = "nextActivity";

    public static final String PICK_CONTACT_LABEL_INTENT_KEY = "pickContactLabel";

    public static final String PICK_NAMEDAY_LABEL_INTENT_KEY = "pickNameDayLabel";

    public static final String CALLED_FOR_RESULT_INTENT_KEY = "calledForResult";
    
    public static final String NAMEDAY_INTENT_KEY = "nameDay";

    public static final String NAMEDAYID_INTENT_KEY = "nameDayId";

    /* contact methods in the string array 'contactmethods_items' */
    public static final int CALL_ITEM_INDEX = 0;

    public static final int SMS_ITEM_INDEX = 1;

    public static final int EMAIL_ITEM_INDEX = 2;

    public static final String CONTACTFEAST_INTENT_KEY = "ContactFeasts";

    public static final int MAX_DAY_EVENTS = 15;
}
