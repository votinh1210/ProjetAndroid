package com.enivl.enivldroid.Settings;

import com.enivl.enivldroid.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * Classe qui gére la connection dans le réseau WIFI
 * */
public class Connections extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public String sla = "1";

	public static final String IP_ADDRESS = "IpAddress";
	public static final String PORT = "PortSetting";
	public static final String POLL_TIME = "PollTime";

	private EditTextPreference IPAddressPref;
	private EditTextPreference PortPref;
	private EditTextPreference PollTimePref;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		IPAddressPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(IP_ADDRESS);

		IPAddressPref.setSummary(getPreferenceScreen().getSharedPreferences()
				.getString(IP_ADDRESS, "Set a new IP address"));

		PortPref = (EditTextPreference) getPreferenceScreen().findPreference(
				PORT);

		PortPref.setSummary(getPreferenceScreen().getSharedPreferences()
				.getString(PORT,
						"Définissez un port de destination (défault = 502)"));

		PollTimePref = (EditTextPreference) getPreferenceScreen()
				.findPreference(POLL_TIME);

		PollTimePref.setSummary(getPreferenceScreen().getSharedPreferences()
				.getString(POLL_TIME,
						"Définir la durée de sondage en ms (0 = Poll une fois")
				+ "ms");

	}

	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {

		super.onPause();

		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (key.equals(IP_ADDRESS)) {
			IPAddressPref.setSummary(sharedPreferences.getString(key,
					"Définir une nouvelle adresse IP"));
		}

		else if (key.equals(PORT)) {
			PortPref.setSummary(sharedPreferences.getString(key,
					"Définissez un port de destination (par défaut = 502)"));
		}

		else if (key.equals(POLL_TIME)) {
			PollTimePref.setSummary(sharedPreferences.getString(key,
					"Définir la durée de sondage en ms (0 = une fois)") + "ms");
		}
	}
}