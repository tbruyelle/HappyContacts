package com.kamosoft.happycontacts;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class AboutDialog
    extends Dialog
{
  /**
   * @param context
   */
  public AboutDialog(Context context)
  {
    super(context);
    setContentView(R.layout.about);
    setTitle(context.getText(R.string.app_name) + " " + context.getText(R.string.version));
    
    Button ok = (Button) findViewById(R.id.ok_button);
    ok.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        AboutDialog.this.dismiss();
      }
    });
  }

}