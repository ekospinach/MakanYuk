package com.kitsune.foursquarehelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Foursquare Helper
 * helper class to help gaining information from foursquare
 * @author panjigautama
 */
public class FoursquareHelper 
{
	
	private final String TAG = "FoursquareHelper";
	
	private AsyncHttpClient mHttpClient;
	private String mClientID;
	private String mClientSecret;
	private boolean mIsDebug = false;
	
	// API
	private final String API_SEARCH 	= "https://api.foursquare.com/v2/venues/search";
	private final String API_VENUE		= "https://api.foursquare.com/v2/venues/";
	
	// default
	private int mRadius = 1000;
	private int mLimit  = 10;
	
	// foursquare param
	private final String PARAM_LL 				= "ll";
	private final String PARAM_LIMIT 			= "limit";
	private final String PARAM_RADIUS 			= "radius";
	private final String PARAM_CATEGORY_ID 		= "categoryId";
	private final String PARAM_QUERY 			= "query";
	private final String PARAM_OAUTH			= "oauth_token";
	private final String PARAM_CLIENT_ID		= "client_id";
	private final String PARAM_CLIENT_SECRET 	= "client_secret";
	private final String PARAM_VERSIONING		= "v";
	
	private OnRequestVenueListener mRequestFetchVenue;
	private OnRequestVenueDetailListener mRequestFetchVenueDetail;

	public FoursquareHelper( Context context )
	{
		this( context, null, null );
	}
	
	public FoursquareHelper( Context context, String clientId, String clientSecret )
	{
		mHttpClient = new AsyncHttpClient();
		init( clientId, clientSecret );
	}
	
	public void init( String clientId, String clientSecret )
	{
		mClientID 		= clientId;
		mClientSecret 	= clientSecret;
	}
	
	public void setDebug( boolean isDebug )
	{
		 mIsDebug = isDebug;
	}
	
	/**
	 * set radius for searching
	 * @param radius radius value in meters
	 */
	public void setRadius( int radius )
	{
		this.mRadius = radius;
	}
	
	/**
	 * set limit for searching venue request
	 * @param limit limit request up to 50 venues
	 */
	public void setLimit( int limit )
	{
		this.mLimit = limit;
	}
	
	/**
	 * set listener that will handle callback from venue search request
	 * @param listener this listener will be called when requesting search
	 */
	public void setOnRequestVenueListener( OnRequestVenueListener listener ) 
	{
		mRequestFetchVenue = listener;
	}
	
	/**
	 * set listener that will handle callback from venue detail request
	 * @param listener this listener will be called when requesting detail of venue
	 */
	public void setOnRequestVenueDetailListener( OnRequestVenueDetailListener listener ) 
	{
		mRequestFetchVenueDetail = listener;
	}
	
	/**
	 * Interface for request venue, this must be set before calling any request to search venue.
	 */
	public interface OnRequestVenueListener 
	{
		/**
		 * called when request failed
		 * @param response error message
		 */
		void onFetchFailed( String response );
		/**
		 * called when request success, venues may in zero item
		 * @param venues list of venue objects
		 */
		void onFetchSuccess( List<Venue> venues );
	}
	
	/**
	 * Interface for request detail venue, this must be set before calling any request to get venue details.
	 */
	public interface OnRequestVenueDetailListener
	{
		/**
		 * called when request failed
		 * @param response error message
		 */
		void onFetchFailed( String response );
		/**
		 * called when request success, venues may in zero item
		 * @param venues list of venue objects
		 */
		void onFetchSuccess( Venue venues );
	}
	
	public void exploreVenue()
	{
		// TODO explore venue api
	}
	
	public void checkInVenue()
	{
		// TODO check in venue api
	}
	
