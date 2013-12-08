package com.enivl.enivldroid.Settings;


import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ip.IpParameters;


public class EnivlDroidFactory extends ModbusFactory {
	
    public TCPMaster createModbusTCPMaster(IpParameters params, boolean keepAlive) {
       
    	return new TCPMaster(params, keepAlive);
    }
	
}