/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.format.DateFormat;

import com.kamosoft.happycontacts.R;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 * created 21 déc. 2010
 * @since 
 * @version $Id$
 */
public final class DateUtils
{
    /**
     * @param context
     * @param when
     * @return
     */
    public static String getDateLabel( Context context, String when )
    {
        if ( when.equals( "0" ) )
        {
            return context.getString( R.string.today );
        }
        else if ( when.equals( "1" ) )
        {
            return context.getString( R.string.tomorrow );
        }
        else
        {
            return context.getString( R.string.in_x_days, when );
        }
    }

    public static String getToday( Context context )
    {
        return DateFormat.getDateFormat( context ).format( new Date() );
    }

    public static int getTodayToDateDiff( Date date )
    {
        return getDaysDiff( new Date(), date );
    }

    /** mulitiplicateur ms en jour */
    public static final double MS_DAY_MULTIPLIER = ( 1000 * 60 * 60 * 24 );

    /**
     * nombre de jours entre deux dates
     * @param start date deb
     * @param end date fin
     * @return int 0 si egale -n si end inferieur a start
     */
    public static int getDaysDiff( Date start, Date end )
    {
        if ( start == null )
        {
            throw new IllegalArgumentException( "start ne peut etre null" );
        }
        if ( end == null )
        {
            throw new IllegalArgumentException( "end ne peut etre null" );
        }

        GregorianCalendar calendarStart = new GregorianCalendar();
        calendarStart.setTime( start );
        // forcage a 12 pour pb passage hiver/ete
        calendarStart.set( Calendar.HOUR_OF_DAY, 12 );
        calendarStart.set( Calendar.MINUTE, 0 );
        calendarStart.set( Calendar.SECOND, 0 );
        calendarStart.set( Calendar.MILLISECOND, 0 );
        //calendarStart.setLenient( false );

        GregorianCalendar calendarEnd = new GregorianCalendar();
        calendarEnd.setTime( end );
        // forcage a 12 pour pb passage hiver/ete
        calendarEnd.set( Calendar.HOUR_OF_DAY, 12 );
        calendarEnd.set( Calendar.MINUTE, 0 );
        calendarEnd.set( Calendar.SECOND, 0 );
        calendarEnd.set( Calendar.MILLISECOND, 0 );
        //calendarEnd.setLenient( false );
        if ( calendarEnd.getTime().compareTo( calendarStart.getTime() ) == 0 )
        {
            return 0;
        }

        double diff = ( (double) calendarEnd.getTime().getTime() - (double) calendarStart.getTime().getTime() );
        double div = diff / MS_DAY_MULTIPLIER;

        // Pour gestion des changement d'heure hiver/ete (on a pas de petrol mais on a des idees)
        return (int) Math.round( div );
    }

}
