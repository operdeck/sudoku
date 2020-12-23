package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;

import java.util.*;

//http://www.extremesudoku.info/sudoku.html
//http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon

public class SudokuSolver {
    private PossibilitiesContainer possibilities;
    private IPuzzle myPuzzle;
    private boolean trace = true;

    public enum EliminationMethods {
        BASICRADIATION (1),
        NAKEDPAIRS  (2),
        INTERSECTION (4),
        XWINGS   (8),
        SIMPLEST (1),
        SMARTEST (15);

        private final int levelCode;
        EliminationMethods(int levelCode) {
            this.levelCode = levelCode;
        }
        int getLevelCode() {
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
        possibilities = new PossibilitiesContainer(p.getGroups(), trace);
    }

    public boolean eliminateByRadiationFromIntersections() {
        boolean updated = false;
        // TODO maybe not even need to explicitly create these intersections
        Set<GroupIntersection> groupIntersections = GroupIntersection.createGroupIntersections(myPuzzle.getGroups());

        for (GroupIntersection a : groupIntersections) {
            Set<Integer> pa = possibilities.getPossibilities(a.getIntersection());
            for (int digit : Digits.all) {
                if (pa.contains(digit)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] r = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i=0; i<2; i++) {
                        r[i] = new HashSet<>(a.getIntersectionGroup(i).getCoords());
                        r[i].removeAll(a.getIntersection());
                        pr[i] = possibilities.getPossibilities(r[i]);
                    }
                    for (int i=0; i<2; i++) {
                        if (!pr[i].contains(digit)) {
                            // If 'digit' is not possible anywhere else in this group, then it
                            // has to be in the intersection. Which means it cannot be
                            // anywhere else in the other group either.
                            if (possibilities.removePossibility(digit, r[1-i],
                                    " (in " + a.getIntersectionGroup(1-i) + ") because " +
                                            myPuzzle.symbolCodeToSymbol(digit) + " has to be in " +
                                            a.getIntersectionGroup(i) + " in one of " +
                                            a + " (Intersection Radiation)")) updated = true;;
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
        for (int digit : Digits.all) {
            // For each digit, figure out in which rows of each column it occurs. Then
            // get the set of columns that have the same row set. Same for rows x cols.
            // For those entries that have the same size of {columns} x {rows}, we now
            // know that 'digit' has to be in (one or more of) the intersections of those,
            // so it can be eliminated from the possibilities in each of those groups
            // outside of the intersections.

            Map<Set<AbstractGroup>, Set<AbstractGroup>> map = new HashMap<>();
            for (AbstractGroup g : myPuzzle.getGroups()) {
                Set<AbstractGroup> intersectingGroups = null;
                if (g instanceof ColumnGroup) {
                    // list of rows intersecting with column g
                    intersectingGroups = toRowGroups(g.getRowSet(digit, possibilities));
                } else if (g instanceof RowGroup) {
                    // list of columns intersecting with row g
                    intersectingGroups = toColumnGroups(g.getColSet(digit, possibilities));
                }
                if (intersectingGroups != null && !intersectingGroups.isEmpty()) {
                    Set<AbstractGroup> grps = map.computeIfAbsent(intersectingGroups, k -> new HashSet<AbstractGroup>());
                    grps.add(g);
                }
            }

            // Now, with this map, if the set of rows is of the same size as the set of
            // columns that have identical row sets, eliminate 'digit' from other cells
            // in the rows. Same for col vs row.
            for (Map.Entry<Set<AbstractGroup>, Set<AbstractGroup>> entry : map.entrySet()) {
                if (entry.getKey().size() > 1 &&
                        entry.getKey().size() == entry.getValue().size()) { // becomes * 2 now ??
                    for (AbstractGroup g : entry.getKey()) {
                        // Eliminate 'digit' from this row 'g' except for the groups it intersects
                        Set<Coord> candidateRemovals = new TreeSet<Coord>();
                        candidateRemovals.addAll(g.getCoords());
                        for (AbstractGroup other : entry.getValue()) {
                            candidateRemovals.removeAll(other.getCoords());
                        }
                        if (possibilities.removePossibility(digit, candidateRemovals,
                                " of " + g + " because " + myPuzzle.symbolCodeToSymbol(digit) + " has to be in " +
                                        entry.getKey() + " X " + entry.getValue() + " (X-Wing)")) updated = true;
                    }
                }
            }
        }
        return updated;
    }

    public boolean solveSimplest() {
        return solve(EliminationMethods.BASICRADIATION);
    }

    public boolean solve() {
        return solve(EliminationMethods.SMARTEST);
    }

    public Map.Entry<Coord, Integer> nextMove() {
        return nextMove(EliminationMethods.SMARTEST);
    }

    public Map.Entry<Coord, Integer> nextMove(EliminationMethods level) {
        Map.Entry<Coord, Integer> nextMove = null;

        if ((level.getLevelCode() & EliminationMethods.BASICRADIATION.getLevelCode()) != 0) {
            if(trace) System.out.println("Eliminate with BASIC radiation");
        }

        if (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
            possibilities = new PossibilitiesContainer(myPuzzle.getGroups(), trace);

            if ((level.getLevelCode() & EliminationMethods.NAKEDPAIRS.getLevelCode()) != 0) {
                if(trace) System.out.println("Eliminate naked pairs");
                eliminateNakedPairs();
            }
            if ((level.getLevelCode() & EliminationMethods.INTERSECTION.getLevelCode()) != 0) {
                if(trace) System.out.println("Eliminate intersections");
                eliminateByRadiationFromIntersections();
            }
            if ((level.getLevelCode() & EliminationMethods.XWINGS.getLevelCode()) != 0) {
                if(trace) System.out.println("Eliminate XWings");
                eliminateByXWings();
            }

            SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
            for (AbstractGroup g : myPuzzle.getGroups()) {
                g.addLoneNumbersToSolution(possibilities, sols);
                g.addUniqueValuesToSolution(possibilities, sols);
            }
            Iterator<Map.Entry<Coord, Integer>> it = sols.getSolutions().entrySet().iterator();
            if (it.hasNext()) {
                nextMove = it.next();
            }
        }
        return nextMove;
    }

    public boolean solve(EliminationMethods level) {
        while (!myPuzzle.isSolved() && !myPuzzle.isInconsistent()) {
            Map.Entry<Coord, Integer> nextMove = nextMove(level);
            if (nextMove != null) {
                if (trace) {
                    System.out.println("Do move: " + nextMove.getValue() + " at " + nextMove.getKey());
                }
                IPuzzle nextPuzzle = myPuzzle.doMove(nextMove.getKey(),
                        myPuzzle.symbolCodeToSymbol(nextMove.getValue().intValue()));
                myPuzzle = nextPuzzle;
                possibilities = new PossibilitiesContainer(nextPuzzle.getGroups(), trace);
            } else {
                return false;
            }
        }
        //if (myPuzzle.isSolved()) System.out.println("Solved: " + myPuzzle.getName());
        //System.out.println(myPuzzle);
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

    public SolutionContainer getLoneNumbers() {
        SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
        for (AbstractGroup g : myPuzzle.getGroups()) {
            g.addLoneNumbersToSolution(possibilities, sols);
        }

        return sols;
    }

    public SolutionContainer getUniqueValues() {
        SolutionContainer sols = new SolutionContainer(myPuzzle, trace);
        for (AbstractGroup g : myPuzzle.getGroups()) {
            g.addUniqueValuesToSolution(possibilities, sols);
        }

        return sols;
    }

    public Map<Coord, Set<Integer>> getAllPossibilities() {
        return possibilities.getAllPossibilities();
    }

    public int getNumberOfPossibilities()
    {
        Map<Coord, Set<Integer>> p = possibilities.getAllPossibilities();
        int result = 0;
        for (Coord c : p.keySet()) {
            result += p.get(c).size();
        }
        return result;
    }
}
