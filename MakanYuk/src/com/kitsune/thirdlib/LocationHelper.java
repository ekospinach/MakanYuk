package com.kitsune.thirdlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.kitsune.thirdlib.ReverseGeocodeHelper.OnReverseGeocode;

/**
 * LocationHelper class to help getting user location
 * @author panjigautama
 */
public class LocationHelper implements LocationListener
{
	private final String TAG = "LocationHelper";
	private String mActiveGPSmessage = "Activate GPS for better experience";
	private String mProvider;
	private Context mActivity;
	private Location mLocation;
	private Criteria mCriteria;
	private LocationManager mLocationManager;
	private OnGetLocationListener mGetLocationListener;
	private ReverseGeocodeHelper mReverseGeocodeHelper;

	private boolean mIsOnlyUseEnabledProvider 	 = true;
	private boolean mIsPromptGPSEnabled 		 = true;
	private static boolean mIsInitLocationCalled = false;

	private int mIntervalUpdate = 1000;
	private int mMinimumDistance = 0;
	
	private final int MINUTES = 1000 * 60;
	private final int INTERVAL_LOCATION_CHECKER = MINUTES * 1;
	
	public LocationHelper(Activity activity) 
	{
		mActivity 	= activity;
		mCriteria 	= new Criteria();
		mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		mLocationManager = (LocationManager) mActivity.getSystemService( Context.LOCATION_SERVICE );
    	mReverseGeocodeHelper = new ReverseGeocodeHelper();
    	
    	mReverseGeocodeHelper.setOnReverseGeocodeListener( reverseGeocodeListener );
	}
	
	public void setPromptActivateGPS( boolean enabled )
	{
		mIsPromptGPSEnabled = enabled;
	}
	
	public void setActivateGPSMessage( String message )
	{
		mActiveGPSmessage = message;
	}
	
	public void setCriteria( Criteria criteria )
	{
		this.mCriteria = criteria;
	}
	
	public void setOnGetLocationListener( OnGetLocationListener listener ) 
	{
		mGetLocationListener = listener;
	}
	
	public interface OnGetLocationListener 
	{
		void onLocationUpdate( Location location, List<Address> addresses );
	}
	
	public Location getLocation()
	{
		return mLocation;
	}
	
	public void initLocation()
	{
		// set flag
		if( !mIsInitLocationCalled )
	    {
	    	mIsInitLocationCalled = true;
	    }
		
		// check whether GPS is enabled
		if( mIsPromptGPSEnabled )
		{
			boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if ( !gpsEnabled ) 
			{
				// prompt to enabled GPS for better experience
				AlertDialog.Builder promptActivateGPS = new AlertDialog.Builder( mActivity );
				promptActivateGPS.setMessage( mActiveGPSmessage );
				
				promptActivateGPS.setPositiveButton( "OK", new DialogInterface.OnClickListener() 
				{
				    public void onClick(DialogInterface dialog, int id) 
				    {
				    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				    	mActivity.startActivity(intent);
				    }
				});
				
				promptActivateGPS.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int id) 
		           	{
						// User cancelled the dialog
		           	}
				});
		
