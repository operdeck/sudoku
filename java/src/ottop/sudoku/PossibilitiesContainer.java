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
            // TODO: strange call, maybe just check groups of cell
            candidatesPerCell.put(c, getCandidatesInArea(c, groups));
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

    public Set<Integer> getCandidatesInArea(@NotNull Set<Coord> subarea) {
        Set<Integer> p = new HashSet<>();
        for (Coord c : subarea) {
            p.addAll(getCandidatesAtCell(c));
        }
        return p;
    }

    public List<EliminationReason> getEliminationReasons(Coord c) {
        return removalReasons.get(c);
    }

    private void recordEliminationReason(Coord coord, EliminationReason reason) {
        removalReasons.put(coord, reason.combine(removalReasons.get(coord)));
    }

    private Set<Integer> getCandidatesInArea(Coord coord, AbstractGroup[] groups) {
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

    public Map<Coord, String> getAllNakedSingles() {
        return getNakedSingles(true);
    }

    public Map.Entry<Coord, String> getFirstNakedSingle() {
        Map<Coord, String> results = getNakedSingles(false);
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    private Map<Coord, String> getNakedSingles(boolean all) {
        Map<Coord, String> result = new TreeMap<>();
        for (Coord coord: myPuzzle.getAllCells()) {
            if (!myPuzzle.isOccupied(coord)) {
                Set<Integer> cellPossibilities = getCandidatesAtCell(coord);
                if (cellPossibilities != null) {
                    if (cellPossibilities.size() == 1) {
                        Integer symbolCode = cellPossibilities.iterator().next();
                        result.put(coord, myPuzzle.symbolCodeToSymbol(symbolCode));
                        if (!all) return result;
                    }
                }
            }
        }
        return result;
    }

    public Map<Coord, Map.Entry<String, List<AbstractGroup>>> getAllUniqueValues() {
        return getUniqueValues(true);
    }

    public Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> getFirstUniqueValue() {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> results = getUniqueValues(false);
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    private Map<Coord, Map.Entry<String, List<AbstractGroup>>> getUniqueValues(boolean all) {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> result = new TreeMap<>();

        for (AbstractGroup g: myPuzzle.getGroups()) {
            for (Coord c: g.getCoords()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> remainingPossibilities = new HashSet<>(getCandidatesAtCell(c));
                    for (Coord otherCell : g.getCoords()) {
                        if (!myPuzzle.isOccupied(otherCell)) {
                            if (!otherCell.equals(c)) {
                                remainingPossibilities.removeAll(getCandidatesAtCell(otherCell));
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
                    }
                }
            }
        }

        return result;
    }

}
