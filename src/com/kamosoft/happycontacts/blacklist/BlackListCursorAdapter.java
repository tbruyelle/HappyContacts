/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.blacklist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * Custom cursor adapter to display or not the value of lastwishdate column.
 */
public class BlackListCursorAdapter
    extends CursorAdapter
{
    private LayoutInflater mInflater;

    private Context mContext;

    public BlackListCursorAdapter( Context context, Cursor c )
    {
        super( context, c );
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    private void setLastWishDate( View view, String lastWishDate )
    {
        TextView lastWishDateTextView = (TextView) view.findViewById( R.id.last_wish_year );
        lastWishDateTextView.setText( mContext.getString( R.string.last_wish_date, lastWishDate ) );
    }

    private void setContactName( View view, String contactName )
    {
        TextView contactNameTextView = (TextView) view.findViewById( R.id.contact_name );
        contactNameTextView.setText( contactName );
    }

    @Override
    public void bindView( View view, Context context, Cursor cursor )
    {
        String lastWishDate = cursor.getString( cursor.getColumnIndex( HappyContactsDb.BlackList.LAST_WISH_DATE ) );
        String contactName = cursor.getString( cursor.getColumnIndex( HappyContactsDb.BlackList.CONTACT_NAME ) );

        if ( lastWishDate != null )
        {
            setLastWishDate( view, lastWishDate );
        }
        setContactName( view, contactName );
    }

    @Override
    public View newView( Context context, Cursor cursor, ViewGroup parent )
    {
        View view = mInflater.inflate( R.layout.blacklist_element, null );
        bindView( view, context, cursor );
        return view;
    }

}