				// Create the AlertDialog
				AlertDialog dialog = promptActivateGPS.create();
				dialog.show();
			} 
		}
		
	    // Define the criteria how to select the location provider -> use default
	    mProvider = mLocationManager.getBestProvider( mCriteria, mIsOnlyUseEnabledProvider );
	    if( mProvider != null)
	    {
	    	mLocation = mLocationManager.getLastKnownLocation( mProvider );
	    }
	    else
	    {	
	    	String msg = "There is no available location services";
	    	// provider will be null if there is no available enabled provider
	    	Log.i( TAG , msg);
	    	showInformationDialog( msg );
	    	return;
	    }
	    	
	    if ( mLocation != null ) 
	    {
	    	Log.i( TAG , "Provider " + mProvider + " has been selected.");
	    	onLocationChanged( mLocation );
		} 
	    else 
	    {
	    	Log.d( TAG , "Location is currently not available" );
		}
	    
	    // always request update
	    requestUpdateLocation();
		
	}
	
	public void requestUpdateLocation()
	{
		if( mIsInitLocationCalled )
		{
		    mProvider = mLocationManager.getBestProvider( mCriteria, true );
		    if( mProvider!= null )
		    {
		    	mLocationManager.requestLocationUpdates( mProvider, mIntervalUpdate, mMinimumDistance, this );	
		    }
					
		}
		else
		{
			initLocation();
		}
	}
	
	public void removeUpdateLocation()
	{
		if( mIsInitLocationCalled )
		{
			mLocationManager.removeUpdates( this );		
		}
	}

	@Override
	public void onLocationChanged( Location location ) 
	{
		// check whether update is better
		if( isBetterLocation(location, mLocation) )
		{
		    mLocation = location;
		}
		else
		{
			Log.i( TAG, "New location doesn't give more accurate location" );
		}
		
		// pass them to activity
	    if( mGetLocationListener != null )
	    {
	    	mGetLocationListener.onLocationUpdate( mLocation, null );
	    }
	    else
	    {
	    	Log.e( TAG, "You must set onGetLocationListener" );
	    }
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		Log.d( TAG , provider + " is disabled" );
	    mProvider = mLocationManager.getBestProvider( mCriteria, mIsOnlyUseEnabledProvider );
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		Log.d( TAG , provider + " is enabled" );
	    mProvider = mLocationManager.getBestProvider( mCriteria, mIsOnlyUseEnabledProvider );
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// TODO Auto-generated method stub
	}
	
	@SuppressLint("NewApi")
	private List<Address> getAddress( double currentLatitude, double currentLongitude )
	{
		List<Address> addresses = new ArrayList<Address>();
        try
        {
            boolean isGCDAvailable = Geocoder.isPresent();
            if( isGCDAvailable )
            {
                Geocoder gcd = new Geocoder( mActivity, Locale.getDefault() );
	            addresses = gcd.getFromLocation( currentLatitude, currentLongitude, 5 );
	            if (addresses.size() > 0) 
	            {
	                StringBuilder result = new StringBuilder();
	                for(int i = 0; i < addresses.size(); i++)
	                {
	                    Address address =  addresses.get(i);
	                    int maxIndex = address.getMaxAddressLineIndex();
	                    for (int x = 0; x <= maxIndex; x++ ){
	                        result.append(address.getAddressLine(x));
	                        result.append(",");
	                    }               
	                    result.append(address.getLocality());
	                    result.append(",");
	                    result.append(address.getPostalCode());
	                    result.append("\n\n");
	                }
	            }
            }
            else
            {
            	Log.e( TAG, "GCD is not available, switch to reverse geocode helper" );
            	mReverseGeocodeHelper.reverseGeocode(currentLatitude, currentLongitude, false);
            }
        }
        catch(IOException ex)
        {
        	Log.e( TAG, ex.getMessage() );
        }
        return addresses;
    }
	
	public List<Address> getCurrentAddress()
	{
		List<Address> addresses = new ArrayList<Address>();
		
		if( mLocation != null )
		{
			addresses = getAddress( mLocation.getLatitude(), mLocation.getLongitude() );
		}
		
		return addresses;
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
	    if ( currentBestLocation == null ) 
	    {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > INTERVAL_LOCATION_CHECKER;
	    boolean isSignificantlyOlder = timeDelta < -INTERVAL_LOCATION_CHECKER;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) 
	    {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } 
	    else if (isSignificantlyOlder) 
	    {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) 
	    {
	        return true;
	    } 
	    else if (isNewer && !isLessAccurate) 
	    {
	        return true;
	    } 
	    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
	    {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) 
	{
	    if (provider1 == null) 
	    {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	public float distanceFrom(float lat1, float lng1, float lat2, float lng2) 
	{
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	
	    int meterConversion = 1609;
	
	    return (float) dist * meterConversion;
	}
	
	private OnReverseGeocode reverseGeocodeListener = new OnReverseGeocode() 
	{
					
			@Override
			public void onSuccess(List<Address> addresses) 
			{
				mGetLocationListener.onLocationUpdate( mLocation, addresses );
			}
			
			@Override
			public void onFailed(String message) 
			{
				Log.e( TAG, message );
			}
			
	};
	
	public void showInformationDialog( String message )
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder( mActivity );
		dialog.setMessage( message );
		dialog.setPositiveButton("OK", null);
		
		AlertDialog infoDialog = dialog.create();
		infoDialog.show();
	}
	
}