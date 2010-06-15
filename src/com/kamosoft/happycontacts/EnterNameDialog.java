/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author tom
 *
 * @since 30 déc. 2009
 */
public class EnterNameDialog
    extends Dialog
    implements Constants
{
    private Context mContext;

    /**
     * @param context
     */
    public EnterNameDialog( Context context )
    {
        super( context );
        mContext = context;
        setContentView( R.layout.enter_name_popup );
        setTitle( R.string.enter_name );

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = getWindow().getAttributes().height;
        getWindow().setAttributes( lp );

        final EditText editText = (EditText) findViewById( R.id.edittext_name );

        Button buttonOK = (Button) findViewById( R.id.button_name_ok );
        buttonOK.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View view )
            {
                Intent intent = new Intent( mContext, DateListActivity.class );
                intent.putExtra( NAME_INTENT_KEY, editText.getText().toString() );
                mContext.startActivity( intent );
            }
        } );
        Button buttonCancel = (Button) findViewById( R.id.button_name_cancel );
        buttonCancel.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View view )
            {
                EnterNameDialog.this.cancel();
            }
        } );
    }

}
