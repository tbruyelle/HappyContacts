/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.template;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
    implements Constants, OnClickListener
{
    private SharedPreferences mPrefs;

    private EditText mSmsNamedayContent;

    private EditText mSmsBirthdayContent;

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

        mSmsNamedayContent = (EditText) findViewById( R.id.sms_template_content );
        mSmsNamedayContent.setText( mPrefs.getString( PREF_SMS_BODY_TEMPLATE,
                                                      getString( R.string.default_sms_body_template ) ) );

        mSmsBirthdayContent = (EditText) findViewById( R.id.sms_birthday_template_content );
        mSmsBirthdayContent.setText( mPrefs.getString( PREF_SMS_BIRTHDAY_BODY_TEMPLATE,
                                                       getString( R.string.default_sms_birthday_body_template ) ) );

        Button ok = (Button) findViewById( R.id.ok_button );
        ok.setOnClickListener( this );
        Button reset = (Button) findViewById( R.id.reset_button );
        reset.setOnClickListener( this );
        Button cancel = (Button) findViewById( R.id.cancel_button );
        cancel.setOnClickListener( this );
        if ( Log.DEBUG )
        {
            Log.v( "SmsTemplateActivity: end onCreate()" );
        }
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick( View view )
    {
        switch ( view.getId() )
        {
            case R.id.cancel_button:
                this.finish();
                return;

            case R.id.reset_button:
                mSmsNamedayContent.setText( getString( R.string.default_sms_body_template ) );
                mSmsBirthdayContent.setText( getString( R.string.default_sms_birthday_body_template ) );
                return;

            case R.id.ok_button:
                Editor editor = mPrefs.edit();
                editor.putString( PREF_SMS_BODY_TEMPLATE, mSmsNamedayContent.getText().toString() );
                editor.putString( PREF_SMS_BIRTHDAY_BODY_TEMPLATE, mSmsBirthdayContent.getText().toString() );
                editor.commit();
                Toast.makeText( this, R.string.toast_sms_template_saved, Toast.LENGTH_SHORT ).show();
                this.finish();
                return;
        }
    }
}
