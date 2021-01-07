package ottop.sudoku.solver;

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.EliminationReason;
import ottop.sudoku.explain.IntersectionRadiationEliminationReason;
import ottop.sudoku.explain.XWingEliminationReason;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.ColumnGroup;
import ottop.sudoku.board.RowGroup;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

// http://www.extremesudoku.info/sudoku.html
// http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
// https://www.sudokuessentials.com/x-wing.html
// https://www.extremesudoku.info/

public class SudokuSolver {
    private PossibilitiesContainer possibilitiesContainer;
    private ISudoku myPuzzle;

    private boolean doEliminationNakedPairs;
    private boolean doEliminationIntersectionRadiation;
    private boolean doEliminationXWings;

    private int numberOfEliminationIterations;

    public SudokuSolver(ISudoku p) {
        resetToNewPuzzle(p);
        setSimplest();
    }

    public SudokuSolver setEliminateNakedPairs() {
        return setEliminateNakedPairs(true);
    }

    public SudokuSolver setEliminateNakedPairs(boolean onOff) {
        doEliminationNakedPairs = onOff;
        return this;
    }

    public SudokuSolver setEliminateIntersectionRadiation() {
        return setEliminateIntersectionRadiation(true);
    }

    public SudokuSolver setEliminateIntersectionRadiation(boolean onOff) {
        doEliminationIntersectionRadiation = onOff;
        return this;
    }

    public SudokuSolver setEliminateXWings() {
        return setEliminateXWings(true);
    }

    public SudokuSolver setEliminateXWings(boolean onOff) {
        doEliminationXWings = onOff;
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

    public boolean eliminatePossibilities() {
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

    private void resetToNewPuzzle(ISudoku p) {
        myPuzzle = p;
        p.initAllGroups();

        // This will just do basic elimination. Additional elimination steps done
        // via next move or solve. Or by calling elimination explicitly.
        possibilitiesContainer = new PossibilitiesContainer(p);
    }

    private boolean eliminateByRadiationFromIntersections() {
        boolean updated = false;
        // TODO maybe not even need to explicitly create these intersections
        // TODO intersections can be smaller anyway
        Set<GroupIntersection> groupIntersections =
                GroupIntersection.createGroupIntersections(myPuzzle.getGroups());

        for (GroupIntersection intersection : groupIntersections) {
            Set<Integer> possibilitiesAtGroupIntersection =
                    possibilitiesContainer.getCandidatesInArea(intersection.getIntersection());
            for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
                if (possibilitiesAtGroupIntersection.contains(symbolCode)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] groupCoordSet = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i = 0; i < 2; i++) {
                        groupCoordSet[i] = new HashSet<>(intersection.getIntersectionGroup(i).getCoords());
                        groupCoordSet[i].removeAll(intersection.getIntersection());
                        pr[i] = possibilitiesContainer.getCandidatesInArea(groupCoordSet[i]);
                    }
                    for (int i = 0; i < 2; i++) {
                        if (!pr[i].contains(symbolCode)) {
                            // If 'digit' is not possible anywhere else in this group, then it
                            // has to be in the intersection. Which means it cannot be
                            // anywhere else in the other group either.
                            if (possibilitiesContainer.removePossibility(symbolCode,
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
            if (g.eliminateNakedGroups(possibilitiesContainer, myPuzzle)) updated = true;
        }

        return updated;
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
                    intersectingGroups = toRowGroups(g.getRowSet(symbolCode, possibilitiesContainer));
                } else if (g instanceof RowGroup) {
                    // list of columns intersecting with row g
                    intersectingGroups = toColumnGroups(g.getColSet(symbolCode, possibilitiesContainer));
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
                        if (possibilitiesContainer.removePossibility(symbolCode, candidateRemovals,
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

    public boolean checkForcedChains()
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

    public Map.Entry<Coord, String> nextMove() {
        Map.Entry<Coord, String> nextMove = null;
        possibilitiesContainer = new PossibilitiesContainer(myPuzzle);

        numberOfEliminationIterations = 0;
        while (!myPuzzle.isSolved() && !myPuzzle.isInconsistent() && nextMove == null) {

            numberOfEliminationIterations++;

            // TODO: multiple iterations could count as higher level
            //System.out.println("Elimination round " + eliminationRound);

            // see if there is a move, naked singles first
            nextMove = possibilitiesContainer.getFirstNakedSingle();
            if (nextMove == null) {
                Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueSymbol =
                        possibilitiesContainer.getFirstUniqueValue();
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

    // TODO: this is ONLY used in tests right now
    public ISudoku solve() {
        ISudoku nextPuzzle = myPuzzle;
        while (!nextPuzzle.isSolved() && !nextPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = nextMove();
            if (nextMove != null) {

                // TODO: instead of completely new puzzles this could update existing one
                nextPuzzle = nextPuzzle.doMove(nextMove.getKey(), nextMove.getValue());

                // TODO: instead of a full reset an update might be possible
                resetToNewPuzzle(nextPuzzle);
            } else {
                return null;
            }
        }
        return nextPuzzle;
    }

    public static int assessDifficulty(ISudoku p) {
        ISudoku nextPuzzle = p;
        SudokuSolver sv = new SudokuSolver(p);
        sv.setSmartest();
        int maxReasonLevel = -1;
        int maxNumberOfIterations = 1;
        while (!nextPuzzle.isSolved() && !nextPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = sv.nextMove();
            if (nextMove != null) {
                // TODO: reasons could be recursive if dependent on other non-trivial cells
                List<EliminationReason> reasons = sv.getPossibilitiesContainer().getEliminationReasons(nextMove.getKey());
                for (EliminationReason r : reasons) {
                    maxReasonLevel = Math.max(maxReasonLevel, r.getDifficulty());
                }

                // Bonus when multiple rounds needed
                maxNumberOfIterations = Math.max(maxNumberOfIterations, sv.numberOfEliminationIterations);

                // TODO: consider just updating
                nextPuzzle = nextPuzzle.doMove(nextMove.getKey(), nextMove.getValue());

                // TODO: consider doing interatively
                sv.resetToNewPuzzle(nextPuzzle);
           } else {
                break;
            }
        }

        // Bonus when multiple rounds were needed in some step
        maxReasonLevel = maxReasonLevel + maxNumberOfIterations - 1;

        if (!nextPuzzle.isSolved()) return -1;

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

    public PossibilitiesContainer getPossibilitiesContainer() {
        return possibilitiesContainer;
    }

    // Just a list of strings for the UI
    public SortedSet<String> getNakedSingles() {
        SortedSet<String> results = new TreeSet<>();
        Map<Coord, String> nakedSingles = possibilitiesContainer.getAllNakedSingles();

        for (Map.Entry<Coord, String> nakedSingle: nakedSingles.entrySet()) {
            results.add(nakedSingle.getValue() + "@" + nakedSingle.getKey());
        }

        return results;
    }

    // Just a list of strings for the UI
    public SortedSet<String> getUniqueValues() {
        SortedSet<String> results = new TreeSet<>();
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValues = possibilitiesContainer.getAllUniqueValues();

        for (Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValue: uniqueValues.entrySet()) {
            results.add(uniqueValue.getValue().getKey() + "@" + uniqueValue.getKey());
        }

        return results;
    }

//     All possible moves
    public SortedSet<String> getPossibleMoves() {
        SortedSet<String> moves = new TreeSet<>();
        moves.addAll(getNakedSingles());
        moves.addAll(getUniqueValues());
        return moves;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Solver\n");
        sb.append(myPuzzle.toString()).append("\n");
        sb.append(possibilitiesContainer.toString());
        return sb.toString();
    }
}
