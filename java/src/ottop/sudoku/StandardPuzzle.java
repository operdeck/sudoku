package ottop.sudoku;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.group.SquareGroup;

import java.util.*;

// TODO create abstract class out of this
public class StandardPuzzle implements IPuzzle {
    public static String TYPE = "Standard";

    private final String name;

    // Below is specific to 9x9 puzzle
    private int[][] board = new int[9][9]; //[x][y]
    private final List<String> possibleSymbols =
            Arrays.asList(" ","1","2","3","4","5","6","7","8","9");

    // State is in these properties:
    protected IPuzzle previousPuzzle = null;
    protected List<AbstractGroup> groups;

    protected StandardPuzzle(String name, int[][] board) {
        this.name = name;
        this.board = board;
        resetState();
    }

    public StandardPuzzle(String name, String[] sudokuRows) {
        this.name = name;
        readBoard(sudokuRows); // Keep board etc
        resetState();
    }

    public void resetState() {
        initGroups(); // Init groups
    }

    public StandardPuzzle(String[] sudokuRows) {
        this("", sudokuRows);
    }

    public StandardPuzzle(String row1, String row2, String row3,
                          String row4, String row5, String row6,
                          String row7, String row8, String row9) {
        this("", new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9});
    }

    public StandardPuzzle(String name,
                          String row1, String row2, String row3,
                          String row4, String row5, String row6,
                          String row7, String row8, String row9) {
        this(name, new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9});
    }

    // below is specific to 9x9 boards or at least single character representations
    protected void readBoard(String[] sudokuRows) {
        if (sudokuRows.length != 9) throw new IllegalArgumentException("Initialization must have 9 rows");

        Set<Character> chars = new TreeSet<>();
        for (int y=0; y<9; y++) {
            String s = sudokuRows[y];
            if (s.length() != 9) throw new IllegalArgumentException("Initialization must have 9 chars for each row");
            for (int x = 0; x<s.length(); x++) {
                String symbol = s.substring(x, x+1);
                this.board[x][y] = cellSymbolToIndex(symbol);
            }
        }
    }

    @Override
    // seems specific to 9x9 boards or at least single symbol boards
    public String toString() {
        StringBuffer result = new StringBuffer();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y=0; y<9; y++) {
            for (int x=0; x<9; x++) {
                result.append(possibleSymbols.get(board[x][y]));
            }
            result.append("\n");
        }

        return result.toString();
    }

    protected void initGroups() {
        groups = new ArrayList<>();

        int cnt=0;
        for (int y=0; y<3; y++) {
            for (int x=0; x<3; x++) {
                groups.add(new SquareGroup(x*3, y*3, this, "Group "+(++cnt)));
            }
        }
        for (int i=0; i<9;i++) {
            groups.add(new RowGroup(0, i, this, "Row "+(1+i)));
            groups.add(new ColumnGroup(i, 0, this, "Column "+(1+i)));
        }
    }

    @Override
    public boolean isSolved() {
        boolean isSolved = true;
        for (AbstractGroup g:groups) {
            if (!g.solved()) isSolved = false;
        }
        return isSolved;
    }

    @Override
    public int getSymbolCodeAtCoordinates(Coord coord) {
        return board[coord.getX()][coord.getY()];
    }

    @Override
    public String symbolCodeToSymbol(int i) {
        return possibleSymbols.get(i);
    }

    protected int cellSymbolToIndex(String symbol) {
        int idx = Math.max(0, possibleSymbols.indexOf(symbol)); // return 0 if not found
        return idx;
    }

    @Override
    public String getSymbolAtCoordinates(Coord coord) {
        return possibleSymbols.get(board[coord.getX()][coord.getY()]);
    }

    @Override
    public boolean isOccupied(Coord coord) {
        return board[coord.getX()][coord.getY()] != 0;
    }

    @Override
    public int getWidth() { return 9; }

    @Override
    public int getHeight() { return 9; }

    @Override
    public IPuzzle doMove(Coord coord, String symbol) { // x, y start at 0
        //if (isOccupied(yNew, xNew)) return null;
        int[][] newBoard = new int[9][9];
        for (int x=0; x<getWidth(); x++) {
            for (int y=0; y<getHeight(); y++) {
                if (x==coord.getX() && y==coord.getY()) {
                    newBoard[x][y] = cellSymbolToIndex(symbol);
                } else {
                    newBoard[x][y] = board[x][y];
                }
            }
        }

        IPuzzle nextPuzzle = newInstance(this.name, newBoard);

        return nextPuzzle;
    }

    public boolean canUndo() {
        return previousPuzzle != null;
    }
    public IPuzzle undoMove()
    {
        return previousPuzzle;
    }

    protected IPuzzle newInstance(String name, int[][] brd) {
        StandardPuzzle p = new StandardPuzzle(name, brd);
        p.previousPuzzle = this; // link new puzzle state to current

        return p;
    }

    @Override
    public boolean isInconsistent() {
        boolean isInconsistent = false;
        for (AbstractGroup g:groups) {
            if (g.isInconsistent()) {
                isInconsistent = true;
                break;
            }
        }
        return isInconsistent;
    }

    @Override
    public AbstractGroup[] getGroups() {
        return groups.toArray(new AbstractGroup[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSudokuType() { return TYPE; }

    @Override
    public Paint getCellBackground(int x, int y) {
        return Color.WHITE;
    }
}
