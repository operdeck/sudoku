package ottop.sudoku.tests;

import org.junit.Before;
import org.junit.Test;
import ottop.sudoku.*;

import static org.junit.Assert.*;

public class GroupTests {
    @Test
    public void testGroupCount()
    {
        IPuzzle emptyPuzzle = new StandardPuzzle("Anonymous",
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
