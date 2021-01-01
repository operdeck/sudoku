package ottop.sudoku;

import org.jetbrains.annotations.NotNull;
import ottop.sudoku.explain.EliminationReason;
import ottop.sudoku.explain.SimpleEliminationReason;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

public class PossibilitiesContainer {
    // Map of cell to a set of possible values. The values are the
    // internal representation of the cell symbols.
    private final Map<Coord, Set<Integer>> candidatesPerCell = new HashMap<>();
    private final Map<Coord, List<EliminationReason>> removalReasons = new HashMap<>();
    private final IPuzzle myPuzzle;

    public PossibilitiesContainer(IPuzzle p) {
        myPuzzle = p;
        AbstractGroup[] groups = p.getGroups();
        for (Coord c : p.getAllCells()) {
            candidatesPerCell.put(c, getCandidatesAtCell(c, groups));
        }
    }

    public Map<Coord, Set<Integer>> getAllCandidates() {
        Map<Coord, Set<Integer>> allPossibilities = new HashMap<>();

        for (Coord c : myPuzzle.getAllCells()) {
            Set<Integer> p = candidatesPerCell.get(c);
            if (p != null && !p.isEmpty()) {
                allPossibilities.put(c, p);
            }
        }

        return allPossibilities;
    }

    public Set<Integer> getCandidatesAtCell(Coord c) {
        return candidatesPerCell.get(c);
    }

    public Set<Integer> getCandidatesAtCell(@NotNull Set<Coord> subarea) {
        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(getCandidatesAtCell(c));
        }
        return p;
    }

    public List<EliminationReason> getEliminations(Coord c) {
        return removalReasons.get(c);
    }

    private void recordEliminationReason(Coord coord, EliminationReason reason) {
        removalReasons.put(coord, reason.combine(removalReasons.get(coord)));
    }

    private Set<Integer> getCandidatesAtCell(Coord coord, AbstractGroup[] groups) {
        int symbolCodeRange = myPuzzle.getSymbolCodeRange();
        Set<Integer> s = new HashSet<>();

        for (int symbolCode = 1; symbolCode < symbolCodeRange; symbolCode++) {
            boolean isPossible = true;
            for (AbstractGroup g : groups) {
                if (!g.isPossibility(symbolCode, coord)) {
                    isPossible = false;
                    recordEliminationReason(coord,
                            new SimpleEliminationReason(myPuzzle.symbolCodeToSymbol(symbolCode), coord, g));
                    break;
                }
            }
            if (isPossible) s.add(symbolCode);
        }
        return s;
    }

    public boolean removePossibility(int symbolCode, Set<Coord> coords, EliminationReason reason) {
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

    public boolean removePossibilities(Set<Integer> symbolCodes, Coord coord, EliminationReason reason) {
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

    public String getLoneSymbolAt(Coord coord) {
        Set<Integer> cellPossibilities = getCandidatesAtCell(coord);
        if (cellPossibilities != null) {
            if (cellPossibilities.size() == 1) {
                Integer loneNumber = cellPossibilities.iterator().next();
                return myPuzzle.symbolCodeToSymbol(loneNumber);
            }
        }
        return null;
    }

    public AbstractMap.SimpleEntry<String, List<AbstractGroup>> getUniqueSymbolAt(Coord coord) {
        // for all groups G that coord is part of
        // see if there is a candidate in coord that is unique in G
        // if so add value + G
        AbstractMap.SimpleEntry<String, List<AbstractGroup>> result = null;

        List<AbstractGroup> grps = myPuzzle.getGroups(coord);
        for (AbstractGroup g : grps) {

            Set<Integer> remainingPossibilities = new HashSet<>(getCandidatesAtCell(coord));
            for (Coord otherCell : g.getCoords()) {
                if (!otherCell.equals(coord)) {
                    remainingPossibilities.removeAll(getCandidatesAtCell(otherCell));
                }
            }
            if (remainingPossibilities.size() == 1) {
                Integer uniqueSymbolCode = remainingPossibilities.iterator().next();

                if (null == result) {
                    result = new AbstractMap.SimpleEntry<>(myPuzzle.symbolCodeToSymbol(uniqueSymbolCode),
                            new ArrayList<>());
                }
                result.getValue().add(g);
            }
        }

        return result;
    }
}
