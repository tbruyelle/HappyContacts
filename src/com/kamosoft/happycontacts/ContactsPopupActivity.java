/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import com.kamosoft.happycontacts.model.ContactFeast;
import com.kamosoft.happycontacts.model.ContactFeasts;

/**
 * Displayed when user do a contact search from NameListActivity
 * <p><i>not used</i>
 * @author tom
 * created 11 janv. 2010
 */
public class ContactsPopupActivity
    extends ListActivity
    implements Constants
{
    ContactFeasts mContactFeasts;

    private static final String NAME_COLUMN = "name";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        mContactFeasts = (ContactFeasts) getIntent().getExtras().getSerializable( CONTACTFEAST_INTENT_KEY );

        setTitle( getString( R.string.contact_feast_popup_title, mContactFeasts.getDay() ) );

        fillList();

        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: end onCreate" );
        }
    }

    private void fillList()
    {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for ( Map.Entry<Long, ContactFeast> entrySet : mContactFeasts.getContactList().entrySet() )
        {
            ContactFeast contactFeast = entrySet.getValue();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put( NAME_COLUMN, contactFeast.getContactName() );
            list.add( map );
        }
        String[] from = { NAME_COLUMN };
        int[] to = { android.R.id.text1 };
        SimpleAdapter simpleAdapter = new SimpleAdapter( this, list, android.R.layout.simple_list_item_1, from, to );
        setListAdapter( simpleAdapter );
    }
}
