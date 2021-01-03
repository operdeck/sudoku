package ottop.sudoku;

import ottop.sudoku.explain.IntersectionRadiationEliminationReason;
import ottop.sudoku.explain.XWingEliminationReason;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

// http://www.extremesudoku.info/sudoku.html
// http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
// https://www.sudokuessentials.com/x-wing.html
// https://www.extremesudoku.info/

public class SudokuSolver {
    private PossibilitiesContainer possibilitiesContainer;
    private IPuzzle myPuzzle;

    private boolean doEliminationBasicRadiation;
    private boolean doEliminationNakedPairs;
    private boolean doEliminationIntersectionRadiation;
    private boolean doEliminationXWings;

    public SudokuSolver(IPuzzle p) {
        resetToNewPuzzle(p);
        setSimplest();
    }

    public SudokuSolver setEliminateBasicRadiation() {
        return setEliminateBasicRadiation(true);
    }

    public SudokuSolver setEliminateBasicRadiation(boolean onOff) {
        doEliminationBasicRadiation = onOff;
        return this;
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
        setEliminateBasicRadiation(true);
        setEliminateNakedPairs(false);
        setEliminateIntersectionRadiation(false);
        setEliminateXWings(false);
        return this;
    }

    public SudokuSolver setSmartest() {
        setEliminateBasicRadiation(true);
        setEliminateNakedPairs(true);
        setEliminateIntersectionRadiation(true);
        setEliminateXWings(true);
        return this;
    }

    public boolean eliminatePossibilities() {
        // Basic radiation will be done always

        boolean hasEliminated = false;

//        if (doEliminationBasicRadiation) {
//            if (true) hasEliminated = true;
//        }
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

    private void resetToNewPuzzle(IPuzzle p) {
        myPuzzle = p;
        p.resetState();

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
            if (g.eliminateNakedPairs(possibilitiesContainer, myPuzzle)) updated = true;
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

            Map<Set<AbstractGroup>, Set<AbstractGroup>> map = new HashMap<>();
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
                    Set<AbstractGroup> grps = map.computeIfAbsent(intersectingGroups, k -> new HashSet<>());
                    grps.add(g);
                }
            }

            // Now, with this map, if the set of rows is of the same size as the set of
            // columns that have identical row sets, eliminate 'symbolCode' from other cells
            // in the rows. Same for col vs row.
            for (Map.Entry<Set<AbstractGroup>, Set<AbstractGroup>> entry : map.entrySet()) {
                if (entry.getKey().size() > 1 &&
                        entry.getKey().size() == entry.getValue().size()) { // becomes * 2 now ??
                    for (AbstractGroup g : entry.getKey()) {
                        // Eliminate 'symbolCode' from this row 'g' except for the groups it intersects
                        Set<Coord> candidateRemovals = new TreeSet<>();
                        candidateRemovals.addAll(g.getCoords());
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

    public Map.Entry<Coord, String> nextMove() {
        Map.Entry<Coord, String> nextMove = null;
        // TODO: is this needed?? The puzzle will give a new container at construction.

        possibilitiesContainer = new PossibilitiesContainer(myPuzzle);
        eliminatePossibilities();

        //System.out.println(this);

        if (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
//            SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
//            for (AbstractGroup g : myPuzzle.getGroups()) {
//                g.addPossibilitiesToSolution(possibilities, sols);
//            }
            nextMove = possibilitiesContainer.getFirstNakedSingle();

            if (nextMove == null) {
                Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueSymbol =
                        possibilitiesContainer.getFirstUniqueValue();
                if (uniqueSymbol != null) {
                    nextMove = new AbstractMap.SimpleEntry<>(uniqueSymbol.getKey(), uniqueSymbol.getValue().getKey());
                }
            }
        }

        return nextMove;
    }

    public IPuzzle solve() {
        while (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = nextMove();
            if (nextMove != null) {
                IPuzzle nextPuzzle = myPuzzle.doMove(nextMove.getKey(), nextMove.getValue());
                resetToNewPuzzle(nextPuzzle);
            } else {
                return null;
            }
        }
        return myPuzzle;
    }

    private Set<AbstractGroup> toRowGroups(Set<Integer> rowset) {
        Set<AbstractGroup> result = new HashSet<>();
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
        Set<AbstractGroup> result = new HashSet<>();
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

    // All possible moves
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
