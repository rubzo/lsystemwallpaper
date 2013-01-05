package eu.whrl.lsystemwallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WallpaperPreferencesActivity extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	public final static String name = "lsystem_prefs";
	public final static String lsystemKeyName = "lsystem";
	public final static String lsystemDefaultValue = "hilbert";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(name);
		addPreferencesFromResource(R.xml.prefs);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);   
	}
  
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Do validation stuff here?
	}
} 