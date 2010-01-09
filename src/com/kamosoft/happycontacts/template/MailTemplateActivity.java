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
 * Mail template editor
 * @author tom
 * created 9 janv. 2010
 */
public class MailTemplateActivity
    extends Activity
    implements Constants
{
    SharedPreferences mPrefs;

    EditText mMailSubject;

    EditText mMailBody;

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

        mMailSubject = (EditText) findViewById( R.id.mail_template_subject );
        mMailSubject.setText( mPrefs.getString( PREF_MAIL_SUBJECT_TEMPLATE,
                                                getString( R.string.default_mail_subject_tempate ) ) );

        mMailBody = (EditText) findViewById( R.id.mail_template_body );
        mMailBody
            .setText( mPrefs.getString( PREF_MAIL_BODY_TEMPLATE, getString( R.string.default_mail_body_template ) ) );

        Button ok = (Button) findViewById( R.id.ok_button );
        ok.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                Editor editor = mPrefs.edit();
                editor.putString( PREF_MAIL_SUBJECT_TEMPLATE, mMailSubject.getText().toString() );
                editor.putString( PREF_MAIL_BODY_TEMPLATE, mMailBody.getText().toString() );
                editor.commit();
                Toast.makeText( MailTemplateActivity.this, R.string.toast_mail_template_saved, Toast.LENGTH_SHORT )
                    .show();
                MailTemplateActivity.this.finish();
            }
        } );
        Button reset = (Button) findViewById( R.id.reset_button );
        reset.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                mMailSubject.setText( getString( R.string.default_mail_subject_tempate ) );
                mMailBody.setText( getString( R.string.default_mail_body_template ) );
            }
        } );
        Button cancel = (Button) findViewById( R.id.cancel_button );
        cancel.setOnClickListener( new View.OnClickListener()
        {

            @Override
            public void onClick( View v )
            {
                MailTemplateActivity.this.finish();
            }
        } );
        if ( Log.DEBUG )
        {
            Log.v( "MailTemplateActivity: end onCreate()" );
        }
    }
}
