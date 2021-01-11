package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.board.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solver.SolveStats;
import ottop.sudoku.solver.SudokuSolver;

import java.util.Map;

import static org.junit.Assert.*;

public class StandardSudokuTest {
    @Test
    public void testCounts() {
        ISudoku emptyPuzzle = new StandardSudoku("Anonymous",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");

        assertEquals(27, emptyPuzzle.getGroups().size());
        assertEquals(3, emptyPuzzle.getBuddyGroups(new Coord("r2c2")).size());
        assertEquals(20, emptyPuzzle.getBuddies(new Coord("r2c2")).size());
    }

    @Test
    public void checkBoard() {
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
        assertFalse(p.isComplete());
        assertEquals(9, p.getHeight());
        assertEquals(9, p.getWidth());
        assertFalse(p.canUndo());

        assertEquals("4", p.symbolCodeToSymbol(4));

        assertTrue(p.isOccupied(new Coord("r6c2")));
        assertTrue(p.isOccupied(new Coord("r1c6")));
        assertTrue(p.isOccupied(new Coord("r3c9")));
        assertEquals("1", p.getSymbolAtCoordinates(new Coord("r3c1")));
        assertFalse(p.isOccupied(new Coord("r9c9")));
        assertEquals(" ", p.getSymbolAtCoordinates(new Coord("r9c9")));
    }

    @Test
    public void checkState() {
        StandardSudoku p = new StandardSudoku("Bad puzzle",
                "123456789",
                "123456789",
                "123456789",
                "123456789",
                "123456789",
                "123456789",
                "123456789",
                "123456789",
                "123456789");
        assertTrue(p.isComplete());
        assertTrue(p.isInconsistent());

        StandardSudoku p2 = new StandardSudoku("Solved puzzle",
                "827154396",
                "965327148",
                "341689752",
                "593468271",
                "472513689",
                "618972435",
                "786235914",
                "154796823",
                "239841567");
        assertTrue(p2.isComplete());
        assertFalse(p2.isInconsistent());
    }

    @Test
    public void checkGroups() {
        ISudoku p = PuzzleDB.Trouw_535;
        assertEquals(27, p.getGroups().size());
    }

    @Test
    public void checkDoMove() {
        ISudoku p = PuzzleDB.Trouw_535;
        SudokuSolver s = new SudokuSolver(p);

        assertFalse(p.isOccupied(new Coord("r4c4")));
        assertFalse(p.canUndo());

        assertEquals("[r2c5, r3c7, r4c6, r4c9, r5c2, r5c8, r7c3, r8c1, r8c8, r9c1, r9c2]",
                String.valueOf(s.getAllUniqueValues().keySet()));
        assertEquals("[r4c4, r6c9]",
                String.valueOf(s.getAllNakedSingles().keySet()));

        Map.Entry<Coord, String> move = s.nextMove(new SolveStats()); // should give first lone symbol

        assertNotNull(move);
        assertEquals("r4c4=7", move.toString());


        assertEquals("7", move.getValue());
        assertEquals("r4c4", move.getKey().toString());

        p.doMove(new Coord("r4c4"), "7");
        assertTrue(p.canUndo());
        assertTrue(p.isOccupied(new Coord("r4c4")));
    }
}
