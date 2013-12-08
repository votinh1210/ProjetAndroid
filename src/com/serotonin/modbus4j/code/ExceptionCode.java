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
package com.serotonin.modbus4j.code;

public class ExceptionCode {
    public static final byte ILLEGAL_FUNCTION = 0x1;
    public static final byte ILLEGAL_DATA_ADDRESS = 0x2;
    
    public static final byte SLAVE_DEVICE_FAILURE = 0x4;

   
    public static String getExceptionMessage(byte id) {
        switch (id) {
        case ILLEGAL_FUNCTION:
            return "Illegal function";
        case ILLEGAL_DATA_ADDRESS:
            return "Illegal data address";
            
        case SLAVE_DEVICE_FAILURE:
            return "Slave device failure";
        }
        return null;
    }
}
