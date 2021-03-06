package ch.ethz.glukas.orderedset;

import java.util.Arrays;

public class CocoTree implements StaticSearchTree {

	//does not rebuild the keys of the tree. Call rebuild() or update(Integer.MinValue, Integer,MaxValue) before using (except if content.length == 1, then it doesn't matter)
	public CocoTree(int[] content)
	{
		assert BinaryMath.isPowerOfTwo(content.length);
		assert content.length > 0;//TODO: generalize to handle 0 length content
	
		internalKeys = content;
		
		int magnitude = BinaryMath.log(content.length);
		int height = magnitude/nodeMagnitude;
		boolean leftover = magnitude == 0 || height*nodeMagnitude<magnitude;
		
		if (leftover) {
			height++;
		}
		treeHeight = height;
		
		assert treeHeight > 0;
		
		if (height == 1) {
			numberOfKeysInTheRootNode = content.length;
			children = new int[1];
			tree = content;
			
		} else {

			numberOfKeysInTheRootNode = leftover ? BinaryMath.powerOfTwo(magnitude-(magnitude/nodeMagnitude)*nodeMagnitude) : nodeSize;
			
			int numberOfNodes = numberOfKeysInTheRootNode+numberOfKeysInTheRootNode*numberOfNodesForHeight(height-1);
			children = new int[numberOfNodes];
			tree = new int[numberOfNodes];
			rebuildChildrenPointers();
		}
		
		assert height == 1 || numberOfKeysInTheRootNode > 1;
		assert numberOfKeysInTheRootNode <= nodeSize;
		
	}
	
	
	public boolean contains(int key)
	{
		int index = indexOf(key);
		return internalKeys[index] == key;
		
		//explicit navigation
		/*int currentIndex = 0;
		int depth = 1;
		while (depth < treeHeight) {
			currentIndex = children[currentIndex+baseCaseFind(key, currentIndex)];
			depth++;
		}
		return tree[currentIndex+baseCaseFind(key, currentIndex)] == key;*/
	}
	
	public int indexOf(int key)
	{
		int index = baseCaseFind(key, 0);
		if (treeHeight > 1) {
			index = index*numberOfLeavesForHeight(treeHeight-1)+find(key, children[index], treeHeight-1);
		}
		return index;
	}
	
	//both values are inclusive
	public void update(int smallestValueToUpdate, int largestValueToUpdate)
	{
		rebuildKeys(0, 0, smallestValueToUpdate, largestValueToUpdate);
		assert internalKeysAndTreeConsistent();
		assert checkInvariants();
	}

	//only invoke on full subtrees
	//T(K^2) = T(K) + O(1) = O(log(K)) (note that if we divide a tree of size K^2 in half by height, the resulting subtrees have size K)
	//returns the index of the leaf the search should continue with (with respect to the parent tree)
	private int find(int key, int rootIndex, int height)
	{
		//base case
		if (height == 1) {
			return baseCaseFind(key, rootIndex);
		}
		
		//subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		//recurse on the top half of the tree
		int topindex = find(key, rootIndex, topTreeHeight);
		//check if result is within range
		assert topindex >= 0;
		assert topindex < numberOfLeavesForHeight(bottomTreeHeight);
		
		//recurse on one of the bottom trees (the topindex-th bottom tree)
		int bottomFoundIndex = find(key, rootIndex+topTreeSize+topindex*bottomTreeSize, bottomTreeHeight);
		//check if result is within range
		assert bottomFoundIndex >= 0;
		assert bottomFoundIndex < numberOfLeavesForHeight(bottomTreeHeight);
		
		return bottomFoundIndex+(topindex*numberOfLeavesForHeight(bottomTreeHeight));
	}
	
