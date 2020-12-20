package ottop.sudoku.group;

import ottop.sudoku.*;

import java.util.*;
import java.util.Map.Entry;

public abstract class AbstractGroup {
	private boolean[] hasDigit = new boolean[10]; // map that tells which digits are currently contained
	private Map<Coord, Integer> coords; // the cell coordinates contained in this group, mapped to internal index (0..9)
	private int[] digits = new int[9];
	protected int startX;
	protected int startY;
	private String groupID;

	public AbstractGroup(int startX, int startY, StandardPuzzle myPuzzle, String id) {
		this.startX = startX;
		this.startY = startY;
		this.groupID = id;
		for (int i=0; i<9; i++) {
			digits[i] = myPuzzle.getInternalRepresentation(startY+internalIndexToRelativeY(i), startX+internalIndexToRelativeX(i));
			if (digits[i] != -1) {
				hasDigit[digits[i]] = true;
			}
		}
		
		coords = new HashMap<Coord, Integer>();
		for (int internalIndex=0; internalIndex<9; internalIndex++) {
			int absX = startX + internalIndexToRelativeX(internalIndex);
			int absY = startY + internalIndexToRelativeY(internalIndex);
			coords.put(new Coord(absX, absY), internalIndex);
		}
	}

	public abstract int internalIndexToRelativeX(int idx);
	public abstract int internalIndexToRelativeY(int idx);
	
	private boolean isInGroup(Coord c) {
		return coords.containsKey(c);
	}
	
	private boolean isOccupied(Coord c) {
		Integer val = coords.get(c);
		if (val == null) return false;
		return digits[val.intValue()] != -1;
	}
	
	public boolean isPossibility(int n, Coord c) {
		if (isInGroup(c)) {
			if (hasDigit[n]) return false;
			if (isOccupied(c)) return false;
		}
		return true;
	}
	
	private Integer getUniquePossibility(PossibilitiesContainer cache,
										 SolutionContainer sols, Coord myCell) {
		Set<Integer> remainingPossibilities = new HashSet<>(cache.getPossibilities(myCell));
		for (Coord otherCell : coords.keySet()) {
			if (otherCell != myCell) {
				remainingPossibilities.removeAll(cache.getPossibilities(otherCell));
			}
		}
		if (remainingPossibilities.size() == 1) {
			return remainingPossibilities.iterator().next();
		}
		return null;
	}

	public boolean addLoneNumbersToSolution(PossibilitiesContainer cache, SolutionContainer sols) {
		boolean found = false;

		for (Coord myCell : coords.keySet()) {
			if (coords.get(myCell) != null && digits[coords.get(myCell)] != -1) continue;
			
			Set<Integer> cellPossibilities = cache.getPossibilities(myCell);
			if (cellPossibilities != null) {
				if (cellPossibilities.size() == 1) {
					Integer loneNumber = cellPossibilities.iterator().next();
					sols.addSolution(myCell, loneNumber, null); // group ID is not relevant
					found = true;
				}
			}
		}
		return found;
	}

	public boolean addUniqueValuesToSolution(PossibilitiesContainer poss, SolutionContainer sols) {
		boolean found = false;

		for (Coord myCell : coords.keySet()) {
			if (coords.get(myCell) != null && digits[coords.get(myCell)] != -1) continue;
			
			Set<Integer> cellPossibilities = poss.getPossibilities(myCell);
			if (cellPossibilities != null) {
				if (cellPossibilities.size() >= 1) {
					Integer uniquePossibility = getUniquePossibility(poss, sols, myCell);
					if (uniquePossibility != null) {
						sols.addSolution(myCell, uniquePossibility, groupID);
						found = true;
					}
				}
			}
		}
		return found;
	}
	
	public boolean solved() {
		for (int n : Digits.all) if (!hasDigit[n]) return false;
		return true;
	}

	public Set<Coord> getCoords() {
		return coords.keySet();
	}

	public boolean eliminateNakedPairs(IPuzzle p,
									   PossibilitiesContainer cache) {
		
		boolean result = false;
		
		// create map from sets of possibilities to the coordinates (in this group) that have those (same) possibilities
		Map<Set<Integer>, Set<Coord>> map = new LinkedHashMap<>();
		for (Coord c : coords.keySet()) {
			if (!isOccupied(c)) {
				Set<Integer> pc = cache.getPossibilities(c);
				Set<Coord> coordSet = map.computeIfAbsent(pc, k -> new HashSet<>());
				coordSet.add(c);
			}
		}

		// find groups of cells that all have the same possibilities, and which is of the same size as the
		// nr of possibilities: the possibilities can be removed from the rest of the group
		if (eliminateInGroup(cache, result, map, "Simple Naked Pairs")) result = true;
		
		// Combine elements in this map. For example, if there are entries
		// {26} --> [a], {27} --> [c], {26} --> [c] can be combined
		// to a new entry {267} --> [abc]
		combineNakedPairs(map);

		// Do further elimination (in two steps just to improve reporting)
		if (eliminateInGroup(cache, result, map, "Extended Naked Pairs")) result = true;
		
		return result;
	}

