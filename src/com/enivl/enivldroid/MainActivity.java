package com.enivl.enivldroid;

import com.enivl.enivldroid.Settings.EnivlDroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * Classes d'application d'Android
 * */

public class MainActivity extends Activity {

	// Vérifiez pour Android 3.0 ou version supérieure et un format grand écran
	private final boolean CheckAndroidVersion = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent startActivityIntent = null;
		if (CheckAndroidVersion /* Ecran Large */) {
			startActivityIntent = new Intent(this, EnivlDroidActivity.class);
		} else {
			// Ancienne version
			startActivityIntent = new Intent(this, EnivlDroid.class);
		}
		startActivity(startActivityIntent);
		finish();
	}
}