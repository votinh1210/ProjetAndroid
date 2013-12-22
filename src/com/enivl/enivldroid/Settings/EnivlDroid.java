package com.enivl.enivldroid.Settings;


import com.enivl.enivldroid.Graphics.GraphicsActivity;
import com.enivl.enivldroid.R;
import com.serotonin.modbus4j.base.SlaveAndRange;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.ip.IpParameters;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

@SuppressLint("HandlerLeak")
@TargetApi(Build.VERSION_CODES.FROYO)

/**
 * 
 * Classe appelé lorsque l'activité est d'abord créé.
 * 
 * */
public class EnivlDroid extends Activity {

	final boolean DEBUG = false;

	/*
	 * Champs de variables
	 * */
	// Champs du menu
	private static final int connect = Menu.FIRST;
	private static final int disconnect = Menu.FIRST + 1;
	private static final int parametres = Menu.FIRST + 2;
	private static final int dataTypes = Menu.FIRST + 3;
	private static final int about = Menu.FIRST + 4;
	private static final int visual = Menu.FIRST + 5;

	//Paramétre de connexion
	private IpParameters ipParameters;
	private EnivlDroidFactory mbFactory;
	private TCPMaster mbTCPMaster;
	private EnivlDroidLocator mbLocator;

	private String hostIPaddress;
	private int hostPort;
	private int slaveAddress = 1;
	
	//Strings pour affiche les mots parmi " "
	private static final String IpAdressPref = "IpAddress";
	private static final String PORTPref = "PortSetting";
	private static final String PollTimePref = "PollTime";
	private static final String SlaveAdressPref = "SlaveAddress";
	
	//variables pour gérer les valeurs
	private int pollTime;
	private int offset;
	private int m_count;
	private int regType;
	private int dataType;
	private String oldHostIPaddress = hostIPaddress;

	private int oldHostPort = hostPort;
	private PollModbus mb = null;
	private EnivlDroidList mbList;

	//variables pour développer la présentation de la page
	private LinearLayout mainLayout;
	private TextView notConnTextView;
	private LinearLayout.LayoutParams listParams;
	private AlertDialog.Builder dataTypeMenuBuilder;
	private AlertDialog dataTypeAlert;
	private MenuItem dataTypeMenuItem;
	private MenuItem visualItem;
	private EnivlDroidMsgExceptionHandler exceptionHandler;

	private SharedPreferences settings;
	Thread mbThread = null;

	private Object[] modbusData;

	/**
	 * Faire un nouveau gestionnaire pour obtenir les messages du scrutin thread,
	 * et les afficher dans l'interface utilisateur
	 */
	
	Handler pollHandler = new Handler() {

		@Override
		public void handleMessage(Message pollingMsg) {
			int arg1 = pollingMsg.arg1;
			int arg2 = pollingMsg.arg2;
			String msgString = (String) pollingMsg.obj;
			String displayString;

			switch (arg1) {

			case 0: // Nous sommes déconnectés

				hideMBList();
				
				switch (arg2) {

				case 0: // Déconnecté de l'hôte

					displayString = "Déconnecté du " + hostIPaddress;
					break;

				case 1: /*Nous jamais connecté à un hôte*/

					displayString = "Aucune donnée disponible";
					break;

				default:

					displayString = "Il ya un problème.";
					break;

				}
				break;
				
			case 1:  
				/*Nous sommes connectés*/
				displayString = "Connecté à" + hostIPaddress;
				
				showMBList();
				break;

			case -1: 
				/*On a un certain type d'erreur*/
				displayString = "Erreur: " + msgString;
				
				break;

			default: 
				/* Si nous n'avons pas un de ces numéros quelque chose est cassé*/
				displayString = "Erreur!";
				
				break;

			}

			Toast.makeText(getBaseContext(), displayString, Toast.LENGTH_SHORT).show();
			super.handleMessage(pollingMsg);
		}

	};

	@Override
	public void onResume() {
		super.onResume();

		if (settings == null) {
			settings = PreferenceManager.getDefaultSharedPreferences(this);
		}
		getSharedSettings();

	}

