package ottop.sudoku.puzzle;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.board.SquareGroup;

// The Four-Square Sudoku is a standard puzzle with four extra shaded 3x3 squares on
// the Sudoku board, for which each shaded region must also contain each digit from
// 1 to 9 exactly once.
// The Four-Square Sudoku is also known as Hyper-Sudoku or NRC-Sudoku.

public class NRCSudoku extends StandardSudoku {

    public NRCSudoku(String name,
                     String row1, String row2, String row3,
                     String row4, String row5, String row6,
                     String row7, String row8, String row9) {
        super(name, row1, row2, row3, row4, row5, row6, row7, row8, row9);
    }

    @Override
    public void initGroups() {
        super.initGroups();

        // add special 'NRC' groups
        int cnt = 0;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                AbstractGroup newGrp = new SquareGroup(x * 4 + 1, y * 4 + 1, this, "NRC Group " + (++cnt));
                groups.add(newGrp);
            }
        }
    }

    @Override
    public boolean isAtOverlay(Coord c) {
        int x = c.getX();
        int y = c.getY();
        return (((x >= 1 && x <= 3) || (x >= 5 && x <= 7)) && ((y >= 1 && y <= 3) || (y >= 5 && y <= 7)));
    }

}
