package com.enivl.enivldroid.Settings;

import android.util.Log;

import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.IllegalFunctionException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.ip.tcp.TcpMaster;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ReadCoilsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadResponse;

/**
 * Classe qui compose des méthodes pour établir la connexion avec le serveur.
 * */

public class TCPMaster extends TcpMaster {

	public TCPMaster(IpParameters params, boolean keepAlive) {
		super(params, keepAlive);
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	public synchronized Object[] getValues(EnivlDroidLocator locator)
			throws ModbusTransportException, ErrorResponseException,
			ArrayIndexOutOfBoundsException {

		int registerRange = locator.getSlaveAndRange().getRange();

		int registersPerValue = locator.getLength();

		if (registersPerValue == 0) {

			if ((registerRange == RegisterRange.COIL_STATUS)
					|| (registerRange == RegisterRange.INPUT_STATUS)) {
				locator.setDataType(DataType.BINARY);

			}

			else {
				locator.setDataType(DataType.TWO_BYTE_INT_UNSIGNED);
			}
		}

		int valueLength = locator.getRegistersLength() / registersPerValue;

		byte[] data = new byte[locator.getRegistersLength()];

		Object[] values = new Object[valueLength];

		ModbusRequest request;
		ReadResponse response;

		if (locator.getRegistersLength() == 1
				|| locator.getLength() == locator.getRegistersLength()) {

			values[0] = this.getValue(locator);
		}

		else {
			/*
			 * Déterminer le type de demande que nous allons utiliser
			 * */
			switch (registerRange) {

			case RegisterRange.INPUT_STATUS:
				request = new ReadDiscreteInputsRequest(locator
						.getSlaveAndRange().getSlaveId(), locator.getOffset(),
						locator.getRegistersLength());
				break;

			case RegisterRange.INPUT_REGISTER:
				request = new ReadInputRegistersRequest(locator
						.getSlaveAndRange().getSlaveId(), locator.getOffset(),
						locator.getRegistersLength());
				break;

			default:
				request = null;
				throw new IllegalFunctionException((byte) registerRange,
						locator.getSlaveAndRange().getSlaveId());
			}

			try {

				response = (ReadResponse) send(request);

				data = response.getData();

				values = locator.bytesToValueArray(data);
			}

			catch (ModbusTransportException e) {
				Log.w(getClass().getSimpleName(), e.getMessage());
				throw e;
			}

			catch (Exception e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}

		return values;
	}

}