/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.linkedlistset;

import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.LinkedListSetItem;

/**
 * {@link LinkedListSet} that provides strict adherence to ensuring correctness.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class StrictLinkedListSet<E extends LinkedListSetEntry<E, O>, O>
		implements LinkedListSet<E, O> {

	/**
	 * Head of the {@link LinkedListSet}.
	 */
	private E head = null;

	/**
	 * Tail of the {@link LinkedListSet}.
	 */
	private E tail = null;

	/**
	 * Obtains the owner of this {@link LinkedListSet}.
	 * 
	 * @return Owner of this {@link LinkedListSet}.
	 */
	protected abstract O getOwner();

	/*
	 * ====================== LinkedList ====================================
	 */

	@Override
	public final E getHead() {
		return this.head;
	}

	@Override
	public final E getTail() {
		return this.tail;
	}

	@Override
	public void addEntry(E entry) {

		// Ensure the same owner
		if (entry.getLinkedListSetOwner() != this.getOwner()) {
			throw new IllegalStateException("Invalid "
					+ LinkedListSet.class.getSimpleName()
					+ " owner (entry owner=" + entry.getLinkedListSetOwner()
					+ ", list owner=" + this.getOwner() + ", entry=" + entry
					+ ")");
		}

		// Ensure entry not added (not first or somewhere in list)
		if ((this.head == entry) || (entry.getPrev() != null)) {
			throw new IllegalStateException("Entry already added (entry="
					+ entry + ", list=" + this + ")");
		}

		// Append to end of linked list
		if (this.head == null) {
			// Empty linked list (first entry)
			this.head = entry;
		} else {
			this.tail.setNext(entry);
			entry.setPrev(this.tail);
		}
		this.tail = entry;
	}

	@Override
	public final boolean removeEntry(E entry) {

		// Ensure the same owner
		if (entry.getLinkedListSetOwner() != this.getOwner()) {
			throw new IllegalStateException("Invalid "
					+ LinkedListSet.class.getSimpleName()
					+ " owner (entry owner=" + entry.getLinkedListSetOwner()
					+ ", list owner=" + this.getOwner() + ", entry=" + entry
					+ ")");
		}

		// Ensure the entry is in the list (first or somewhere in list)
		if ((this.head != entry) && (entry.getPrev() == null)) {
			throw new IllegalStateException("Entry not in list (entry=" + entry
					+ ", list=" + this + ")");
		}

		// Flag indicating if last entry in linked list
		boolean isLast = true;

		// Determine if first entry
		if (entry.getPrev() == null) {
			// First entry
			this.head = entry.getNext();
		} else {
			// Middle entry
			entry.getPrev().setNext(entry.getNext());
			isLast = false;
		}

		// Determine if last entry
		if (entry.getNext() == null) {
			// Last entry
			this.tail = entry.getPrev();
		} else {
			// Middle entry
			entry.getNext().setPrev(entry.getPrev());
			isLast = false;
		}

		// Clear entry from being in the list
		entry.setNext(null);
		entry.setPrev(null);

		// Return whether the last entry
		return isLast;
	}

	@Override
	public final E purgeEntries() {

		// Obtain the head of list
		E entry = this.head;

		// Purge the list
		this.head = null;
		this.tail = null;

		// Return the head of the list
		return entry;
	}

	@Override
	public final LinkedListSetItem<E> copyEntries() {

		// Determine if have items in list
		if (this.head == null) {
			// No items
			return null;
		}

		// Copy the list (going backwards)
		E currentEntry = this.tail;
		LinkedListSetItem<E> currentItem = null;
		LinkedListSetItem<E> nextItem = null;
		while (currentEntry != null) {
			// Create item for entry
			currentItem = new LinkedListItemImpl(currentEntry, nextItem);

			// Next iteration
			nextItem = currentItem;
			currentEntry = (E) currentEntry.getPrev();
		}

		// Return the copied list
		return currentItem;
	}

	/**
	 * Implementation of the {@link LinkedListSetItem}.
	 */
	protected class LinkedListItemImpl implements LinkedListSetItem<E> {

		/**
		 * {@link LinkedListSetEntry}.
		 */
		private final E entry;

		/**
		 * Next {@link LinkedListSetItem}.
		 */
		private final LinkedListSetItem<E> next;

		/**
		 * Initiate.
		 * 
		 * @param entry
		 *            {@link LinkedListSetEntry}.
		 * @param next
		 *            Next {@link LinkedListSetItem}.
		 */
		public LinkedListItemImpl(E entry, LinkedListSetItem<E> next) {
			this.entry = entry;
			this.next = next;
		}

		/*
		 * ====================== LinkedListItem =============================
		 */

		@Override
		public E getEntry() {
			return this.entry;
		}

		@Override
		public LinkedListSetItem<E> getNext() {
			return this.next;
		}
	}

}