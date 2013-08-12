package com.kitsune.makanyuk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;

public class SplashActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_splash );
		
		Thread t = new Thread(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				try 
				{
					Thread.sleep( 2000 );
					Intent i = new Intent( SplashActivity.this, MainActivity.class );
					startActivity(i);
					finish();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
			}
		});
		t.start();
	}
	
	@Override
	protected void onStart() 
	{
		super.onStart();
		// google analytics
		EasyTracker.getInstance().activityStart( SplashActivity.this);
		// flurry
		((MakanYukApplication) getApplication()).getFlurryInstance().startSession( SplashActivity.this );
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		// google analytics
		EasyTracker.getInstance().activityStop( SplashActivity.this );
		// flurry
		((MakanYukApplication) getApplication()).getFlurryInstance().endSession( SplashActivity.this );
	}

}
