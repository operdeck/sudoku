package ottop.sudoku.tests;

import org.junit.Assert;
import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solver.SolveStats;
import ottop.sudoku.solver.SudokuSolver;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class EliminatorTest {
    @Test
    public void testBasicElimination() {
        StandardSudoku p = new StandardSudoku("Test",
                "....65...",
                ".......6.",
                "1......78",
                ".........",
                "..27.....",
                ".3..9...1",
                "..6..45..",
                ".8...2...",
                ".........");

        SudokuSolver s = new SudokuSolver(p);

        // Basic elimination will be done right away

        StringBuilder sb = new StringBuilder();
        for (Coord c: p.getAllCells()) {
            sb.append(c).append(s.getCandidatesAtCell(c)).append("\n");
        }
        assertEquals("r1c1[2, 3, 4, 7, 8, 9]\n" +
                "r2c1[2, 3, 4, 5, 7, 8, 9]\n" +
                "r3c1[]\n" +
                "r4c1[4, 5, 6, 7, 8, 9]\n" +
                "r5c1[4, 5, 6, 8, 9]\n" +
                "r6c1[4, 5, 6, 7, 8]\n" +
                "r7c1[2, 3, 7, 9]\n" +
                "r8c1[3, 4, 5, 7, 9]\n" +
                "r9c1[2, 3, 4, 5, 7, 9]\n" +
                "r1c2[2, 4, 7, 9]\n" +
                "r2c2[2, 4, 5, 7, 9]\n" +
                "r3c2[2, 4, 5, 6, 9]\n" +
                "r4c2[1, 4, 5, 6, 7, 9]\n" +
                "r5c2[1, 4, 5, 6, 9]\n" +
                "r6c2[]\n" +
                "r7c2[1, 2, 7, 9]\n" +
                "r8c2[]\n" +
                "r9c2[1, 2, 4, 5, 7, 9]\n" +
                "r1c3[3, 4, 7, 8, 9]\n" +
                "r2c3[3, 4, 5, 7, 8, 9]\n" +
                "r3c3[3, 4, 5, 9]\n" +
                "r4c3[1, 4, 5, 7, 8, 9]\n" +
                "r5c3[]\n" +
                "r6c3[4, 5, 7, 8]\n" +
                "r7c3[]\n" +
                "r8c3[1, 3, 4, 5, 7, 9]\n" +
                "r9c3[1, 3, 4, 5, 7, 9]\n" +
                "r1c4[1, 2, 3, 4, 8, 9]\n" +
                "r2c4[1, 2, 3, 4, 8, 9]\n" +
                "r3c4[2, 3, 4, 9]\n" +
                "r4c4[1, 2, 3, 4, 5, 6, 8]\n" +
                "r5c4[]\n" +
                "r6c4[2, 4, 5, 6, 8]\n" +
                "r7c4[1, 3, 8, 9]\n" +
                "r8c4[1, 3, 5, 6, 9]\n" +
                "r9c4[1, 3, 5, 6, 8, 9]\n" +
                "r1c5[]\n" +
                "r2c5[1, 2, 3, 4, 7, 8]\n" +
                "r3c5[2, 3, 4]\n" +
                "r4c5[1, 2, 3, 4, 5, 8]\n" +
                "r5c5[1, 3, 4, 5, 8]\n" +
                "r6c5[]\n" +
                "r7c5[1, 3, 7, 8]\n" +
                "r8c5[1, 3, 5, 7]\n" +
                "r9c5[1, 3, 5, 7, 8]\n" +
                "r1c6[]\n" +
                "r2c6[1, 3, 7, 8, 9]\n" +
                "r3c6[3, 9]\n" +
                "r4c6[1, 3, 6, 8]\n" +
                "r5c6[1, 3, 6, 8]\n" +
                "r6c6[6, 8]\n" +
                "r7c6[]\n" +
                "r8c6[]\n" +
                "r9c6[1, 3, 6, 7, 8, 9]\n" +
                "r1c7[1, 2, 3, 4, 9]\n" +
                "r2c7[1, 2, 3, 4, 9]\n" +
                "r3c7[2, 3, 4, 9]\n" +
                "r4c7[2, 3, 4, 6, 7, 8, 9]\n" +
                "r5c7[3, 4, 6, 8, 9]\n" +
                "r6c7[2, 4, 6, 7, 8]\n" +
                "r7c7[]\n" +
                "r8c7[1, 3, 4, 6, 7, 9]\n" +
                "r9c7[1, 2, 3, 4, 6, 7, 8, 9]\n" +
                "r1c8[1, 2, 3, 4, 9]\n" +
                "r2c8[]\n" +
                "r3c8[]\n" +
                "r4c8[2, 3, 4, 5, 8, 9]\n" +
                "r5c8[3, 4, 5, 8, 9]\n" +
                "r6c8[2, 4, 5, 8]\n" +
                "r7c8[1, 2, 3, 8, 9]\n" +
                "r8c8[1, 3, 4, 9]\n" +
                "r9c8[1, 2, 3, 4, 8, 9]\n" +
                "r1c9[2, 3, 4, 9]\n" +
                "r2c9[2, 3, 4, 5, 9]\n" +
                "r3c9[]\n" +
                "r4c9[2, 3, 4, 5, 6, 7, 9]\n" +
                "r5c9[3, 4, 5, 6, 9]\n" +
                "r6c9[]\n" +
                "r7c9[2, 3, 7, 9]\n" +
                "r8c9[3, 4, 6, 7, 9]\n" +
                "r9c9[2, 3, 4, 6, 7, 9]\n", sb.toString());
    }

    @Test
    public void testIntersectionRadiationElimination() {
        StandardSudoku p = new StandardSudoku("https://www.sudokuessentials.com/sudoku-hints.html",
                "  6  35  ",
                "1  7  3 6",
                "8 7 5  24",
                "4  6 79 2",
                "   1 9   ",
                "6 85 2  7",
                "36  7 2 1",
                "7 4  1  5",
                "  12  7  ");

        SudokuSolver s = new SudokuSolver(p);
        s.setEliminateIntersectionRadiation();

        s.setEarlyStop(false); // forces further elimination even if a move is found already

        // "3" should be eliminated because of col 5 x square 5
        assertTrue(String.valueOf(s.getEliminationReasons(new Coord("r8c5"))).
                indexOf("Removed 3 because it has to be in the intersection of Column 5 with Group 5 (Intersection Radiation)") != -1);
        assertEquals("[6, 9]", String.valueOf(s.getCandidatesAtCell(new Coord("r8c5"))));
    }

    @Test
    public void testSimpleNakedPairElimination() {
        StandardSudoku p = new StandardSudoku("https://www.sudokuessentials.com/sudoku-hints.html",
                "  6  35  ",
                "1  7  3 6",
                "8 7 5  24",
                "4  6 79 2",
                "   1 9   ",
                "6 85 2  7",
                "36  7 2 1",
                "7 4  1  5",
                "  12  7  ");

        SudokuSolver s = new SudokuSolver(p);
        s.setEliminateNakedPairs();

        s.setEarlyStop(false); // forces further elimination even if a move is found already

//        System.out.println(String.valueOf(s.getEliminationReasons(new Coord("r3c2"))));

        // Simple naked pair

        assertTrue(String.valueOf(s.getEliminationReasons(new Coord("r9c2"))).
                indexOf("Removed [5, 9] in Group 7 because [5, 9] have to be in [r9c1, r7c3] (Simple Naked Pair)") != -1);
        assertEquals("[8]", String.valueOf(s.getCandidatesAtCell(new Coord("r9c2"))));

        // Extended naked trio

        assertTrue(String.valueOf(s.getEliminationReasons(new Coord("r8c4"))).
                indexOf("Removed [8, 9] in Column 4 because [4, 8, 9] have to be in [r1c4, r3c4, r7c4] (Extended Naked Trio)") != -1);
        assertEquals("[3]", String.valueOf(s.getCandidatesAtCell(new Coord("r8c4"))));

        // Extended naked quad

        assertTrue(String.valueOf(s.getEliminationReasons(new Coord("r3c2"))).
                indexOf("Removed 9 in Group 1 because [2, 4, 5, 9] have to be in [r1c1, r1c2, r2c2, r2c3] (Extended Naked Quad)") != -1);
        assertEquals("[3]", String.valueOf(s.getCandidatesAtCell(new Coord("r3c2"))));

    }

    @Test
    public void testSwordfish() {
        ISudoku p = new StandardSudoku("Extreme Sudoku Excessive 4/1/21",
                "..6..7..2",
                ".7..5..1.",
                "3..6..9..",
                "4..5..1..",
                ".1..4..8.",
                "..8..2..9",
                "..1..6..5",
                ".4..8..3.",
                "5..2..7..");
        SudokuSolver solver = new SudokuSolver(p);
        solver.setSmartest().setEarlyStop(false);

        //System.out.println(String.valueOf(p));

        p.doMove(new Coord("r1c1"), "1");
        p.doMove(new Coord("r3c5"), "2");
        p.doMove(new Coord("r3c6"), "1");
        p.doMove(new Coord("r4c6"), "8");
        p.doMove(new Coord("r8c6"), "5");

        // Now, there are
        //  x-wing of 8s at r3c2, r3c9, r9c2, r9c9
        //  x-wing of 2s at r4c2, r7c2, r4c8, r7c8
        //  swordfish of 4s at r1c4, r1c7, r1c8, r6c7, r6c8, r7c4, r7c7, r7c8


        // Verify there are X-Wings (or higher dimensions) in the solutions

//        assertEquals("pipo",
//                String.valueOf(eliminationReasons(solver, new Coord("r1c2"))));
//
//        System.out.println(eliminationReasons(solver, new Coord("r1c2"))+"\n");
//        System.out.println(eliminationReasons(solver, new Coord("r7c2"))+"\n");
//        System.out.println(eliminationReasons(solver, new Coord("r6c1"))+"\n");

        // Sometimes X-Wing, sometimes Swordfish

//        assertNotEquals(-1,
//                eliminationReasons(solver, new Coord("r1c2")).indexOf("Swordfish"));
//        assertNotEquals(-1,
//                eliminationReasons(solver, new Coord("r7c2")).indexOf("Swordfish"));
        assertNotEquals(-1,
                String.valueOf(solver.getEliminationReasons(new Coord("r6c1"))).indexOf("Swordfish"));

        // Solving requires a swordfish
        SolveStats stats = new SolveStats();
        Assert.assertEquals("r6c1=7", String.valueOf(solver.nextMove(stats)) );

        // But only requires one iteration
        Assert.assertEquals("Rounds: 1", String.valueOf(stats));

        // eventually, just make sure it is completely solved
        solver.solve();
        Assert.assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
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
    public void testForcingChains() {
        ISudoku p = PuzzleDB.getPuzzleByName("Forcing chains example");

        // Cannot be solved w/o forcing chains

        SudokuSolver sv = new SudokuSolver(p);
        sv.setEliminateNakedPairs().setEliminateIntersectionRadiation().setEliminateXWings();
        Map.Entry<Coord, String> move = sv.nextMove(new SolveStats());

        assertNull(move);

        // Enable forcing chains

        sv.setForcingChains();
        move = sv.nextMove(new SolveStats());

        assertNotNull(move);
        assertEquals("r1c2=7", String.valueOf(move));
    }
}
