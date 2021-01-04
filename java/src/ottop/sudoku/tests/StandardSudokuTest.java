package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.board.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.StandardSudoku;
import ottop.sudoku.solve.SudokuSolver;

import java.util.Map;

import static org.junit.Assert.*;

public class StandardSudokuTest {
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
        assertFalse(p.isSolved());
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
        assertFalse(p.isSolved());
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
        assertTrue(p2.isSolved());
        assertFalse(p2.isInconsistent());
    }

    @Test
    public void checkGroups() {
        ISudoku p = PuzzleDB.Trouw_535;
        assertEquals(27, p.getGroups().length);
    }

    @Test
    public void checkDoMove() {
        ISudoku p = PuzzleDB.Trouw_535;
        SudokuSolver s = new SudokuSolver(p);

        assertFalse(p.isOccupied(new Coord("r4c4")));
        assertFalse(p.canUndo());

        assertEquals("[1@r2c5, 1@r4c6, 1@r8c8, 3@r3c7, 3@r5c2, 3@r7c3, 5@r5c8, 6@r8c1, 8@r4c9, 8@r9c1, 9@r9c2]",
                s.getUniqueValues().toString());
        assertEquals("[7@r4c4, 7@r6c9]",
                s.getNakedSingles().toString());
        assertEquals("[1@r2c5, 1@r4c6, 1@r8c8, 3@r3c7, 3@r5c2, 3@r7c3, 5@r5c8, 6@r8c1, 7@r4c4, 7@r6c9, 8@r4c9, 8@r9c1, 9@r9c2]",
                s.getPossibleMoves().toString());

        Map.Entry<Coord, String> move = s.nextMove(); // should give first lone symbol

        assertNotNull(move);
        assertEquals("r4c4=7", move.toString());


        assertEquals("7", move.getValue());
        assertEquals("r4c4", move.getKey().toString());

        ISudoku nextPuzzle = p.doMove(new Coord("r4c4"), "7");
        assertTrue(nextPuzzle.canUndo());
        assertTrue(nextPuzzle.isOccupied(new Coord("r4c4")));
    }
}
