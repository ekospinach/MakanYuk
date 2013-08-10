package com.kitsune.makanyuk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.kitsune.thirdlib.CrittercismHelper;

public class MakanYukApplication extends Application
{

	public final String TAG = "WorkIN";
	private CrittercismHelper crittercism;
	
	@Override
	public void onCreate() 
	{ 
		super.onCreate();		
		crittercism = new CrittercismHelper();
		Log.i(TAG, "@Override"); 
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
	
	public synchronized void initCrittercism( Context context )
	{
		crittercism.initCrittercism(context);
	}
	
	public synchronized void reportException( Exception exception )
	{
		crittercism.handleException(exception);
	}
	
}
