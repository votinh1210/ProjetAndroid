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
package com.serotonin.modbus4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.modbus4j.base.KeyedModbusLocator;
import com.serotonin.modbus4j.base.RangeAndOffset;
import com.serotonin.modbus4j.base.ReadFunctionGroup;
import com.serotonin.modbus4j.base.SlaveAndRange;
import com.serotonin.modbus4j.code.RegisterRange;

public class BatchRead<K> {
	private final List<KeyedModbusLocator<K>> requestValues = new ArrayList<KeyedModbusLocator<K>>();

	private boolean contiguousRequests = false;

	/**
	 * Si cette valeur est fausse, toute réponse d'erreur reçue va provoquer une
	 * levée de l'exception, et l'ensemble du lot d'être interrompu (sauf
	 * exceptionsInResults est vrai - voir ci-dessous). Si vrai, les réponses
	 * d'erreur sera défini comme le résultat de tous les localisateurs
	 * concernés et le lot entier sera tentée sans exceptions lancées.
	 */
	private boolean errorsInResults = false;

	/**
	 * Si cette valeur est false, toutes les exceptions levées fera le lot
	 * entier d'être abandonnée. Si la valeur true, l'exception sera défini
	 * comme le résultat de tous les localisateurs concernés et le lot entier
	 * sera tentée sans exceptions lancées.
	 */
	private boolean exceptionsInResults = false;

	/**
	 * C'est ce que les données ressemble après le partitionnement.
	 */
	private List<ReadFunctionGroup<K>> functionGroups;

	public boolean isContiguousRequests() {
		return contiguousRequests;
	}

	public void setContiguousRequests(boolean contiguousRequests) {
		this.contiguousRequests = contiguousRequests;
	}

	public boolean isErrorsInResults() {
		return errorsInResults;
	}

	public void setErrorsInResults(boolean errorsInResults) {
		this.errorsInResults = errorsInResults;
	}

	public boolean isExceptionsInResults() {
		return exceptionsInResults;
	}

	public void setExceptionsInResults(boolean exceptionsInResults) {
		this.exceptionsInResults = exceptionsInResults;
	}

	public List<ReadFunctionGroup<K>> getReadFunctionGroups() {
		if (functionGroups == null)
			doPartition();
		return functionGroups;
	}

	public void addLocator(K id, ModbusLocator locator) {
		addLocator(new KeyedModbusLocator<K>(id, locator));
	}

	public void addLocator(K id, int slaveId, int range, int offset,
			int dataType) {
		addLocator(new KeyedModbusLocator<K>(id, slaveId, range, offset,
				dataType));
	}

	public void addLocator(K id, int slaveId, int range, int offset, byte bit) {
		addLocator(new KeyedModbusLocator<K>(id, slaveId, range, offset, bit));
	}

	public void addLocator(K id, int slaveId, int registerId, int dataType) {
		RangeAndOffset rao = new RangeAndOffset(registerId);
		addLocator(id, slaveId, rao.getRange(), rao.getOffset(), dataType);
	}

	public void addLocator(K id, int slaveId, int registerId, byte bit) {
		RangeAndOffset rao = new RangeAndOffset(registerId);
		addLocator(id, slaveId, rao.getRange(), rao.getOffset(), bit);
	}

	private void addLocator(KeyedModbusLocator<K> locator) {
		requestValues.add(locator);
	}

	private void doPartition() {
		Map<SlaveAndRange, List<KeyedModbusLocator<K>>> slaveRangeBatch = new HashMap<SlaveAndRange, List<KeyedModbusLocator<K>>>();

		List<KeyedModbusLocator<K>> functionList;
		for (KeyedModbusLocator<K> locator : requestValues) {

			functionList = slaveRangeBatch.get(locator.getSlaveAndRange());
			if (functionList == null) {
				functionList = new ArrayList<KeyedModbusLocator<K>>();
				slaveRangeBatch.put(locator.getSlaveAndRange(), functionList);
			}

			functionList.add(locator);
		}

		Collection<List<KeyedModbusLocator<K>>> functionLocatorLists = slaveRangeBatch
				.values();
		FunctionLocatorComparator comparator = new FunctionLocatorComparator();
		functionGroups = new ArrayList<ReadFunctionGroup<K>>();
		for (List<KeyedModbusLocator<K>> functionLocatorList : functionLocatorLists) {

			Collections.sort(functionLocatorList, comparator);

			if (contiguousRequests)
				createContiguousRequestGroups(functionGroups,
						functionLocatorList);
			else {

				int maxReadCount = RegisterRange
						.getMaxReadCount(functionLocatorList.get(0)
								.getSlaveAndRange().getRange());
				createRequestGroups(functionGroups, functionLocatorList,
						maxReadCount);
			}
		}
	}

	/**
	 * We aren't trying to do anything fancy here, like some kind of artificial
	 * optimal group for performance or anything. We pretty much just try to fit
	 * as many locators as possible into a single valid request, and then move
	 * on.
	 * 
	 * This method assumes the locators have already been sorted by start
	 * offset.
	 */
	private void createRequestGroups(List<ReadFunctionGroup<K>> functionGroups,
			List<KeyedModbusLocator<K>> locators, int maxCount) {
		ReadFunctionGroup<K> functionGroup;
		KeyedModbusLocator<K> locator;
		int index;
		int endOffset;

		while (locators.size() > 0) {
			functionGroup = new ReadFunctionGroup<K>(locators.remove(0));
			functionGroups.add(functionGroup);
			endOffset = functionGroup.getStartOffset() + maxCount - 1;

			index = 0;
			while (locators.size() > index) {
				locator = locators.get(index);
				if (locator.getEndOffset() <= endOffset)

					functionGroup.add(locators.remove(index));
				else {

					if (locator.getOffset() > endOffset)

						break;

					index++;
				}
			}
		}
	}

	private void createContiguousRequestGroups(
			List<ReadFunctionGroup<K>> functionGroups,
			List<KeyedModbusLocator<K>> locators) {
		ReadFunctionGroup<K> functionGroup = null;
		KeyedModbusLocator<K> locator;

		while (locators.size() > 0) {
			locator = locators.remove(0);
			if (functionGroup == null
					|| locator.getOffset() > functionGroup.getEndOffset() + 1) {
				functionGroup = new ReadFunctionGroup<K>(locator);
				functionGroups.add(functionGroup);
			} else
				functionGroup.add(locator);
		}
	}

	class FunctionLocatorComparator implements
			Comparator<KeyedModbusLocator<K>> {
		public int compare(KeyedModbusLocator<K> ml1, KeyedModbusLocator<K> ml2) {
			return ml1.getOffset() - ml2.getOffset();
		}
	}
}
