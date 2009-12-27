package com.tsoft.happycontacts.blacklist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.tsoft.happycontacts.R;
import com.tsoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * Custom cursor adapter to display or not the value of lastwishdate column.
 */
public class BlackListCursorAdapter
    extends CursorAdapter
{
    LayoutInflater mInflater;

    public BlackListCursorAdapter( Context context, Cursor c )
    {
        super( context, c );
        mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    @Override
    public void bindView( View view, Context context, Cursor cursor )
    {
        // contact name
        TextView contactNameTextView = (TextView) view.findViewById( R.id.contact_name );
        String contactName = cursor.getString( cursor.getColumnIndex( HappyContactsDb.BlackList.CONTACT_NAME ) );
        contactNameTextView.setText( contactName );

        // last wish date
        TextView lastWishDateTextView = (TextView) view.findViewById( R.id.last_wish_year );
        String lastWishDate = cursor.getString( cursor.getColumnIndex( HappyContactsDb.BlackList.LAST_WISH_DATE ) );
        if ( lastWishDate != null )
        {
            lastWishDateTextView.setText( context.getString( R.string.last_wish_date, lastWishDate ) );
        }
    }

    @Override
    public View newView( Context context, Cursor cursor, ViewGroup parent )
    {
        View view = mInflater.inflate( R.layout.blacklist_element, null );
        bindView( view, context, cursor );
        return view;
    }

}
