package ch.ethz.glukas.orderedset;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

import org.junit.Test;

public class OrderedSetTests {
	//sets are assumed to be empty
	
	public static void testSubsets(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 20;
		int testRange = 3*testSize;
		SetTests.sequenceAdd(set, control, testSize);
		SetTests.randomAdd(set, control, testSize, testRange);
		
		//exhaustively test all possible subsets
		for (int lower = 20; lower<=testRange+2; lower++) {
			for (int upper=lower+1; upper<=testRange+2; upper++) {
				assertEqualSubsets(set, control, lower, upper);
			}
		}
		

		System.out.println("OrderedSetTests: testSubsets done.");
	}
	
	public static void testTailSets(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 20;
		int testRange = 3*testSize;
		SetTests.sequenceAdd(set, control, testSize);
		SetTests.randomAdd(set, control, testSize, testRange);
		
		for (int upper=-5; upper<testRange+5; upper++) {
			SortedSet<Integer> subset = set.tailSet(upper);
			SortedSet<Integer> controlSubset = control.tailSet(upper);
			assertEqualSortedSets(subset, controlSubset);
			
			NavigableSet<Integer> nvSubset = set.tailSet(upper, true);
			NavigableSet<Integer> nvControlSubset = control.tailSet(upper, true);
			assertEqualNavigableSets(nvSubset, nvControlSubset);
			
			nvSubset = set.tailSet(upper, false);
			 nvControlSubset = control.tailSet(upper, false);
			assertEqualNavigableSets(nvSubset, nvControlSubset);
		}
		System.out.println("OrderedSetTests: testTailSets done.");
	}
	
	public static void testSubsetModification(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 50;
		int testRange = 3*testSize;
		SetTests.randomAdd(set, control, testSize, testRange);
		
		//test clear operation for subsets
		SortedSet<Integer> subset = set.subSet(testSize/4, testSize/2);
		SortedSet<Integer> controlSubset = control.subSet(testSize/4, testSize/2);
		subset.clear();
		controlSubset.clear();
		SetTests.assertEqualSets(subset, controlSubset);
		SetTests.assertEqualSets(set, control);
		
		//test add operation
		for (int i=testSize/4; i<testSize/2; i++) {
			assertTrue(subset.add(i) == controlSubset.add(i));
			assertTrue(subset.add(i) == controlSubset.add(i));
		}
		SetTests.assertEqualSets(subset, controlSubset);
		SetTests.assertEqualSets(set, control);
		
		//test remove operation
		subset = set.subSet(testSize/5, testSize/2);
		controlSubset = control.subSet(testSize/5, testSize/2);
		for (int i=0; i<testRange; i++) {
			assertTrue(subset.remove(i) == controlSubset.remove(i));
		}
		SetTests.assertEqualSets(subset, controlSubset);
		SetTests.assertEqualSets(set, control);
		
		System.out.println("OrderedSetTests: testSubsetModification done.");
	}
	
	
	
	public static void testPolling(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 100;
		SetTests.randomAdd(set, control, testSize);
		while (set.size() >= 2) {
			assertEquals(set.pollFirst(), control.pollFirst());
			assertEquals(set.pollLast(), control.pollLast());
		}
		System.out.println("OrderedSetTests: testPolling done.");
	}
	
	public static void testNavigation(NavigableSet<Integer> set)
	{
		TreeSet<Integer> control = new TreeSet<Integer>();
		int testSize = 100;
		SetTests.sequenceAdd(set, control, testSize);
		testSetsReturnSameNeighborhoods(set, control);
		
		Random rand = new Random(0);
		for (int i=0; i<testSize; i++) {
			int next = rand.nextInt();
			set.add(next);
			control.add(next);
			testSetsReturnSameNeighborhoods(control, set, next);
		}
		System.out.println("OrderedSetTests: testNavigation done.");
	}
	

	
	public static void testIterator(SortedSet<Integer> set, SortedSet<Integer> controlSet)
	{
		SetTests.randomAdd(set, controlSet, 1000);
		Iterator<Integer> control = controlSet.iterator();
		for (Integer value : set) {
			assertEquals(control.next(), value);
		}
		assertFalse(control.hasNext());
		System.out.println("OrderedSetTests: testIterator done.");
	}
	
