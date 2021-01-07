package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.StandardSudoku;

import static org.junit.Assert.assertEquals;

public class BoardTests {
    @Test
    public void testGroupCount() {
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
    }
}