	@Override
	public void onStop() {
		super.onStop();
		if (isFinishing() && mb.isConnected()) {
			mb.disconnect();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isFinishing() && mb.isConnected()) {
			mb.disconnect();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mb.isConnected()) {
			outState.putBoolean("Lié", true);
		} else {
			outState.putBoolean("Lié", false);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mb;
	}
	/*
	 * Méthode qui crée tous les composants de l'écran.	
	 * */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final EditText offset_editText = (EditText) findViewById(R.id.offset);
		final EditText registerLength = (EditText) findViewById(R.id.length);

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		/*Obtenir les préférences actuellement stockées dans les SharedPreferences*/
		getSharedSettings();
		/*mettre en place des données de la liste factices*/
		modbusData = new Object[] { 0 };
		
		/*lets get our new list*/
		mbList = new EnivlDroidList(this, modbusData,
				DataType.getRegisterCount(dataType));
		mbList.setFocusable(false);
		
		/*besoin pour obtenir la disposition relative parent avant d'ajouter le point de vue*/
		mainLayout = (LinearLayout) findViewById(R.id.main_layout);
		
		listParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		listParams.setMargins(10, 5, 10, 5);

		setLayoutAnim_slideupfrombottom(mbList, this);

		notConnTextView = new TextView(this);
		notConnTextView
				.setText("Bienvenue à ENIVL-DROID.\nL'application n'est pas connecté.");

		mainLayout.addView(notConnTextView, listParams);
		
		switchRegType(regType);

		ipParameters = new IpParameters();
		ipParameters.setHost(hostIPaddress);
		ipParameters.setPort(hostPort);

		mbFactory = new EnivlDroidFactory();
		mbTCPMaster = mbFactory.createModbusTCPMaster(ipParameters, true);

		exceptionHandler = new EnivlDroidMsgExceptionHandler(pollHandler);

		mbTCPMaster.setTimeout(15000);
		mbTCPMaster.setRetries(0);
		mbTCPMaster.setExceptionHandler(exceptionHandler);

		setModbusMultiLocator();
		
		/*
		 * Handler pour spinner pour sélectionner le type de données Modbus
		 * */
		final Spinner s = (Spinner) findViewById(R.id.point_Type);
		
		/*
		 * Construire tableau avec les types de points Modbus
		 * */
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.pointTypes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		
		/*
		 * La création d'un dialogue de registres
		 * */
		
		dataTypeMenuBuilder = new AlertDialog.Builder(this);
		dataTypeMenuBuilder.setTitle("Afficher les registres comme : ");
		dataTypeMenuBuilder.setSingleChoiceItems(R.array.dataType, dataType,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {

						setDataType(item + 1);

						dataTypeAlert.dismiss();

					}

				});

		dataTypeMenuBuilder.setCancelable(true);
		dataTypeAlert = dataTypeMenuBuilder.create();
		
		/*
		 * Définir des valeurs dans les éléments d'interface en fonction des préférences partagées
		 * 
		 * */
		offset_editText.setText(Integer.toString(offset));
		registerLength.setText(Integer.toString(m_count));
		s.setSelection(regType - 1);
		

		/*
		 * Listener pour spinner sélection
		 * */
		s.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				regType = s.getSelectedItemPosition() + 1;

