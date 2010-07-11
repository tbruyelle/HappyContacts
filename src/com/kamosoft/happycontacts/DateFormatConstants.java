package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface DateFormatConstants
{
    public static final String DAY_FORMAT = "dd/MM";

    public static final String FULL_DATE_FORMAT = "dd/MM/yyyy";

    /* date formats */
    public static final SimpleDateFormat FB_birthdayFull = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

    public static final SimpleDateFormat FB_birthdaySmall = new SimpleDateFormat( "MMMM dd", Locale.ENGLISH );

    public static final SimpleDateFormat GoogleContact_birthdayFull = new SimpleDateFormat( "yyyy-MM-dd",
                                                                                            Locale.ENGLISH );

    public static final SimpleDateFormat GoogleContact_birthdaySmall = new SimpleDateFormat( "--MM-dd", Locale.ENGLISH );

    public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat( DAY_FORMAT );

    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat( FULL_DATE_FORMAT );

    public static final SimpleDateFormat yearDateFormat = new SimpleDateFormat( "yyyy" );

}
