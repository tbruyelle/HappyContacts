/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commonsware.android.listview.SectionedAdapter;
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
        result.setText( caption );
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
