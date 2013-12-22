package com.enivl.enivldroid.Settings;

import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Classe qui fait la connexion utilisant la bibliotheque modbus4j
 * */

public class PollModbus implements Runnable {

	private static final boolean DEBUG = false;

	private int m_polltime;
	private boolean m_connected = false;

	public static Object[] modbusValues;

	private TCPMaster mbTCPMaster;
	private EnivlDroidLocator mbLocator;
	private EnivlDroidLocator mbWriteLocator;

	private Handler mainThreadHandler;

	private EnivlDroidList m_ListView = null;

	private boolean doWriteValue = false;
	private Object writeValue;
	

	/* Constructeur PollModus
	 *  adr = Adresse d'IP
	 *  port = Standart est 502
	 *  poll time = 1000ms
	 */

	public PollModbus(TCPMaster mbMaster, int polltime,
			EnivlDroidLocator mbLocator, EnivlDroidList m_ListView,
			Handler mainThreadHandler) {

		this.m_polltime = polltime;
		this.mbTCPMaster = mbMaster;
		this.mbLocator = mbLocator;
		this.m_ListView = m_ListView;
		this.mainThreadHandler = mainThreadHandler;
		this.mbWriteLocator = null;
		this.writeValue = false;

	}

	public void updateMembersFromUI(EnivlDroidLocator mbLocator,
			EnivlDroidList mListView, Handler mainThreadHandler) {
		this.mbLocator = mbLocator;
		this.m_ListView = mListView;
		this.mainThreadHandler = mainThreadHandler;

	}

	public synchronized void setPollTime(int polltime) {
		this.m_polltime = polltime;
	}
	/*
	 * Se connecte au serveur et démarre du thread
	 *  
	 * */
	public synchronized void connect() throws Exception {

		if (this.isConnected()) {
			this.disconnect();
		}

		try {
			mbTCPMaster.init();

		}

		catch (ModbusInitException initException) {
			Log.e(getClass().getSimpleName(),
					exceptionStringHelper(initException));
			m_connected = false;
			throw initException;
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), exceptionStringHelper(e));
			m_connected = false;
			throw e;
		}

		m_connected = true;

		Message m = this.mainThreadHandler.obtainMessage();

		m.arg1 = 1;
		mainThreadHandler.sendMessage(m);
	}

	/*
	 * Déconnecte du serveur et met le bit connecté à false.
	 * Cela entraînera le fil pour compléter ce que fait le scrutin
	 */
	public synchronized void disconnect() {

		Message m = this.mainThreadHandler.obtainMessage();

		if (m_connected || mbTCPMaster.isInitialized()) {

			if (DEBUG)
				Log.i(getClass().getSimpleName(),
						"Essayez de détruire connexion.");
			mbTCPMaster.destroy();

			if (DEBUG)
				Log.i(getClass().getSimpleName(), "Connexion détruite.");
			m.arg2 = 0;

		}

		else {
			m.arg2 = 1;
			if (DEBUG)
				Log.i(getClass().getSimpleName(),
						"A essayé de détruire la connexion.");
		}

		m_connected = false;

		m.arg1 = 0;
		mainThreadHandler.sendMessage(m);

	}
	/*
	 * Méthode qui vérifie si elle est connectée.
	 * */
	public synchronized boolean isConnected() {

		return m_connected;
	}

	public synchronized void writeValue(EnivlDroidLocator writeLocator,
			Object value) {
		this.mbWriteLocator = writeLocator;
		this.writeValue = value;
		this.doWriteValue = true;
		this.notify();
	}
	
	/*
	 * Méthode pour mise à jour le thread
	 * */

	public void run() {

		modbusValues = null;
		Message m = this.mainThreadHandler.obtainMessage();

		if (!this.isConnected()) {
			try {
				this.connect();

				while (!this.isConnected()) {
					Thread.currentThread();
					Thread.sleep(200);
				}

			}

			catch (RuntimeException runtime_e) {
				Log.e(getClass().getSimpleName(),
						exceptionStringHelper(runtime_e));
				/*
				 * Envoyer un message au thread d'interface utilisateur
				 * principal, que nous avons eu une erreur de connexion
				 * */
				m.arg1 = -1;
				m.obj = runtime_e.getMessage();
				mainThreadHandler.sendMessage(m);
			}

			catch (Exception connect_e) {
				Log.e(getClass().getSimpleName(),
						exceptionStringHelper(connect_e));
				/*
				 * Envoyer un message au thread d'interface utilisateur
				 * principal, que nous avons eu une erreur de connexion
				 * */
				m.arg1 = -1;
				m.obj = connect_e.getMessage();
				mainThreadHandler.sendMessage(m);
			}

		}

		try {
			while (m_connected) {
				modbusValues = mbTCPMaster.getValues(mbLocator);
				//System.out.println(Boolean.parseBoolean(modbusValues[0].toString()));
				m_ListView.post(new Runnable() {
					public void run() {
						m_ListView.updateData(modbusValues);
					}
				});

				if (m_polltime != 0) {

					synchronized (this) {
						this.wait(m_polltime);
					}
				} else {

					this.disconnect();
				}
				/*
				 * Après que nous appelons WriteValue du thread d'interface utilisateur, 
				 * puis nous appelons notify () et ce code est traitée. 
				 * 
				 * */
				if (doWriteValue) {
					if (DEBUG)
						Log.i(getClass().getSimpleName(), "Valeur écrit: "
								+ writeValue);
					mbTCPMaster.setValue(mbWriteLocator, writeValue);
					doWriteValue = false;
				}
			}

		}

		catch (ModbusTransportException m_exception) {
			Log.e(getClass().getSimpleName(),
					exceptionStringHelper(m_exception));
			this.disconnect();
			m.arg1 = -1;
			m.obj = m_exception.getMessage();
			mainThreadHandler.sendMessage(m);
		}

		catch (NullPointerException nullException) {

			m.arg1 = -1;
			m.obj = nullException.getMessage();
			mainThreadHandler.sendMessage(m);
			this.disconnect();
		} catch (Exception poll_e) {
			Log.e(getClass().getSimpleName(), exceptionStringHelper(poll_e));
			m.arg1 = -1;
			m.obj = poll_e.getMessage();
			mainThreadHandler.sendMessage(m);

		}
	}
	/*
	 * Méthode d'aide à éliminer tout journal NullPointerExceptions
	 *  lorsqu'une exception ne contient pas une chaîne de message
	 * */
	private String exceptionStringHelper(Exception e) {
		return (e.getMessage() != null) ? e.getMessage() : "";
	}

}