				switchRegType(regType);

			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Rien
			}
		});
		
		offset_editText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					offset = Integer.parseInt(offset_editText.getText()
							.toString());
					mbLocator.setOffset(offset);

					SharedPreferences.Editor editor = settings.edit();
					editor.putInt("registerOffset", offset);
					editor.commit();

					switchRegType(regType);

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

					return true;
				}

				else {

					return false;
				}

			}

		});
		
		/*
		 * Enregistrer pression de touche gestionnaire à changer de registre de décalage
		 * 
		 * */
		offset_editText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_DONE) {
							offset = Integer.parseInt(offset_editText.getText()
									.toString());
							mbLocator.setOffset(offset);

							SharedPreferences.Editor editor = settings.edit();
							editor.putInt("registreOffset", offset);
							editor.commit();

							switchRegType(regType);

							InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

							return true;
						}

						return false;
					}
				});
		/*
		 * Enregistrer pression de touche gestionnaire de modifier la longueur de lire
		 * 
		 * */
		registerLength.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					m_count = Integer.parseInt(registerLength.getText()
							.toString());

					mbLocator.setRegistersLength(m_count);

					SharedPreferences.Editor editor = settings.edit();

					editor.putInt("registreCount", m_count);
					editor.commit();

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

					return true;

				} else {

					return false;
				}

			}
		});
		/*
		 * Attrapez le "fait ou la prochaine", appuyez sur la touche de l'intérieur du clavier virtuel
		 * sont les mêmes que la touche "entrée", appuyez sur la touche de clavier matériel ci-dessus.
		 * 
		 * */
		registerLength
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_DONE) {

							m_count = Integer.parseInt(registerLength.getText()
									.toString());

							mbLocator.setRegistersLength(m_count);

							SharedPreferences.Editor editor = settings.edit();

							editor.putInt("registreCount", m_count);
							editor.commit();

							InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

							return true;

						}

						return false;

					}
				});

		PollModbus temp = (PollModbus) getLastNonConfigurationInstance();

		if (temp != null) { 
			/*
			 * Si nous avons une configuration existante stockée, puis on la charge
			 * */
			mb = temp;

		} else {
			/*
        	 * Autrement obtenir un nouveau sondage modbus objet que nous allons passer au démarreur fil.
        	 * 
        	 * */
			mb = new PollModbus(mbTCPMaster, pollTime, mbLocator, mbList,
					pollHandler);
		}

		if (savedInstanceState != null) {

			boolean connectedBool = savedInstanceState.getBoolean("Connecté");
			if (!connectedBool) {

			} else {

				mb.updateMembersFromUI(mbLocator, mbList, pollHandler);
				forceShowMBList();

			}

		}
	}
	/*
	 * Crée les éléments de menu
	 * */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, connect, 0, "Se connecter").setIcon(
				android.R.drawable.ic_menu_manage);
		menu.add(0, disconnect, 0, "Déconnecter").setIcon(
				android.R.drawable.ic_menu_delete);
		;
		menu.add(0, parametres, 0, "Paramètres").setIcon(
				android.R.drawable.ic_menu_edit);

		dataTypeMenuItem = menu.add(0, dataTypes, 0, "Affichage des données")
				.setIcon(android.R.drawable.ic_input_get);

		menu.add(0, about, 0, "Sur ENIVL-DROID").setIcon(
				android.R.drawable.ic_menu_info_details);
		
		visualItem = menu.add(0, visual, 0, "Visualialiser");

		setDataType(dataType);
		
		if (mb.isConnected()) visualItem.setEnabled(true); else visualItem.setEnabled(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case parametres:
			startActivityForResult(new Intent(this, Connections.class), 0);

			return (true);

		case connect:

			startPollingThread();

			return true;

		case disconnect:
			killPollingThread();
			return true;

		case dataTypes:

			dataTypeAlert.show();

			return true;

		case about:

			startActivityForResult(new Intent(this,
					com.enivl.enivldroid.AboutActivity.class), 0);

			return true;
			
		case visual:
			//mbLocator.setSlaveAndRange(new SlaveAndRange(slaveAddress, RegisterRange.COIL_STATUS));
			//mbWriteValue = !mbWriteValue;
			//mb.writeValue(mbLocator, mbWriteValue);
			startActivityForResult(new Intent(this, GraphicsActivity.class), 0);
			return true;
		}

		return false;
	}
/*
 * Détermine s'il ya une connexion, et si il ya un objet scrutin thread déjà créé.
 * Sinon, il se connecte et crée un nouveau scrutin thread.
 * 
 * */
	private void startPollingThread() {

		if (mb == null) {
			mb = new PollModbus(mbTCPMaster, pollTime, mbLocator, mbList,
					pollHandler);
		} else if (mb.isConnected()) {
			mb.disconnect();
		}
/*
 * Vérifiez si le fil est en marche - si c'est le cas, arrêtez-le avant de créer une nouvelle.
 * 
 * */
		if (mbThread != null) {

			if (mbThread.isAlive()) {
				mbThread.interrupt();
				mbThread = null;
			}

			mbThread = null;

		}
		mbThread = new Thread(mb, "PollingThread");
		mbThread.start();

	}
    /* 
    * S'il ya un objet scrutin thread, puis débranchez
    *  
    */
	private void killPollingThread() {

		if (!mb.isConnected()) {
			Toast.makeText(this, "Non Connecté", Toast.LENGTH_SHORT).show();
		} else {

			mb.disconnect();
		}
	}
