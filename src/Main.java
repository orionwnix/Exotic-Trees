import java.io.IOException;
import java.util.*;

import ch.ethz.glukas.orderedset.*;

class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		testSet(new TreeSet<Integer>());

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Treap<Integer> treap = new Treap<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1,Integer o2) {
            	
            	return  ((Integer) o1.compareTo(o2));
            }
        });
		testSet(treap);
		treap.clear();
		//randomTestSet(treap);
		treap.clear();
		//performanceTestSet(treap, 80000);
		//performanceTestSet(new HashSet<Integer>(), 80000);
		//performanceTestSet(new TreeSet<Integer>(), 80000);
		
	}
	
	
	public static void randomTestSet(Set<Integer> set)
	{
		//use a proven java.util set as golden model
		Set<Integer> controlSet = new HashSet<Integer>();
		
		int testSize = 10000;
		int testRange = testSize/5;
		Random random = new Random(9);
		Date start = new Date();
		
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
		}
		
		if (controlSet.size() != set.size()) throw new Error();
		for (Integer number : controlSet) {
			if (!controlSet.contains(number)) throw new Error();
		}
		
		Date end = new Date();
		System.out.println("randomized test set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
	}
	
	public static void testSet(Set<Integer> set)
	{
		Set<Integer> controlSet = new HashSet<Integer>();
		
		int testSize = 5000;
		Random random = new Random(5);
		
		Date start = new Date();
		for (int i=0; i<testSize; i++) {
			set.add(i);
			controlSet.add(i);
			if (!set.contains(i)) throw new Error();
		}
		if (set.size() != controlSet.size()) throw new Error();
		
		
		for (int i=0; i<testSize; i++) {
			int next = random.nextInt();
			set.add(next);
			controlSet.add(next);
			if (!set.contains(next)) throw new Error();
		}
		for (int i=0; i<testSize; i++) {
			if (!set.contains(i)) throw new Error();
		}
		for (int i=0; i<testSize; i++) {
			set.remove(i);
			controlSet.remove(i);
			if (set.contains(i)) throw new Error();
		}
		
		for (Integer i : controlSet) {
			if (!set.contains(i)) throw new Error();
		}
		set.clear();
		if (set.size() > 0) throw new Error();
		Date end = new Date();
		System.out.println("test set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
		
	}
	
	static void performanceTestSet(Set<Integer> set, int testSize)
	{
		int testRange = testSize/5;
		Random random = new Random(3);
		Date start = new Date();
		
		for (int i=0; i< testSize; i++) {
			int nextOperation = random.nextInt(10);
			int nextNumber = random.nextInt(testRange);
			if (nextOperation < 5) {
				set.add(nextNumber);
			} else if (nextOperation < 7) {
				set.remove(nextNumber);
			} else {
				set.contains(nextNumber);
			}
			
		}
		Date end = new Date();
		System.out.println("randomized performance test set done!" + " took " + (end.getTime()-start.getTime())/ 1000.0 + " s");
	}


}