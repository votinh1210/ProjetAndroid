package com.enivl.enivldroid;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


/**
* Classe d'activit� propos
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
		 * Ce qui ajoute des �l�ments � la barre d'action, si elle est pr�sente.
		 * */
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