/*
 * Modifications de la liste des valeurs à modbus aprés message "déconnecté".
 * 
 * */
	public void hideMBList() {

		if (mbList.isShown()) {

			mainLayout.removeView(mbList);
			mainLayout.addView(notConnTextView);
			visualItem.setEnabled(false);
		}
	}

	public void showMBList() {

		if (notConnTextView.isShown()) {
			forceShowMBList();

		}
	}

	private void forceShowMBList() {

		mainLayout.removeView(notConnTextView);
		visualItem.setEnabled(true);
		mainLayout.addView(mbList, listParams);
		
		
	}
/*
 * 
 * Appelle la valeur par défaut, puis re-vérifie les préférences, et si cela se déconnecte 
 * Ils ont changé à partir du serveur actuel et définit la nouvelle adresse IP ou le port.
 * 
 * */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {

		super.onActivityResult(reqCode, resultCode, data);

		getSharedSettings();

		if ((hostIPaddress != oldHostIPaddress) || (oldHostPort != hostPort)) {

			if (mb.isConnected()) {
				mb.disconnect();
				mainLayout.removeView(mbList);
				mainLayout.addView(notConnTextView);
			}

			ipParameters.setHost(hostIPaddress);
			ipParameters.setPort(hostPort);

			if (mb != null) {
				mb.setPollTime(pollTime);
			}
		}

	}

	/*
	 * Methodes d'aidées
	 * 
	 * */
	
	private void setModbusMultiLocator() {

		try {
			mbLocator = new EnivlDroidLocator(slaveAddress, regType, offset,
					dataType, m_count);
			

		} catch (Exception e) {

			Log.e(getClass().getSimpleName(), e.getMessage());
		}
	}
	
	
	/*
	 * Obtient les paramètres de Préférences, et assigne les variables locales
	 * */
	private void getSharedSettings() {

		hostIPaddress = settings.getString(IpAdressPref, "127.0.0.1");
		hostPort = Integer.parseInt(settings.getString(PORTPref, "502"));
		pollTime = Integer.parseInt(settings.getString(PollTimePref, "1000"));
		slaveAddress = Integer.parseInt(settings
				.getString(SlaveAdressPref, "1"));

		m_count = settings.getInt("registerCount", 1);
		offset = settings.getInt("registerOffset", 0);
		regType = settings.getInt("registerType", 0);
		dataType = settings.getInt("dataType", 1);

	}

	public void setDataType(int dataType) {

		if ((regType != RegisterRange.COIL_STATUS)
				&& (regType != RegisterRange.INPUT_STATUS)) {
			this.dataType = dataType;
			if (dataTypeMenuItem != null) {
				dataTypeMenuItem.setEnabled(true);

			}
		} else {
			this.dataType = DataType.BINARY;
			if (dataTypeMenuItem != null) {
				dataTypeMenuItem.setEnabled(false);
			}
		}

		if (mbLocator != null) {
			mbLocator.setDataType(this.dataType);
		}

		mbList.setRegistersPerValue(DataType.getRegisterCount(this.dataType));

		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("dataType", dataType);
		editor.commit();

		switch (dataType) {

		case DataType.BINARY:

			break;

		case DataType.TWO_BYTE_INT_SIGNED:

		}

	}

	public void switchRegType(int regType) {

		this.regType = regType;

		switch (regType) {

		case RegisterRange.INPUT_STATUS:
			mbList.setStartAddress(1000 + offset);

			setDataType(DataType.BINARY);

			break;

		case RegisterRange.INPUT_REGISTER:
			mbList.setStartAddress(3000 + offset);
			if (dataType <= 1)
				dataType = 2;
			setDataType(dataType);
		}

		if (mbLocator == null) {
			setModbusMultiLocator();
		}

		mbLocator.setSlaveAndRange(new SlaveAndRange(slaveAddress, regType));

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("registerType", regType);
		editor.commit();

	}

	// Ajouter d'annimation aux choses - coulissant par le bas - ce besoin de
	// travail
	public static void setLayoutAnim_slideupfrombottom(ViewGroup panel,
			Context ctx) {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(500);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.25f);
		panel.setLayoutAnimation(controller);

	}
	
}