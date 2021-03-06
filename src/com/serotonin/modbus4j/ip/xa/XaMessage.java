/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.modbus4j.ip.xa;

import com.serotonin.messaging.WaitingRoomKey;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.ip.IpMessage;
import com.serotonin.modbus4j.msg.ModbusMessage;
import com.serotonin.util.queue.ByteQueue;

public class XaMessage extends IpMessage {
	protected int transactionId;

	public XaMessage(ModbusMessage modbusMessage, int transactionId) {
		super(modbusMessage);
		this.transactionId = transactionId;
	}

	public byte[] getMessageData() {
		ByteQueue msgQueue = new ByteQueue();

		modbusMessage.write(msgQueue);

		ByteQueue xaQueue = new ByteQueue();
		ModbusUtils.pushShort(xaQueue, transactionId);
		ModbusUtils.pushShort(xaQueue, ModbusUtils.IP_PROTOCOL_ID);
		ModbusUtils.pushShort(xaQueue, msgQueue.size());
		xaQueue.push(msgQueue);

		return xaQueue.popAll();
	}

	public WaitingRoomKey getWaitingRoomKey() {
		return new XaWaitingRoomKey(transactionId, modbusMessage.getSlaveId(),
				modbusMessage.getFunctionCode());
	}
}
