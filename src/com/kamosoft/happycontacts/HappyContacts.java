/**
 * 
 */
package com.kamosoft.happycontacts;

import greendroid.app.GDActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * @author tom
 *
 */
public class HappyContacts
    extends GDActivity
{

    /**
     * @see greendroid.app.GDActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setActionBarContentView( R.layout.dashboard );
    }

    /**
     * Handle Alarm button click
     * @param v
     */
    public void onAlarmClick( View v )
    {
        Toast.makeText( this, "yeah", Toast.LENGTH_SHORT ).show();
    }
}