	private int baseCaseFind(int key, int rootIndex)
	{
		int maxIndex = rootIndex+sizeOfNodeAtIndex(rootIndex);
		int currentIndex = rootIndex+1;
		while (currentIndex < maxIndex && tree[currentIndex] <= key) {
			currentIndex++;
		}
		return currentIndex-rootIndex-1;
	}
	
	
	//updates the tree to reflect the current state of the content array
	//returns the current object (for convenience)
	public CocoTree rebuild()
	{
		assert isSorted(internalKeys);
		if (treeHeight > 1) {
			totalRebuildKeys(0, 0);
		}
		assert internalKeysAndTreeConsistent();
		assert checkInvariants();
		return this;
	}
	
	
	private void rebuildChildrenPointers()
	{
		int sizeOfSubtrees = numberOfNodesForHeight(treeHeight-1);
		int currentIndex = numberOfKeysInTheRootNode;
		for (int i=0; i<numberOfKeysInTheRootNode; i++) {
			rebuildChildrenPointers(currentIndex, treeHeight-1, null, 0);
			children[i] = currentIndex;
			currentIndex += sizeOfSubtrees;
		}
	}
	
	
	//(re)builds a minimum tree
	//assumes all internalKeys are distinct and sorted in ascending order
	private void rebuildKeys(int rootIndex, int layerIndex, int smallestValueToUpdate, int largestValueToUpdate)
	{
		if (isLeafIndex(rootIndex)) {//base case : replace node
			System.arraycopy(internalKeys, layerIndex, tree, rootIndex, sizeOfNodeAtIndex(rootIndex));
		} else {
			
			int currentIndex = rootIndex;
			int remainingChildren = sizeOfNodeAtIndex(rootIndex);
			int currentLayerIndex = layerIndex*nodeSize;
			
			
			while (remainingChildren > 0 && tree[currentIndex] <= largestValueToUpdate) {
				
				if (remainingChildren == 1 || tree[currentIndex+1] >= smallestValueToUpdate) {
					//update:
					rebuildKeys(children[currentIndex], currentLayerIndex, smallestValueToUpdate, largestValueToUpdate);
					tree[currentIndex] = tree[children[currentIndex]];
				}
				
				remainingChildren--;
				currentIndex++;
				currentLayerIndex += nodeSize;
			}
			
		}
	}
	