	/**
	 * get venue details like photos, mayor, and many mores
	 * @param venueId venue id in string
	 */
	public void getVenueDetail( String venueId )
	{
		String URL = API_VENUE + venueId + "?";
		
		// build request param
		RequestParams params = new RequestParams();
		if( (mClientID != null ) && (mClientSecret != null) )
		{
			params.put( PARAM_CLIENT_ID, mClientID );
			params.put( PARAM_CLIENT_SECRET, mClientSecret );
		}
		else
		{
			Log.e( TAG, "client id and client secret not initialize !" );
			return;
		}
		params.put( PARAM_VERSIONING, getCurrentDate() );
		
		mHttpClient.get( URL, params, new AsyncHttpResponseHandler() 
		{
			
			
		    @Override
			public void onFailure(Throwable throwable, String msg) 
		    {
		    	
		    	if ( throwable.getCause() instanceof ConnectTimeoutException ) 
				{
		    		msg = "Request timeout";
			    }
		    	
		    	printDebug( DEBUG_ERROR, "Request failed : "+msg );
				mRequestFetchVenueDetail.onFetchFailed( msg );
			}

			@Override
		    public void onSuccess(String response) 
		    {
		    	try 
		    	{
					JSONObject foursquareResponseObj = new JSONObject( response );					
					if( isResponseOK( foursquareResponseObj ) )
					{
						Venue venues = extractObject( foursquareResponseObj.getJSONObject("response") );
						mRequestFetchVenueDetail.onFetchSuccess( venues );
					}
					else
					{
						printDebug( DEBUG_ERROR, "Request failed" );
						mRequestFetchVenueDetail.onFetchFailed( response );
					}
					
				} 
		    	catch (JSONException e) 
		    	{
		    		printDebug( DEBUG_ERROR, "Returning value from foursquare API is different" );
		    		mRequestFetchVenueDetail.onFetchFailed( response );
				}
		    	
		    }
		    
		});
		
	}
	
	/**
	 * fetch nearest venue from given user location
	 * see <a href="https://developer.foursquare.com/docs/venues/search">Foursquare API docs</a> for further details
	 * @param latitude <b>required</b> this location detail is must be provided, latitude value must be in decimal
	 * @param longitude <b>required</b> this location detail is must be provided, longitude value must be in decimal
	 * @param categoryId a comma separated list of categories to limit results to
	 * @param radius Limit results to venues within this many meters of the specified location
	 * @param limit Number of results to return, up to 50.
	 * @param query A search term to be applied against venue names.
	 */
	public void requestNearestVenue( String latitude, String longitude, String categoryId, String radius, String limit, String query )
	{
		// check listener
		if( mRequestFetchVenue == null )
		{
			Log.e( TAG, "You must set OnRequestVenueListener before requesting this feature" );
			return;
		}
		
		// build request param
		RequestParams params = new RequestParams();
		if( (mClientID != null ) && (mClientSecret != null) )
		{
			params.put( PARAM_CLIENT_ID, mClientID );
			params.put( PARAM_CLIENT_SECRET, mClientSecret );
		}
		else
		{
			Log.e( TAG, "client id and client secret not initialize !" );
			return;
		}
		
		if( (latitude != null) && (longitude != null) )
		{
			params.put( PARAM_LL, latitude+","+longitude );
		}
		else
		{
			Log.e( TAG, "no given latitude and longitude !" );
			return;
		}

		if( categoryId != null )
		{
			params.put( PARAM_CATEGORY_ID, categoryId );
		}
				
		if( radius != null  )
		{
			params.put( PARAM_RADIUS, radius );
		}
		else
		{
			params.put( PARAM_RADIUS, Integer.toString( mRadius ) );
		}
		
		if( limit != null )
		{
			params.put( PARAM_LIMIT, limit );
		}
		else
		{
			params.put( PARAM_LIMIT, Integer.toString( mLimit ) );
		}
		
		if( query != null )
		{
			params.put( PARAM_QUERY, query );
		}
		
		params.put( PARAM_VERSIONING, getCurrentDate() );
		
		// call get request 
		mHttpClient.get( API_SEARCH, params, new AsyncHttpResponseHandler() 
		{
			
			
		    @Override
			public void onFailure(Throwable throwable, String msg) 
		    {
		    	
		    	if ( throwable.getCause() instanceof ConnectTimeoutException ) 
				{
		    		msg = "Request timeout";
			    }
		    	
		    	printDebug( DEBUG_ERROR, "Request failed : "+msg );
				mRequestFetchVenue.onFetchFailed( msg );
			}

			@Override
		    public void onSuccess(String response) 
		    {
		    	try 
		    	{
					JSONObject foursquareResponseObj = new JSONObject( response );					
					if( isResponseOK( foursquareResponseObj ) )
					{
						List<Venue> venues = extractArrayObject( foursquareResponseObj.getJSONObject("response") );
						mRequestFetchVenue.onFetchSuccess( venues );
					}
					else
					{
						printDebug( DEBUG_ERROR, "Request failed" );
						mRequestFetchVenue.onFetchFailed( response );
					}
					
				} 
		    	catch (JSONException e) 
		    	{
		    		printDebug( DEBUG_ERROR, "Returning value from foursquare API is different" );
		    		mRequestFetchVenue.onFetchFailed( response );
				}
		    	
		    }
		    
		});
	}
	
