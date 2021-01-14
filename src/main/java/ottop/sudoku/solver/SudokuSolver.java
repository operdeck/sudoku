package ottop.sudoku.solver;

import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.*;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.puzzle.ISudoku;

import java.util.*;

// http://www.extremesudoku.info/sudoku.html
// http://en.wikipedia.org/wiki/List_of_Sudoku_terms_and_jargon
// https://www.sudokuessentials.com/x-wing.html
// https://www.extremesudoku.info/

public class SudokuSolver implements Updateable {
    private ISudoku myPuzzle;

    // Map of cell to a set of possible values. The values are the
    // internal representation of the cell symbols.
    private Map<Coord, Set<Integer>> candidatesPerCell = null;
    private final Map<Coord, List<Explanation>> eliminationReasons = new HashMap<>();

    // TODO instead of this,
    // keep a list of Eliminator objects that are used for elimination
    private List<Eliminator> eliminators = new ArrayList<>();

    private boolean doEliminationNakedPairs;
    private boolean doEliminationIntersectionRadiation;
    private boolean doEliminationXWings;

    public SudokuSolver(ISudoku p) {
        myPuzzle = p;
        p.setSolver(this);
        setSimplest();
    }

    public SudokuSolver setEliminateNakedPairs() {
        return setEliminateNakedPairs(true);
    }

    public SudokuSolver setEliminateNakedPairs(boolean onOff) {
        doEliminationNakedPairs = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
        return this;
    }

    public SudokuSolver setEliminateIntersectionRadiation() {
        return setEliminateIntersectionRadiation(true);
    }

    public SudokuSolver setEliminateIntersectionRadiation(boolean onOff) {
        doEliminationIntersectionRadiation = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
        return this;
    }

    public SudokuSolver setEliminateXWings() {
        return setEliminateXWings(true);
    }

