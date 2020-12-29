package ottop.sudoku;

import org.jetbrains.annotations.NotNull;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

public class PencilMarkContainer {
    // Map of cell to a set of possible values. The values are the
    // internal representation of the cell symbols.
    private final Map<Coord, Set<Integer>> pencilMarks = new HashMap<>();
    // TODO
    // private final Map<Coord, Map<Integer, EliminationReason>> removalReasons = new HashMap<>();

    private final boolean trace;

    public PencilMarkContainer(IPuzzle p, boolean trace) {
        this.trace = trace;

        AbstractGroup[] groups = p.getGroups();
        for (Coord c : p.getAllCells()) {
            pencilMarks.put(c, getPossibilities(c, groups, p.getSymbolCodeRange()));
        }
    }

    public Map<Coord, Set<Integer>> getAllPossibilities(IPuzzle puzzle) {
        Map<Coord, Set<Integer>> allPossibilities = new HashMap<>();

        for (Coord c : puzzle.getAllCells()) {
            Set<Integer> p = pencilMarks.get(c);
            if (p != null && !p.isEmpty()) {
                allPossibilities.put(c, p);
            }
        }

        return allPossibilities;
    }

    private Set<Integer> getPossibilities(Coord coord, AbstractGroup[] groups, int symbolCodeRange) {
        Set<Integer> s = new HashSet<>();
        for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
            boolean isPossible = true;
            for (AbstractGroup g : groups) {
                if (!g.isPossibility(symbolCode, coord)) {
                    isPossible = false;
                    break;
                }
            }
            if (isPossible) s.add(symbolCode);
        }
        return s;
    }

    public Set<Integer> getPossibilities(Coord c) {
        return pencilMarks.get(c);
    }

    public boolean removePossibility(int symbolCode, Set<Coord> coords, EliminationReason reason) {
        Set<Coord> removed = new TreeSet<>();
        for (Coord c : coords) {
            Set<Integer> set = pencilMarks.get(c);
            if (set != null) {
                if (set.remove(symbolCode)) {
                    removed.add(c);
                }
            }
        }
        if (removed.size() > 0) {
            if (trace) {
                System.out.println(reason);
            }
        }
        return removed.size() > 0;
    }

    public boolean removePossibilities(Set<Integer> possibilities, Set<Coord> coords, EliminationReason reason) {
        Set<Integer> removed = new TreeSet<>();
        for (Coord c : coords) {
            for (int symbolCode : possibilities) {
                Set<Integer> set = pencilMarks.get(c);
                if (set != null) {
                    if (set.remove(symbolCode)) removed.add(symbolCode);
                }
            }
            if (removed.size() > 0) {
                if (trace) {
                    System.out.println(reason);
                }
            }
        }
        return removed.size() > 0;
    }

    public Set<Integer> getPossibilities(@NotNull Set<Coord> subarea) {
        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(getPossibilities(c));
        }
        return p;
    }

//	public void merge(PencilMarkContainer newCache) {
//		for (Coord c : nonCollissionMap.keySet()) {
//			Set<Integer> p = nonCollissionMap.get(c);
//			p.retainAll(newCache.nonCollissionMap.get(c));
//  		}
//	}
}
