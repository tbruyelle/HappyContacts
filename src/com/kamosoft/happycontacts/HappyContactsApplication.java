/**
 * 
 */
package com.kamosoft.happycontacts;

import greendroid.app.GDApplication;
import android.content.Intent;

/**
 * @author tom
 *
 */
public class HappyContactsApplication
    extends GDApplication
{

    @Override
    public Class<?> getHomeActivityClass()
    {
        return HappyContacts.class;
    }

    @Override
    public Intent getMainApplicationIntent()
    {
        // TODO Auto-generated method stub
        return super.getMainApplicationIntent();
    }

}
