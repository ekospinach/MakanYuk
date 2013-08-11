package com.kitsune.makanyuk;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kitsune.foursquarehelper.FoursquareHelper;
import com.kitsune.foursquarehelper.FoursquareHelper.OnRequestVenueDetailListener;
import com.kitsune.foursquarehelper.FoursquareHelper.OnRequestVenueListener;
import com.kitsune.foursquarehelper.Venue;
import com.kitsune.makanyuk.YetAnotherImageView.OnImageChangeListener;
import com.kitsune.thirdlib.LocationHelper;
import com.kitsune.thirdlib.LocationHelper.OnGetLocationListener;
import com.kitsune.thirdlib.ShakeEventListener;
import com.kitsune.thirdlib.ShakeEventListener.OnShakeListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity 
{
	MakanYukApplication mMainApplication;
	
	// location
	private LocationHelper mLocHelper;
	private Location mCurrentLocation;
	private List<Address> mAddresses;
	private FoursquareHelper mFoursquareHelper;

	// shake
	private Vibrator mVibrator; 
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	
	// components
	TextView mLocationText;
	TextView mTextStage;
	TextView mVenueTitleText;
	TextView mDistanceText;
	ImageView mImageStageOverlay;
	LinearLayout mButtonGroup;
	YetAnotherImageView mImageStage;
	
	// venue
	Venue mFoundedVenue;
	Random rand = new Random(); 

	// flag
	private boolean isOnSearchVenue = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_search );
		
		// get application
		mMainApplication = (MakanYukApplication) getApplication();
		
		// main components
		mImageStageOverlay 	= (ImageView) findViewById(R.id.mainStageImageOverlay);
		mImageStage 		= (YetAnotherImageView) findViewById(R.id.mainStageImage);
		mTextStage 			= (TextView) findViewById(R.id.mainViewText);
		mImageStage.setOnImageChangeListener(new OnImageChangeListener() {
			
			@Override
			public void imageDrawed() {
			}
			
			@Override
			public void imageChangedinView(Drawable drawable) {
				if( isOnSearchVenue == false )
				{
					if( drawable != getResources().getDrawable(R.drawable.ic_light ) )
					{
						mImageStage.setScaleType(ScaleType.CENTER_CROP);
					}
						
				}
			}
		});
		
		// text
		mLocationText 		= (TextView) findViewById(R.id.mainLocationText);
		mLocationText.setText( "mencari lokasi mu ..." );
		
		mVenueTitleText 	= (TextView) findViewById(R.id.mainVenueTitleText);
		mVenueTitleText.setVisibility( TextView.INVISIBLE );
		
		mDistanceText = (TextView) findViewById(R.id.distanceText);

		// location helper
		mLocHelper = new LocationHelper( this );
		mLocHelper.setOnGetLocationListener(getLocationListener);
		
		// sensor helper & listener
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mSensorManager 	= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeEventListener();
		mSensorListener.setOnShakeListener(shakeListener);
		
		// four square
		mFoursquareHelper = new FoursquareHelper(this, "L1USFKH2MOHXVAE5BE4A4SWE0OXGVN3E1KRJKUI5UDYMCHLT", "01QZXTU0FL5UDX1KDPNPON0CUES3W3I00J2K2KOIU1OK3ZTL");
		mFoursquareHelper.setDebug(true);
		mFoursquareHelper.setOnRequestVenueListener( requestVenueListener );
		mFoursquareHelper.setOnRequestVenueDetailListener( requestVenueDetailListener );
		
		// button listener
		ImageButton share = (ImageButton) findViewById(R.id.shareButton);
		share.setOnClickListener( shareButtonListener );
		
		ImageButton location = (ImageButton) findViewById(R.id.locationButton);
		location.setOnClickListener( locationButtonListener );
		
		ImageButton mSettingButton = (ImageButton) findViewById(R.id.settingButton);
		mSettingButton.setOnClickListener( settingButtonListener );
		
		ImageView about = (ImageView) findViewById(R.id.aboutImage);
		about.setOnClickListener( aboutButtonListener );
		
		mButtonGroup 		= (LinearLayout) findViewById(R.id.buttonGroup);
		mButtonGroup.setVisibility(LinearLayout.INVISIBLE);
		
	}
	
	private void populateSharedpreferences()
	{
		SharedPreferences prefs = ((MakanYukApplication) getApplication()).getPreferences();
		int limit 	= Integer.parseInt( prefs.getString( getString(R.string.pref_key_venue_limit_search), "10" ) );
		int radius 	= Integer.parseInt( prefs.getString( getString(R.string.pref_key_distance), "1000" ) );
		
		if( mFoursquareHelper != null )
		{
			mFoursquareHelper.setLimit( limit );
			mFoursquareHelper.setRadius( radius );
		}
	}
	
	// shake listener
	private OnShakeListener shakeListener = new ShakeEventListener.OnShakeListener() 
	{
		
		@Override
		public void onShake() 
		{
			// lets give feedback to user			
			if( (mCurrentLocation != null) && ( !isOnSearchVenue ) )
			{
				// flag UI
				mTextStage.setText( getResources().getString(R.string.find_location) );
				mTextStage.setVisibility( TextView.VISIBLE );
				mVenueTitleText.setVisibility( TextView.INVISIBLE );
				mButtonGroup.setVisibility( LinearLayout.INVISIBLE );

				// set flag
				isOnSearchVenue = true;
				highlightImageStageWithOverlay( false );
				
				animate(mImageStage).alpha(0f).setListener(new ImageStageAnimator(mImageStage));
				mFoursquareHelper.requestNearestVenue( Double.toString(mCurrentLocation.getLatitude()), Double.toString(mCurrentLocation.getLongitude()), "4d4b7105d754a06374d81259", null, null, null);
				mVibrator.vibrate(500);
			}
			else
			{
				Toast.makeText(MainActivity.this, "Sedang mencari lokasi mu..", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	};
	
	void updateLocation( Location location, List<Address> addresses )
	{
		mCurrentLocation = location;
		if( addresses.size() > 0 )
		{
			String delimiter = ";";
			
			Address addr = addresses.get(0);
			
			String locationStr = "";
			if( addr.getLocality() != null )
				locationStr += addr.getLocality();
			if( addr.getFeatureName() != null && addr.getFeatureName().length() > 1 )
				locationStr += delimiter + addr.getFeatureName();
			if( addr.getSubAdminArea() != null )
				locationStr += delimiter + addr.getSubAdminArea();
			if( locationStr.charAt(0) == delimiter.charAt(0) )
				locationStr = locationStr.substring( 1 );
			locationStr = locationStr.trim();
			locationStr = locationStr.replaceAll( delimiter, ", ");
			
			mLocationText.setText( locationStr );
		}
	}
	
	void updateDistance()
	{
		// get distance
		if( mFoundedVenue != null )
		{
			float distance = mLocHelper.distanceFrom( (float)mCurrentLocation.getLatitude(), (float)mCurrentLocation.getLongitude(), Float.parseFloat(mFoundedVenue.getLocationLatitude()), Float.parseFloat(mFoundedVenue.getLocationLongitude()) );
		 	mDistanceText.setText( (int) distance + " meter away" );
		}
	}
	
	// location listener
	private OnGetLocationListener getLocationListener = new OnGetLocationListener() 
	{
		
		@Override
		public void onLocationUpdate(Location location) 
		{
			if( location != null )
			{
				mAddresses = mLocHelper.getCurrentAddress();
				updateLocation( location, mAddresses );
			}
			
			Toast.makeText( MainActivity.this, "latest location : "+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT ).show();
		}
	};
	
	private void highlightImageStageWithOverlay( boolean enabled )
	{
		final int duration = 1000;
		final float alphaVal = 0.15f;
		if( enabled )
		{
			animate( mImageStageOverlay ).alpha(alphaVal).setListener( new AnimatorListenerAdapter() 
			{
				@Override
				public void onAnimationEnd(Animator animation) 
				{
					float currAlpha = ViewHelper.getAlpha(mImageStageOverlay);
					animate( mImageStageOverlay ).alpha( currAlpha == 0f ? alphaVal : 0f ).setDuration(duration);
				}
				
			});
		}
		else
		{
			float currAlpha = ViewHelper.getAlpha(mImageStageOverlay);
			if( currAlpha != 0f )
			{
				animate( mImageStageOverlay ).alpha(0f).setDuration(duration).setListener(null);	
			}
				
		}
	}
	
	// foursquare helper search venue listener
	private OnRequestVenueListener requestVenueListener = new OnRequestVenueListener() 
	{
		
		@Override
		public void onFetchSuccess(List<Venue> venues) 
		{
			Toast.makeText( MainActivity.this, "venues : "+venues.size(), Toast.LENGTH_LONG).show();
			
			if( isOnSearchVenue && venues.size() > 0 )
			{
				try
				{
					animate(mImageStage).cancel();
					isOnSearchVenue = false;
					
					int position = rand.nextInt( venues.size() ); 
	
					mFoundedVenue = venues.get( position );
					updateLocation( mCurrentLocation, mAddresses );
					
					showUpVenue();
					highlightImageStageWithOverlay( true );
					
					// get venue detail
					mFoursquareHelper.getVenueDetail( mFoundedVenue.getId() );
				}
				catch( IllegalArgumentException e )
				{
					mMainApplication.reportException(e);
				}
			}
			else
			{
				mTextStage.setText( getString(R.string.main_text));
			}
			
		}
		
		@Override
		public void onFetchFailed(String response) 
		{
			Toast.makeText( MainActivity.this, response, Toast.LENGTH_LONG).show();
			if( isOnSearchVenue )
			{
				animate(mImageStage).cancel();
				isOnSearchVenue = false;
			}
		}
	};
	
	// foursquare helper venue detail listener
	private OnRequestVenueDetailListener requestVenueDetailListener = new OnRequestVenueDetailListener() 
	{
		
		@Override
		public void onFetchSuccess(Venue venues) 
		{
			mFoundedVenue = venues;
			Toast.makeText( MainActivity.this, "Photos "+mFoundedVenue.getPhotosCount(), Toast.LENGTH_LONG).show();
			if( mFoundedVenue.getPhotosCount() > 0 )
			{
				DisplayMetrics metrics = getResources().getDisplayMetrics();
				int position = rand.nextInt( mFoundedVenue.getPhotosItems().size() );
				String imageURl = mFoundedVenue.getPhotoURL( position, metrics.widthPixels );
				if( imageURl != null && isOnSearchVenue == false )
				{
					Picasso.with( MainActivity.this ).load( imageURl ).into( mImageStage );
				}
			}
			
		}
		
		@Override
		public void onFetchFailed(String response) 
		{
			Toast.makeText( MainActivity.this, response, Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected void onResume() 
	{
		super.onResume();
		mSensorManager.registerListener( mSensorListener,
		        mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER ),
		        SensorManager.SENSOR_DELAY_UI);
		
		// update location
		if( mLocHelper!= null )
		{
			mLocHelper.requestUpdateLocation();
		}
		
		populateSharedpreferences();
	}

	@Override
	protected void onPause() 
	{
		// remove location = listener
		if( mLocHelper!= null )
		{
			mLocHelper.removeUpdateLocation();
		}
		
		// remove sensor listener
		if( mSensorManager != null )
		{
			mSensorManager.unregisterListener( mSensorListener );
		}
		super.onPause();
	}

	void showUpVenue()
	{
		updateDistance();
		
		mTextStage.setVisibility( TextView.GONE );
		mVenueTitleText.setText( mFoundedVenue.getName() );
		mVenueTitleText.setVisibility( TextView.VISIBLE );
		mButtonGroup.setVisibility(LinearLayout.VISIBLE);
		
		ViewHelper.setAlpha( mVenueTitleText, 0f );
		ViewHelper.setAlpha( mImageStage, 0f );
		ViewHelper.setAlpha( mButtonGroup, 0f );
		ViewHelper.setAlpha( mDistanceText, 0f );
		
		mImageStage.setImageDrawable( getResources().getDrawable( R.drawable.def_image ) );
		
		animate(mButtonGroup).alpha(1f).setDuration(500);
		animate(mDistanceText).alpha(1f).setDuration(500);
		animate(mVenueTitleText).alpha(1f).setDuration(500);
		animate(mImageStage).alpha(1f).setDuration(500).setListener(null);

	}

	private class ImageStageAnimator extends AnimatorListenerAdapter implements AnimatorListener
	{
		private ImageView imageStage;
		private int mKey;
		private float alpha = 1;
		private boolean onCancel = false;
		private boolean isFirstTime = true;

		int[] mImages = { R.drawable.ic_bubble, R.drawable.ic_cup, R.drawable.ic_fire, R.drawable.ic_food, R.drawable.ic_heart, R.drawable.ic_money,  R.drawable.ic_paperplane };

		public ImageStageAnimator( ImageView imageStage )
		{
			this.imageStage = mImageStage;
			mKey = mImageStage.getId();
			imageStage.setTag( mKey, 0 );
		}
		
		@Override
		public void onAnimationStart(Animator animation) 
		{
			if( isFirstTime )
			{
				isFirstTime = false;
				imageStage.setScaleType(ScaleType.FIT_CENTER);
				imageStage.setImageDrawable( getResources().getDrawable( R.drawable.ic_search ) );
			}
			
			onCancel = false;
		}

		@Override
		public void onAnimationEnd(Animator animation) 
		{
			if( alpha == 0 && !onCancel )
			{ 
				int counter = (Integer) imageStage.getTag(mKey);
				
				alpha = 1; 
				imageStage.setScaleType(ScaleType.FIT_CENTER);
				imageStage.setImageDrawable( getResources().getDrawable( mImages[counter]) );
				counter++;
				if( counter == mImages.length )
				{
					counter = 0;
				}
				
				imageStage.setTag( mKey, counter );
			}
			else
			{ 
				alpha = 0; 
			};
			
			if( onCancel )
			{
				imageStage.setScaleType(ScaleType.FIT_CENTER);
				imageStage.setImageDrawable( getResources().getDrawable( R.drawable.ic_search ) );
				ViewHelper.setAlpha( imageStage, 1f );
			}
			else
			{
				animate(mImageStage).alpha(alpha).setDuration(500);
			}
			
		}

		@Override
		public void onAnimationCancel(Animator animation) 
		{
			onCancel = true;
		}
		
	}
	
	private OnClickListener locationButtonListener = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			String start_address 		= "saddr=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
			String destination_address 	= "daddr=" + mFoundedVenue.getLocationLatitude() + "," + mFoundedVenue.getLocationLongitude();
			String targetUri 			= "http://maps.google.com/maps?" + start_address + "&" + destination_address;
			
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(targetUri));
			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(intent);
		}
	};
	
	private OnClickListener shareButtonListener = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v)
		{
			Intent emailIntent = new Intent();
			emailIntent.setAction(Intent.ACTION_SEND);
			
			// Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Ayo makan disini !");
			String shareMessage = "makan di " + mFoundedVenue.getName() + " yuk ! " + mFoundedVenue.getCanonicalUrl();
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
			emailIntent.setType("message/rfc822");
			
			PackageManager pm = getPackageManager();
			Intent sendIntent = new Intent(Intent.ACTION_SEND);     
			sendIntent.setType("text/plain");
			
			Intent openInChooser = Intent.createChooser(emailIntent, "Share Venue");
			
			List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
			List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();        
			for (int i = 0; i < resInfo.size(); i++) 
			{
			    // Extract the label, append it, and repackage it in a LabeledIntent
				ResolveInfo ri = resInfo.get(i);
				String packageName = ri.activityInfo.packageName;
				if(packageName.contains("android.email")) 
				{
				    emailIntent.setPackage(packageName);
				} 
				else if(packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm") || packageName.contains("whatsapp") || packageName.contains("line") || packageName.contains("kakao") || packageName.contains("wechat") ) 
				{
				    Intent intent = new Intent();
				    intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
				    intent.setAction(Intent.ACTION_SEND);
				    
				    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Ayo makan disini !");
					intent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
					
				    if(packageName.contains("android.gm")) 
				    {
		                intent.setType("message/rfc822");
		            }
				    else
				    {
				    	intent.setType("text/plain");
				    }
				    intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
				}
			}
			
			// convert intentList to array
			LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
			
			openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
			startActivity(openInChooser);
		}
	};
	
	private OnClickListener settingButtonListener = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			Intent prefsIntent = new Intent( MainActivity.this, SettingActivity.class);
			startActivity(prefsIntent);
		}
		
	};
	
	private OnClickListener aboutButtonListener = new OnClickListener() 
	{
		
		@Override
		public void onClick(View v) 
		{
			Intent about = new Intent( MainActivity.this, AboutActivity.class );
			startActivity(about);
		}
	};
	
}

