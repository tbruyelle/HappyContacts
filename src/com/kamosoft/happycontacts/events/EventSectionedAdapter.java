/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commonsware.android.listview.SectionedAdapter;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;

/**
 * @author tom
 *
 */
public class EventSectionedAdapter
    extends SectionedAdapter
{
    private Activity mActivity;

    private int nbEvents;

    public EventSectionedAdapter( Activity activity )
    {
        mActivity = activity;
    }

    /**
     * @see com.commonsware.android.listview.SectionedAdapter#getHeaderView(java.lang.String, int, android.view.View, android.view.ViewGroup)
     */
    @Override
    protected View getHeaderView( String caption, int index, View convertView, ViewGroup parent )
    {
        TextView result = (TextView) convertView;

        if ( convertView == null )
        {
            result = (TextView) mActivity.getLayoutInflater().inflate( R.layout.event_header, null );
        }
        try
        {
            result.setText( getDateLabel( mActivity, caption ) );
        }
        catch ( java.text.ParseException e )
        {
            Log.e( "Unable to parse date " + caption );
            result.setText( caption );
        }

        return ( result );
    }

    public static String getDateLabel( Context context, String dateString )
        throws ParseException
    {
        Date date = DateFormat.getDateFormat( context ).parse( dateString );
        int nbDay = getDaysDiff( new Date(), date );
        if ( nbDay == 0 )
        {
            return context.getString( R.string.today );
        }
        else if ( nbDay == 1 )
        {
            return context.getString( R.string.tomorrow );
        }
        else
        {
            return context.getString( R.string.in_x_days, String.valueOf( nbDay ) );
        }
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

    /**
     * @param nbEvents the nbEvents to set
     */
    public void setNbEvents( int nbEvents )
    {
        this.nbEvents = nbEvents;
    }

    /**
     * @return the nbEvents
     */
    public int getNbEvents()
    {
        return nbEvents;
    }

}
