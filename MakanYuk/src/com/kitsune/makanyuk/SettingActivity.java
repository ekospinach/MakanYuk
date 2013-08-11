package com.kitsune.makanyuk;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity 
{
	private SharedPreferences mPrefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        String prefsName = ((MakanYukApplication) getApplication()).getPreferencesName();
        
        getPreferenceManager().setSharedPreferencesName( prefsName );
        
        mPrefs = this.getSharedPreferences( prefsName, 0 );
        mPrefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() 
        {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
			{
				if( key.equalsIgnoreCase( getString(R.string.pref_key_language) ))
				{
					String locale = mPrefs.getString( getString(R.string.pref_key_language), "id" );				    
				    ((MakanYukApplication) getApplication()).changeLocale(locale);
				}
				
				String value = mPrefs.getString( key, "default not found" );				    
				((MakanYukApplication) getApplication()).getFlurryInstance().logEvent( "Change pref : " + key + " to : " + value );
			}
			
		});
        
        addPreferencesFromResource(R.xml.activity_setting);

    }   
    
	@Override
	protected void onStart() 
	{
		super.onStart();
		((MakanYukApplication) getApplication()).getFlurryInstance().startSession( SettingActivity.this );
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		((MakanYukApplication) getApplication()).getFlurryInstance().endSession( SettingActivity.this );
	}
    
}
