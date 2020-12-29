package ottop.sudoku.group;

import ottop.sudoku.Coord;
import ottop.sudoku.PencilMarkContainer;
import ottop.sudoku.SolutionContainer;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;
import java.util.Map.Entry;

public abstract class AbstractGroup {
	protected int groupSize; // number of cells in a group, identical to number of distinct symbols -1 for empty
	protected static int EMPTYSYMBOLCODE = 0; // 0 by definition, code for empty cell is 0

	private final boolean[] hasSymbolCode; // map that tells which symbols are currently contained
	private final Map<Coord, Integer> coords; // the cell coordinates in this group, mapped to internal index
	private final int[] groupSymbolCodes; // current cell state

	/*
	For a standard Sudoku:

	Group has cells with internal index 0..8, mapped to (current) symbol codes via "groupSymbolCodes"
	"coords" indicates where a coord is in this group: a map of Coord --> internal index
	"hasSymbolCodes" indicates which symbol codes are currently in this group

	So

	Puzzle.symbolCodeToSymbol( groupSymbolCodes[coords[CELL]] ) gives the symbol at Coord CELL

	and

	groupSymbolCodes[i] = myPuzzle.getSymbolCodeAtCoordinates(
	   new Coord(startX+internalIndexToRelativeX(i),
	             startY+internalIndexToRelativeY(i)));

	 */

	protected int startX;
	protected int startY;
	private final String groupID;

	public AbstractGroup(int startX, int startY, IPuzzle myPuzzle, String id) {
		this.startX = startX;
		this.startY = startY;
		this.groupID = id;
		this.groupSize = myPuzzle.getSymbolCodeRange()-1;
		this.hasSymbolCode = new boolean[myPuzzle.getSymbolCodeRange()];
		this.groupSymbolCodes = new int[this.groupSize];

		for (int i = 0; i< groupSize; i++) {
			groupSymbolCodes[i] = myPuzzle.getSymbolCodeAtCoordinates(new Coord(startX+internalIndexToRelativeX(i),startY+internalIndexToRelativeY(i)));
			if (groupSymbolCodes[i] != EMPTYSYMBOLCODE) {
				hasSymbolCode[groupSymbolCodes[i]] = true;
			}
		}
		
		coords = new HashMap<>();
		for (int internalIndex = 0; internalIndex< groupSize; internalIndex++) {
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
		return groupSymbolCodes[val] != EMPTYSYMBOLCODE;
	}
	
	public boolean isPossibility(int symbolCode, Coord c) {
		if (isInGroup(c)) {
			if (hasSymbolCode[symbolCode]) return false;
			if (isOccupied(c)) return false;
		}
		return true;
	}
	
	private Integer getUniquePossibility(PencilMarkContainer cache,
										 Coord myCell) {
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

	public boolean addPossibilitiesToSolution(PencilMarkContainer poss, SolutionContainer sols) {
		boolean found = false;

		for (Coord myCell : coords.keySet()) {
			if (coords.get(myCell) != null && groupSymbolCodes[coords.get(myCell)] != 0) continue;
			
			Set<Integer> cellPossibilities = poss.getPossibilities(myCell);
			if (cellPossibilities != null) {
				if (cellPossibilities.size() == 1) {
					Integer loneNumber = cellPossibilities.iterator().next();
					sols.addLoneSymbolCode(myCell, loneNumber);
					found = true;
				}
				if (cellPossibilities.size() >= 1) {
					Integer uniquePossibility = getUniquePossibility(poss, myCell);
					if (uniquePossibility != null) {
						sols.addUniqueSymbolCode(myCell, uniquePossibility, this);
						found = true;
					}
				}
			}
		}
		return found;
	}

	public boolean solved() {
		boolean isSolved = true;
		for (int symbolCode = 1; symbolCode<=groupSize; symbolCode++) {
			if (!hasSymbolCode[symbolCode]) {
				isSolved = false;
				break;
			}
		}
		return isSolved;
	}

	public Set<Coord> getCoords() {
		return coords.keySet();
	}

	public boolean eliminateNakedPairs(PencilMarkContainer cache) {
		
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

	private boolean eliminateInGroup(PencilMarkContainer cache,
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
		final int range = (1 << groupSize); // range of possibilities for 9 digits: 2^9
		final int mask = range - 1;
		int groupFillSize = 0;
		for (boolean b : hasSymbolCode) {
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
		    if (coordinates.size() > 1 && coordinates.size() < (groupSize - groupFillSize)) {
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
	
	public Set<Integer> getRowSet(int symbolCode, PencilMarkContainer cache) {
		Set<Integer> set = new HashSet<>();
		
		for (Coord c : coords.keySet()) {
			if (cache.getPossibilities(c).contains(symbolCode)) {
				set.add(c.getY());
			}
		}
		
		return set;
	}

	public Set<Integer> getColSet(int symbolCode, PencilMarkContainer cache) {
		Set<Integer> set = new HashSet<>();
		
		for (Coord c : coords.keySet()) {
			if (cache.getPossibilities(c).contains(symbolCode)) {
				set.add(c.getX());
			}
		}
		
		return set;
	}
	
	@Override
	public String toString() {
		return groupID;
	}

	public boolean isInconsistent()
	{
		boolean isInconsistent = false;
		int[] symbolCodeCount = new int[1+groupSize];
		for (int i = 0; i< groupSize; i++) {
			symbolCodeCount[groupSymbolCodes[i]] += 1;
		}
		for (int j = 1; j<=groupSize; j++) {
			if (symbolCodeCount[j] > 1) {
				isInconsistent = true;
				break;
			}
		}
		return isInconsistent;
	}
}
