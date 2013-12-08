package com.enivl.enivldroid;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Classes avec tout les activités du ENIVL DROID
 * */


public class EnivlDroidActivity extends Activity {

	private ActionBar actionBar;
	
	//Type de API 8
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		actionBar = getActionBar();

	}
	/* 
	 * Cycle de vie de l'application
	 * 
	 * Pensez à utiliser onPause() / onResume() et un service pour permettre à une
	 * application cachée de continuer de vote et puis re-affichage des résultats.
	 * 
	 */
	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return 1;
	}

}
