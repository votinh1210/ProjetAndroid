package com.enivl.enivldroid;

import android.os.Bundle;

import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;


/**
 *Classe contient des méthodes pour faire la transition entre le logo et la page de connexion 
 * */

public class SplashActivity extends Activity {

	private final int SCREEN_DURATION = 5000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this,
						StartupActivity.class);

				SplashActivity.this.startActivity(intent);

				SplashActivity.this.finish();

			}
		}, SCREEN_DURATION);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Ce qui ajoute des éléments à la barre d'action, si elle est présente.
		getMenuInflater().inflate(R.menu.activity_splash, menu);
		return true;
	}

}
