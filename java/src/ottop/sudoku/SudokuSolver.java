package ottop.sudoku;

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
    private PencilMarkContainer possibilities;
    private IPuzzle myPuzzle;
    private boolean trace;

    public enum EliminationMethods {
        BASICRADIATION (1),
        NAKEDPAIRS  (2),
        INTERSECTION (4),
        XWINGS   (8);

        private final int levelCode;
        EliminationMethods(int levelCode) {
            this.levelCode = levelCode;
        }
        public int code() {
            return levelCode;
        }
    }

    public SudokuSolver(IPuzzle p) {
        this(p, true);
    }

    public SudokuSolver(IPuzzle p, boolean trace) {
        this.trace = trace;
        myPuzzle = p;
        p.resetState();
        possibilities = new PencilMarkContainer(p, trace);
    }

    public boolean eliminateByRadiationFromIntersections() {
        boolean updated = false;
        // TODO maybe not even need to explicitly create these intersections
        Set<GroupIntersection> groupIntersections =
                GroupIntersection.createGroupIntersections(myPuzzle.getGroups());

        for (GroupIntersection intersection : groupIntersections) {
            Set<Integer> possibilitiesAtGroupIntersection =
                    possibilities.getPossibilities(intersection.getIntersection());
            for (int symbolCode=1; symbolCode<myPuzzle.getSymbolCodeRange(); symbolCode++) {
                if (possibilitiesAtGroupIntersection.contains(symbolCode)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] r = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i=0; i<2; i++) {
                        r[i] = new HashSet<>(intersection.getIntersectionGroup(i).getCoords());
                        r[i].removeAll(intersection.getIntersection());
                        pr[i] = possibilities.getPossibilities(r[i]);
                    }
                    for (int i=0; i<2; i++) {
                        if (!pr[i].contains(symbolCode)) {
                            // If 'digit' is not possible anywhere else in this group, then it
                            // has to be in the intersection. Which means it cannot be
                            // anywhere else in the other group either.
                            if (possibilities.removePossibility(symbolCode, r[1-i],
                                    " (in " + intersection.getIntersectionGroup(1-i) + ") because " +
                                            myPuzzle.symbolCodeToSymbol(symbolCode) + " has to be in " +
                                            intersection.getIntersectionGroup(i) + " in one of " +
                                            intersection + " (Intersection Radiation)")) updated = true;;
                        }
                    }
                }
            }
        }
        return updated;
    }

    public boolean eliminateNakedPairs() {
        boolean updated = false;

        for (AbstractGroup g : myPuzzle.getGroups()) {
            if (g.eliminateNakedPairs(possibilities)) updated = true;
        }

        return updated;
    }

    public boolean eliminateByXWings() {
        boolean updated = false;
        for (int symbolCode=1; symbolCode<myPuzzle.getSymbolCodeRange(); symbolCode++) {
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
                    intersectingGroups = toRowGroups(g.getRowSet(symbolCode, possibilities));
                } else if (g instanceof RowGroup) {
                    // list of columns intersecting with row g
                    intersectingGroups = toColumnGroups(g.getColSet(symbolCode, possibilities));
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
                        Set<Coord> candidateRemovals = new TreeSet<Coord>();
                        candidateRemovals.addAll(g.getCoords());
                        for (AbstractGroup other : entry.getValue()) {
                            candidateRemovals.removeAll(other.getCoords());
                        }
                        if (possibilities.removePossibility(symbolCode, candidateRemovals,
                                " of " + g + " because " + myPuzzle.symbolCodeToSymbol(symbolCode) + " has to be in " +
                                        entry.getKey() + " X " + entry.getValue() + " (X-Wing)")) updated = true;
                    }
                }
            }
        }
        return updated;
    }

    public void eliminate(int level) {
        if ((level & EliminationMethods.BASICRADIATION.code()) != 0) {
            if(trace) System.out.println("Eliminate with BASIC radiation");
        }
        if ((level & EliminationMethods.NAKEDPAIRS.code()) != 0) {
            if (trace) System.out.println("Eliminate naked pairs");
            eliminateNakedPairs();
        }
        if ((level & EliminationMethods.INTERSECTION.code()) != 0) {
            if (trace) System.out.println("Eliminate intersections");
            eliminateByRadiationFromIntersections();
        }
        if ((level & EliminationMethods.XWINGS.code()) != 0) {
            if (trace) System.out.println("Eliminate XWings");
            eliminateByXWings();
        }
    }

    public boolean solveSimplest() {
        return solve(EliminationMethods.BASICRADIATION.code());
    }

    public boolean solve() {
        return solve(EliminationMethods.BASICRADIATION.code() +
                EliminationMethods.NAKEDPAIRS.code() +
                EliminationMethods.INTERSECTION.code() +
                EliminationMethods.XWINGS.code());
    }

    public Map.Entry<Coord, String> nextMove() {
        return nextMove(EliminationMethods.BASICRADIATION.code() +
                EliminationMethods.NAKEDPAIRS.code() +
                EliminationMethods.INTERSECTION.code() +
                EliminationMethods.XWINGS.code());
    }

    public Map.Entry<Coord, String> nextMove(int level) {
        Map.Entry<Coord, String> nextMove = null;
        possibilities = new PencilMarkContainer(myPuzzle, trace);

        eliminate(level);

        if (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
            SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
            for (AbstractGroup g : myPuzzle.getGroups()) {
                g.addPossibilitiesToSolution(possibilities, sols);
            }
            nextMove = sols.getFirstMove();
        }
        return nextMove;
    }

    public boolean solve(int level) {
        while (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = nextMove(level);
            if (nextMove != null) {
                if (trace) {
                    System.out.println("Do move: " + nextMove.getValue() + " at " + nextMove.getKey());
                }
                IPuzzle nextPuzzle = myPuzzle.doMove(nextMove.getKey(), nextMove.getValue());
                myPuzzle = nextPuzzle;
                possibilities = new PencilMarkContainer(nextPuzzle, trace);
            } else {
                return false;
            }
        }
        return myPuzzle.isSolved();
    }

    // TODO: why??
    public IPuzzle getPuzzle()
    {
        return myPuzzle;
    }

    private Set<AbstractGroup> toRowGroups(Set<Integer> rowset) {
        Set<AbstractGroup> result = new HashSet<>();
        for (AbstractGroup g : myPuzzle.getGroups()) {
            if (g instanceof RowGroup) {
                if (rowset.contains(((RowGroup)g).getRow())) {
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
                if (colset.contains(((ColumnGroup)g).getColumn())) {
                    result.add(g);
                }
            }
        }
        return result;
    }

    public SolutionContainer getMoves() {
        SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
        for (AbstractGroup g : myPuzzle.getGroups()) {
            g.addPossibilitiesToSolution(possibilities, sols);
        }

        return sols;
    }

    public Map<Coord, Set<Integer>> getPencilMarks() {
        return possibilities.getAllPossibilities(myPuzzle);
    }

    public int getTotalNumberOfPencilMarks()
    {
        Map<Coord, Set<Integer>> p = possibilities.getAllPossibilities(myPuzzle);
        int result = 0;
        for (Coord c : p.keySet()) {
            result += p.get(c).size();
        }
        return result;
    }
}
