package ch.ethz.glukas.orderedset;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.NavigableSet;

/*
 * Provides a general implementation for subsets to a navigable set
 * The navigable set has to implement some additional methods "sizeOfRange" and "removeRange"
 * Remove range may be implemented by subsequently polling from a SortedSubset.
 */

class SortedSubset<T> extends AbstractCollection<T> implements NavigableSet<T> {

	//TODO: Implement unbounded ranges
	public SortedSubset(RangeSet<T> constitutingSuperset, T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive)
	{
		init(constitutingSuperset, lowerbound, upperbound, fromInclusive, toInclusive);
	}
	
	public SortedSubset(RangeSet<T> constitutingSuperset, T lowerbound, T upperbound)
	{
		init(constitutingSuperset, lowerbound, upperbound, true, false);
	}
	
	private void init(RangeSet<T> constitutingSuperset, T lowerbound, T upperbound, boolean lowerboundInclusive, boolean upperboundInclusive)
	{
		this.superset = constitutingSuperset;
		this.lower = lowerbound;
		this.upper = upperbound;
		this.fromInclusive = lowerboundInclusive;
		this.toInclusive = upperboundInclusive;
	}


	
	@Override
	/**
	 * The set will throw an IllegalArgumentException on an attempt to insert an element outside its range.
	 */
	public boolean add(T e) {
		if (!isInsideRange(e)) throw new IllegalArgumentException();
		
		return superset.add(e);
	}

	
	public boolean isInsideRange(T e)
	{
		return !isBelowRange(e) && !isAboveRange(e);
	}

	public boolean isBelowRange(T e)
	{
		boolean result;
		if (fromInclusive) {
			result = comparator().compare(e, lower) < 0;
		} else {
			result = comparator().compare(e, lower) <= 0;
		}
		return result;
	}
	
	public boolean isAboveRange(T e)
	{
		boolean result;
		if (toInclusive) {
			result = comparator().compare(e, upper) > 0;
		} else {
			result = comparator().compare(e, upper) >= 0;
		}
		return result;
	}
	
	///
	//SORTED SET
	///
	
	@Override
	public void clear() {
		
		superset.removeRange(lower, upper, fromInclusive, toInclusive);
		
		//Alternative direct implementation:
		//while (pollFirst() != null) {
		//}
		
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		if (isInsideRange((T)o)) {
			return superset.contains(o);
		}
		return false;
	}
	

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (o != null && isInsideRange((T)o)) {
			return superset.remove(o);
		}
		return false;
	}
	

	@Override
	public int size() {
		return superset.sizeOfRange(lower, upper, fromInclusive, toInclusive);
	}

	@Override
	public Comparator<? super T> comparator() {
		return superset.comparator();
	}

	@Override
	public T first() {
		T found;
		if (fromInclusive) {
			found = superset.ceiling(lower);
		} else {
			found = superset.higher(lower);
		}
		return nullIfOutOfBounds(found);
	}

	@Override
	public SortedSet<T> headSet(T arg0) {
		return subSet(lower, arg0);
	}

	@Override
	public T last() {
		T found;
		if (toInclusive) {
			found = superset.floor(upper);
		} else {
			found = superset.lower(upper);
		}
		return nullIfOutOfBounds(found);
	}

	@Override
	/**
	 * Throws an IllegalArgumentException if the lower bound is smaller or the upper bound is larger than that of this subset
	 * if arg0 == lower and fromInclusive == false, an Exception is thrown since the resulting set would not be a subset
	 */
	public SortedSet<T> subSet(T arg0, T arg1) {
		
		return subSet(arg0, true, arg1, false);
	}

	@Override
	public SortedSet<T> tailSet(T arg0) {
		return subSet(arg0, upper);
	}
	
	///
	//NAVIGABLE SET
	//Operations rely on the superset implementation. All they do is bounds checking.
	///
	
	@Override
	public T ceiling(T e) {
		T found = null;
		if (isBelowRange(e)) {
			found = first();
		} else if (!isAboveRange(e)) {
			found = superset.ceiling(e);
		}
		
		return nullIfOutOfBounds(found);
	}
	

	@Override
	public T floor(T e) {
		T found = null;
		if (isAboveRange(e)) {
			found = last();
		} else if (!isBelowRange(e)) {
			found = superset.floor(e);
		}
		
		return nullIfOutOfBounds(found);
	}

	@Override
	public T higher(T e) {

		T found = null;
		if (isBelowRange(e)) {
			found = first();
		} else if (!isAboveRange(e)) {
			found = superset.higher(e);
		}
		
		return nullIfOutOfBounds(found);
	}

	@Override
	public T lower(T e) {
		T found = null;
		if (isAboveRange(e)) {
			found = last();
		} else if (!isBelowRange(e)) {
			found = superset.lower(e);
		}
		
		return nullIfOutOfBounds(found);
	}

	public T nullIfOutOfBounds(T value)
	{
		if (value == null || !isInsideRange(value)) return null;
		return value;
	}
	
	
	
	@Override
	public T pollFirst() {
		T first = first();
		remove(first);
		return first;
	}

	@Override
	public T pollLast() {
		T last = last();
		remove(last);
		return last;
	}

	
	
	@Override
	public NavigableSet<T> headSet(T toElement, boolean inclusive) {
		return subSet(lower, fromInclusive, toElement, inclusive);
	}

	@Override
	public Iterator<T> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<T> descendingSet() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	/**
	 * Throws an exception if the new bounds exceed the bounds of this set
	 */
	public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
		//check bounds
		int fromComparison = comparator().compare(fromElement, lower);
		int toComparison = comparator().compare(toElement, upper);
		if (fromComparison < 0) throw new IllegalArgumentException();
		if (fromInclusive && !this.fromInclusive && fromComparison == 0) throw new IllegalArgumentException();
		if (toComparison > 0) throw new IllegalArgumentException();
		if (toInclusive && !this.toInclusive && toComparison == 0) throw new IllegalArgumentException();
		//get a subset from the superset
		return superset.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}

	@Override
	public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
		return subSet(fromElement, inclusive, upper, toInclusive);
	}
	
	
	///
	//INSTANCE VARIABLES
	///
	
	private boolean toInclusive;
	private boolean fromInclusive;
	private RangeSet<T> superset;
	private T lower;
	private T upper;
	
}
