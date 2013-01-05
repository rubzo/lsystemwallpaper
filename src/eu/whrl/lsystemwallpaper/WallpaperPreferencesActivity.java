package eu.whrl.lsystemwallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class WallpaperPreferencesActivity extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	public final static String name = "lsystem_prefs";
	public final static String lsystemKeyName = "lsystem";
	public final static String lsystemDefaultValue = "hilbert";
	
	public final static String headColorKeyName = "headcolor";
	public final static String headColorDefaultValue = "FFFFFF";
	public final static String tailColorKeyName = "tailcolor";
	public final static String tailColorDefaultValue = "444444";
	public final static String bgColorKeyName = "bgcolor";
	public final static String bgColorDefaultValue = "AAAAAA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(name);
		addPreferencesFromResource(R.xml.prefs);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		getPreferenceScreen().findPreference(headColorKeyName).setOnPreferenceChangeListener(colorCheckListener);
		getPreferenceScreen().findPreference(tailColorKeyName).setOnPreferenceChangeListener(colorCheckListener);
		getPreferenceScreen().findPreference(bgColorKeyName).setOnPreferenceChangeListener(colorCheckListener);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// nix
	}
	
	Preference.OnPreferenceChangeListener colorCheckListener = new OnPreferenceChangeListener() {
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
	      if (newValue != null && newValue.toString().length() > 0
	          && newValue.toString().matches("[0-9A-Fa-f]{6}")) {
	        return true;
	      }
	   
	      Toast.makeText(WallpaperPreferencesActivity.this, "Color must be RGB and hex!",
	          Toast.LENGTH_SHORT).show();
	      return false;
	    }
	  };
} 