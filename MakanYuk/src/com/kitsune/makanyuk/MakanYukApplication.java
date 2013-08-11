package com.kitsune.makanyuk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.kitsune.thirdlib.CrittercismHelper;

public class MakanYukApplication extends Application
{

	public final String TAG = "WorkIN";
	private CrittercismHelper crittercism;
	private final String PREFS_NAME = "makayuk_preferences";
	private final String CRITTERCISM_APP_ID = "520732a5558d6a563b000004";
	
	@Override
	public void onCreate() 
	{ 
		super.onCreate();		
		crittercism = new CrittercismHelper();
		Log.i(TAG, "@Override"); 
		
		initCrittercism(getApplicationContext());
	}
	
	@Override
	public void onTerminate() 
	{ 
		super.onTerminate();
		Log.i(TAG, "onTerminated"); 
	}

	@Override
	public void onLowMemory() 
	{
		super.onLowMemory();
	}
	
	public SharedPreferences getPreferences()
	{
		SharedPreferences prefs = getSharedPreferences( PREFS_NAME, 0 );
		return prefs;
	}
	
	public String getPreferencesName()
	{
		return PREFS_NAME;
	}
	
	public synchronized void initCrittercism( Context context )
	{
		crittercism.initCrittercism( context, CRITTERCISM_APP_ID, true );
	}
	
	public synchronized void reportException( Exception exception )
	{
		crittercism.handleException(exception);
	}
	
}