	//assumes ascending ordering
	public static void testIterator(SortedSet<Integer> set)
	{
		testIterator(set, new TreeSet<Integer>());
	}

	//tests add, remove, contains and size
	public static void testSortedSet(SortedSet<Integer> set) {
		SortedSet<Integer> controlSet = new TreeSet<Integer>();//golden model
		
		int testSize = 500;
		int testRange = testSize/5;
		Random random = new Random(38);

		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(3);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation > 0) {
				controlSet.add(nextNumber);
				set.add(nextNumber);
			} else {
				controlSet.remove(nextNumber);
				set.remove(nextNumber);
			}
			assertEqualFirstAndLast(set, controlSet);
		}
		
		SetTests.assertEqualSets(set, controlSet);
		System.out.println("OrderedSetTests: testSortedSet done.");
	}

	
	//SUBSETS HELPERS
	
	public static void assertEqualFirstAndLast(SortedSet<Integer> set, SortedSet<Integer> controlSet)
	{
		assertEquals(controlSet.size(), set.size());
		if (controlSet.size() > 0) {
			assertEquals(set.first(), controlSet.first()) ;
			assertEquals(set.last(), controlSet.last()) ;
		}
	}
	
	public static void assertEqualSubsets(NavigableSet<Integer> set, NavigableSet<Integer> control, int lower, int upper)
	{
		//test sorted set interface subsets
		SortedSet<Integer> subset = set.subSet(lower, upper);
		SortedSet<Integer> controlSubset = control.subSet(lower, upper);
		SetTests.assertEqualSets(subset, controlSubset);
		assertEqualFirstAndLast(subset, controlSubset);
		
		//test navigable set interface subsets
		testSubsetsEqual(set, control, lower, upper, false, false);
		testSubsetsEqual(set, control, lower, upper, false, true);
		testSubsetsEqual(set, control, lower, upper, true, false);
		testSubsetsEqual(set, control, lower, upper, true, true);
	}
	
	private static void testSubsetsEqual(NavigableSet<Integer> set, NavigableSet<Integer> control, int lower, int upper, boolean fromInclusive, boolean toInclusive)
	{
		NavigableSet<Integer> subset = set.subSet(lower, fromInclusive, upper, toInclusive);
		NavigableSet<Integer> controlSubset = control.subSet(lower, fromInclusive, upper, toInclusive);
		assertEqualNavigableSets(subset, controlSubset);
	}
	
	
	public static void assertEqualNavigableSets(NavigableSet<Integer> set, NavigableSet<Integer> control)
	{
		assertEqualSortedSets(set, control);
		testSetsReturnSameNeighborhoods(set, control);
	}
	
	public static void assertEqualSortedSets(SortedSet<Integer> set, SortedSet<Integer> control)
	{
		SetTests.assertEqualSets(set, control);
		assertEqualFirstAndLast(set, control);
	}
	
	
	private static void testSetsReturnSameNeighborhoods(NavigableSet<Integer> set, NavigableSet<Integer> control)
	{
		if (control.isEmpty() && set.isEmpty()) return;
		for (int i=control.first()-5; i<control.last()+5; i++) {
			testSetsReturnSameNeighborhoods(control, set, i);
		}
	}
	
	
	private static void testSetsReturnSameNeighborhoods(NavigableSet<Integer> control, NavigableSet<Integer> set, int next)
	{
		assertEquals("floor - error at " + next, control.floor(next), set.floor(next));
		assertEquals("higher - error at " + next, control.higher(next), set.higher(next));
		assertEquals("ceiling - error at " + next, control.ceiling(next), set.ceiling(next));
		assertEquals("lower - error at " + next, control.lower(next), set.lower(next));
	}
	
}
