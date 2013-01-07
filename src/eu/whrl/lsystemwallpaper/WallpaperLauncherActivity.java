package eu.whrl.lsystemwallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import eu.whrl.lsystemwallpaper.R;

public class WallpaperLauncherActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void onClick(View view) {
	Intent intent = null;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
	    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
	        new ComponentName(this, LSystemDrawingService.class));
	} else {
		// Android versions pre-Jelly Bean don't have the above Intent, so we 
		// can only really take them to the chooser application.
		intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
	}
	startActivity(intent);
  }
} 