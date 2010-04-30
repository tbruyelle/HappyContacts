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
 * Mail template editor
 * @author tom
 * created 9 janv. 2010
 */
public class MailTemplateActivity
    extends Activity
    implements Constants, OnClickListener
{
    SharedPreferences mPrefs;

    EditText mMailNamedaySubject;

    EditText mMailNamedayBody;

    EditText mMailBirthdaySubject;

    EditText mMailBirthdayBody;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "MailTemplateActivity: start onCreate()" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.mail_template );
        mPrefs = getSharedPreferences( APP_NAME, 0 );

        mMailNamedaySubject = (EditText) findViewById( R.id.mail_template_subject );
        mMailNamedaySubject.setText( mPrefs.getString( PREF_MAIL_SUBJECT_TEMPLATE,
                                                       getString( R.string.default_mail_subject_tempate ) ) );

        mMailNamedayBody = (EditText) findViewById( R.id.mail_template_body );
        mMailNamedayBody.setText( mPrefs.getString( PREF_MAIL_BODY_TEMPLATE,
                                                    getString( R.string.default_mail_body_template ) ) );

        mMailBirthdaySubject = (EditText) findViewById( R.id.mail_birthday_template_subject );
        mMailBirthdaySubject.setText( mPrefs.getString( PREF_MAIL_BIRTHDAY_SUBJECT_TEMPLATE,
                                                        getString( R.string.default_mail_birthday_subject_template ) ) );

        mMailBirthdayBody = (EditText) findViewById( R.id.mail_birthday_template_body );
        mMailBirthdayBody.setText( mPrefs.getString( PREF_MAIL_BIRTHDAY_BODY_TEMPLATE,
                                                     getString( R.string.default_mail_birthday_body_template ) ) );

        Button ok = (Button) findViewById( R.id.ok_button );
        ok.setOnClickListener( this );
        Button reset = (Button) findViewById( R.id.reset_button );
        reset.setOnClickListener( this );
        Button cancel = (Button) findViewById( R.id.cancel_button );
        cancel.setOnClickListener( this );

        if ( Log.DEBUG )
        {
            Log.v( "MailTemplateActivity: end onCreate()" );
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
                mMailNamedaySubject.setText( getString( R.string.default_mail_subject_tempate ) );
                mMailNamedayBody.setText( getString( R.string.default_mail_body_template ) );
                mMailBirthdaySubject.setText( getString( R.string.default_mail_birthday_subject_template ) );
                mMailBirthdayBody.setText( getString( R.string.default_mail_birthday_body_template ) );
                return;

            case R.id.ok_button:
                Editor editor = mPrefs.edit();
                editor.putString( PREF_MAIL_SUBJECT_TEMPLATE, mMailNamedaySubject.getText().toString() );
                editor.putString( PREF_MAIL_BODY_TEMPLATE, mMailNamedayBody.getText().toString() );
                editor.putString( PREF_MAIL_BIRTHDAY_SUBJECT_TEMPLATE, mMailBirthdaySubject.getText().toString() );
                editor.putString( PREF_MAIL_BIRTHDAY_BODY_TEMPLATE, mMailBirthdayBody.getText().toString() );
                editor.commit();
                Toast.makeText( this, R.string.toast_mail_template_saved, Toast.LENGTH_SHORT ).show();
                this.finish();
                return;
        }
    }
}
