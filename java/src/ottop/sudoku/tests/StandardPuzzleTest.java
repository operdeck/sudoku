package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.SudokuSolver;
import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.Standard9x9Puzzle;

import java.util.Map;

import static org.junit.Assert.*;

public class StandardPuzzleTest {
    @Test
    public void checkBoard() {
        Standard9x9Puzzle p = new Standard9x9Puzzle("Test",
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
        Standard9x9Puzzle p = new Standard9x9Puzzle("Bad puzzle",
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

        Standard9x9Puzzle p2 = new Standard9x9Puzzle("Solved puzzle",
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
        IPuzzle p = PuzzleDB.Trouw_535;
        assertEquals(27, p.getGroups().length);
    }

    @Test
    public void checkDoMove() {
        IPuzzle p = PuzzleDB.Trouw_535;
        SudokuSolver s = new SudokuSolver(p);

        assertFalse(p.isOccupied(new Coord("r4c9")));
        assertFalse(p.canUndo());

        Map.Entry<Coord, String> move = s.nextMove();
        assertNotNull(move);

        assertEquals("8", move.getValue());
        assertEquals("r4c9", move.getKey().toString());

        IPuzzle nextPuzzle = p.doMove(new Coord("r4c9"), "8");
        assertTrue(nextPuzzle.canUndo());
        assertTrue(nextPuzzle.isOccupied(new Coord("r4c9")));
    }
}
