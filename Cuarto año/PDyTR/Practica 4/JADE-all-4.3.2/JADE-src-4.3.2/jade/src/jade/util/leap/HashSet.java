package jade.util.leap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HashSet implements Set, Cloneable, Serializable {

	private static final long serialVersionUID = 5439595441274382166L;

	private transient java.util.HashSet internalHashSet; 

	
	public HashSet() {
		internalHashSet = new java.util.HashSet();
	}

	public HashSet(Collection c) {
		internalHashSet = new java.util.HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			internalHashSet.add(it.next());
		}
	}

	public HashSet(int initialCapacity) {
		internalHashSet = new java.util.HashSet(initialCapacity);
	}

    public boolean add(Object o) {
		return internalHashSet.add(o);
	}

	public boolean isEmpty() {
		return internalHashSet.isEmpty();
	}

	public Iterator iterator() {
        return new Iterator() {
			java.util.Iterator it = internalHashSet.iterator();

			/**
			 * @see jade.util.leap.Iterator interface
			 */
			public boolean hasNext() {
				return it.hasNext();
			}

			/**
			 * @see jade.util.leap.Iterator interface
			 */
			public Object next() {
				return it.next();
			}

			/**
			 * @see jade.util.leap.Iterator interface
			 */
			public void remove() {
				it.remove();
			}
		};
	}

	public boolean remove(Object o) {
		return internalHashSet.remove(o);
	}

	public int size() {
		return internalHashSet.size();
	}

	public Object[] toArray() {
		return internalHashSet.toArray();
	}

	public void clear() {
		internalHashSet.clear();
	}

	public boolean contains(Object o) {
		return internalHashSet.contains(o);
	}

	public Object clone() {
		try {
			HashSet newSet;
			newSet = (HashSet)super.clone();
			newSet.internalHashSet = (java.util.HashSet)internalHashSet.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public int hashCode() {
		int h = 0;
		Iterator i = iterator();
		while (i.hasNext()) {
			Object obj = i.next();
			if (obj != null)
				h += obj.hashCode();
		}
		return h;
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o instanceof Set))
			return false;
		Collection c = (Collection) o;
		if (c.size() != size())
			return false;
		try {
			return containsAll(c);
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.writeInt(internalHashSet.size());

		java.util.Iterator it = internalHashSet.iterator();

		while (it.hasNext()) {
			out.writeObject(it.next());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		int size = in.readInt();

		internalHashSet = new java.util.HashSet(size);

		for (int i = 0; i < size; i++) {
			Object o = in.readObject();
			internalHashSet.add(o);
		}
	}

	public boolean addAll(Collection c) {
		boolean modified = false;

		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			if (internalHashSet.add(iter.next())) {
				modified = true;
			}
		}

		return modified;
	}

	public boolean containsAll(Collection c) {
		java.util.Iterator iter = c.iterator();

		while (iter.hasNext()) {
			if(!internalHashSet.contains(iter.next())) {
				return false;
			}
		}

		return true;
	}

	public boolean removeAll(Collection c) {
		boolean modified = false;

		Iterator iter = c.iterator();

		while (iter.hasNext()) {
			Object o = iter.next();
			if (internalHashSet.contains(o)) {
				internalHashSet.remove(o);
				modified = true;
			}
		}
		return modified;
	}

	public Object[] toArray(Object[] a) {
		return internalHashSet.toArray();
	}
}
