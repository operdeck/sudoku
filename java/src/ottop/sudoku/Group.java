package ottop.sudoku;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class Group {
	private boolean[] hasDigit = new boolean[10]; // map that tells which digits are currently contained
	protected Map<Coord, Integer> coords; // the cell coordinates contained in this group, mapped to internal index (0..9)
	protected int[] digits = new int[9];
	protected int startX, startY;
	private String groupID;
	
	public Group(int startX, int startY, Puzzle myPuzzle, String id) {
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
	
	private Integer getUniquePossibility(PossibilitiesPerCellCache cache,
										 SolutionContainer sols, Coord myCell) {
		Set<Integer> remainingPossibilities = new HashSet<Integer>(cache.getPossibilities(myCell));
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

	public boolean fillLoneNumbers(PossibilitiesPerCellCache cache, SolutionContainer sols) {
		boolean found = false;

		for (Coord myCell : coords.keySet()) {
			if (coords.get(myCell) != null && digits[coords.get(myCell)] != -1) continue;
			
			Set<Integer> cellPossibilities = cache.getPossibilities(myCell);
			if (cellPossibilities != null) {
				Integer loneNumber = null;
				if (cellPossibilities.size() == 1) {
					loneNumber = cellPossibilities.iterator().next();
				}
				if (loneNumber != null) {
					sols.addSolution(myCell, loneNumber, "in " + groupID + ": Lone Number");
					found = true;
				}
			}
		}
		return found;
	}

	public boolean fillUniqueCells(PossibilitiesPerCellCache cache, SolutionContainer sols) {
		boolean found = false;

		for (Coord myCell : coords.keySet()) {
			if (coords.get(myCell) != null && digits[coords.get(myCell)] != -1) continue;
			
			Set<Integer> cellPossibilities = cache.getPossibilities(myCell);
			if (cellPossibilities != null) {
				Integer uniquePossibility = null;
				if (cellPossibilities.size() >= 1) {
					uniquePossibility = getUniquePossibility(cache, sols, myCell);
				}
				if (uniquePossibility != null) {
					sols.addSolution(myCell, uniquePossibility, "in " + groupID + ": Unique Cell");
					found = true;
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

	public boolean eliminateNakedPairs(Puzzle p,
			PossibilitiesPerCellCache cache) {
		
		boolean result = false;
		
		// create map from sets of possibilities to the coordinates (in this group) that have those (same) possibilities
		Map<Set<Integer>, Set<Coord>> map = new LinkedHashMap<Set<Integer>, Set<Coord>>();
		for (Coord c : coords.keySet()) {
			if (!isOccupied(c)) {
				Set<Integer> pc = cache.getPossibilities(c);
				Set<Coord> coordSet = map.get(pc);
				if (coordSet == null) {
					coordSet = new HashSet<Coord>();
					map.put(pc, coordSet);
				}
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

	private boolean eliminateInGroup(PossibilitiesPerCellCache cache,
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
	
	public Set<Integer> getRowSet(int digit, PossibilitiesPerCellCache cache) {
		Set<Integer> set = new HashSet<Integer>();
		
		for (Coord c : coords.keySet()) {
			if (cache.getPossibilities(c).contains(digit)) {
				set.add(c.getRow());
			}
		}
		
		return set;
	}

	public Set<Integer> getColSet(int digit, PossibilitiesPerCellCache cache) {
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

}
