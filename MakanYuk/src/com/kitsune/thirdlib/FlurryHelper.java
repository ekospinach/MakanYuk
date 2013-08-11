package com.kitsune.thirdlib;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.flurry.android.FlurryAgent;

public class FlurryHelper 
{
	
	private Context mContext;
	private String mApiKey;
	
	public FlurryHelper( Context context, String apiKey )
	{
		mContext 	= context;
		mApiKey 	= apiKey;
	}
	
	public void setLogEnabled( boolean enabled )
	{
		FlurryAgent.setLogEnabled(enabled);
	}
	
	public void setReportLocation( boolean enabled )
	{
		FlurryAgent.setReportLocation(enabled);
	}
	
	public void startSession( Activity activity )
	{
		FlurryAgent.onStartSession( activity, mApiKey );
	}
	
	public void endSession( Activity activity )
	{
		FlurryAgent.onEndSession( activity );
	}
	
	public void logEvent( String eventId )
	{
		FlurryAgent.logEvent(eventId);
	}
	
	public void logEvent( String eventId, boolean timed )
	{
		FlurryAgent.logEvent(eventId, timed);
	}
	
	public void logEvent( String eventId, Map<String, String> parameters )
	{
		FlurryAgent.logEvent(eventId, parameters);
	}
	
	public void logEvent( String eventId, Map<String, String> parameters, boolean timed )
	{
		FlurryAgent.logEvent(eventId, parameters, timed);
	}
	
	public void pageView()
	{
		FlurryAgent.onPageView();
	}

}
