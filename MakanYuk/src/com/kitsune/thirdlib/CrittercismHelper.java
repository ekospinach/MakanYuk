package com.kitsune.thirdlib;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.crittercism.app.Crittercism;

/**
 * Crittercism Helper
 * Mobile App Performance Management access it on https://www.crittercism.com/
 * @author panjigautama
 */
public class CrittercismHelper {

	private final String CRITTERCISM_APP_ID = "5201d287558d6a2997000007";
	private final String TAG = "CRITTERCISM HELPER";
	
	/**
	 * getCrittercismConfig
	 * get config for crittercism, modify this method to add or remove configuration
	 * @return json JSONObject of the crittercism config
	 */
	private JSONObject getCrittercismConfig()
	{
		JSONObject crittercismConfig = new JSONObject();
		
		boolean shouldIncludeVersionCode = true;
		boolean shouldCollectLogcat = true;
		
		try 
		{
			crittercismConfig.put( "includeVersionCode", shouldIncludeVersionCode );
			crittercismConfig.put( "shouldCollectLogcat", shouldCollectLogcat );
		} 
		catch (JSONException e)
		{
		    Log.e( TAG, e.getMessage() );
		}
		return crittercismConfig;
	}
	
	/**
	 * initCrittercism
	 * initialization of crittercism instance, call this in every activity to be tracked
	 * @param context context of current active activity
	 */
	public void initCrittercism( Context context )
	{
		Crittercism.init( context, CRITTERCISM_APP_ID, getCrittercismConfig() );
	}
	
	/**
	 * handleException
	 * track error conditions in an app that do not necessarily cause the app to crash, put this to catch exception from try catch wrapper
	 * @param e exception from the try catch wrapper
	 */
	public void handleException( Exception e )
	{
		Crittercism.logHandledException(e);
	}
	
}