	private boolean isResponseOK( JSONObject jsonObj )
	{
		JSONObject meta;
		int code = 0;
		try 
		{
			meta = jsonObj.getJSONObject("meta");
			code = meta.getInt("code");
			if( code == 200 )
			{
				return true;
			}
			else
			{
				printDebug(DEBUG_ERROR, "message response : " +code );
			}
		} 
		catch (JSONException e) 
		{
			printDebug(DEBUG_ERROR, "message response failed " );
		}

		return false;
	}
	
	private int extractIntValue( JSONObject jsonObj, String key )
	{
		int value = 0;
		try
		{
			value = jsonObj.getInt(key);
		}
		catch( JSONException e )
		{
			printDebug( DEBUG_ERROR, key + " value is not found" );
		}
		return value;
	}
	
	private String extractStringValue( JSONObject jsonObj, String key )
	{
		String value = null;
		try
		{
			value = jsonObj.getString(key);
		}
		catch( JSONException e )
		{
			printDebug( DEBUG_ERROR, key + " value is not found" );
		}
		return value;
	}
	
	private boolean extractBooleanValue( JSONObject jsonObj, String key )
	{
		boolean value = false;
		try
		{
			value = jsonObj.getBoolean(key);
		}
		catch( JSONException e )
		{
			printDebug( DEBUG_ERROR, key + " value is not found" );
		}
		return value;
	}
	
	private JSONObject extractJSONObjectValue( JSONObject jsonObj, String key )
	{
		JSONObject value = null;
		try
		{
			value = jsonObj.getJSONObject(key);
		}
		catch( JSONException e )
		{
			printDebug( DEBUG_ERROR, key + " object is not found" );
		}
		return value;
	}
	
	private JSONArray extractJSONArrayValue( JSONObject jsonObj, String key )
	{
		JSONArray value = null;
		try
		{
			value = jsonObj.getJSONArray(key);
		}
		catch( JSONException e )
		{
			printDebug( DEBUG_ERROR, key + " object is not found" );
		}
		return value;
	}
	
