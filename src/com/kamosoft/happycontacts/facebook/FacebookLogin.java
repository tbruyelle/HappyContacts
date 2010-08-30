//
//  FacebookLogin.java
//
//  Authors:
// 		Neil Loknath <neil.loknath@gmail.com>
//
//  Copyright 2009 Neil Loknath
//
//  Licensed under the Apache License, Version 2.0 (the "License"); 
//  you may not use this file except in compliance with the License. 
//  You may obtain a copy of the License at 
//
//  http://www.apache.org/licenses/LICENSE-2.0 
//
//  Unless required by applicable law or agreed to in writing, software 
//  distributed under the License is distributed on an "AS IS" BASIS, 
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//  See the License for the specific language governing permissions and 
//  limitations under the License. 
//

package com.kamosoft.happycontacts.facebook;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.nloko.simplyfacebook.net.*;
import com.nloko.simplyfacebook.util.*;

/**
 * @deprecated
 * @author tom
 *
 */
public class FacebookLogin extends FacebookRequest {

	public FacebookLogin ()
	{
		super ();
		
		try {
			setUrl ("https://ssl.facebook.com/login.php");
			setNextUrl ("https://ssl.facebook.com/connect/login_success.html");
			setCancelUrl ("https://ssl.facebook.com/connect/login_failure.html");
		}
		catch (Exception ex) {}
	}
	
	public FacebookLogin (String nextUrl, String cancelUrl) throws MalformedURLException
	{
		super ();
		try {
			setUrl ("https://ssl.facebook.com/login.php");
			setNextUrl (nextUrl);
			setCancelUrl (cancelUrl);
		}
		catch (Exception ex) {}
	}
	
	public FacebookLogin (String nextUrl, String cancelUrl, String apiKey) throws MalformedURLException
	{
		this (nextUrl, cancelUrl);
		setAPIKey (apiKey);
	}
		
	private URL nextUrl = null;
	public URL getNextUrl ()
	{
		return nextUrl;
	}
	
	public void setNextUrl (String url) throws MalformedURLException
	{
		if (url == null) {
			throw new IllegalArgumentException ("url");
		}
		
		nextUrl = new URL (url);
	}
	
	private URL cancelUrl = null;
	public URL getCancelUrl ()
	{
		return cancelUrl;
	}
	
	public void setCancelUrl (String url) throws MalformedURLException
	{
		if (url == null) {
			throw new IllegalArgumentException ("url");
		}
		
		cancelUrl = new URL (url);
	}
	
	public boolean isLoggedIn ()
	{
		return getUid () != null && 
			getSessionKey () != null && 
			getSecret () != null;
	}
	
	protected Map <String, String> getRequiredQueryString ()
	{
		Map <String, String> query = new HashMap <String, String> ();
		query.put("api_key", getAPIKey ());
		query.put("fbconnect", "true");
		query.put("v", getVersion ());
		query.put("connect_display", "popup");
		query.put("next", getNextUrl ().toString());
		query.put("cancel_url", getCancelUrl ().toString());
		query.put("return_session", "true");
		query.put("req_perms", "offline_access,user_birthday,friends_birthday");
		
		return query;
	}
	
	protected void parseResponse () throws JSONException, IOException
	{
		if (getResponse () == null) {
			return;
		}
		
		String query = getResponse ().getQuery ();
		if (query == null) {
			throw new IOException ("Querystring is empty.");
		}
		
		if (!query.startsWith ("session=")) {
			return;
		}
	
		try {
			query = URLDecoder.decode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		
		JSONObject obj = new JSONObject (query.substring(8));
		if (!obj.isNull ("session_key")) {
			setSessionKey (obj.getString("session_key").trim ());
		}
		
		if (!obj.isNull ("uid")) {
			setUid (obj.getString("uid").trim ());
		}
		
		if (!obj.isNull ("secret")) {
			setSecret (obj.getString("secret").trim ());
		}
	}
	
	private URL response = null;
	public void setResponseFromExternalBrowser (URL response) throws JSONException, IOException
	{
		if (response == null) {
			throw new IllegalArgumentException ("response");
		}
		
		this.response = response;
		parseResponse ();
	}
	
	protected URL getResponse ()
	{
		return response;
	}
	
	public String getFullLoginUrl ()
	{
		try {
			return Utilities.buildUrl (getUrl ().toString (), getRequiredQueryString ());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
