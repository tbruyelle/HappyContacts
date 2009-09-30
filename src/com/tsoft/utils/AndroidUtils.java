/**
 * 
 */
package com.tsoft.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;

/**
 * @author tom
 * 
 */
public final class AndroidUtils
{
  public static String getFileContent(Resources resources, int rawId) throws IOException
  {
    InputStream is = resources.openRawResource(rawId);

    // We guarantee that the available method returns the total
    // size of the asset... of course, this does mean that a single
    // asset can't be more than 2 gigs.
    int size = is.available();

    // Read the entire asset into a local byte buffer.
    byte[] buffer = new byte[size];
    is.read(buffer);
    is.close();

    // Convert the buffer into a string.
    return new String(buffer);
  }
}