    public SudokuSolver setEliminateXWings(boolean onOff) {
        doEliminationXWings = onOff;
        candidatesPerCell = null; // flags that this cache needs reinitialization
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

    private void recalculateCandidates()
    {
        candidatesPerCell = new HashMap<>();

        // Clear out the reasons for the non-occupied cells
        // TODO: this may not work out for undo/redo sequences
        for (Coord c: myPuzzle.getAllCells()) {
            if (!myPuzzle.isOccupied(c)) {
                eliminationReasons.put(c, null);
            }
        }

        // TODO maybe fill all cell candidates with all symbols

        Eliminator simpleEliminator =
                new BasicEliminationEliminator(myPuzzle, candidatesPerCell, eliminationReasons);
        simpleEliminator.eliminate();

        //oneRoundOfCandidateElimination();
    }

    private boolean oneRoundOfCandidateElimination() {
        // Basic radiation will be done always

        boolean hasEliminated = false;

        if (doEliminationNakedPairs) {
            Eliminator e = new NakedGroupEliminator(myPuzzle, candidatesPerCell, eliminationReasons);
            if (e.eliminate()) hasEliminated = true;
        }
        if (!hasEliminated && doEliminationIntersectionRadiation) {
            Eliminator e = new IntersectionRadiationEliminator(myPuzzle, candidatesPerCell, eliminationReasons);
            if (e.eliminate()) hasEliminated = true;
        }
        if (!hasEliminated && doEliminationXWings) {
            Eliminator e = new XWingEliminator(myPuzzle, candidatesPerCell, eliminationReasons);
            if (e.eliminate()) hasEliminated = true;
        }

        return hasEliminated;
    }


    private boolean checkForcedChains()
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

    public Map<Coord, String> getAllNakedSingles() {
        if (candidatesPerCell == null) recalculateCandidates();

        return getNakedSingles(true);
    }

    private Map.Entry<Coord, String> getFirstNakedSingle() {
        Map<Coord, String> results = getNakedSingles(false);
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    private String getNakedSingleAt(Coord coord) {
        if (!myPuzzle.isOccupied(coord)) {
            Set<Integer> cellPossibilities = candidatesPerCell.get(coord);
            if (cellPossibilities != null) {
                if (cellPossibilities.size() == 1) {
                    Integer symbolCode = cellPossibilities.iterator().next();
                    return myPuzzle.symbolCodeToSymbol(symbolCode);
                }
            }
        }
        return null;
    }

    private Map<Coord, String> getNakedSingles(boolean all) {
        Map<Coord, String> result = new TreeMap<>();
        for (Coord coord: myPuzzle.getAllCells()) {
            String symbol = getNakedSingleAt(coord);
            if (symbol != null) {
                result.put(coord, symbol);
                if (!all) return result;
            }
        }
        return result;
    }

    public Map<Coord, Map.Entry<String, List<AbstractGroup>>> getAllUniqueValues() {
        if (candidatesPerCell == null) recalculateCandidates();

        return getUniqueValues(true, myPuzzle.getGroups());
    }

    private Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> getFirstUniqueValue() {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> results =
                getUniqueValues(false, myPuzzle.getGroups());
        if (results.size() >= 1) {
            return results.entrySet().iterator().next();
        }
        return null;
    }

    // TODO: this is not super efficient - will consider too many coordinates
    private Map.Entry<String, List<AbstractGroup>> getUniqueValueAt(Coord c) {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValues =
                getUniqueValues(false, myPuzzle.getBuddyGroups(c));
        if (uniqueValues != null && uniqueValues.size()>0) {
            Map.Entry<String, List<AbstractGroup>> uniqueValue = uniqueValues.get(c);
            return uniqueValue;
        }
        return null;
    }

    private Map<Coord, Map.Entry<String, List<AbstractGroup>>> getUniqueValues(boolean all, AbstractGroup[] groups) {
        Map<Coord, Map.Entry<String, List<AbstractGroup>>> result = new TreeMap<>();

        for (AbstractGroup g: groups) {
            for (Coord c: g.getCoords()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> remainingPossibilities = new HashSet<>(candidatesPerCell.get(c));
                    for (Coord otherCell : g.getCoords()) {
                        if (!myPuzzle.isOccupied(otherCell)) {
                            if (!otherCell.equals(c)) {
                                remainingPossibilities.removeAll(candidatesPerCell.get(otherCell));
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
//                        recordEliminationReason(c, new UniqueValueSolution(myPuzzle.symbolCodeToSymbol(symbolCode), c,
//                                result.get(c).getValue()));
                        if (!all) return result;
                    }
                }
            }
        }

        return result;
    }


    public Map.Entry<Coord, String> nextMove(SolveStats stats) {
        if (candidatesPerCell == null) {
            recalculateCandidates();
        }

        stats.startFindMove();
        Map.Entry<Coord, String> nextMove = null;
        while (!myPuzzle.isComplete() && nextMove == null) {
            stats.addIteration();

            // TODO: multiple iterations could count as higher level

            // see if there is a move, naked singles first
            nextMove = getFirstNakedSingle();
            if (nextMove == null) {
                Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueSymbol =
                        getFirstUniqueValue();
                if (uniqueSymbol != null) {
                    nextMove = new AbstractMap.SimpleEntry<>(uniqueSymbol.getKey(), uniqueSymbol.getValue().getKey());
                }
            }

            // no move? try different types of elimination, possibly iteratively
            if (nextMove == null) {
                boolean hasEliminatedCandidates = oneRoundOfCandidateElimination();

                if (!hasEliminatedCandidates) break;
            }
        }
        stats.endFindMove();

        return nextMove;
    }

    // TODO: this is ONLY used in tests right now - consider moving there
    public boolean solve() {
        if (candidatesPerCell == null) recalculateCandidates();

        SolveStats stats = new SolveStats();
        while (!myPuzzle.isComplete() && !myPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = nextMove(stats);

            if (nextMove != null) {
                myPuzzle.doMove(nextMove.getKey(), nextMove.getValue());
            } else {
                return false;
            }
        }
        return myPuzzle.isSolved();
    }

    // Add moves on the fly if there are any
    public List<Explanation> getEliminationReasons(Coord c) {
        if (candidatesPerCell == null) recalculateCandidates();

        List<Explanation> reasonsPlusCandidateMove = new ArrayList<>();
        reasonsPlusCandidateMove.addAll(eliminationReasons.get(c));
        String symbol = getNakedSingleAt(c);
        if (symbol != null) {
            reasonsPlusCandidateMove.add(new NakedSingleSolution(symbol, c));
        } else {
            Map.Entry<String, List<AbstractGroup>> uniqueValue = getUniqueValueAt(c);
            if (uniqueValue != null) {
                reasonsPlusCandidateMove.add(new UniqueValueSolution(uniqueValue.getKey(), c,
                        uniqueValue.getValue()));
            }
        }
        return reasonsPlusCandidateMove;
    }

    public static int assessDifficulty(ISudoku p) {
        ISudoku shadowPuzzle = p; // clone should not be necessary at all
        //System.out.println("Difficulty " + p.getName());
        SudokuSolver sv = new SudokuSolver(shadowPuzzle);
        SolveStats s = new SolveStats();
        sv.setSmartest();
        int maxReasonLevel = -1;
        //int maxNumberOfIterations = 1;
        while (!shadowPuzzle.isComplete() && !shadowPuzzle.isInconsistent()) {
            Map.Entry<Coord, String> nextMove = sv.nextMove(s);

            if (nextMove != null) {
                // TODO: reasons could be recursive if dependent on other non-trivial cells
                List<Explanation> reasons = sv.getEliminationReasons(nextMove.getKey());
                for (Explanation r : reasons) {
//                    System.out.println("Difficulty " + r.getDifficulty() + ":" + r);
//                    System.out.println("Stats** " + String.valueOf(s));
                    maxReasonLevel = Math.max(maxReasonLevel, r.getDifficulty());
                }

                // Bonus when multiple rounds needed
                //maxNumberOfIterations = Math.max(maxNumberOfIterations, sv.numberOfEliminationIterations);

                shadowPuzzle.doMove(nextMove.getKey(), nextMove.getValue());
           } else {
                break;
            }
        }

        // Bonus when multiple rounds were needed in some step
//        System.out.println("Stats** " + String.valueOf(s));
        maxReasonLevel = maxReasonLevel + s.getIterations() - 1;

        if (!shadowPuzzle.isSolved()) return -1;

        return maxReasonLevel;
    }


//    public PossibilitiesContainer getPossibilitiesContainer() {
//        return possibilitiesContainer;
//    }

    public Set<Integer> getCandidatesAtCell(Coord c) {
    if (candidatesPerCell == null) recalculateCandidates();

    return candidatesPerCell.get(c);
    }

    @Override
    public void update() {
        recalculateCandidates();
    }
}
