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

    public static int DATABASE_VERSION = 40;

    public static final class Feast
    {
        public static String TABLE_NAME = "feast";

        public static String ID = "_id";

        public static String DAY = "day";

        public static String NAME = "name";

        public static String SOURCE = "source";

        public static String[] COLUMNS = { ID, DAY, NAME, SOURCE };
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

        public static String[] COLUMNS = { ID, CONTACT_NAME, NAME_DAY };
    }
}