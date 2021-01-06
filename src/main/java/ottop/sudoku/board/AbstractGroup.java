package ottop.sudoku.board;

import ottop.sudoku.solve.PossibilitiesContainer;
import ottop.sudoku.explain.NakedGroupEliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;
import java.util.Map.Entry;

public abstract class AbstractGroup implements Comparable<AbstractGroup> {
    protected static int EMPTYSYMBOLCODE = 0; // 0 by definition, code for empty cell is 0
    private boolean[] hasSymbolCode; // map that tells which symbols are currently contained
    private Map<Coord, Integer> coords; // the cell coordinates in this group, mapped to internal index
    private int[] groupSymbolCodes; // current cell state
    private final String groupID;

    /*
    For a standard SudokuMain:

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
    protected int groupSize; // number of cells in a group, identical to number of distinct symbols -1 for empty
    protected int startX;
    protected int startY;

    public AbstractGroup(int startX, int startY, ISudoku myPuzzle, String id) {
        this.startX = startX;
        this.startY = startY;
        this.groupID = id;

        resetGroup(myPuzzle);
    }

    public void resetGroup(ISudoku myPuzzle) {
        this.groupSize = myPuzzle.getSymbolCodeRange() - 1;
        this.hasSymbolCode = new boolean[myPuzzle.getSymbolCodeRange()];
        this.groupSymbolCodes = new int[this.groupSize];

        for (int i = 0; i < groupSize; i++) {
            groupSymbolCodes[i] = myPuzzle.getSymbolCodeAtCoordinates(new Coord(startX + internalIndexToRelativeX(i), startY + internalIndexToRelativeY(i)));
            if (groupSymbolCodes[i] != EMPTYSYMBOLCODE) {
                hasSymbolCode[groupSymbolCodes[i]] = true;
            }
        }

        coords = new HashMap<>();
        for (int internalIndex = 0; internalIndex < groupSize; internalIndex++) {
            int absX = startX + internalIndexToRelativeX(internalIndex);
            int absY = startY + internalIndexToRelativeY(internalIndex);
            coords.put(new Coord(absX, absY), internalIndex);
        }
    }

    public abstract int internalIndexToRelativeX(int idx);

    public abstract int internalIndexToRelativeY(int idx);

    public boolean isInGroup(Coord c) {
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
            return !isOccupied(c);
        }
        return true;
    }

    public boolean solved() {
        boolean isSolved = true;
        for (int symbolCode = 1; symbolCode <= groupSize; symbolCode++) {
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

    public boolean eliminateNakedGroups(PossibilitiesContainer possibilitiesContainer, ISudoku myPuzzle) {

        boolean result = false;

        // create map from sets of possibilities to the coordinates (in this group) that have those (same) possibilities
        Map<Set<Integer>, Set<Coord>> nakedGroupMap = new LinkedHashMap<>();
        for (Coord c : coords.keySet()) {
            if (!isOccupied(c)) {
                Set<Integer> pc = possibilitiesContainer.getCandidatesAtCell(c);
                Set<Coord> coordSet = nakedGroupMap.computeIfAbsent(pc, k -> new HashSet<>());
                coordSet.add(c);
            }
        }

//        if (this.groupID.equals("Column 3")) {
//            System.out.println(nakedGroupMap.toString());
//        }

        // find groups of cells that all have the same possibilities, and which is of the same size as the
        // nr of possibilities: the possibilities can be removed from the rest of the group
        if (eliminateInGroup(possibilitiesContainer, result, nakedGroupMap, false, myPuzzle)) result = true;

        // Combine elements in this map. For example, if there are entries
        // {26} --> [a], {27} --> [c], {26} --> [c] can be combined
        // to a new entry {267} --> [abc]
        combineNakedGroups(nakedGroupMap);

//        if (this.groupID.equals("Column 3")) {
//            System.out.println("Combined:");
//            System.out.println(nakedGroupMap.toString());
//        }

        // Do further elimination (in two steps just to improve reporting)
        if (eliminateInGroup(possibilitiesContainer, result, nakedGroupMap, true, myPuzzle)) result = true;

        return result;
    }

    private boolean eliminateInGroup(PossibilitiesContainer possibilitiesContainer,
                                     boolean result, Map<Set<Integer>, Set<Coord>> map,
                                     boolean isExtended, ISudoku myPuzzle) {
        for (Entry<Set<Integer>, Set<Coord>> entry : map.entrySet()) {
            Set<Integer> nakedGroupSymbolCodes = entry.getKey();
            Set<Coord> nakedGroupCoords = entry.getValue();
            Set<String> nakedGroupSymbols = new HashSet<>();
            for (Integer p : nakedGroupSymbolCodes) {
                nakedGroupSymbols.add(myPuzzle.symbolCodeToSymbol(p));
            }
            if (nakedGroupSymbolCodes.size() > 1 && nakedGroupSymbolCodes.size() == nakedGroupCoords.size()) {
                for (Coord c : coords.keySet()) {
                    if (!isOccupied(c)) {
                        if (!nakedGroupCoords.contains(c)) {

                            // naked pair symbols to be removed at c but find the
                            // intersection with the remaining possibilities so only
                            // really remove the ones not already removed earlier
                            Set<Integer> currentPossibilities = possibilitiesContainer.getCandidatesAtCell(c);
                            Set<Integer> actualRemovals = new HashSet<>(nakedGroupSymbolCodes);
                            actualRemovals.retainAll(currentPossibilities);

                            // translate actual removals to symbols
                            Set<String> actualRemovalSymbols = new HashSet<>();
                            for (Integer p : actualRemovals) {
                                actualRemovalSymbols.add(myPuzzle.symbolCodeToSymbol(p));
                            }

                            if (possibilitiesContainer.removePossibilities(actualRemovals, c,
                                    new NakedGroupEliminationReason(actualRemovalSymbols, c,
                                            this,
                                            nakedGroupSymbols, nakedGroupCoords, isExtended))) result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    private void combineNakedGroups(Map<Set<Integer>, Set<Coord>> map) {
        final int range = (1 << groupSize); // range of possibilities for 9 digits: 2^9
        final int mask = range - 1;
        int groupFillSize = 0;
        for (boolean b : hasSymbolCode) {
            if (b) groupFillSize++;
        }

        // Representing the possibilities in a bitmap (9 digits), find out which of
        // all possible bitmaps (total of 512, 2^9), are a superset of the bitmap of
        // a set of possibilities. Combine the mapped coordinates of those.
        Map<Integer, Set<Coord>> newCombinationsMap = new HashMap<>();
        for (Set<Integer> key : map.keySet()) { // TODO: loop over entry set instead? Avoid the get later on.
            int keyAsBitSet = toBitSet(key);
            for (int counter = 0; counter < range; counter++) {
                // bitwise operation to verify that all of "key" are contained in the digit set represented by "i"
                if ((counter | (~keyAsBitSet & mask)) == mask) {
                    Set<Coord> coords = newCombinationsMap.computeIfAbsent(counter, k -> new HashSet<>());
                    Set<Coord> originalCoords = map.get(key); // TODO: get twice can be removed both...
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
            result += (1 << (i - 1));
        }
        return result;
    }

    // This could be static, the range of sets is limited
    private Set<Integer> fromBitSet(int i) {
        Set<Integer> result = new HashSet<>();
        int val = 1;
        while (i != 0) {
            if ((i & 1) != 0) result.add(val);
            i = i >> 1;
            val++;
        }
        return result;
    }

    // Get indices of rows where given symbol is a candidate
    public Set<Integer> getRowSet(int symbolCode, PossibilitiesContainer cache) {
        Set<Integer> set = new TreeSet<>();

        for (Coord c : coords.keySet()) {
            if (cache.getCandidatesAtCell(c).contains(symbolCode)) {
                set.add(c.getY());
            }
        }

        return set;
    }

    public Set<Integer> getColSet(int symbolCode, PossibilitiesContainer possibilities) {
        Set<Integer> set = new TreeSet<>();

        for (Coord c : coords.keySet()) {
            if (possibilities.getCandidatesAtCell(c).contains(symbolCode)) {
                set.add(c.getX());
            }
        }

        return set;
    }

    @Override
    public String toString() {
        return groupID;
    }

    public boolean isInconsistent() {
        boolean isInconsistent = false;
        int[] symbolCodeCount = new int[1 + groupSize];
        for (int i = 0; i < groupSize; i++) {
            symbolCodeCount[groupSymbolCodes[i]] += 1;
        }
        for (int j = 1; j <= groupSize; j++) {
            if (symbolCodeCount[j] > 1) {
                isInconsistent = true;
                break;
            }
        }
        return isInconsistent;
    }

    @Override
    public int compareTo(AbstractGroup g) {
        return groupID.compareTo(g.groupID);
    }
}
