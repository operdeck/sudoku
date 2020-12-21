package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.*;

import static org.junit.Assert.*;

public class SudokuSolverTest {
    private SudokuSolver solver;

    @Before
    public void setUp()
    {
        IPuzzle p = new NRCPuzzle(
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
        assertEquals(65, solver.getAllPossibilities().size()); // is just empty squares
        assertEquals(311, solver.getNumberOfPossibilities());

        SolutionContainer loneNumbers = solver.getLoneNumbers();
        assertEquals(1, loneNumbers.size());

        SolutionContainer uniqueValues = solver.getUniqueValues();
        assertEquals(6, uniqueValues.size());
    }

    @Test
    public void testRadiationFromIntersections()
    {
        solver.eliminateByRadiationFromIntersections();

        assertEquals(65, solver.getAllPossibilities().size()); // empty cells remain the same
        assertEquals(261, solver.getNumberOfPossibilities()); // possibilities strongly reduced

        SolutionContainer loneNumbers = solver.getLoneNumbers();
        assertEquals(1, loneNumbers.size());

        SolutionContainer uniqueValues = solver.getUniqueValues();
        assertEquals(9, uniqueValues.size()); // increased after this elimination step
    }

    @Test
    public void testNakedPairElimination()
    {
        solver.eliminateNakedPairs();

        assertEquals(65, solver.getAllPossibilities().size()); // empty cells remain the same
        assertEquals(266, solver.getNumberOfPossibilities()); // possibilities strongly reduced

        SolutionContainer loneNumbers = solver.getLoneNumbers();
        assertEquals(9, loneNumbers.size());

        SolutionContainer uniqueValues = solver.getUniqueValues();
        assertEquals(13, uniqueValues.size()); // increased after this elimination step
    }

    @Test
    public void testRadiationFromIntersectionsAndNakedPairElimination()
    {
        solver.eliminateByRadiationFromIntersections();
        solver.eliminateNakedPairs();

        assertEquals(65, solver.getAllPossibilities().size()); // empty cells remain the same
        assertEquals(157, solver.getNumberOfPossibilities()); // possibilities strongly reduced

        SolutionContainer loneNumbers = solver.getLoneNumbers();
        assertEquals(27, loneNumbers.size());

        SolutionContainer uniqueValues = solver.getUniqueValues();
        assertEquals(31, uniqueValues.size()); // increased after this elimination step
    }

    @Test
    public void testSolveSimplePuzzle()
    {
        assertTrue(solver.solveSimplest());

        assertEquals( ""+
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
        assertFalse(solver.solve(SudokuSolver.EliminationMethods.BASICRADIATION));
    }

    @Test
    public void testNeedSomeSmarterSolution() {
        solver = new SudokuSolver(PuzzleDB.Parool_18nov, false);
        assertTrue(solver.solve(SudokuSolver.EliminationMethods.INTERSECTION)); // some others work as well
    }

    @Test
    public void testUnsolvable() {
        solver = new SudokuSolver(PuzzleDB.unsolvable, false);
        // TODO: this gives an exception
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
        // TODO: I don't think we even want to solve this, but it gives an exception
        assertFalse(solver.solve());
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
