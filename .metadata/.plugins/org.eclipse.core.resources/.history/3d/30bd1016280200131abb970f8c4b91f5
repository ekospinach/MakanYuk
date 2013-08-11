package com.kitsune.makanyuk;

import android.app.Activity;
import android.content.Intent;
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
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
			}
		});
		t.start();
	}

	
	
}
