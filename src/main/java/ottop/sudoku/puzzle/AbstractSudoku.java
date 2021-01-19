package ottop.sudoku.puzzle;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.solver.Updateable;

import java.util.*;

public abstract class AbstractSudoku implements ISudoku {
    final String name;
    final List<String> possibleSymbols; // [0] = symbol for empty cell, 1..N are for the real symbols
    Coord[] allCells;

    // TODO: consider making this String. Drop symbolCode all over the place. Drop stateful groups.
    int[][] board; // [x][y] to symbolCode
    Map<Coord, AbstractGroup[]> buddyGroups;

    // Groups also keep state of which cells in the group are occupied

    AbstractGroup[] groups = null;
    List<AbstractGroup> groupsWithBoundaries = null;

    Updateable solver = null;
    List<Map.Entry<Coord, String>> undoStack = new ArrayList<>();
    int undoStackPointer = -1;

    public AbstractSudoku(String name, String[] symbols, int[][] board) {
        this.name = name;
        this.possibleSymbols = Arrays.asList(symbols);
        this.board = board; // new int[getWidth()][getHeight()];

        // Static list of all coordinates in the board
        List<Coord> cells = new ArrayList<>();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                cells.add(new Coord(x, y));
            }
        }
        this.allCells = cells.toArray(new Coord[0]);

        // Groups of cells - different for different Sudoku types
        this.groups = createGroups().toArray(new AbstractGroup[0]);

        // Buddy groups are the groups a cell is part of
        buddyGroups = new HashMap<>();
        for (Coord c: allCells) {
            List<AbstractGroup> grps = new ArrayList<>();
            for (AbstractGroup g : groups) {
                if (g.isInGroup(c)) {
                    grps.add(g);
                }
            }
            buddyGroups.put(c, grps.toArray(new AbstractGroup[0]));
        }
    }

    abstract List<AbstractGroup> createGroups();

    @Override
    public ISudoku clone() {
        AbstractSudoku c = null;
        try {
            c = (AbstractSudoku) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        c.allCells = this.allCells.clone();

        // Deep copy of board
        c.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            c.board[i] = Arrays.copyOf(board[i], board[i].length);
        }

        // Reset state

        for (AbstractGroup g: c.groups) {
            g.resetGroup(c);
        }

        c.undoStack.clear();
        c.undoStackPointer = -1;

        return c;
    }

    @Override
    public void setSolver(Updateable s) {
        solver = s;

        // Undo moves

        while (undoStackPointer >= 0) {
            Map.Entry<Coord, String> move = undoStack.get(undoStackPointer);
            board[move.getKey().getX()][move.getKey().getY()] = 0;
            undoStackPointer--;
        }
        undoStack.clear();

        // Reset state

        for (AbstractGroup g: groups) {
            g.resetGroup(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Coord c = new Coord(x, y);
                if (isOccupied(c)) {
                    result.append(getSymbolAtCoordinates(c));
                } else {
                    result.append(".");
                }
            }
            result.append("\n");
        }

/*
        result.append("Possible symbols: ").append(possibleSymbols).append("\n");
        result.append("All cells: ").append(allCells).append("\n");
        result.append("All groups: ").append(groups).append("\n");
        result.append("Groups with boundaries: ").append(groupsWithBoundaries).append("\n");
        result.append("Solver: ").append(solver).append("\n");
        result.append("Undo stack: (").append(undoStackPointer).append(") ").append(undoStack).append("\n");
*/

        return result.toString();
    }

    @Override
    public boolean isComplete() {
        for (int x=0; x<getWidth(); x++) {
            for (int y=0; y<getHeight(); y++) {
                if (board[x][y] == 0) return false;
            }
        }
        return true;
    }

    @Override
    public boolean doMove(Coord coord, String symbol) { // x, y start at 0

        board[coord.getX()][coord.getY()] = symbolToSymbolCode(symbol);
        for (AbstractGroup g: getBuddyGroups(coord)) {
            g.resetGroup(this);
        }

        // Put on undo stack, remove any entries after (because of undo/redo)

        undoStackPointer++;
        undoStack.add(undoStackPointer, new AbstractMap.SimpleEntry<>(coord, symbol));
        while (canRedo()) {
            undoStack.remove(undoStack.size()-1);
        }

        // Update all candidates
        solver.update();

        return true;
    }

    @Override
    public boolean canUndo() {
        return undoStackPointer >= 0;
    }

    @Override
    public boolean canRedo() {
        return (undoStack.size()-1) > undoStackPointer;
    }

    @Override
    public Map.Entry<Coord, String> undoMove() {
        if (canUndo()) {
            Map.Entry<Coord, String> lastMove = undoStack.get(undoStackPointer);
            Coord coord = lastMove.getKey();

            board[coord.getX()][coord.getY()] = 0;
            for (AbstractGroup g: getBuddyGroups(coord)) {
                g.resetGroup(this);
            }

            undoStackPointer--;

            // Update all candidates
            solver.update();

            return lastMove;
        }
        return null;
    }

    @Override
    public Map.Entry<Coord, String> redoMove() {
        if (canRedo()) {
            undoStackPointer++;

            Map.Entry<Coord, String> move = undoStack.get(undoStackPointer);
            Coord coord = move.getKey();

            int symbolCode = symbolToSymbolCode(move.getValue());

            board[coord.getX()][coord.getY()] = symbolCode;
            for (AbstractGroup g: getBuddyGroups(coord)) {
                g.resetGroup(this);
            }

            // Update all candidates
            solver.update();

            return move;
        }
        return null;
    }

    @Override
    public boolean isInconsistent() {
        boolean isInconsistent = false;
        for (AbstractGroup g : groups) {
            if (g.isInconsistent()) {
                isInconsistent = true;
                break;
            }
        }
        return isInconsistent;
    }

    @Override
    public AbstractGroup[] getGroups() {
        return groups;
    }

    @Override
    public AbstractGroup[] getBuddyGroups(Coord c) {
        return buddyGroups.get(c);
    }

    @Override
    public List<AbstractGroup> getGroupsWithVisualBoundary()
    {
        return groupsWithBoundaries;
    }

    @Override
    public Coord[] getAllCells() {
        return allCells;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSymbolCodeRange() {
        return possibleSymbols.size();
    }

    @Override
    public int getSymbolCodeAtCoordinates(Coord coord) {
        return board[coord.getX()][coord.getY()];
    }

    @Override
    public String getSymbolAtCoordinates(Coord coord) {
        return symbolCodeToSymbol(board[coord.getX()][coord.getY()]);
    }

    @Override
    public boolean isOccupied(Coord coord) {
        return board[coord.getX()][coord.getY()] != 0;
    }

    @Override
    public String symbolCodeToSymbol(int i) {
        return possibleSymbols.get(i);
    }

    @Override
    public int symbolToSymbolCode(String symbol) {
        return Math.max(0, possibleSymbols.indexOf(symbol));
    }

    @Override
    public boolean isAtOverlay(Coord c) {
        return false;
    }

    @Override
    public Set<Coord> getBuddies(Coord coord) {
        Set<Coord> buddies = new TreeSet<>();

        if (coord != null) {
            for (AbstractGroup g : groups) {
                if (g.getCoords().contains(coord)) {
                    buddies.addAll(g.getCoords());
                }
            }
            buddies.remove(coord); // you are not your own buddy
        }

        return buddies;
    }

    static int[][] readCommaSeparatedBoard(String[] sudokuRows, int width, int height, String[] symbols) {
        if (sudokuRows.length != height)
            throw new IllegalArgumentException("Initialization must have " + height + " rows");

        int[][] brd = new int[width][height];

        for (int y = 0; y < height; y++) {
            String s = sudokuRows[y];
            String[] aRow = s.split(",");
            if (aRow.length != width)
                throw new IllegalArgumentException("Initialization must have " + width + " chars for each row");
            for (int x = 0; x < width; x++) {
                String symbol = aRow[x];
                brd[x][y] = Math.max(0, Arrays.asList(symbols).indexOf(symbol));
            }
        }

        return brd;
    }

    static int[][] readSingleCharBoard(String[] sudokuRows, int width, int height, String[] symbols) {
        if (sudokuRows.length != height)
            throw new IllegalArgumentException("Initialization must have " + height + " rows");

        int[][] brd = new int[width][height];

        for (int y = 0; y < height; y++) {
            String s = sudokuRows[y];
            if (s.length() != width)
                throw new IllegalArgumentException("Initialization must have " + width + " chars for each row");
            for (int x = 0; x < s.length(); x++) {
                String symbol = s.substring(x, x + 1);
                brd[x][y] = Math.max(0, Arrays.asList(symbols).indexOf(symbol));
            }
        }

        return brd;
    }
}
