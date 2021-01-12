package ottop.sudoku.solver;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.ColumnGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.board.RowGroup;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.explain.XWingEliminationReason;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

public class XWingEliminator extends Eliminator {

    XWingEliminator(ISudoku myPuzzle, Map<Coord, Set<Integer>> candidatesPerCell, Map<Coord, List<Explanation>> removalReasons) {
        super(myPuzzle, candidatesPerCell, removalReasons);
    }

    public boolean eliminate() {
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



}
