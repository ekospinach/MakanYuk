package com.kitsune.makanyuk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

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
		((MakanYukApplication) getApplication()).getFlurryInstance().startSession( SplashActivity.this );
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		((MakanYukApplication) getApplication()).getFlurryInstance().endSession( SplashActivity.this );
	}

}
