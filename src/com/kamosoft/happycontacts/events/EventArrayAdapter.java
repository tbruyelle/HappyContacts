/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.events;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.model.ContactFeast;

/**
 * @author tom
 *
 */
public class EventArrayAdapter
    extends ArrayAdapter<ContactFeast>
{
    private Context mContext;

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     * @param objects
     */
    public EventArrayAdapter( Context context, int textViewResourceId, ArrayList<ContactFeast> users )
    {
        super( context, textViewResourceId, users );
        mContext = context;
    }

    /**
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        View view = convertView;
        if ( view == null )
        {
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = layoutInflater.inflate( R.layout.event_element, null );
        }

        ContactFeast user = getItem( position );
        if ( user != null )
        {
            TextView userNameText = (TextView) view.findViewById( R.id.contact_name );
            userNameText.setText( user.getContactName() );

            TextView eventType = (TextView) view.findViewById( R.id.event_type );
            if ( user.getBirthdayDate() == null )
            {
                eventType.setText( R.string.nameday_of );
            }
            else
            {
                eventType.setText( R.string.birthday_of );
            }
        }
        return view;
    }
}
