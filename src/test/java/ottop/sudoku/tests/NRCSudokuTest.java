package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.board.Coord;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.NRCSudoku;

import static org.junit.Assert.assertEquals;

public class NRCSudokuTest {

    @Test
    public void testNRCCounts() {
        ISudoku emptyPuzzle = new NRCSudoku("Anonymous",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");

        assertEquals(31, emptyPuzzle.getGroups().length);
        assertEquals(3, emptyPuzzle.getBuddyGroups(new Coord("r1c1")).length);
        assertEquals(20, emptyPuzzle.getBuddies(new Coord("r1c1")).size());
        assertEquals(4, emptyPuzzle.getBuddyGroups(new Coord("r2c2")).length);
        assertEquals(23, emptyPuzzle.getBuddies(new Coord("r2c2")).size());
    }
}
