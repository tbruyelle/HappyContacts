/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.template;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;

/**
 * SMS template editor
 * @author tom
 * created 9 janv. 2010
 */
public class SmsTemplateActivity
    extends Activity
    implements Constants
{
    SharedPreferences mPrefs;

    EditText mSmsContent;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "SmsTemplateActivity: start onCreate()" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.sms_template );
        mPrefs = getSharedPreferences( APP_NAME, 0 );

        mSmsContent = (EditText) findViewById( R.id.sms_template_content );
        mSmsContent
            .setText( mPrefs.getString( PREF_SMS_BODY_TEMPLATE, getString( R.string.default_sms_body_template ) ) );

        Button ok = (Button) findViewById( R.id.ok_button );
        ok.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                Editor editor = mPrefs.edit();
                editor.putString( PREF_SMS_BODY_TEMPLATE, mSmsContent.getText().toString() );
                editor.commit();
                Toast.makeText( SmsTemplateActivity.this, R.string.toast_sms_template_saved, Toast.LENGTH_SHORT )
                    .show();
                SmsTemplateActivity.this.finish();
            }
        } );
        Button reset = (Button) findViewById( R.id.reset_button );
        reset.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                mSmsContent.setText( getString( R.string.default_sms_body_template ) );
            }
        } );
        Button cancel = (Button) findViewById( R.id.cancel_button );
        cancel.setOnClickListener( new View.OnClickListener()
        {

            @Override
            public void onClick( View v )
            {
                SmsTemplateActivity.this.finish();
            }
        } );
        if ( Log.DEBUG )
        {
            Log.v( "SmsTemplateActivity: end onCreate()" );
        }
    }
}
