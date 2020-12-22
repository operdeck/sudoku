package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.group.SquareGroup;

import java.util.*;
import java.util.stream.Collectors;

public class StandardPuzzle implements IPuzzle {
    public static String TYPE = "Standard";

    private String name;
    private char[][] board = new char[9][9]; // TODO get rid of char, some internal code is OK

    // generic way to map characters encountered to indices and back
    // TODO see what this really is, maybe just static arrays work better
    private Map<Character, Integer> toInternalRepresentation = null;
    private List<Character> fromInternalRepresentation = null;

    // State is in these properties:
    protected IPuzzle previousPuzzle = null;
    private List<AbstractGroup> groups;
    private Set<GroupIntersection> intersections;

    public StandardPuzzle(String name, String[] sudokuRows) {
        this.name = name;
        setRepresentation(sudokuRows); // Keep board etc
        resetState();

        System.out.println("** internal state stuff **");
        System.out.println(toInternalRepresentation);
        System.out.println(fromInternalRepresentation);
        System.out.println("***");
    }

    public void resetState() {
        previousPuzzle = null;
        initGroups(); // Init groups
        initGroupIntersections();
    }

//    public void init(String[] sudokuRows) {
//        setRepresentation(sudokuRows);
//        initGroups();
//        initGroupIntersections();
//    }

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

//    public StandardPuzzle(String name) {
//        this.name = name;
//        initGroups();
//        initGroupIntersections();
//    }


    protected void setRepresentation(String[] sudokuRows) {
        if (sudokuRows.length != 9) throw new IllegalArgumentException("Initialization must have 9 rows");

        toInternalRepresentation = new HashMap<>();
        fromInternalRepresentation = new ArrayList<>();
        fromInternalRepresentation.add(null); // we'll start at index 1

        Set<Character> chars = new TreeSet<>();
        for (int y=0; y<9; y++) {
            String s = sudokuRows[y];
            if (s.length() != 9) throw new IllegalArgumentException("Initialization must have 9 chars for each row");
            for (int x = 0; x<s.length(); x++) {
                char c = s.charAt(x);
                if (charRepresentsNonEmptyCell(c)) {
                    chars.add(c);
                    this.board[y][x] = c;
                } else {
                    this.board[y][x] = ' ';
                }
            }
        }
        for (Character c : chars) {
            if (!toInternalRepresentation.containsKey(c)) {
                int idx = fromInternalRepresentation.size();
                fromInternalRepresentation.add(c);
                toInternalRepresentation.put(c, idx);
            }
        }

//		for (int i = 0; i<fromInternalRepresentation.size(); i++) {
//			System.out.println(i + ": " + fromInternalRepresentation.get(i) + "->" + toInternalRepresentation.get(fromInternalRepresentation.get(i)));
//		}
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        if (name != null && name.length() > 0) result.append(name).append(":\n");

        for (int y=0; y<9; y++) {
            result.append(String.valueOf(board[y])).append("\n");
        }

        return result.toString();
    }

    private void initGroupIntersections() {
        intersections = new LinkedHashSet<>();
        for (AbstractGroup a:groups) {
            for (AbstractGroup b:groups) {
                if (a != b) {
                    GroupIntersection overlap = new GroupIntersection(a, b);
                    if (!overlap.isEmpty()) {
                        intersections.add(overlap);
                    }
                }
            }
        }
    }

    protected void initGroups() {
        groups = new ArrayList<>();

        int cnt=0;
        for (int y=0; y<3; y++) {
            for (int x=0; x<3; x++) {
                addGroup(new SquareGroup(x*3, y*3, this, "Group "+(++cnt)));
            }
        }
        for (int i=0; i<9;i++) {
            addGroup(new RowGroup(0, i, this, "Row "+(1+i)));
            addGroup(new ColumnGroup(i, 0, this, "Column "+(1+i)));
        }
    }

    protected void addGroup(AbstractGroup g) {
        groups.add(g);
    }

    @Override
    public boolean isSolved() {
        boolean isSolved = true;
        for (AbstractGroup g:groups) {
            if (!g.solved()) isSolved = false;
        }
        return isSolved;
    }

    public int getInternalRepresentation(int y, int x) {
        return toInternalRepresentation(getValueAtCell(y,x));
    }

    public char getValueAtCell(int y, int x) {
        return board[y][x];
    }

    public boolean isOccupied(int y, int x) {
        return charRepresentsNonEmptyCell(getValueAtCell(y,x));
    }

    private boolean charRepresentsNonEmptyCell(char c) {
        return c != '.' && c != ' ';
    }

    private int toInternalRepresentation(char charAt) {
        if (!charRepresentsNonEmptyCell(charAt)) return -1;
        return toInternalRepresentation.get(charAt);
    }

    @Override
    public char toChar(int internalRepresentation) {
        if (internalRepresentation == -1) return '.';
        return fromInternalRepresentation.get(internalRepresentation);
    }

    @Override
    public int getWidth() { return 9; }

    @Override
    public int getHeight() { return 9; }

    @Override
    public IPuzzle doMove(int xNew, int yNew, char value) { // x, y start at 0
        if (isOccupied(yNew, xNew)) return null;

        char[][] newBoard = Arrays.stream(board).toArray(char[][]::new);
        newBoard[yNew][xNew] = value;

        String[] newBoardStrings = new String[9];
        //System.out.println("doMove creates new puzzle:");
        for (int y=0; y<9; y++) {
            newBoardStrings[y] = String.valueOf(newBoard[y]);
            //System.out.println(newBoardStrings[y]);
        }

        IPuzzle nextPuzzle = newInstance(this.name, newBoardStrings);

        return nextPuzzle;
    }

    public boolean canUndo() {
        return previousPuzzle != null;
    }
    public IPuzzle undoMove()
    {
        return previousPuzzle;
    }

    protected IPuzzle newInstance(String name, String[] brd) {
        StandardPuzzle p = new StandardPuzzle(name, brd);
        p.previousPuzzle = this; // link new puzzle state to current
        return p;
    }

    @Override
    public boolean isInconsistent() {
        return false;
    }

    @Override
    public AbstractGroup[] getGroups() {
        return groups.toArray(new AbstractGroup[0]);
    }

    @Override
    public GroupIntersection[] getIntersections() {
        return intersections.toArray(new GroupIntersection[0]);
    }

    @Override
    public AbstractGroup[] getSquareGroups() {
        List<AbstractGroup> squareGroups =
                groups.stream()
                        .filter(c -> c instanceof SquareGroup)
                        .collect(Collectors.toList());
        return squareGroups.toArray(new AbstractGroup[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSudokuType() { return TYPE; }
}
