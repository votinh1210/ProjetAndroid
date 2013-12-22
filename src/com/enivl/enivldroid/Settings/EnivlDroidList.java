package com.enivl.enivldroid.Settings;

import java.util.Arrays;

import com.enivl.enivldroid.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Classe étendu de ListView utilisé dans le programme
 * */
public class EnivlDroidList extends ListView {

	static public Object[] responses;

	private int var_startAddress;

	public EfficientAdapter adapter = null;

	private int registerPerValue;

	public EnivlDroidList(Context context, Object[] modbusResponse,
			int registersPerValue) {

		super(context);

		this.registerPerValue = registersPerValue;
		EnivlDroidList.responses = modbusResponse;

		modbusResponse.clone();

		adapter = new EfficientAdapter(context);
		this.setAdapter(adapter);
	}
	public EnivlDroidList(Context context) {
		super(context);
	}
	/*
	 * Methode set pour affiche l'adresse de début du registre 
	 **/
	public void setStartAddress(int address) {

		var_startAddress = address;
		adapter.notifyDataSetChanged();
	}

	public int getStartAddress() {
		return var_startAddress;
	}
	
	public void setRegistersPerValue(int registersPerValue) {

		this.registerPerValue = registersPerValue;
	}

	public class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {

			mInflater = LayoutInflater.from(context);
		}

		/*Le nombre d'éléments dans la liste est déterminée par
		 * le nombre de discours dans notre tableau
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		public int getCount() {
			return responses.length; // DATA.length;
		}

		public Object getItem(int position) {

			return responses[position];

		}
		/*Utilisez l'index du tableau comme un identifiant unique.
		 * 
		 * */
		public long getItemId(int position) {
			return position;
		}
		/*
		 * Faire en vue de tenir chaque ligne.
		 * */
		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 * Un ViewHolder maintient des références aux opinions de l'enfant pour
			 * éviter les appels inutiles au findViewById () sur chaque ligne.
			 * 
			 * */
			ViewHolder holder;
			
			/*
			 * Lorsque convertView n'est pas null, on peut le réutiliser directement.
			 * */
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.modbus_value_row, null);

				holder = new ViewHolder();
				holder.address = (TextView) convertView
						.findViewById(R.id.mod_Address);
				holder.value = (TextView) convertView
						.findViewById(R.id.mod_Value);

				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}
			/*
			 * Lier les données de manière efficace avec le titulaire.
			 * */
			if (responses[position] != null) {
				holder.value.setText(responses[position].toString());
			} else {
				holder.value.setText("Aucune Valeur renvoyée.");
			}

			/* 
			 * Vérifie si l'adresse d'enregistrement est inférieure à 1000 et ensuite affiché sur l'écran.
			 * */
			if (var_startAddress < 1000) {
				String tempAddress = Integer.toString(var_startAddress
						+ (position * registerPerValue));
				if (tempAddress.length() < 4) {
					for (int i = tempAddress.length(); i < 4; i++) {
						tempAddress = "0" + tempAddress;
					}
				}
				holder.address.setText(tempAddress);
			} else {
				holder.address.setText(Integer.toString(var_startAddress
						+ (position * registerPerValue)));
			}

			return convertView;
		}

		class ViewHolder {

			TextView address;
			TextView value;
		}
	}
/*
 * Méthode que met à jour les valeurs des registres
 * */
	public void updateData(Object[] values) {

		if ((values.getClass() != responses.getClass())
				|| !(Arrays.equals(values, responses))) {

			responses = null;
			responses = values.clone();
			adapter.notifyDataSetChanged();
		}
	}

}