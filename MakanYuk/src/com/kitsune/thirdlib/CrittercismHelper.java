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

	private String mAppID;
	private final String TAG = "CRITTERCISM HELPER";
	private JSONObject mCrittercismConfig;
	private static boolean isInitialized = false;
	
	private void setDeviceInfoAsUser()
	{
		try 
		{
						
			// instantiate metadata json object
			JSONObject metadata = new JSONObject();
			
			// add arbitrary metadata
			metadata.put("sdk", 		android.os.Build.VERSION.SDK_INT);
			metadata.put("device", 		android.os.Build.DEVICE);
			metadata.put("model", 		android.os.Build.MODEL);
			metadata.put("product", 	android.os.Build.PRODUCT);

			// send metadata to crittercism (asynchronously)
			Crittercism.setMetadata(metadata);
			
			String username = android.os.Build.PRODUCT+android.os.Build.VERSION.SDK_INT;
			Crittercism.setUsername( username );
			
		} 
		catch (JSONException e)
		{
		    Log.e( TAG, e.getMessage() );
		}
	}
	
	/**
	 * getCrittercismConfig
	 * get config for crittercism, modify this method to add or remove configuration
	 * @return json JSONObject of the crittercism config
	 */
	private JSONObject getCrittercismConfig()
	{		
		boolean shouldIncludeVersionCode = true;
		boolean shouldCollectLogcat 	 = true;
		
		try 
		{
			mCrittercismConfig.put( "includeVersionCode", shouldIncludeVersionCode );
			mCrittercismConfig.put( "shouldCollectLogcat", shouldCollectLogcat );
		} 
		catch (JSONException e)
		{
		    Log.e( TAG, e.getMessage() );
		}
		return mCrittercismConfig;
	}
	
	/**
	 * initCrittercism
	 * initialization of crittercism instance, call this in every activity to be tracked
	 * @param context context of current active activity
	 * @param appID critercism application id
	 */
	public void initCrittercism( Context context, String appID, boolean setDeviceInfoAsUserInfo )
	{
		if( isInitialized == false )
		{
			if( setDeviceInfoAsUserInfo )
			{
				setDeviceInfoAsUser();
			}
			
			mAppID = appID;
			mCrittercismConfig = new JSONObject();
			Crittercism.init( context, mAppID, getCrittercismConfig() );
		}
		else
		{
			Log.w( TAG, "Crittercism already initialized" );
		}
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
