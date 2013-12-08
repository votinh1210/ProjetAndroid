package com.enivl.enivldroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * Classe que crée tout les activités d'application
 * */
public class StartupActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final EditText login = (EditText) findViewById(R.id.username);
		final EditText pass = (EditText) findViewById(R.id.user_password);

		final Button loginButton = (Button) findViewById(R.id.connect);

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * La page de connexion avant de démarrer l'application
				 * */
				final String loginTxt = login.getText().toString();
				final String passTxt = pass.getText().toString();

				if (loginTxt.equals("") || passTxt.equals("")) {

					Toast.makeText(StartupActivity.this,
							R.string.user_or_password_empty, Toast.LENGTH_SHORT)
							.show();
					return;
				}

				if (!loginTxt.equals("enivl") || !passTxt.equals("0123")) {

					Toast.makeText(StartupActivity.this,
							R.string.user_format_error, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				/*
				 * Commencez l'activité correspondant à la version en cours
				 * d'exécution c'est pour prise en charge héritée
				 */
				if (loginTxt.equals("enivl") && passTxt.equals("0123")) {

					Intent intent = new Intent(StartupActivity.this,
							com.enivl.enivldroid.Settings.EnivlDroid.class);
					startActivity(intent);

					StartupActivity.this.finish();

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
