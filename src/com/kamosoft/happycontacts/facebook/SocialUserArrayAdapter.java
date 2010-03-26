/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.facebook;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.model.SocialNetworkUser;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 26 mars 2010
 * @version $Id$
 */
public class SocialUserArrayAdapter
    extends ArrayAdapter<SocialNetworkUser>
{
    private Context mContext;

    private ArrayList<SocialNetworkUser> mUsers;

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     * @param objects
     */
    public SocialUserArrayAdapter( Context context, int textViewResourceId, ArrayList<SocialNetworkUser> users )
    {
        super( context, textViewResourceId, users );
        mUsers = users;
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = inflater.inflate( R.layout.socialnetworkuser, null );
        }

        SocialNetworkUser user = mUsers.get( position );
        if ( user != null )
        {
            // My layout has only one TextView
            TextView itemView = (TextView) view.findViewById( R.id.name );
            if ( itemView != null )
            {
                // do whatever you want with your string and long
                itemView.setText( user.name );
            }
        }
        return view;
    }
}
