package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.board.Coord;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solver.SudokuSolver;

import static junit.framework.TestCase.assertEquals;

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


}
