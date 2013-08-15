package com.kitsune.makanyuk;

import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.kitsune.thirdlib.CrittercismHelper;
import com.kitsune.thirdlib.FlurryHelper;
import com.newrelic.agent.android.NewRelic;

public class MakanYukApplication extends Application
{

	public final String TAG = "MakanYuk";
	
	private CrittercismHelper crittercism;
	private FlurryHelper mFlurry;
	
	private final String PREFS_NAME 		= "makanyuk_preferences";
	private final String CRITTERCISM_APP_ID = "520732a5558d6a563b000004";
	private final String FLURRY_API_KEY 	= "7DK4YWV82HT394T2GSZ9";

	private boolean isDebug = false;
	
	@Override
	public void onCreate() 
	{ 
		super.onCreate();		
		crittercism = new CrittercismHelper();
		Log.i(TAG, "@Override"); 
		
		initSharedPreferences();
		initCrittercism(getApplicationContext());
		mFlurry = new FlurryHelper(getApplicationContext(), FLURRY_API_KEY);
		
		// init new relic
		NewRelic.withApplicationToken( "AA9e7cb03cefe6b746ed389a9c1d430370d4406b6a").start(this);
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
		Log.w(TAG, "onLowMemory"); 
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
	
	public void setDebug( boolean enabled )
	{
		isDebug = enabled;
	}

	public void debugToast( String message ) 
	{
		if( isDebug )
		{
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT ).show();
		}
	}
	
	// for makan yuk only
	private void initSharedPreferences()
	{
		SharedPreferences prefs = getPreferences();
		boolean locationUpdate = prefs.getBoolean( getString(R.string.pref_key_flag_location_update), true );
		String limit 	= prefs.getString( getString(R.string.pref_key_venue_limit_search), "10" );
		String radius 	= prefs.getString( getString(R.string.pref_key_distance), "1000" );
		String locale 	= prefs.getString( getString(R.string.pref_key_language), getString(R.string.default_locale) );

	    Editor prefsEditor = prefs.edit();
	    prefsEditor.putBoolean( getString(R.string.pref_key_flag_location_update), locationUpdate );
	    prefsEditor.putString( getString(R.string.pref_key_venue_limit_search), limit );
	    prefsEditor.putString( getString(R.string.pref_key_distance), radius );
	    prefsEditor.putString( getString(R.string.pref_key_language), locale );
	    prefsEditor.commit();
	    
	    setLanguage( locale );
	}
	
	public void setLanguage( String languageToLoad ) 
	{
	    Locale locale = new Locale(languageToLoad); //e.g "sv"
	    Locale systemLocale = getResources().getConfiguration().locale;	    
	    if (systemLocale != null && systemLocale.equals(locale))
	    {
	       return;
	    }
	    Locale.setDefault(locale);
	    android.content.res.Configuration config = new android.content.res.Configuration();
	    config.locale = locale;
	    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
	}
	
	public FlurryHelper getFlurryInstance()
	{
		return mFlurry;
	}
	
}
