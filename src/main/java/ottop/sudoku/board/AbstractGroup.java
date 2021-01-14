package ottop.sudoku.board;

import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public abstract class AbstractGroup implements Comparable<AbstractGroup> {
    static final int EMPTYSYMBOLCODE = 0; // 0 by definition, code for empty cell is 0
    private final Map<Coord, Integer> coords; // the cell coordinates in this group, mapped to internal index
    private final String groupID;
    final int groupSize; // number of cells in a group, identical to number of distinct symbols - 1
    final int startX;
    final int startY;

    // Below depends on state of puzzle - which cells are occupied

    private boolean[] hasSymbolCode; // map that tells which symbols are currently contained
    private int[] groupSymbolCodes; // current cell state
    private int groupOccupiedSize; // nr of occupied cells in group

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


    public AbstractGroup(int startX, int startY, int size, String id) {
        this.startX = startX;
        this.startY = startY;
        this.groupID = id;
        this.groupSize = size;

        coords = new HashMap<>();
        for (int internalIndex = 0; internalIndex < groupSize; internalIndex++) {
            int absX = startX + internalIndexToRelativeX(internalIndex);
            int absY = startY + internalIndexToRelativeY(internalIndex);
            coords.put(new Coord(absX, absY), internalIndex);
        }

        //resetGroup(myPuzzle);
    }

    public void resetGroup(ISudoku myPuzzle) {
        this.hasSymbolCode = new boolean[myPuzzle.getSymbolCodeRange()];
        this.groupSymbolCodes = new int[this.groupSize];

        this.groupOccupiedSize = 0;
        for (int i = 0; i < groupSize; i++) {
            groupSymbolCodes[i] = myPuzzle.getSymbolCodeAtCoordinates(new Coord(startX + internalIndexToRelativeX(i), startY + internalIndexToRelativeY(i)));
            if (groupSymbolCodes[i] != EMPTYSYMBOLCODE) {
                hasSymbolCode[groupSymbolCodes[i]] = true;
                groupOccupiedSize++;
            }
        }

    }

    public abstract int internalIndexToRelativeX(int idx);

    public abstract int internalIndexToRelativeY(int idx);

    public boolean isInGroup(Coord c) {
        return coords.containsKey(c);
    }

    public int getGroupSize() { return groupSize; }

    public int getGroupOccupiedSize() { return groupOccupiedSize; }

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

    public boolean isComplete() {
        return groupSize == groupOccupiedSize;
    }

    public Set<Coord> getCoords() {
        return coords.keySet();
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
