/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
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
            Date date = DateFormat.getDateFormat( mActivity ).parse( caption );
            Calendar calendarToday = Calendar.getInstance();
            Calendar calendarCaption = Calendar.getInstance();
            calendarCaption.setTime( date );
            int nbDay = calendarCaption.get( Calendar.DAY_OF_YEAR ) - calendarToday.get( Calendar.DAY_OF_YEAR );
            if ( nbDay == 0 )
            {
                result.setText( mActivity.getString( R.string.today ) );
            }
            else if ( nbDay == 1 )
            {
                result.setText( mActivity.getString( R.string.tomorrow ) );
            }
            else
            {
                result.setText( caption );
            }
        }

        catch ( java.text.ParseException e )
        {
            Log.e( "Unable to parse date " + caption );
            result.setText( caption );
        }

        return ( result );
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
