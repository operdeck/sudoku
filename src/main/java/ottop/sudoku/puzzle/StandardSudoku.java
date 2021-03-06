package ottop.sudoku.puzzle;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.ColumnGroup;
import ottop.sudoku.board.RowGroup;
import ottop.sudoku.board.SquareGroup;

import java.util.ArrayList;
import java.util.List;

public class StandardSudoku extends AbstractSudoku {

    public StandardSudoku(String name,
                          String row1, String row2, String row3,
                          String row4, String row5, String row6,
                          String row7, String row8, String row9) {
        this(name,
                new String[]{" ", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
                new String[]{row1, row2, row3, row4, row5, row6, row7, row8, row9});
    }

    // From subclasses that are 9x9 but use different symbols
    protected StandardSudoku(String name, String[] symbols, String[] sudokuRows) {
        super(name, symbols, readSingleCharBoard(sudokuRows, 9, 9, symbols));
    }

    public StandardSudoku(String puzzleName, String puzzleData) {
        this(puzzleName,
                puzzleData.substring(0, 9),
                puzzleData.substring(9, 18),
                puzzleData.substring(18, 27),
                puzzleData.substring(27, 36),
                puzzleData.substring(36, 45),
                puzzleData.substring(45, 54),
                puzzleData.substring(54, 63),
                puzzleData.substring(63, 72),
                puzzleData.substring(72, 81));
    }

    public StandardSudoku(String puzzleName) {
        this(puzzleName, " ".repeat(81));
    }

    @Override
    List<AbstractGroup> createGroups() {
        List<AbstractGroup> grps = new ArrayList<>();
        groupsWithBoundaries = new ArrayList<>();

        int cnt = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                AbstractGroup g = new SquareGroup(x * 3, y * 3, "Group " + (++cnt));
                grps.add(g);
                groupsWithBoundaries.add(g);
            }
        }
        for (int i = 0; i < 9; i++) {
            grps.add(new RowGroup(0, i, 9));
            grps.add(new ColumnGroup(i, 0,  9));
        }

        return grps;
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
    }



}
