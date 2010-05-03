/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.dao;

/**
 * @author tom
 * 
 */
public final class HappyContactsDb
{
    public static String DATABASE_NAME = "happy_contacts";

    public static int DATABASE_VERSION = 44;

    public static final class SyncResult
    {
        public static String TABLE_NAME = "syncResult";

        public static String ID = "_id";

        public static String USER_ID = "userId";

        public static String USER_NAME = "userName";

        public static String BIRTHDAY_DATE = "birthdayDate";

        public static String CONTACT_ID = "contactId";

        public static String CONTACT_NAME = "contactName";

        public static String[] COLUMNS = { ID, USER_ID, USER_NAME, BIRTHDAY_DATE, CONTACT_ID, CONTACT_NAME };
    }

    public static final class Birthday
    {
        public static String TABLE_NAME = "birthday";

        public static String ID = "_id";

        public static String BIRTHDAY_DATE = "birthdayDate";

        public static String BIRTHDAY_YEAR = "birthdayYear";

        public static String CONTACT_ID = "contactId";

        public static String CONTACT_NAME = "contactName";

        public static String[] COLUMNS = { ID, CONTACT_ID, CONTACT_NAME, BIRTHDAY_DATE, BIRTHDAY_YEAR };
    }

    public static final class Feast
    {
        public static String TABLE_NAME = "feast";

        public static String ID = "_id";

        public static String DAY = "day";

        public static String NAME = "name";

        public static String SOURCE = "source";

        public static String[] COLUMNS = { ID, DAY, NAME, SOURCE };
    }

    /**
     * this table is only usefull to have a distinct name day list (improve app perf)
     */
    public static final class NameDay
    {
        public static String TABLE_NAME = "nameDay";

        public static String ID = "_id";

        public static String NAME_DAY = "nameDay";

        public static String[] COLUMNS = { ID, NAME_DAY };
    }

    public static final class BlackList
    {
        public static String TABLE_NAME = "blackList";

        public static String ID = "_id";

        public static String CONTACT_ID = "contactId";

        public static String CONTACT_NAME = "contactName";

        public static String LAST_WISH_DATE = "lastWishDate";

        public static String[] COLUMNS = { ID, CONTACT_ID, CONTACT_NAME, LAST_WISH_DATE };
    }

    public static final class WhiteList
    {
        public static String TABLE_NAME = "whiteList";

        public static String ID = "_id";

        public static String CONTACT_ID = "contactId";

        public static String CONTACT_NAME = "contactName";

        public static String NAME_DAY = "nameDay";

        public static String[] COLUMNS = { ID, CONTACT_ID, CONTACT_NAME, NAME_DAY };
    }
}