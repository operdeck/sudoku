package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.Standard9x9Puzzle;

import static org.junit.Assert.assertEquals;

public class GroupTests {
    @Test
    public void testGroupCount()
    {
        IPuzzle emptyPuzzle = new Standard9x9Puzzle("Anonymous",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");

        assertEquals(27, emptyPuzzle.getGroups().length );
    }
}
