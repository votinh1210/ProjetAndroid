package com.enivl.enivldroid;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


/**
* Classe d'activité propos
*/
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		/*
		 * Ce qui ajoute des éléments à la barre d'action, si elle est présente.
		 * */
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
