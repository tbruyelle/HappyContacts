package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;

public interface DateFormatConstants
{
    public static final String DAY_FORMAT = "dd/MM";

    public static final String FULL_DATE_FORMAT = "dd/MM/yyyy";

    /* date formats */
    public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat( DAY_FORMAT );

    public static final SimpleDateFormat fullDateFormat = new SimpleDateFormat( FULL_DATE_FORMAT );

    public static final SimpleDateFormat yearDateFormat = new SimpleDateFormat( "yyyy" );
}
