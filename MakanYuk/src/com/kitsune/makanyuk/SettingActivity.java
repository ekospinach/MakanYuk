package com.kitsune.makanyuk;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity 
{
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName( ((MakanYukApplication) getApplication()).getPreferencesName() );
        addPreferencesFromResource(R.xml.activity_setting);
    }   
    
}
