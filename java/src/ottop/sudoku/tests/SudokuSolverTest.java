package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.SudokuSolver;
import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.NRCPuzzle;
import ottop.sudoku.puzzle.Standard9x9Puzzle;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SudokuSolverTest {
    IPuzzle p;
    private SudokuSolver solver;

    @Before
    public void setUp() {
        p = new NRCPuzzle("Test",
                "....65...",
                ".......6.",
                "1......78",
                ".........",
                "..27.....",
                ".3..9...1",
                "..6..45..",
                ".8...2...",
                ".........");
        solver = new SudokuSolver(p);
    }

    private int getTotalNumberOfPencilMarks() {
        Map<Coord, Set<Integer>> p = solver.getPossibilitiesContainer().getAllCandidates();
        int result = 0;
        for (Coord c : p.keySet()) {
            result += p.get(c).size();
        }
        return result;
    }

    @Test
    public void testBasicElimination() {
        assertEquals(65, solver.getPossibilitiesContainer().getAllCandidates().size()); // is just empty squares
        assertEquals(311, getTotalNumberOfPencilMarks());

        assertEquals(1, solver.getNakedSingles().size());
        assertEquals(6, solver.getUniqueValues().size());
    }

    @Test
    public void testRadiationFromIntersections() {

        solver.setSimplest().setEliminateIntersectionRadiation();
        solver.eliminatePossibilities();

        assertEquals(65, solver.getPossibilitiesContainer().getAllCandidates().size()); // empty cells remain the same
        assertEquals(261, getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        assertEquals(1, solver.getNakedSingles().size());

        assertEquals(9, solver.getUniqueValues().size()); // increased after this elimination step
    }

    @Test
    public void testNakedPairElimination() {

        solver.setSimplest().setEliminateNakedPairs();
        solver.eliminatePossibilities();

        assertEquals(65, solver.getPossibilitiesContainer().getAllCandidates().size()); // empty cells remain the same
        assertEquals(264, getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        assertEquals(10, solver.getNakedSingles().size());
        assertEquals(13, solver.getUniqueValues().size()); // increased after this elimination step
    }

    @Test
    public void testRadiationFromIntersectionsAndNakedPairElimination() {
        solver.setSimplest().setEliminateIntersectionRadiation().setEliminateNakedPairs();
        solver.eliminatePossibilities();

        assertEquals(65, solver.getPossibilitiesContainer().getAllCandidates().size()); // empty cells remain the same
        assertEquals(184, getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        assertEquals(12, solver.getNakedSingles().size());
        assertEquals(23, solver.getUniqueValues().size()); // increased after this elimination step
    }

    @Test
    public void testSolveSimplePuzzle() {
        solver.setSimplest();
        IPuzzle solvedPuzzle = solver.solve();

        assertNotNull(solvedPuzzle);

        assertEquals("Test:\n" +
                "748165392\n" +
                "329478165\n" +
                "165329478\n" +
                "671843259\n" +
                "892751643\n" +
                "534296781\n" +
                "216984537\n" +
                "487532916\n" +
                "953617824\n", solvedPuzzle.toString());
    }

    @Test
    public void testRepeatedSolves() {
        solver = new SudokuSolver(PuzzleDB.Trouw_535);
        assertNotNull(solver.solve());

        // Doing the same thing again should work if no state is left behind in the puzzle itself
        solver = new SudokuSolver(PuzzleDB.Trouw_535);
        assertNotNull(solver.solve());
    }

    @Test
    public void testPuzzleNeedsIntersectionRadiation() {
        solver = (new SudokuSolver(PuzzleDB.Parool_18nov)).setSimplest();
        assertNull(solver.solve());

        solver.setEliminateIntersectionRadiation();
        assertNotNull(solver.solve());
    }

    @Test
    public void testNakedPairs() {
        IPuzzle p = new Standard9x9Puzzle("https://www.sudokuessentials.com/support-files/sudoku-very-hard-1.pdf",
                ".374816.9",
                ".9..27.38",
                "8..3.9...",
                ".19873.6.",
                "78...2.93",
                "...9.487.",
                "...295..6",
                "...1369..",
                "962748315");

        solver = (new SudokuSolver(p)).setEliminateIntersectionRadiation();
        solver.eliminatePossibilities();
        assertEquals(0, solver.getPossibleMoves().size());

        solver.setEliminateNakedPairs();
        solver.eliminatePossibilities();
        assertEquals(2, solver.getPossibleMoves().size());
    }

    @Test
    public void testXWings() {
        IPuzzle p = new Standard9x9Puzzle("https://www.sudokuessentials.com/x-wing.html",
                ".374816.9",
                ".9..27.38",
                "8..3.9...",
                ".19873.6.",
                "78...2.93",
                "...9.487.",
                "...295.86",
                "..81369..",
                "962748315");
        solver = (new SudokuSolver(p)).setEliminateIntersectionRadiation(true).setEliminateNakedPairs(true);
        solver.eliminatePossibilities();
        assertEquals(0, solver.getPossibleMoves().size());

        solver.setEliminateXWings();
        solver.eliminatePossibilities();
        assertEquals(2, solver.getPossibleMoves().size());
    }

    @Test
    public void testUnsolvable() {
        solver = new SudokuSolver(PuzzleDB.unsolvable);
        assertNull(solver.solve());
    }

    @Test
    public void testEasyPuzzles() {
        IPuzzle[] puzzles = {PuzzleDB.NRC_17nov, PuzzleDB.NRC_5dec14,
                PuzzleDB.NRC_28dec};

        for (IPuzzle p : puzzles) {
            solver = new SudokuSolver(p);
            assertNotNull("Can't solve " + p.getName(), solver.solve());
        }
    }

    @Test
    public void testCharPuzzle() {
        solver = new SudokuSolver(PuzzleDB.EOC_dec14);
        assertEquals("Char puzzle:\n" +
                "SHFCRPTIU\n" +
                "IPRTUSCHF\n" +
                "UCTIFHRSP\n" +
                "FRIPSUHCT\n" +
                "TUHFCISPR\n" +
                "CSPHTRFUI\n" +
                "RTSUICPFH\n" +
                "PFUSHTIRC\n" +
                "HICRPFUTS\n" , solver.solve().toString());
    }

    @Test
    public void testHarderPuzzles() {
        IPuzzle[] puzzles = {PuzzleDB.www_extremesudoku_info_evil,
                PuzzleDB.extremesudoku_10_nov_2013,
                PuzzleDB.extremesudoku_28_nov_2013};

        for (IPuzzle p : puzzles) {
            solver = new SudokuSolver(p);
            solver.setSmartest();
            solver.eliminatePossibilities();
            assertNotNull("Can't solve " + p.getName(), solver.solve());
        }
    }

    @Test
    public void testExtremeEvilSuduko() {
        IPuzzle[] puzzles = {PuzzleDB.www_extremesudoku_info_evil_271113};

        for (IPuzzle p : puzzles) {
            solver = new SudokuSolver(p);
            IPuzzle solvedPuzzle = solver.solve();

            assertNull("Can't solve " + p.getName(), solvedPuzzle);
        }
    }

}
