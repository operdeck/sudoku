package ottop.sudoku.puzzle;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.board.MultiCellGroup;
import ottop.sudoku.board.SquareGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SDoku extends StandardSudoku {
    static AbstractGroup[] pattern = {
            new MultiCellGroup(new String[]{"r1c3", "r1c4", "r1c5", "r1c6", "r1c7",
                    "r2c2", "r2c8", "r2c9", "r3c1"}, "Snake top"),
            new MultiCellGroup(new String[]{"r4c1",
                    "r5c2", "r5c3", "r5c4", "r5c5", "r5c6", "r5c7", "r5c8",
                    "r6c9"}, "Snake mid"),
            new MultiCellGroup(new String[]{"r7c9", "r8c1", "r8c2", "r8c8",
                    "r9c3", "r9c4", "r9c5", "r9c6", "r9c7"}, "Snake bot")
    };

    public SDoku(String name,
                 String row1, String row2, String row3,
                 String row4, String row5, String row6,
                 String row7, String row8, String row9) {
        super(name, row1, row2, row3, row4, row5, row6, row7, row8, row9);
    }

    @Override
    public List<AbstractGroup> createGroups() {
        List<AbstractGroup> grps = super.createGroups();
        Arrays.stream(pattern).forEach((g) -> grps.add(g));
        return grps;
    }

    @Override
    public boolean isAtOverlay(Coord c) {
        return Arrays.stream(pattern).anyMatch((g) -> g.isInGroup(c));
    }

}
