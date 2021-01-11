package ottop.sudoku.solver;

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.*;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.ColumnGroup;
import ottop.sudoku.board.RowGroup;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

// http://www.extremesudoku.info/sudoku.html
// http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
// https://www.sudokuessentials.com/x-wing.html
// https://www.extremesudoku.info/

public class SudokuSolver implements Updateable {
    private ISudoku myPuzzle;

    // Map of cell to a set of possible values. The values are the
    // internal representation of the cell symbols.
    private Map<Coord, Set<Integer>> candidatesPerCell = null;
    private final Map<Coord, List<EliminationReason>> removalReasons = new HashMap<>();

    // TODO instead of this,
    // keep a list of Eliminator objects that are used for elimination
    private List<Eliminator> eliminators = new ArrayList<>();

    private boolean doEliminationNakedPairs;
    private boolean doEliminationIntersectionRadiation;
    private boolean doEliminationXWings;

    public SudokuSolver(ISudoku p) {
        myPuzzle = p;
        p.setSolver(this);
        setSimplest();
    }

    public SudokuSolver setEliminateNakedPairs() {
        return setEliminateNakedPairs(true);
    }

    public SudokuSolver setEliminateNakedPairs(boolean onOff) {
        doEliminationNakedPairs = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
        return this;
    }

    public SudokuSolver setEliminateIntersectionRadiation() {
        return setEliminateIntersectionRadiation(true);
    }

    public SudokuSolver setEliminateIntersectionRadiation(boolean onOff) {
        doEliminationIntersectionRadiation = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
        return this;
    }

    public SudokuSolver setEliminateXWings() {
        return setEliminateXWings(true);
    }

    public SudokuSolver setEliminateXWings(boolean onOff) {
        doEliminationXWings = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
        return this;
    }

    public SudokuSolver setSimplest() {
        setEliminateNakedPairs(false);
        setEliminateIntersectionRadiation(false);
        setEliminateXWings(false);
        return this;
    }

    public SudokuSolver setSmartest() {
        setEliminateNakedPairs(true);
        setEliminateIntersectionRadiation(true);
        setEliminateXWings(true);
        return this;
    }

    private void calculateCandidates()
    {
        candidatesPerCell = new HashMap<>();
        removalReasons.clear();

        // TODO maybe fill all cell candidates with all symbols

        // This will just do basic elimination. Additional elimination steps done
        // via next move or solve. Or by calling elimination explicitly.
//        possibilitiesContainer = new PossibilitiesContainer(myPuzzle);

        Eliminator simpleEliminator =
                new BasicEliminationEliminator(myPuzzle, candidatesPerCell, removalReasons);
        simpleEliminator.eliminate();

        // TODO: apply other eliminators in same style

        eliminatePossibilities();

//        int n=0;
//        for (Map.Entry<Coord, Set<Integer>> x:candidatesPerCell.entrySet()) {
//            n+=x.getValue().size();
//        }
//        System.out.println("Calc candidates... " + n);

    }