	private Venue extractVenueObjectFromJSONObject( JSONObject venueObjects )
	{
		Venue venue = new Venue();
		
		String name = extractStringValue( venueObjects, "name" );
		
		venue.setId( extractStringValue( venueObjects,"id" ) );
		venue.setName( name );
		venue.setCanonicalUrl( extractStringValue( venueObjects, "canonicalUrl" ));
		venue.setStoreId( extractStringValue( venueObjects, "storeId" ));
		venue.setReferralId( extractStringValue( venueObjects, "referralId" ));
		venue.setVerified( extractBooleanValue( venueObjects, "verified" ));
		
		// contact
		JSONObject contactObj = extractJSONObjectValue( venueObjects, "contact" );
		if( contactObj!= null )
		{
			venue.setContactPhone( extractStringValue( contactObj, "phone" ));
			venue.setContactFormattedPhone( extractStringValue( contactObj, "formattedPhone" ));
		}
		
		// location
		JSONObject locationObj = extractJSONObjectValue( venueObjects, "location" );
		if( locationObj != null )
		{
			venue.setLocationAddress( extractStringValue( locationObj, "address" ));
			venue.setLocationCrossStreet( extractStringValue( locationObj, "crossStreet" ));
			venue.setLocationLatitude( extractStringValue( locationObj, "lat" ));
			venue.setLocationLongitude( extractStringValue( locationObj, "lng" ));
			venue.setLocationDistance( extractStringValue( locationObj, "distance" ));
			venue.setLocationPostalCode( extractStringValue( locationObj, "postalCode" ));
			venue.setLocationCity( extractStringValue( locationObj, "city" ));
			venue.setLocationState( extractStringValue( locationObj, "state" ));
			venue.setLocationCountry( extractStringValue( locationObj, "country" ));
			venue.setLocationCC( extractStringValue( locationObj, "cc" ));
		}
		
		// stats
		JSONObject statsObj = extractJSONObjectValue( venueObjects, "stats" );
		if( statsObj != null )
		{
			venue.setStatsCheckinsCount( extractIntValue( statsObj, "checkinsCount" ));
			venue.setStatsUsersCount( extractIntValue( statsObj, "usersCount" ));
			venue.setStatsTipCount( extractIntValue( statsObj, "tipCount" ));
		}
		
		// menu
		JSONObject menuObj = extractJSONObjectValue( venueObjects, "menu" );
		if( menuObj != null )
		{
			venue.setMenuType( extractStringValue( menuObj, "type" ));
			venue.setMenuLabel( extractStringValue( menuObj, "label" ));
			venue.setMenuAnchor( extractStringValue( menuObj, "anchor" ));
			venue.setMenuURL( extractStringValue( menuObj, "url" ));
			venue.setMenuMobileURL( extractStringValue( menuObj, "mobileUrl" ));
		}

		// here
		JSONObject hereNowObj = extractJSONObjectValue( venueObjects, "hereNow");
		if( hereNowObj != null )
		{
			venue.setHereNowCount( extractIntValue( hereNowObj, "count" ));
		}
		
		// photos
		JSONObject photosObj = extractJSONObjectValue( venueObjects, "photos");
		if( photosObj != null )
		{
			venue.setPhotosCount( extractIntValue( photosObj, "count" ));
			
			JSONArray photosGroups = extractJSONArrayValue( photosObj, "groups");
			if( photosGroups != null && photosGroups.length() > 0 )
			{
				int counter_group = 0;
				int group_length = photosGroups.length();
				while( counter_group < group_length )
				{
					try
					{
						JSONObject groupObject 	 = photosGroups.getJSONObject(counter_group);
						JSONArray photosItemsObj = extractJSONArrayValue( groupObject, "items");
						if( photosItemsObj != null && photosItemsObj.length() != 0 )
						{
							int counter = 0;
							int photosItems_length = photosItemsObj.length();
							while( counter < photosItems_length )
							{
								try 
								{
									JSONObject photosItemObj = photosItemsObj.getJSONObject( counter );
									
									PhotosItem photosItem = new PhotosItem();
									
									photosItem.setCreatedAt( extractIntValue( photosItemObj, "createdAt" ));
									photosItem.setPrefix( extractStringValue( photosItemObj, "prefix" ));
									photosItem.setSuffix( extractStringValue( photosItemObj, "suffix" ));
									photosItem.setWidth( extractIntValue( photosItemObj, "width" ));
									photosItem.setHeight( extractIntValue( photosItemObj, "height" ));
	
									venue.addPhotosItems(photosItem);
								} 
								catch (JSONException e) 
								{
									printDebug( DEBUG_ERROR, e.getMessage() );
								}
								
								counter++;
							}
						}
					}
					catch (JSONException e) 
					{
						printDebug( DEBUG_ERROR, e.getMessage() );
					}
					
					counter_group++;
				}
				
			}
			
			
		}
		
		// reason
		// TODO reason extract
		
		// tips
		// TODO tips extract 
		
		return venue;
	}
	
	private List<Venue> extractArrayObject( JSONObject jsonObj )
	{
		List<Venue> venues = new ArrayList<Venue>();
		try 
		{
			JSONArray venueObjects = jsonObj.getJSONArray( "venues" );
			
			// substract from array
			int counter = 0;
			int len = venueObjects.length();
			while( counter < len )
			{
				JSONObject venueObject = venueObjects.getJSONObject(counter);
				Venue venue = extractVenueObjectFromJSONObject( venueObject );
				venues.add(venue);
				counter++;
			}
			
		} 
		catch (JSONException e) 
		{
			printDebug( DEBUG_ERROR, "venues not found !" );
		}
		
		return venues;
	}
	
	private Venue extractObject( JSONObject jsonObj )
	{
		Venue venue = new Venue();
		try 
		{
			JSONObject venueObject = jsonObj.getJSONObject( "venue" );
			venue = extractVenueObjectFromJSONObject( venueObject );
		} 
		catch (JSONException e) 
		{
			printDebug( DEBUG_ERROR, "venue not found !" );
		}
		
		return venue;
	}
	
	private String getCurrentDate()
	{
		Calendar c 				= Calendar.getInstance();
		SimpleDateFormat df 	= new SimpleDateFormat("yyyyMMdd");
		String formattedDate 	= df.format(c.getTime());
		return formattedDate;
	}
	
	public final int DEBUG 			= 0;
	public final int DEBUG_ERROR 	= 1;
	public final int DEBUG_INFO	 	= 2;
	private void printDebug( int debugType, String message )
	{
		if( mIsDebug )
		{
			switch( debugType )
			{
				case DEBUG:
					Log.d( TAG , message );
					break;
				case DEBUG_ERROR:
					Log.e( TAG , message );
					break;
				case DEBUG_INFO:
					Log.i( TAG , message );
					break;
			}
		}
	}
	
}
