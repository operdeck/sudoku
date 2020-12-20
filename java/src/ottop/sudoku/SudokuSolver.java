package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;

import java.util.*;

public class SudokuSolver {
    private final PossibilitiesContainer possibilities;
    private final IPuzzle myPuzzle;

    public SudokuSolver(IPuzzle p) {
        myPuzzle = p;
        possibilities = new PossibilitiesContainer(p.getGroups());
    }

    // Do one elimination step taking account the flags. Returns true if anything
    // has been updated.
    public boolean doEliminationStep() {

        return false;
    }

    public boolean eliminateByRadiationFromIntersections() {
        boolean updated = false;

        for (GroupIntersection a : myPuzzle.getIntersections()) {
            Set<Integer> pa = possibilities.getPossibilities(a.getIntersection());
            for (int digit : Digits.all) {
                if (pa.contains(digit)) {
                    @SuppressWarnings("unchecked")
                    Set<Coord>[] r = new Set[2];
                    @SuppressWarnings("unchecked")
                    Set<Integer>[] pr = new Set[2];
                    for (int i=0; i<2; i++) {
                        r[i] = new HashSet<Coord>(a.getIntersectionGroup(i).getCoords());
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
                                            myPuzzle.toChar(digit) + " has to be in " +
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
            if (g.eliminateNakedPairs(myPuzzle, possibilities)) updated = true;
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

            Map<Set<AbstractGroup>, Set<AbstractGroup>> map = new HashMap<Set<AbstractGroup>, Set<AbstractGroup>>();
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
                    Set<AbstractGroup> grps = map.get(intersectingGroups);
                    if (grps == null) {
                        grps = new HashSet<AbstractGroup>();
                        map.put(intersectingGroups, grps);
                    }
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
                                " of " + g + " because " + myPuzzle.toChar(digit) + " has to be in " +
                                        entry.getKey() + " X " + entry.getValue() + " (X-Wing)")) updated = true;
                    }
                }
            }
        }
        return updated;
    }

    public boolean solve() {
        return false;
    }

    //http://www.extremesudoku.info/sudoku.html
    //http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
/*
    @Override
    public int solve() {
        boolean updated = false;
        int iterations = 0;
        SolutionContainer sols = new SolutionContainer(this);
        PossibilitiesContainer possibilities = null;
        do {
            updated = false;

            PossibilitiesContainer newCache =
                    new PossibilitiesContainer(myPuzzle.getGroups());
            if (possibilities == null) {
                possibilities = newCache;
            } else {
                possibilities.merge(newCache);
            }

            // Try progressively, simple solutions first
            System.out.println("Check unique cells:");
            for (AbstractGroup g : myPuzzle.getGroups()) {
                if (g.addUniqueValuesToSolution(possibilities, sols)) updated = true;
            }
            if (!updated) {
                System.out.println("Check lone numbers (that can't be placed anywhere else):");
                for (AbstractGroup g : myPuzzle.getGroups()) {
                    if (g.addLoneNumbersToSolution(possibilities, sols)) updated = true;
                }
            }
            if (!updated) {
                System.out.println("Eliminate by radiation from intersections:");
                if (eliminateByRadiationFromIntersections(possibilities)) updated = true;
            }
            if (!updated) {
                System.out.println("Eliminate naked pairs:");

            }
            if (!updated) {
                System.out.println("Eliminate by X-Wings:");
                if (eliminateByXWings(possibilities)) updated = true;
            }

            if (updated) {
                iterations++;
                System.out.println(sols.toString(this));
                init(sols.merge(this));
            }
        } while (updated && !isSolved());

        if (!isSolved()) {
            System.out.println(possibilities);
        } else {
            System.out.println("Final solution in " + iterations + " iterations:");
            System.out.println(sols.toString(this));
        }

        return iterations;
    }
*/

    private Set<AbstractGroup> toRowGroups(Set<Integer> rowset) {
        Set<AbstractGroup> result = new HashSet<AbstractGroup>();
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
        Set<AbstractGroup> result = new HashSet<AbstractGroup>();
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
        SolutionContainer sols = new SolutionContainer(myPuzzle);
        for (AbstractGroup g : myPuzzle.getGroups()) {
            g.addLoneNumbersToSolution(possibilities, sols);
        }

        return sols;
    }

    public SolutionContainer getUniqueValues() {
        SolutionContainer sols = new SolutionContainer(myPuzzle);
        for (AbstractGroup g : myPuzzle.getGroups()) {
            g.addUniqueValuesToSolution(possibilities, sols);
        }

        return sols;
    }

    public Map<Coord, Set<Integer>> getAllPossibilities() {
        return possibilities.getAllPossibilities();
    }
}