    private Set<Integer> getCandidatesInArea(Set<Coord> subarea) {
        if (candidatesPerCell == null) calculateCandidates();

        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(candidatesPerCell.get(c));
        }
        return p;
    }

    // TODO: maybe becomes obsolete eventually
    void recordEliminationReason(Coord coord, EliminationReason reason) {
        removalReasons.put(coord,
                reason.combine(removalReasons.get(coord)));
    }

    private boolean removePossibility(int symbolCode, Set<Coord> coords, EliminationReason reason) {
        boolean anyRemoved = false;
        for (Coord c : coords) {
            boolean removedAtCurrentCoord = false;
            Set<Integer> currentPossibilities = candidatesPerCell.get(c);
            if (currentPossibilities != null) {
                if (currentPossibilities.remove(symbolCode)) {
                    anyRemoved = true;
                    removedAtCurrentCoord = true;
                }
            }
            if (removedAtCurrentCoord) {
                recordEliminationReason(c, reason);
            }
        }
        return anyRemoved;
    }

    private boolean eliminatePossibilities() {
        // Basic radiation will be done always

        boolean hasEliminated = false;

        if (doEliminationNakedPairs) {
            if (eliminateNakedPairs()) hasEliminated=true;
        }
        if (doEliminationIntersectionRadiation) {
            if (eliminateByRadiationFromIntersections()) hasEliminated=true;
        }
        if (doEliminationXWings) {
           if (eliminateByXWings()) hasEliminated=true;
        }

        return hasEliminated;
    }

    private boolean removePossibilities(Set<Integer> symbolCodes, Coord coord, EliminationReason reason) {
        boolean anyRemoved = false;
        for (int symbolCode : symbolCodes) {
            Set<Integer> currentPossibilities = candidatesPerCell.get(coord);
            if (currentPossibilities != null) {
                if (currentPossibilities.remove(symbolCode)) {
                    anyRemoved = true;
                }
            }
        }
        if (anyRemoved) {
            recordEliminationReason(coord, reason);
        }
        return anyRemoved;
    }

    private boolean eliminateByRadiationFromIntersections() {
        boolean updated = false;
        // TODO maybe not even need to explicitly create these intersections
        // TODO intersections can be smaller anyway
        Set<GroupIntersection> groupIntersections =
                GroupIntersection.createGroupIntersections(myPuzzle.getGroups());

        for (GroupIntersection intersection : groupIntersections) {
            Set<Integer> possibilitiesAtGroupIntersection =
                    getCandidatesInArea(intersection.getIntersection());
            for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
                if (possibilitiesAtGroupIntersection.contains(symbolCode)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] groupCoordSet = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i = 0; i < 2; i++) {
                        groupCoordSet[i] = new HashSet<>(intersection.getIntersectionGroup(i).getCoords());
                        groupCoordSet[i].removeAll(intersection.getIntersection());
                        pr[i] = getCandidatesInArea(groupCoordSet[i]);
                    }
                    for (int i = 0; i < 2; i++) {
                        if (!pr[i].contains(symbolCode)) {
                            // If 'digit' is not possible anywhere else in this group, then it
                            // has to be in the intersection. Which means it cannot be
                            // anywhere else in the other group either.
                            if (removePossibility(symbolCode,
                                    groupCoordSet[1 - i],
                                    new IntersectionRadiationEliminationReason(myPuzzle.symbolCodeToSymbol(symbolCode),
                                            groupCoordSet[1 - i],
                                            intersection.getIntersectionGroup(i),
                                            intersection.getIntersectionGroup(1 - i),
                                            intersection))) updated = true;
                        }
                    }
                }
            }
        }
        return updated;
    }

    private boolean eliminateNakedPairs() {
        boolean updated = false;

        for (AbstractGroup g : myPuzzle.getGroups()) {
            // create map from sets of possibilities to the coordinates (in this group) that have those (same) possibilities
            Map<Set<Integer>, Set<Coord>> nakedGroupMap = new LinkedHashMap<>();
            for (Coord c : g.getCoords()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> pc = candidatesPerCell.get(c);
                    Set<Coord> coordSet = nakedGroupMap.computeIfAbsent(pc, k -> new HashSet<>());
                    coordSet.add(c);
                }
            }

            // find groups of cells that all have the same possibilities, and which is of the same size as the
            // nr of possibilities: the possibilities can be removed from the rest of the group
            if (eliminateInGroup(g, nakedGroupMap, false)) updated = true;

            // Combine elements in this map. For example, if there are entries
            // {26} --> [a], {27} --> [c], {26} --> [c] can be combined
            // to a new entry {267} --> [abc]
            combineNakedGroups(g, nakedGroupMap);

            // Do further elimination (in two steps just to improve reporting)
            if (eliminateInGroup(g, nakedGroupMap, true)) updated = true;

        }

        return updated;
    }

    private boolean eliminateInGroup(AbstractGroup g,
                                     Map<Set<Integer>, Set<Coord>> nakedGroupMap,
                                     boolean isExtended) {
        boolean hasEliminated = false;

        for (Map.Entry<Set<Integer>, Set<Coord>> entry : nakedGroupMap.entrySet()) {
            Set<Integer> nakedGroupSymbolCodes = entry.getKey();
            Set<Coord> nakedGroupCoords = entry.getValue();
            Set<String> nakedGroupSymbols = new HashSet<>();
            for (Integer p : nakedGroupSymbolCodes) {
                nakedGroupSymbols.add(myPuzzle.symbolCodeToSymbol(p));
            }
            if (nakedGroupSymbolCodes.size() > 1 && nakedGroupSymbolCodes.size() == nakedGroupCoords.size()) {
                for (Coord c : g.getCoords()) {
                    if (!myPuzzle.isOccupied(c)) {
                        if (!nakedGroupCoords.contains(c)) {

                            // naked pair symbols to be removed at c but find the
                            // intersection with the remaining possibilities so only
                            // really remove the ones not already removed earlier
                            Set<Integer> currentPossibilities = candidatesPerCell.get(c);
                            Set<Integer> actualRemovals = new HashSet<>(nakedGroupSymbolCodes);
                            actualRemovals.retainAll(currentPossibilities);

                            // translate actual removals to symbols
                            Set<String> actualRemovalSymbols = new HashSet<>();
                            for (Integer p : actualRemovals) {
                                actualRemovalSymbols.add(myPuzzle.symbolCodeToSymbol(p));
                            }

                            if (removePossibilities(actualRemovals, c,
                                    new NakedGroupEliminationReason(actualRemovalSymbols, c,
                                            g,
                                            nakedGroupSymbols, nakedGroupCoords, isExtended))) hasEliminated = true;
                        }
                    }
                }
            }
        }

        return hasEliminated;
    }

    private void combineNakedGroups(AbstractGroup g, Map<Set<Integer>, Set<Coord>> nakedGroupMap) {
        final int range = (1 << g.getGroupSize()); // range of possibilities for 9 digits: 2^9
        final int mask = range - 1;

        // Representing the possibilities in a bitmap (9 digits), find out which of
        // all possible bitmaps (total of 512, 2^9), are a superset of the bitmap of
        // a set of possibilities. Combine the mapped coordinates of those.
        Map<Integer, Set<Coord>> newCombinationsMap = new HashMap<>();
        for (Set<Integer> key : nakedGroupMap.keySet()) { // TODO: loop over entry set instead? Avoid the get later on.
            int keyAsBitSet = toBitSet(key);
            for (int counter = 0; counter < range; counter++) {
                // bitwise operation to verify that all of "key" are contained in the digit set represented by "i"
                if ((counter | (~keyAsBitSet & mask)) == mask) {
                    Set<Coord> coords = newCombinationsMap.computeIfAbsent(counter, k -> new HashSet<>());
                    Set<Coord> originalCoords = nakedGroupMap.get(key); // TODO: get twice can be removed both...
                    if (originalCoords != null) {
                        coords.addAll(nakedGroupMap.get(key));
                    }
                }
            }
        }

        // Iterate through the combinations and if they are candidates for naked pair
        // reduction, add them to the original map.
        for (Map.Entry<Integer, Set<Coord>> entry : newCombinationsMap.entrySet()) {
            int possibilitiesAsBitSet = entry.getKey();
            Set<Coord> coordinates = entry.getValue();

            // Only add the coordinates if the size of the set of coordinates leaves
            // at least 1 unfilled cell in this group
            if (coordinates.size() > 1 && coordinates.size() < (g.getGroupSize() - g.getGroupOccupiedSize())) {
                if (getBitSetSize(possibilitiesAsBitSet) == coordinates.size()) {
                    nakedGroupMap.put(fromBitSet(possibilitiesAsBitSet), coordinates);
                }
            }
        }
    }

    private static int getBitSetSize(int possibilitiesAsBitSet) {
        int result = 0;
        while (possibilitiesAsBitSet != 0) {
            if ((possibilitiesAsBitSet & 1) != 0) result++;
            possibilitiesAsBitSet = possibilitiesAsBitSet >> 1;
        }
        return result;
    }

    private static int toBitSet(Set<Integer> key) {
        int result = 0;
        for (int i : key) {
            result += (1 << (i - 1));
        }
        return result;
    }

    private static Set<Integer> fromBitSet(int i) {
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
    private Set<Integer> getRowSet(AbstractGroup g, int symbolCode) {
        Set<Integer> set = new TreeSet<>();

        for (Coord c : g.getCoords()) {
            if (candidatesPerCell.get(c).contains(symbolCode)) {
                set.add(c.getY());
            }
        }

        return set;
    }

    private Set<Integer> getColSet(AbstractGroup g, int symbolCode) {
        Set<Integer> set = new TreeSet<>();

        for (Coord c : g.getCoords()) {
            if (candidatesPerCell.get(c).contains(symbolCode)) {
                set.add(c.getX());
            }
        }

        return set;
    }

    private boolean eliminateByXWings() {
        boolean updated = false;
        for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
            // For each symbolCode, figure out in which rows of each column it occurs. Then
            // get the set of columns that have the same row set. Same for rows x cols.
            // For those entries that have the same size of {columns} x {rows}, we now
            // know that 'symbolCode' has to be in (one or more of) the intersections of those,
            // so it can be eliminated from the possibilities in each of those groups
            // outside of the intersections.

            Map<Set<AbstractGroup>, Set<AbstractGroup>> xWingMap = new HashMap<>();
            for (AbstractGroup g : myPuzzle.getGroups()) {
                Set<AbstractGroup> intersectingGroups = null;
                if (g instanceof ColumnGroup) {
                    // list of rows intersecting with column g
                    intersectingGroups = toRowGroups(getRowSet(g, symbolCode));
                } else if (g instanceof RowGroup) {
                    // list of columns intersecting with row g
                    intersectingGroups = toColumnGroups(getColSet(g, symbolCode));
                }
                if (intersectingGroups != null && !intersectingGroups.isEmpty()) {
                    Set<AbstractGroup> grps = xWingMap.computeIfAbsent(intersectingGroups, k -> new TreeSet<>());
                    grps.add(g);
                }
            }

            // For all keys k
            // if there is another key that k is a full subset of
            // then add all values of k to that one too
            for (Map.Entry<Set<AbstractGroup>, Set<AbstractGroup>> k: xWingMap.entrySet()) {
                for (Map.Entry<Set<AbstractGroup>, Set<AbstractGroup>> l: xWingMap.entrySet()) {
                    if (k != l) {
                        if (l.getKey().containsAll(k.getKey())) {
                            l.getValue().addAll(k.getValue());
                        }
                    }
                }
            }

            // Now, with this map, if the set of rows is of the same size as the set of
            // columns that have identical row sets, eliminate 'symbolCode' from other cells
            // in the rows. Same for col vs row.
            for (Map.Entry<Set<AbstractGroup>, Set<AbstractGroup>> entry : xWingMap.entrySet()) {
                if (entry.getKey().size() > 1 &&
                        entry.getKey().size() == entry.getValue().size()) { // becomes * 2 now ??
                    for (AbstractGroup g : entry.getKey()) {
                        // Eliminate 'symbolCode' from this row 'g' except for the groups it intersects
                        Set<Coord> candidateRemovals = new TreeSet<>(g.getCoords());
                        for (AbstractGroup other : entry.getValue()) {
                            candidateRemovals.removeAll(other.getCoords());
                        }
                        if (removePossibility(symbolCode, candidateRemovals,
                                new XWingEliminationReason(myPuzzle.symbolCodeToSymbol(symbolCode),
                                        candidateRemovals,
                                        g,
                                        entry.getKey(), entry.getValue()))) updated = true;
                    }
                }
            }
        }
        return updated;
    }

    private boolean checkForcedChains()
    {
        // Given an original puzzle O
        // Find a coord C with possibilities Di (> 1, start with C's with only 2)
        // Do move for all possibilities Mi giving new puzzles Pi
        // Apply only forced moves to all of Pi giving Fi until no more moves
        // When all done:
        //    if there is an i for which Fi is solved then we accidentally guessed a solution
        //    if there is an i for which Fi is invalid then Mi is not a valid move
        //    otherwise for all i that do not result in an invalid puzzle Fi
        //        if there is a coordinate K that is empty in O and that is not empty in all of Fi
        //           if that has the SAME value in all of Fi then we have a move: at C place Di (return)
        // Otherwise no move :(
        //


        return true;
    }

    public Map<Coord, String> getAllNakedSingles() {
        if (candidatesPerCell == null) calculateCandidates();

        return getNakedSingles(true);
    }

    private Map.Entry<Coord, String> getFirstNakedSingle() {
        Map<Coord, String> results = getNakedSingles(false);
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    private String getNakedSingleAt(Coord coord) {
        if (!myPuzzle.isOccupied(coord)) {
            Set<Integer> cellPossibilities = candidatesPerCell.get(coord);
            if (cellPossibilities != null) {
                if (cellPossibilities.size() == 1) {
                    Integer symbolCode = cellPossibilities.iterator().next();
                    return myPuzzle.symbolCodeToSymbol(symbolCode);
                }
            }
        }
        return null;
    }

    private Map<Coord, String> getNakedSingles(boolean all) {
        Map<Coord, String> result = new TreeMap<>();
        for (Coord coord: myPuzzle.getAllCells()) {
            String symbol = getNakedSingleAt(coord);
            if (symbol != null) {
                result.put(coord, symbol);
                if (!all) return result;
            }
        }
        return result;
    }

    public Map<Coord, Map.Entry<String, List<AbstractGroup>>> getAllUniqueValues() {
        if (candidatesPerCell == null) calculateCandidates();

        return getUniqueValues(true, myPuzzle.getGroups());
    }

    private Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> getFirstUniqueValue() {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> results =
                getUniqueValues(false, myPuzzle.getGroups());
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    // TODO: this is not super efficient - will consider too many coordinates
    private Map.Entry<String, List<AbstractGroup>> getUniqueValueAt(Coord c) {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValues =
                getUniqueValues(false, myPuzzle.getBuddyGroups(c));
        if (uniqueValues != null && uniqueValues.size()>0) {
            Map.Entry<String, List<AbstractGroup>> uniqueValue = uniqueValues.get(c);
            return uniqueValue;
        }
        return null;
    }

    private Map<Coord, Map.Entry<String, List<AbstractGroup>>> getUniqueValues(boolean all, List<AbstractGroup> groups) {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> result = new TreeMap<>();

        for (AbstractGroup g: groups) {
            for (Coord c: g.getCoords()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> remainingPossibilities = new HashSet<>(candidatesPerCell.get(c));
                    for (Coord otherCell : g.getCoords()) {
                        if (!myPuzzle.isOccupied(otherCell)) {
                            if (!otherCell.equals(c)) {
                                remainingPossibilities.removeAll(candidatesPerCell.get(otherCell));
                            }
                        }
                    }
                    if (remainingPossibilities.size() == 1) {
                        Integer symbolCode = remainingPossibilities.iterator().next();

                        if (!result.containsKey(c)) {
                            result.put(c, new AbstractMap.SimpleEntry<>(myPuzzle.symbolCodeToSymbol(symbolCode),
                                    new ArrayList<>()));
                        }
                        result.get(c).getValue().add(g);
//                        recordEliminationReason(c, new UniqueValue(myPuzzle.symbolCodeToSymbol(symbolCode), c,
//                                result.get(c).getValue()));
                        if (!all) return result;
                    }
                }
            }
        }

        return result;
    }


    public Map.Entry<Coord, String> nextMove(SolveStats stats) {
        if (candidatesPerCell == null) calculateCandidates();

        Map.Entry<Coord, String> nextMove = null;
//        possibilitiesContainer = new PossibilitiesContainer(myPuzzle);

        while (!myPuzzle.isComplete() && nextMove == null) {
            stats.addIteration();

            // TODO: multiple iterations could count as higher level
            //System.out.println("Elimination round " + eliminationRound);

            // see if there is a move, naked singles first
            nextMove = getFirstNakedSingle();
            if (nextMove == null) {
                Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueSymbol =
                        getFirstUniqueValue();
                if (uniqueSymbol != null) {
                    nextMove = new AbstractMap.SimpleEntry<>(uniqueSymbol.getKey(), uniqueSymbol.getValue().getKey());
                }
            }

            // no move? try different types of elimination, possibly iteratively
            if (nextMove == null) {
                boolean hasEliminatedCandidates = false;

                if (doEliminationNakedPairs && !hasEliminatedCandidates) {
                    // TODO: consider doing the extended naked pairs as a 2nd step only
                    // if first does not give new candidates
                    hasEliminatedCandidates = eliminateNakedPairs();
                }
                if (doEliminationIntersectionRadiation && !hasEliminatedCandidates) {
                    hasEliminatedCandidates = eliminateByRadiationFromIntersections();
                }
                if (doEliminationXWings && !hasEliminatedCandidates) {
                    hasEliminatedCandidates = eliminateByXWings();
                }

                if (!hasEliminatedCandidates) break;
            }
        }

        return nextMove;
    }

    // TODO: this is ONLY used in tests right now - consider moving there
    public boolean solve() {
        if (candidatesPerCell == null) calculateCandidates();

        SolveStats stats = new SolveStats();
        while (!myPuzzle.isComplete() && !myPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = nextMove(stats);

            //System.out.println("Puzzle: " + String.valueOf(nextPuzzle));
            //System.out.println("Next move: " + nextMove);

            if (nextMove != null) {
                return myPuzzle.doMove(nextMove.getKey(), nextMove.getValue());
            } else {
                return false;
            }
        }
        return false;
    }

    // Add moves on the fly if there are any
    public List<EliminationReason> getEliminationReasons(Coord c) {
        if (candidatesPerCell == null) calculateCandidates();

        List<EliminationReason> reasonsPlusCandidateMove = new ArrayList<>();
        reasonsPlusCandidateMove.addAll(removalReasons.get(c));
        String symbol = getNakedSingleAt(c);
        if (symbol != null) {
            reasonsPlusCandidateMove.add(new NakedSingle(symbol, c));
        } else {
            Map.Entry<String, List<AbstractGroup>> uniqueValue = getUniqueValueAt(c);
            if (uniqueValue != null) {
                reasonsPlusCandidateMove.add(new UniqueValue(uniqueValue.getKey(), c,
                        uniqueValue.getValue()));
            }
        }
        return reasonsPlusCandidateMove;
    }

    public static int assessDifficulty(ISudoku p) {
        ISudoku shadowPuzzle = p.clone();
        SudokuSolver sv = new SudokuSolver(shadowPuzzle);
        SolveStats s = new SolveStats();
        sv.setSmartest();
        int maxReasonLevel = -1;
        int maxNumberOfIterations = 1;
        while (!shadowPuzzle.isComplete() && !shadowPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = sv.nextMove(s);

            if (nextMove != null) {
                // TODO: reasons could be recursive if dependent on other non-trivial cells
                List<EliminationReason> reasons = sv.getEliminationReasons(nextMove.getKey());
                for (EliminationReason r : reasons) {
                    maxReasonLevel = Math.max(maxReasonLevel, r.getDifficulty());
                }

                // Bonus when multiple rounds needed
                //maxNumberOfIterations = Math.max(maxNumberOfIterations, sv.numberOfEliminationIterations);

                shadowPuzzle.doMove(nextMove.getKey(), nextMove.getValue());

                System.out.println("** original " + p.isSolved() + p.isComplete());
                System.out.println("** shadow " + shadowPuzzle.isSolved() + shadowPuzzle.isComplete());
           } else {
                break;
            }
        }

        // Bonus when multiple rounds were needed in some step
        maxReasonLevel = maxReasonLevel + maxNumberOfIterations - 1;

        if (!shadowPuzzle.isSolved()) return -1;

        return maxReasonLevel;
    }


    // Get rows corresponding to indices
    private Set<AbstractGroup> toRowGroups(Set<Integer> rowset) {
        Set<AbstractGroup> result = new TreeSet<>();
        for (AbstractGroup g : myPuzzle.getGroups()) {
            if (g instanceof RowGroup) {
                if (rowset.contains(((RowGroup) g).getRow())) {
                    result.add(g);
                }
            }
        }
        return result;
    }

    private Set<AbstractGroup> toColumnGroups(Set<Integer> colset) {
        Set<AbstractGroup> result = new TreeSet<>();
        for (AbstractGroup g : myPuzzle.getGroups()) {
            if (g instanceof ColumnGroup) {
                if (colset.contains(((ColumnGroup) g).getColumn())) {
                    result.add(g);
                }
            }
        }
        return result;
    }

//    public PossibilitiesContainer getPossibilitiesContainer() {
//        return possibilitiesContainer;
//    }

    public Set<Integer> getCandidatesAtCell(Coord c) {
    if (candidatesPerCell == null) calculateCandidates();

    return candidatesPerCell.get(c);
    }

    @Override
    public void update() {
        calculateCandidates();
    }
}
