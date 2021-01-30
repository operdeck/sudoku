package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.NRCSudoku;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solver.SolveStats;
import ottop.sudoku.solver.SudokuSolver;

import java.util.*;

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

    private int getTotalNumberOfCellsWithPencilMarks() {
        int count = 0;
        for (Coord c : p.getAllCells()) {
            Set<Integer> x = solver.getCandidatesAtCell(c);
            if (x != null && x.size() > 0) count += 1;
        }
        return count;
    }

    private int getTotalNumberOfPencilMarks() {
        int count = 0;
        for (Coord c : p.getAllCells()) {
            Set<Integer> x = solver.getCandidatesAtCell(c);
            if (x != null) count += x.size();
        }
        return count;
    }

    // All possible moves
    private SortedSet<String> getPossibleMoves() {
        SolveStats stats = new SolveStats();
        solver.nextMove(stats);

        SortedSet<String> moves = new TreeSet<>();

        Map<Coord, String> nakedSingles = solver.getAllNakedSingles();
        for (Map.Entry<Coord, String> nakedSingle: nakedSingles.entrySet()) {
            moves.add(nakedSingle.getValue() + "@" + nakedSingle.getKey());
        }

        Map<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValues = solver.getAllUniqueValues();
        for (Map.Entry<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValue: uniqueValues.entrySet()) {
            moves.add(uniqueValue.getValue().getKey() + "@" + uniqueValue.getKey());
        }

        return moves;
    }

    // Elimination reasons as a list of strings
    private String eliminationReasons(SudokuSolver s, Coord c) {
        List<Explanation> reasons = s.getEliminationReasons(c);
        StringBuilder sb = new StringBuilder();
        for (Explanation e : reasons) {
            sb.append(e).append("\n");
        }
        return sb.toString();
    }

    @Test
    public void testBasicElimination() {
        // TODO: this changes if we split naked groups in simple/advanced

        assertEquals(65, getTotalNumberOfCellsWithPencilMarks()); // is just empty squares
        assertEquals(311, getTotalNumberOfPencilMarks());

        assertEquals(7, getPossibleMoves().size());
    }

    @Test
    public void testSolveSimplePuzzle() {
        solver.setSimplest();
        boolean solved = solver.solve();

        assertTrue(solved);

        assertEquals("Test:\n" +
                "748165392\n" +
                "329478165\n" +
                "165329478\n" +
                "671843259\n" +
                "892751643\n" +
                "534296781\n" +
                "216984537\n" +
                "487532916\n" +
                "953617824\n", p.toString());
    }

    @Test
    public void testRepeatedSolves() {
        ISudoku p = PuzzleDB.Trouw_535.clone();
        solver = new SudokuSolver(p);
        assertTrue(solver.solve());
        assertNotNull(p);
        assertTrue(p.isSolved());

        // Doing the same thing again should work if no state is left behind in the puzzle itself
        p = PuzzleDB.Trouw_535.clone();
        solver = new SudokuSolver(p);
        assertFalse(p.isSolved());
        assertTrue(solver.solve());
        assertNotNull(p);
        assertTrue(p.isSolved());
    }

    @Test
    public void testPuzzleNeedsIntersectionRadiation() {
        solver = (new SudokuSolver(PuzzleDB.getPuzzleByName("Parool_18nov"))).setSimplest();
        assertFalse(solver.solve());

        solver.setEliminateIntersectionRadiation();
        assertTrue(solver.solve());
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
        assertEquals(0, getPossibleMoves().size());

        // This fails now... why?!
        solver.setEliminateNakedPairs();
        assertEquals(2, getPossibleMoves().size());
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

        solver = (new SudokuSolver(p)).setEliminateIntersectionRadiation().setEliminateNakedPairs();
        assertEquals(0, getPossibleMoves().size());

        solver.setEliminateXWings();
        assertEquals(3, getPossibleMoves().size());
    }

    @Test
    public void testUnsolvable() {
        solver = new SudokuSolver(PuzzleDB.unsolvable);
        assertFalse(solver.solve());
    }

    @Test
    public void testMultipleEliminationRounds() {
        ISudoku p = PuzzleDB.extremesudoku_info_excessive_4jan2021.clone();
        solver = new SudokuSolver(p);
        solver.setSmartest();

        // TODO I think state is left behind in "p"
        // System.out.println(String.valueOf(p));

        p.doMove(new Coord("r1c1"), "1");
        p.doMove(new Coord("r3c5"), "2");
        p.doMove(new Coord("r3c6"), "1");
        p.doMove(new Coord("r4c6"), "8");
        p.doMove(new Coord("r8c6"), "5");

        // skip the X-wings needed for next step
        p.doMove(new Coord("r6c1"), "7");
        p.doMove(new Coord("r8c3"), "7");

        // Now, there are
        //  a '39' naked pair at r4c3 and r9c3 but this can be found
        //  only after first finding a swordfish of 9s at r2/r5/r8 x c1/c4/c6


        // TODO: restore test

        // Assert that with successive elimination steps we reduce the possibilities more and more

//        assertEquals(true, solver.eliminatePossibilities());
//        assertEquals(132, getTotalNumberOfPencilMarks());
//        assertEquals(true, solver.eliminatePossibilities());
//        assertEquals(125, getTotalNumberOfPencilMarks());
//        assertEquals(true, solver.eliminatePossibilities());
//        assertEquals(119, getTotalNumberOfPencilMarks());
//        assertEquals(true, solver.eliminatePossibilities());
//        assertEquals(92, getTotalNumberOfPencilMarks());
//        assertEquals(true, solver.eliminatePossibilities());
//        assertEquals(47, getTotalNumberOfPencilMarks());
//        assertEquals(false, solver.eliminatePossibilities());
//        assertEquals(47, getTotalNumberOfPencilMarks());

        // this is only possible after first XWing then (extended) Naked pairs
        SolveStats stats = new SolveStats();
        assertEquals("r7c1=8", String.valueOf(solver.nextMove(stats)) );
        assertEquals("Rounds: 4", String.valueOf(stats));

        // eventually, just make sure it is completely solved
        solver.solve();
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "196437852\n" +
                "274859613\n" +
                "385621974\n" +
                "463598127\n" +
                "912743586\n" +
                "758162349\n" +
                "821376495\n" +
                "647985231\n" +
                "539214768\n", String.valueOf(p));
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
        ISudoku p = PuzzleDB.EOC_dec14;
        solver = new SudokuSolver(p);
        solver.solve();
        assertEquals("Char puzzle:\n" +
                "SHFCRPTIU\n" +
                "IPRTUSCHF\n" +
                "UCTIFHRSP\n" +
                "FRIPSUHCT\n" +
                "TUHFCISPR\n" +
                "CSPHTRFUI\n" +
                "RTSUICPFH\n" +
                "PFUSHTIRC\n" +
                "HICRPFUTS\n" , p.toString());
    }

    @Test
    public void testHarderPuzzles() {
        ISudoku[] puzzles = {PuzzleDB.www_extremesudoku_info_evil,
                PuzzleDB.extremesudoku_10_nov_2013,
                PuzzleDB.extremesudoku_28_nov_2013};

        for (ISudoku p : puzzles) {
            solver = new SudokuSolver(p);
            solver.setSmartest();

            assertNotNull("Can't solve " + p.getName(), solver.solve());
        }
    }

    @Test
    public void testExtremeEvilSuduko() {
        ISudoku[] puzzles = {PuzzleDB.www_extremesudoku_info_evil_271113};

        for (ISudoku p : puzzles) {
            solver = new SudokuSolver(p);
            solver.setSmartest().solve();

            assertEquals(p.getName() + ":\n" +
                    "831692547\n" +
                    "256473819\n" +
                    "947581326\n" +
                    "689247135\n" +
                    "572138964\n" +
                    "413956278\n" +
                    "365824791\n" +
                    "128769453\n" +
                    "794315682\n", String.valueOf(p));
        }
    }

    @Test
    public void testDifficultyLevel()
    {
        // This one can be solved with just basic elimination and naked singles
        assertEquals(1, SudokuSolver.assessDifficulty(PuzzleDB.Trouw_535));

        // Requires naked trio and also a 2nd round
        assertEquals(6, SudokuSolver.assessDifficulty(PuzzleDB.extremesudoku_28_nov_2013));

        // Requires a swordfish and multiple rounds
        // AARGH sometimes 9 sometimes 10
        System.out.println("Diff:"+SudokuSolver.assessDifficulty(PuzzleDB.extremesudoku_info_excessive_4jan2021));
        assertTrue(SudokuSolver.assessDifficulty(PuzzleDB.extremesudoku_info_excessive_4jan2021) >= 9);

        // empty or invalid puzzles
        assertEquals(-1, SudokuSolver.assessDifficulty(PuzzleDB.getPuzzleByName("Empty NRC Sudoku")));
        assertEquals(-1, SudokuSolver.assessDifficulty(PuzzleDB.unsolvable));

    }
}
