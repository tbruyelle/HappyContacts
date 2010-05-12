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
import android.widget.ImageView;
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

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     * @param objects
     */
    public SocialUserArrayAdapter( Context context, int textViewResourceId, ArrayList<SocialNetworkUser> users )
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
            LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = layoutInflater.inflate( R.layout.socialnetworkuser, null );
        }

        SocialNetworkUser user = getItem( position );
        if ( user != null )
        {
            TextView userNameText = (TextView) view.findViewById( R.id.user_name );
            userNameText.setText( user.name );

//            if ( user.getContactName() != null )
//            {
//                TextView contactNameText = (TextView) view.findViewById( R.id.contact_name );
//                contactNameText.setText( user.getContactName() );
//            }

            TextView birthdayText = (TextView) view.findViewById( R.id.birthday_date );
            ImageView iconSync = (ImageView) view.findViewById( R.id.icon_sync );
            if ( user.birthday == null )
            {
                iconSync.setImageResource( R.drawable.sync_ko );
                birthdayText.setText( R.string.unknow_birthday );
            }
            else
            {
                birthdayText.setText( user.birthday );

                if ( user.getContactName() != null )
                {
                    iconSync.setImageResource( R.drawable.sync_ok );
                }
                else
                {
                    iconSync.setImageResource( R.drawable.sync_not_found );
                }
            }
        }
        return view;
    }
}
