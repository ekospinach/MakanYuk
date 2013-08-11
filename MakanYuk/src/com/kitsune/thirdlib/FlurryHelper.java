package com.kitsune.thirdlib;

import java.util.Map;

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
	
	public void startSession( Context context )
	{
		FlurryAgent.onStartSession( context, mApiKey);
	}
	
	public void endSession( Context context )
	{
		FlurryAgent.onEndSession( context );
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
