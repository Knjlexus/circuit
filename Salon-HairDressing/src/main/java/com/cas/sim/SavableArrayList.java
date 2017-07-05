package com.cas.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 * @author 张振宇 Jul 24, 2015 3:35:40 PM
 */
public class SavableArrayList<E extends Savable> implements List<E>, Savable, Cloneable {

	private Class<E> elementType;
	private List<E> buffer;
	private E[] backingArray;
	private int size = 0;

	public SavableArrayList(Class<E> elementType) {
		this.elementType = elementType;
	}

	public SavableArrayList(Class<E> elementType, Collection<? extends E> c) {
		this.elementType = elementType;
		addAll(c);
	}

	protected final <T> T[] createArray(Class<T> type, int size) {
		return (T[]) java.lang.reflect.Array.newInstance(type, size);
	}

	protected final E[] createArray(int size) {
		return createArray(elementType, size);
	}

	/**
	 * Returns a current snapshot of this List's backing array that is guaranteed not to change through further List manipulation. Changes to this array may or may not be reflected in the list and should be avoided.
	 */
	public final E[] getArray() {
		if (backingArray != null) return backingArray;

		if (buffer == null) {
			backingArray = createArray(0);
		} else {
			// Only keep the array or the buffer but never both at
			// the same time. 1) it saves space, 2) it keeps the rest
			// of the code safer.
			backingArray = buffer.toArray(createArray(buffer.size()));
			buffer = null;
		}
		return backingArray;
	}

	protected final List<E> getBuffer() {
		if (buffer != null) return buffer;

		if (backingArray == null) {
			buffer = new ArrayList();
		} else {
			// Only keep the array or the buffer but never both at
			// the same time. 1) it saves space, 2) it keeps the rest
			// of the code safer.
			buffer = new ArrayList(Arrays.asList(backingArray));
			backingArray = null;
		}
		return buffer;
	}

	@Override
	public final int size() {
		return size;
	}

	@Override
	public final boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public Object[] toArray() {
		return getArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {

		E[] array = getArray();
		if (a.length < array.length) {
			return (T[]) Arrays.copyOf(array, array.length, a.getClass());
		}

		System.arraycopy(array, 0, a, 0, array.length);

		if (a.length > array.length) {
			a[array.length] = null;
		}

		return a;
	}

	@Override
	public boolean add(E e) {
		boolean result = getBuffer().add(e);
		size = getBuffer().size();
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = getBuffer().remove(o);
		size = getBuffer().size();
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return Arrays.asList(getArray()).containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = getBuffer().addAll(c);
		size = getBuffer().size();
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = getBuffer().addAll(index, c);
		size = getBuffer().size();
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = getBuffer().removeAll(c);
		size = getBuffer().size();
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = getBuffer().retainAll(c);
		size = getBuffer().size();
		return result;
	}

	@Override
	public void clear() {
		getBuffer().clear();
		size = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof List)) // covers null too
			return false;
		List other = (List) o;
		Iterator i1 = iterator();
		Iterator i2 = other.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			Object o1 = i1.next();
			Object o2 = i2.next();
			if (o1 == o2) continue;
			if (o1 == null || !o1.equals(o2)) return false;
		}
		return !(i1.hasNext() || !i2.hasNext());
	}

	@Override
	public int hashCode() {
		// Exactly the hash code described in the List interface, basically
		E[] array = getArray();
		int result = 1;
		for (E e : array) {
			result = 31 * result + (e == null ? 0 : e.hashCode());
		}
		return result;
	}

	@Override
	public final E get(int index) {
		if (backingArray != null) return backingArray[index];
		if (buffer != null) return buffer.get(index);
		throw new IndexOutOfBoundsException("Index:" + index + ", Size:0");
	}

	@Override
	public E set(int index, E element) {
		return getBuffer().set(index, element);
	}

	@Override
	public void add(int index, E element) {
		getBuffer().add(index, element);
		size = getBuffer().size();
	}

	@Override
	public E remove(int index) {
		E result = getBuffer().remove(index);
		size = getBuffer().size();
		return result;
	}

	@Override
	public int indexOf(Object o) {
		E[] array = getArray();
		for (int i = 0; i < array.length; i++) {
			E element = array[i];
			if (element == o) {
				return i;
			}
			if (element != null && element.equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		E[] array = getArray();
		for (int i = array.length - 1; i >= 0; i--) {
			E element = array[i];
			if (element == o) {
				return i;
			}
			if (element != null && element.equals(o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ArrayIterator<E>(getArray(), 0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ArrayIterator<E>(getArray(), index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {

		// So far JME doesn't use subList that I can see so I'm nerfing it.
		List<E> raw = Arrays.asList(getArray()).subList(fromIndex, toIndex);
		return Collections.unmodifiableList(raw);
	}

	@Override
	public String toString() {

		E[] array = getArray();
		if (array.length == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < array.length; i++) {
			if (i > 0) sb.append(", ");
			E e = array[i];
			sb.append(e == this ? "(this Collection)" : e);
		}
		sb.append(']');
		return sb.toString();
	}

	protected class ArrayIterator<E> implements ListIterator<E> {
		private E[] array;
		private int next;
		private int lastReturned;

		protected ArrayIterator(E[] array, int index) {
			this.array = array;
			this.next = index;
			this.lastReturned = -1;
		}

		@Override
		public boolean hasNext() {
			return next != array.length;
		}

		@Override
		public E next() {
			if (!hasNext()) throw new NoSuchElementException();
			lastReturned = next++;
			return array[lastReturned];
		}

		@Override
		public boolean hasPrevious() {
			return next != 0;
		}

		@Override
		public E previous() {
			if (!hasPrevious()) throw new NoSuchElementException();
			lastReturned = --next;
			return array[lastReturned];
		}

		@Override
		public int nextIndex() {
			return next;
		}

		@Override
		public int previousIndex() {
			return next - 1;
		}

		@Override
		public void remove() {
			SavableArrayList.this.remove(array[lastReturned]);
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.jme3.export.Savable#write(com.jme3.export.JmeExporter)
	 */
	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule out = ex.getCapsule(this);
		out.write(size, "size", 0);
		out.write(backingArray, "backingArray", null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.jme3.export.Savable#read(com.jme3.export.JmeImporter)
	 */
	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule in = im.getCapsule(this);
		size = in.readInt("size", 0);
		backingArray = (E[]) in.readSavableArray("backingArray", null);
	}
}