	private void totalRebuildKeys(int rootIndex, int layerIndex)
	{
		rebuildKeys(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		/*alternative rebuild algorithm (if there wouldn't exist an absolute minimum and maximum element, use this algorithm)
		if (isLeafIndex(rootIndex)) {//base case : replace node
			System.arraycopy(internalKeys, layerIndex, tree, rootIndex, sizeOfNodeAtIndex(rootIndex));
		} else {
			
			int currentIndex = rootIndex;
			int remainingChildren = sizeOfNodeAtIndex(rootIndex);
			int currentLayerIndex = layerIndex*nodeSize;
			
			while (remainingChildren > 0) {
				
				rebuildKeys(children[currentIndex], currentLayerIndex, smallestValueToUpdate, largestValueToUpdate);
				tree[currentIndex] = tree[children[currentIndex]];
				
				remainingChildren--;
				currentIndex++;
				currentLayerIndex += nodeSize;
			}
		}*/
	}
	
	//only invoke on full subtrees
	private void rebuildChildrenPointers(int rootIndex, int height, int[] indexesOfChildren, int offset)
	{
		//base case
		if (height == 1) {
			if (indexesOfChildren != null) {//if indexesOfChildren is null, the node is a leaf and it has no children
				//int length = sizeOfNodeAtIndex(rootIndex);
				System.arraycopy(indexesOfChildren, offset, children, rootIndex, nodeSize);
			}
			return;
		}

		//calculate subtree properties
		int topTreeHeight = height/2;
		int bottomTreeHeight =  height-topTreeHeight;
		
		int topTreeSize = numberOfNodesForHeight(topTreeHeight);
		int bottomTreeSize = numberOfNodesForHeight(bottomTreeHeight);
		
		int numberOfChildrenOfTheTopTree = numberOfLeavesForHeight(topTreeHeight);
		int numberOfChildrenOfEachBottomTree = numberOfLeavesForHeight(bottomTreeHeight);
		
		//recursively build the bottom trees and save their indexes
		int [] bottomTreeIndexes = new int[numberOfChildrenOfTheTopTree];
		
		int currentIndex = rootIndex+topTreeSize;
		for (int i=0; i<numberOfChildrenOfTheTopTree; i++) {
			bottomTreeIndexes[i] = currentIndex;
			rebuildChildrenPointers(currentIndex, bottomTreeHeight, indexesOfChildren, offset);//maybe it would be more cache efficient to first write the bottomTreeIndexes array before recursing?
			currentIndex += bottomTreeSize;
			offset += numberOfChildrenOfEachBottomTree;
		}
		
		//recursively build the top trees
		rebuildChildrenPointers(rootIndex, topTreeHeight, bottomTreeIndexes, 0);
	}
	
	
	private static int numberOfLeavesForHeight(int height)
	{
		return BinaryMath.powerOfTwo(nodeMagnitude*height);
	}
	
	private static int numberOfNodesForHeight(int height)
	{
		//q+q^2+...+q^k = (q-q^(k+1))/(1-q)
		//TODO: investigate more elegant solution
		long size = (long)1 << (nodeMagnitude*(height+1));
		int result = (int)((nodeSize-size)/(1-nodeSize));
		assert result > 0;
		return result;
	}
	
	
	private int sizeOfNodeAtIndex(int index)
	{
		return (index > 0) ? nodeSize : numberOfKeysInTheRootNode;//the root might have less children
	}
	private boolean isLeafIndex(int index)
	{
		return children[index] == 0;
	}
	
	////
	//INSTANCE VARIABLES
	///
	
	private final int[] children;
	private final int[] tree;
	private final int treeHeight;
	private final int[] internalKeys;
	private final int numberOfKeysInTheRootNode;
	
	////
	//CONSTANTS
	////

	private static final int nodeMagnitude = 5;
	private static final int nodeSize = BinaryMath.powerOfTwo(nodeMagnitude);
	
	////
	//INVARIANTS & ASSERTIONS
	////
	
	protected boolean isSorted(int[] array)
	{
		if (array.length == 0) return true;
		int last = array[0];
		boolean result = true;
		for (int i=1; i<array.length; i++) {
			result = result && array[i] >= last;
			last = array[i];
		}
		return result;
	}
	
	protected boolean checkInvariants()
	{
		boolean result = isSorted(internalKeys);
		assert result;
		result = result && subtreesConsistent(0);
		assert result;
		return result;
	}
	
	protected boolean subtreesConsistent(int rootIndex) {
		
		if (isLeafIndex(rootIndex)) {
			return true;
		}
		boolean result = true;
		for (int i=0; i<nodeSize; i++) {
			if (children[rootIndex+i] == 0) break;
			
			result = result && tree[rootIndex+i] == tree[children[rootIndex+i]];
			assert result;
			result = result && subtreesConsistent(children[rootIndex+i]);
		}
		assert result;
		return result;
	}
	
	protected void copyKeysFromTree(int rootIndex, int[] targetArray, int targetIndex)
	{
		if (isLeafIndex(rootIndex)) {
			System.arraycopy(tree, rootIndex, targetArray, targetIndex, sizeOfNodeAtIndex(rootIndex));
			return;
		}
		
		int length = sizeOfNodeAtIndex(rootIndex);
		
		for (int i=0; i<length; i++) {
			copyKeysFromTree(children[rootIndex+i], targetArray, (targetIndex+i)*nodeSize);
		}
	}
	
	protected boolean internalKeysAndTreeConsistent()
	{
		int[] inOrder = new int[internalKeys.length];
		copyKeysFromTree(0, inOrder, 0);
		boolean result = true;
		for (int i=0; i<internalKeys.length; i++) {
			result = result && internalKeys[i] == inOrder[i];
			assert result;
		}
		return result;
	}
	
}
