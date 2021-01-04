package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.board.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.NRCSudoku;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solve.SudokuSolver;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SudokuSolverTest {
    ISudoku p;
    private SudokuSolver solver;

    @Before
    public void setUp() {
        p = new NRCSudoku("Test",
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
        ISudoku solvedPuzzle = solver.solve();

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
        ISudoku p = new StandardSudoku("https://www.sudokuessentials.com/support-files/sudoku-very-hard-1.pdf",
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
        ISudoku p = new StandardSudoku("https://www.sudokuessentials.com/x-wing.html",
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
        assertEquals(3, solver.getPossibleMoves().size());
    }

    @Test
    public void testUnsolvable() {
        solver = new SudokuSolver(PuzzleDB.unsolvable);
        assertNull(solver.solve());
    }

    @Test
    public void testNakedPairAfterXWingMultipleEliminationRounds() {
        ISudoku p = PuzzleDB.extremesudoku_info_excessive_4jan2021;
        p = p.doMove(new Coord("r1c1"), "1");
        p = p.doMove(new Coord("r3c5"), "2");
        p = p.doMove(new Coord("r3c6"), "1");
        p = p.doMove(new Coord("r4c6"), "8");
        p = p.doMove(new Coord("r8c6"), "5");
        // skip the X-wings needed for next step
        p = p.doMove(new Coord("r6c1"), "7");
        p = p.doMove(new Coord("r8c3"), "7");

        // Now, there are
        //  a '39' naked pair at r4c3 and r9c3 but this can be found
        //  only after first finding a swordfish of 9s at r2/r5/r8 x c1/c4/c6

        solver = new SudokuSolver(p);
        solver.setSmartest();

        // Assert that with successive elimination steps we reduce the possibilities more and more

        assertEquals(true, solver.eliminatePossibilities());
        assertEquals(132, getTotalNumberOfPencilMarks());
        assertEquals(true, solver.eliminatePossibilities());
        assertEquals(125, getTotalNumberOfPencilMarks());
        assertEquals(true, solver.eliminatePossibilities());
        assertEquals(119, getTotalNumberOfPencilMarks());
        assertEquals(true, solver.eliminatePossibilities());
        assertEquals(92, getTotalNumberOfPencilMarks());
        assertEquals(true, solver.eliminatePossibilities());
        assertEquals(47, getTotalNumberOfPencilMarks());
        assertEquals(false, solver.eliminatePossibilities());
        assertEquals(47, getTotalNumberOfPencilMarks());

        // this is only possible after first XWing then Naked pairs
        assertEquals("r7c1=8", String.valueOf(solver.nextMove()) );

        // eventually, just make sure it is completely solved
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "196437852\n" +
                "274859613\n" +
                "385621974\n" +
                "463598127\n" +
                "912743586\n" +
                "758162349\n" +
                "821376495\n" +
                "647985231\n" +
                "539214768\n", String.valueOf(solver.solve()));
    }

    @Test
    public void testSwordfish() {
        ISudoku p = PuzzleDB.extremesudoku_info_excessive_4jan2021;
        p = p.doMove(new Coord("r1c1"), "1");
        p = p.doMove(new Coord("r3c5"), "2");
        p = p.doMove(new Coord("r3c6"), "1");
        p = p.doMove(new Coord("r4c6"), "8");
        p = p.doMove(new Coord("r8c6"), "5");

        solver = new SudokuSolver(p);

        // Now, there are
        //  x-wing of 8s at r3c2, r3c9, r9c2, r9c9
        //  x-wing of 2s at r4c2, r7c2, r4c8, r7c8
        //  swordfish of 4s at r1c4, r1c7, r1c8, r6c7, r6c8, r7c4, r7c7, r7c8

        solver.setSmartest();
        assertEquals(true, solver.eliminatePossibilities());
//        assertNotEquals(-1,
//                String.valueOf(solver.getPossibilitiesContainer().getEliminationReasons(new Coord("r1c2"))).indexOf("X-Wing"));
//        assertNotEquals(-1,
//                String.valueOf(solver.getPossibilitiesContainer().getEliminationReasons(new Coord("r7c1"))).indexOf("X-Wing"));
        assertNotEquals(-1,
                String.valueOf(solver.getPossibilitiesContainer().getEliminationReasons(new Coord("r6c1"))).indexOf("Swordfish"));

        // this requires a swordfish
        assertEquals("r6c1=7", String.valueOf(solver.nextMove()) );

        // eventually, just make sure it is completely solved
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "196437852\n" +
                "274859613\n" +
                "385621974\n" +
                "463598127\n" +
                "912743586\n" +
                "758162349\n" +
                "821376495\n" +
                "647985231\n" +
                "539214768\n", String.valueOf(solver.solve()));
    }

    @Test
    public void testEasyPuzzles() {
        ISudoku[] puzzles = {PuzzleDB.NRC_17nov, PuzzleDB.NRC_5dec14,
                PuzzleDB.NRC_28dec};

        for (ISudoku p : puzzles) {
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
        ISudoku[] puzzles = {PuzzleDB.www_extremesudoku_info_evil,
                PuzzleDB.extremesudoku_10_nov_2013,
                PuzzleDB.extremesudoku_28_nov_2013};

        for (ISudoku p : puzzles) {
            solver = new SudokuSolver(p);
            solver.setSmartest();
            solver.eliminatePossibilities();
            assertNotNull("Can't solve " + p.getName(), solver.solve());
        }
    }

    @Test
    public void testExtremeEvilSuduko() {
        ISudoku[] puzzles = {PuzzleDB.www_extremesudoku_info_evil_271113};

        for (ISudoku p : puzzles) {
            solver = new SudokuSolver(p);
            ISudoku solvedPuzzle = solver.setSmartest().solve();

            assertEquals(p.getName() + ":\n" +
                    "831692547\n" +
                    "256473819\n" +
                    "947581326\n" +
                    "689247135\n" +
                    "572138964\n" +
                    "413956278\n" +
                    "365824791\n" +
                    "128769453\n" +
                    "794315682\n", String.valueOf(solvedPuzzle));
        }
    }

    @Test
    public void testDifficultyLevel()
    {
        assertEquals(1, SudokuSolver.assessDifficulty(PuzzleDB.Trouw_535));

        // requires naked pairs and intersection radiation
        assertEquals(5, SudokuSolver.assessDifficulty(PuzzleDB.extremesudoku_28_nov_2013));

        // requires XWings and even multiple iterations of elimination rounds
        assertEquals(10, SudokuSolver.assessDifficulty(PuzzleDB.extremesudoku_info_excessive_4jan2021));

        // empty or invalid puzzles
        assertEquals(-1, SudokuSolver.assessDifficulty(PuzzleDB.emptyNRCPuzzle));
        assertEquals(-1, SudokuSolver.assessDifficulty(PuzzleDB.unsolvable));

    }
}
