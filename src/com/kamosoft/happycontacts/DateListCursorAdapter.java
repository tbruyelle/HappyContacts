/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * used to display the date according to the used locale
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 *
 * @since 22 janv. 2010
 * @version $Id$
 */
public class DateListCursorAdapter
    extends CursorAdapter
{
    private LayoutInflater mInflater;

    private java.text.DateFormat mDateFormat;

    private Calendar mCalendar;

    /**
     * @param context
     * @param c
     */
    public DateListCursorAdapter( Context context, Cursor c )
    {
        super( context, c );
        mDateFormat = DateFormat.getDateFormat( context );
        mCalendar = Calendar.getInstance();
        mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    /**
     * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView( View view, Context context, Cursor cursor )
    {
        String date = cursor.getString( cursor.getColumnIndex( HappyContactsDb.Feast.DAY ) );
        mCalendar.set( Calendar.MONTH, Integer.valueOf( date.substring( 3, 5 ) ) - 1 );
        mCalendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( date.substring( 0, 2 ) ) );
        TextView dateElement = (TextView) view.findViewById( R.id.element );
        dateElement.setText( mDateFormat.format( mCalendar.getTime() ) );
    }

    /**
     * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView( Context context, Cursor cursor, ViewGroup viewGroup )
    {
        View view = mInflater.inflate( R.layout.datename_element, null );
        bindView( view, context, cursor );
        return view;
    }

}