	private boolean eliminateInGroup(PossibilitiesContainer cache,
									 boolean result, Map<Set<Integer>, Set<Coord>> map,
									 String reason) {
		for (Entry<Set<Integer>, Set<Coord>> entry : map.entrySet()) {
		    Set<Integer> possibilities = entry.getKey();
		    Set<Coord> coordinates = entry.getValue();
		    if (possibilities.size() > 1 && possibilities.size() == coordinates.size()) {
		    	// remove all of the digits from 'possibilities' from
		    	// the rest of the possibilities in this group
		    	Set<Coord> restOfGroup = new HashSet<Coord>(coords.keySet());
		    	restOfGroup.removeAll(coordinates);
	    		for (Coord c : restOfGroup) {
	    			if (cache.removePossibilities(possibilities, c, " in " + groupID + 
	    					" because " + possibilities + 
	    					" have to be in one of " + coordinates + " (" + reason + ")")) {
	    				result = true;
	    			}
	    		}
		    }
		}
		return result;
	}

	private void combineNakedPairs(Map<Set<Integer>, Set<Coord>> map) {
		final int range = (1 << 9); // range of possibilities for 9 digits: 2^9
		final int mask = range - 1;
		int groupFillSize = 0;
		for (boolean b : hasDigit) {
			if (b) groupFillSize++;
		}

		// Representing the possibilities in a bitmap (9 digits), find out which of 
		// all possible bitmaps (total of 512, 2^9), are a superset of the bitmap of
		// a set of possibilities. Combine the mapped coordinates of those.
		Map<Integer, Set<Coord>> newCombinationsMap = new HashMap<Integer, Set<Coord>>();
		for (Set<Integer> key : map.keySet()) {
			int keyAsBitSet = toBitSet(key);
			for (int counter=0; counter<range; counter++) {
				// bitwise operation to verify that all of "key" are contained in the digit set represented by "i"
				if ((counter | (~keyAsBitSet & mask)) == mask) {
					Set<Coord> coords = newCombinationsMap.get(counter);
					if (coords == null) {
						coords = new HashSet<Coord>();
						newCombinationsMap.put(counter, coords);
					}
					Set<Coord> originalCoords = map.get(key);
					if (originalCoords != null) {
						coords.addAll(map.get(key));
					}
				}
			}
		}

		// Iterate through the combinations and if they are candidates for naked pair 
		// reduction, add them to the original map.
		for (Entry<Integer, Set<Coord>> entry : newCombinationsMap.entrySet()) {
		    int possibilitiesAsBitSet = entry.getKey();
		    Set<Coord> coordinates = entry.getValue();
		    
		    // Only add the coordinates if the size of the set of coordinates leaves
		    // at least 1 unfilled cell in this group
		    if (coordinates.size() > 1 && coordinates.size() < (9 - groupFillSize)) {
			    if (getBitSetSize(possibilitiesAsBitSet) == coordinates.size()) {
		    		map.put(fromBitSet(possibilitiesAsBitSet), coordinates);
			    }
		    }
		}
	}

	// This could be static, the range of sets is limited
	private int getBitSetSize(int possibilitiesAsBitSet) {
		int result = 0;
		while (possibilitiesAsBitSet != 0) {
			if ((possibilitiesAsBitSet & 1) != 0) result++;
			possibilitiesAsBitSet = possibilitiesAsBitSet >> 1;
		}
		return result;
	}

	// This could be static, the range of sets is limited
	private int toBitSet(Set<Integer> key) {
		int result = 0;
		for (int i : key) {
			result += (1 << (i-1));
		}
		return result;
	}

	// This could be static, the range of sets is limited
	private Set<Integer> fromBitSet(int i) {
		Set<Integer> result = new HashSet<Integer>();
		int val = 1;
		while (i != 0) {
			if ((i & 1) != 0) result.add(val);
			i = i >> 1;
			val++;
		}
		return result;
	}
	
	public Set<Integer> getRowSet(int digit, PossibilitiesContainer cache) {
		Set<Integer> set = new HashSet<Integer>();
		
		for (Coord c : coords.keySet()) {
			if (cache.getPossibilities(c).contains(digit)) {
				set.add(c.getRow());
			}
		}
		
		return set;
	}

	public Set<Integer> getColSet(int digit, PossibilitiesContainer cache) {
		Set<Integer> set = new HashSet<Integer>();
		
		for (Coord c : coords.keySet()) {
			if (cache.getPossibilities(c).contains(digit)) {
				set.add(c.getCol());
			}
		}
		
		return set;
	}
	
	@Override
	public String toString() {
		return groupID;
	}

	public double getMinX() {
		return startX;
	}

	public double getMinY() {
		return startY;
	}
	public double getMaxX() {
		return startX+3;
	}

	public double getMaxY() {
		return startY+3;
	}
}