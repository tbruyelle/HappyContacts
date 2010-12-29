/**
 * Copyright - Accor - All Rights Reserved www.accorhotels.com
 */
package com.kamosoft.happycontacts.blacklist;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
 * created 26 déc. 2010
 * @since 
 * @version $Id$
 */
public class BlacklistCursorWrapper
    extends CursorWrapper
{

    /**
     * @param cursor
     */
    public BlacklistCursorWrapper( Cursor cursor )
    {
        super( cursor );
    }

    /**
     * @see android.database.CursorWrapper#moveToPosition(int)
     */
    @Override
    public boolean moveToPosition( int position )
    {
      
         return super.moveToPosition( position );
    }

}
