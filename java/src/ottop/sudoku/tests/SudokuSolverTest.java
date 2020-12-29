package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.SolutionContainer;
import ottop.sudoku.SudokuSolver;
import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.NRCPuzzle;
import ottop.sudoku.puzzle.Standard9x9Puzzle;

import static org.junit.Assert.*;

public class SudokuSolverTest {
    private SudokuSolver solver;

    @Before
    public void setUp()
    {
        IPuzzle p = new NRCPuzzle("Test",
                "....65...",
                ".......6.",
                "1......78",
                ".........",
                "..27.....",
                ".3..9...1",
                "..6..45..",
                ".8...2...",
                "........." );
        solver = new SudokuSolver(p, false);
    }

    @Test
    public void testBasicElimination()
    {
        assertEquals(65, solver.getPencilMarks().size()); // is just empty squares
        assertEquals(311, solver.getTotalNumberOfPencilMarks());

        SolutionContainer solutions = solver.getMoves();
        assertEquals(1, solutions.getLoneSymbols().size());

        assertEquals(6, solutions.getUniqueSymbols().size());
    }

    @Test
    public void testRadiationFromIntersections()
    {
        solver.eliminateByRadiationFromIntersections();

        assertEquals(65, solver.getPencilMarks().size()); // empty cells remain the same
        assertEquals(261, solver.getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        SolutionContainer solutions = solver.getMoves();
        assertEquals(1, solutions.getLoneSymbols().size());

        assertEquals(9, solutions.getUniqueSymbols().size()); // increased after this elimination step
    }

    @Test
    public void testNakedPairElimination()
    {
        solver.eliminateNakedPairs();

        assertEquals(65, solver.getPencilMarks().size()); // empty cells remain the same
        assertEquals(264, solver.getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        SolutionContainer solutions = solver.getMoves();
        assertEquals(10, solutions.getLoneSymbols().size());

        assertEquals(13, solutions.getUniqueSymbols().size()); // increased after this elimination step
    }

    @Test
    public void testRadiationFromIntersectionsAndNakedPairElimination()
    {
        solver.eliminateByRadiationFromIntersections();
        solver.eliminateNakedPairs();

        assertEquals(65, solver.getPencilMarks().size()); // empty cells remain the same
        assertEquals(157, solver.getTotalNumberOfPencilMarks()); // possibilities strongly reduced

        SolutionContainer solutions = solver.getMoves();
        assertEquals(27, solutions.getLoneSymbols().size());

        assertEquals(31, solutions.getUniqueSymbols().size()); // increased after this elimination step
    }

    @Test
    public void testSolveSimplePuzzle()
    {
        assertTrue(solver.solveSimplest());

        assertEquals( "Test:\n"+
                "748165392\n" +
                "329478165\n" +
                "165329478\n" +
                "671843259\n" +
                "892751643\n" +
                "534296781\n" +
                "216984537\n" +
                "487532916\n" +
                "953617824\n", solver.getPuzzle().toString());
    }

    @Test
    public void testPuzzleState()
    {
        solver = new SudokuSolver(PuzzleDB.Trouw_535, false);
        assertTrue(solver.solve());

        // Doing the same thing again should work if no state is left behind in the puzzle itself
        solver = new SudokuSolver(PuzzleDB.Trouw_535, false);
        assertTrue(solver.solve());
    }

    @Test
    public void testCantSolveWithJustBasicRadiation() {
        solver = new SudokuSolver(PuzzleDB.Parool_18nov, false);
        assertFalse(solver.solve(SudokuSolver.EliminationMethods.BASICRADIATION.code()));
    }

    @Test
    public void testNeedSomeSmarterSolution() {
        solver = new SudokuSolver(PuzzleDB.Parool_18nov, false);
        assertTrue(solver.solve(SudokuSolver.EliminationMethods.INTERSECTION.code())); // some others work as well
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
        solver = new SudokuSolver(p, false);
        solver.eliminateByRadiationFromIntersections();
        assertEquals(0, solver.getMoves().size());

        solver.eliminateNakedPairs();
        assertEquals(2, solver.getMoves().size());
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
        solver = new SudokuSolver(p, false);
        solver.eliminateByRadiationFromIntersections();
        solver.eliminateNakedPairs();
        assertEquals(0, solver.getMoves().size());

        solver.eliminateByXWings();
        assertEquals(2, solver.getMoves().size());
    }

    @Test
    public void testUnsolvable() {
        solver = new SudokuSolver(PuzzleDB.unsolvable, false);
        assertFalse(solver.solve());
    }

    @Test
    public void testEasyPuzzles()
    {
        IPuzzle[] puzzles = {PuzzleDB.NRC_17nov, PuzzleDB.NRC_5dec14,
        PuzzleDB.NRC_28dec};

        for (IPuzzle p: puzzles) {
            solver = new SudokuSolver(p, false);
            assertTrue("Can't solve " + p.getName(), solver.solveSimplest());
        }
    }

    @Test
    public void testCharPuzzle()
    {
        solver = new SudokuSolver(PuzzleDB.EOC_dec14, false);
        assertTrue(solver.solve());
    }

    @Test
    public void testHarderPuzzles()
    {
        IPuzzle[] puzzles = {PuzzleDB.www_extremesudoku_info_evil,
        PuzzleDB.extremesudoku_10_nov_2013,
        PuzzleDB.extremesudoku_28_nov_2013};

        for (IPuzzle p: puzzles) {
            solver = new SudokuSolver(p, false);
            assertTrue("Can't solve " + p.getName(), solver.solve());
        }
    }

    @Test
    public void testHardestPuzzles()
    {
        IPuzzle[] puzzles = {PuzzleDB.www_extremesudoku_info_evil_271113};

        for (IPuzzle p: puzzles) {
            solver = new SudokuSolver(p, false);
            assertTrue("Can't solve " + p.getName(), solver.solve());
        }
    }

}
