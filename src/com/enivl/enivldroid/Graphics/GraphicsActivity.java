package com.enivl.enivldroid.Graphics;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import com.enivl.enivldroid.R;

public class GraphicsActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.activity_main);
		//setContentView(R.layout.about);
		View drawView = new Draw(this);
		setContentView(drawView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
