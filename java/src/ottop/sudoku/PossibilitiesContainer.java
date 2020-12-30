package ottop.sudoku;

import org.jetbrains.annotations.NotNull;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

public class PossibilitiesContainer {
    // Map of cell to a set of possible values. The values are the
    // internal representation of the cell symbols.
    private final Map<Coord, Set<Integer>> pencilMarks = new HashMap<>();
    private final Map<Coord, List<EliminationReason>> removalReasons = new HashMap<>();

    public PossibilitiesContainer(IPuzzle p) {

        AbstractGroup[] groups = p.getGroups();
        for (Coord c : p.getAllCells()) {
            pencilMarks.put(c, getPossibilities(c, groups, p));
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

    private void recordEliminationReason(Coord coord, EliminationReason reason) {
        removalReasons.put(coord, reason.combine(removalReasons.get(coord)));
    }

    private Set<Integer> getPossibilities(Coord coord, AbstractGroup[] groups, IPuzzle p) {
        int symbolCodeRange = p.getSymbolCodeRange();
        Set<Integer> s = new HashSet<>();

        for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
            boolean isPossible = true;
            for (AbstractGroup g : groups) {
                if (!g.isPossibility(symbolCode, coord)) {
                    isPossible = false;
                    recordEliminationReason(coord, new SimpleEliminationReason(p.symbolCodeToSymbol(symbolCode), coord, g));
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

    public List<EliminationReason> getEliminations(Coord c) {
        return removalReasons.get(c);
    }

    public boolean removePossibility(int symbolCode, Set<Coord> coords, EliminationReason reason) {
        boolean anyRemoved = false;
        for (Coord c : coords) {
            boolean removedAtCurrentCoord = false;
            Set<Integer> currentPossibilities = pencilMarks.get(c);
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

    public boolean removePossibilities(Set<Integer> symbolCodes, Coord coord, EliminationReason reason) {
        boolean anyRemoved = false;
        for (int symbolCode : symbolCodes) {
            Set<Integer> currentPossibilities = pencilMarks.get(coord);
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

    public Set<Integer> getPossibilities(@NotNull Set<Coord> subarea) {
        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(getPossibilities(c));
        }
        return p;
    }
}
