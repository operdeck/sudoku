package ottop.sudoku.fx;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import ottop.sudoku.board.Coord;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.solver.SolveStats;
import ottop.sudoku.solver.SudokuSolver;
import ottop.sudoku.explain.Explanation;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.SingleCellGroup;
import ottop.sudoku.puzzle.ISudoku;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Controller {
    public static Controller theController = null;

    public Canvas gameCanvas;
    public ButtonBar digitButtonBar;
    public Button undoButton;
    public CheckBox cbNakedPairs;
    public CheckBox cbXWings;
    public CheckBox cbRadiation;
    public Label tbLevel;
    public CheckBox cbPencilMarks;
    public Label labelPosition;
    public ChoiceBox<String> cbPuzzleDB;
    public Button nextMoveButton;
    public ListView<String> lvEliminationSteps;
    public Label lbNotes;
    public Button hintButton;
    public Button redoButton;

    // Both set when puzzle dropdown changes
    private ISudoku myPuzzle = null;
    private SudokuSolver currentSolver = null;

    // Set by manual click or when doing automatic move
    private Coord currentHighlightedCell = null;

    // Set by selecting a row in the reasons dialog
    private Explanation currentEliminationReason = null;

//    private List<AbstractGroup> currentHighlightedGroups = null;
//    private Set<Coord> currentHighlightedSubArea = null;

    public Controller() {
        theController = this;
    }

    public void initialize() {
        ChangeListener<String> listener = (observableValue, oldValue, newValue) -> {
            int selectionIdx = lvEliminationSteps.getSelectionModel().getSelectedIndex();
            List<Explanation> reasons = currentSolver.getEliminationReasons(currentHighlightedCell);

            if (reasons != null && selectionIdx >= 0 && selectionIdx<reasons.size()) {
                currentEliminationReason = reasons.get(selectionIdx);
//                currentHighlightedGroups = reason.getHighlightGroups();
//                currentHighlightedSubArea = reason.getHighlightSubArea();

                redrawBoard();
            }
        };

        lvEliminationSteps.getSelectionModel().selectedItemProperty().removeListener(listener);
        lvEliminationSteps.getSelectionModel().selectedItemProperty().addListener(listener);

        // List of puzzles
        try {
            String[] puzzles = PuzzleDB.getPuzzles();
            cbPuzzleDB.getItems().setAll(puzzles);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void symbolClicked(ActionEvent actionEvent) {
        String clickedSymbol = ((Button) actionEvent.getSource()).getText();

        myPuzzle.doMove(currentHighlightedCell, clickedSymbol);


    }

    // TODO: support arrow keys move around in canvas
    // TODO: also support pressing keys 1-9

    public void newPuzzle(ISudoku initialPuzzle) {

        // puzzle & solver should only be set here
        myPuzzle = initialPuzzle;

        currentSolver = (new SudokuSolver(myPuzzle))
                .setEliminateIntersectionRadiation(cbRadiation.isSelected())
                .setEliminateNakedPairs(cbNakedPairs.isSelected())
                .setEliminateXWings(cbXWings.isSelected());

        // Puzzle level
//        System.out.println("Assessing difficulty of: " + String.valueOf(myPuzzle));
//        System.out.println("Before, complete=" + myPuzzle.isComplete());
//        System.out.println("Before, solver=" + currentSolver);
//         int level = SudokuSolver.assessDifficulty(myPuzzle);
//         tbLevel.setText("Level: " + level);
//        System.out.println("After, complete=" + myPuzzle.isComplete());
//        System.out.println("After, solver=" + currentSolver);

        // Puzzle canvas and controls
        redrawWholeDisplay();
    }

    // Redraws board plus pencil marks and highlights
    private void redrawBoard() {
        Set<Coord> highlightCells = currentEliminationReason == null ? null : currentEliminationReason.getHighlightSubArea();
        List<AbstractGroup> highlightGroups = currentEliminationReason == null ? null : currentEliminationReason.getHighlightGroups();
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Board with highlights
        FxUtils.drawPuzzleOnCanvas(gameCanvas, myPuzzle, currentHighlightedCell, highlightCells);

        // Puzzle status
        if (myPuzzle.isComplete()) {
            lbNotes.setText("Complete\n");
            nextMoveButton.setDisable(true);
            // TODO: replace by solver inconsistent? Can check for no possibilities.
        } else if (myPuzzle.isInconsistent()) {
            lbNotes.setText("Inconsistent\n");
            nextMoveButton.setDisable(true);
        } else {
            lbNotes.setText("");
            nextMoveButton.setDisable(false);
        }

        // Pencil marks
        if (cbPencilMarks.isSelected()) {
            for (Coord c : myPuzzle.getAllCells()) {
                if (!myPuzzle.isOccupied(c)) {
                    Set<Integer> candidates = currentSolver.getCandidatesAtCell(c);
                    FxUtils.drawPossibilities(gameCanvas, myPuzzle, c, candidates);
                }
            }
        }

        // Highlighted groups
        if (highlightGroups != null) {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(3);
            for (AbstractGroup g : highlightGroups) {
                FxUtils.drawGroup(gameCanvas, myPuzzle, g);
            }
        }

//        if (!myPuzzle.isSolved()) {
//            if (currentHighlightedCell != null && !myPuzzle.isOccupied(currentHighlightedCell)) {
//                PossibilitiesContainer possibilitiesContainer = currentSolver.getPossibilitiesContainer();
//                if (possibilitiesContainer != null) {
//                    notes.appendText("Candidates: " +
//                            possibilitiesContainer.getCandidatesAtCell(currentHighlightedCell) + "\n");
//                }
//            } else {
//                //notes.appendText("Naked Singles: " + currentSolver.getNakedSingles() + "\n");
//                //notes.appendText("Unique Values: " + currentSolver.getUniqueValues() + "\n");
//            }
//        }

//        explainCell(currentHighlightedCell);

        // Highlight last move
        if (currentHighlightedCell != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            FxUtils.drawGroup(gameCanvas, myPuzzle, new SingleCellGroup(currentHighlightedCell, myPuzzle));
        }

        undoButton.setDisable(!myPuzzle.canUndo());
        redoButton.setDisable(!myPuzzle.canRedo());
    }

    // Redraws full display including controls
    private void redrawWholeDisplay() {
//        this.currentHighlightedCell = highlight;

        // Puzzle name and type
        cbPuzzleDB.getSelectionModel().select(myPuzzle.getName());

        // Symbol buttons
        digitButtonBar.getButtons().removeAll(digitButtonBar.getButtons());
        for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
            Button symbolButton = new Button();
            symbolButton.setText(myPuzzle.symbolCodeToSymbol(symbolCode));
            symbolButton.setStyle("-fx-font-size:8; -fx-font-weight: bold");
            symbolButton.setPrefSize((digitButtonBar.getPrefWidth() - digitButtonBar.getPadding().getLeft() - digitButtonBar.getPadding().getRight()) / (myPuzzle.getSymbolCodeRange() - 1), digitButtonBar.getPrefHeight());
            symbolButton.setOnAction(this::symbolClicked);
            digitButtonBar.getButtons().add(symbolButton);
        }

        // Puzzle itself
        redrawBoard();
    }

    public void eliminationAction(ActionEvent actionEvent) {
        currentSolver.setEliminateIntersectionRadiation(cbRadiation.isSelected())
                .setEliminateNakedPairs(cbNakedPairs.isSelected())
                .setEliminateXWings(cbXWings.isSelected());

        showEliminationReasons();
        redrawBoard();
    }

    public void undoAction(ActionEvent actionEvent) {
        currentHighlightedCell = myPuzzle.undoMove();
        showEliminationReasons();

        redrawBoard();
    }

    // Just a mouse over - show coordinates
    public void canvasMouseMove(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth() * mouseEvent.getX() / gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight() * mouseEvent.getY() / gameCanvas.getHeight());
        if (x >= 0 && x <= myPuzzle.getWidth()) {
            if (y >= 0 && y <= myPuzzle.getHeight()) {
                labelPosition.setText(String.valueOf(new Coord(x, y)));
            }
        }
    }

    // A totally new puzzle select will reset puzzle and solver completely
    public void puzzleSelectAction(ActionEvent actionEvent) {
        String puzzleName = String.valueOf(cbPuzzleDB.getValue());

        if (!puzzleName.equals(myPuzzle.getName())) { // event triggers very often
            ISudoku p;
            try {
                p = PuzzleDB.getPuzzleByName(puzzleName);
                if (p != null) {
                    newPuzzle(p);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void canvasMouseClick(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth() * mouseEvent.getX() / gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight() * mouseEvent.getY() / gameCanvas.getHeight());
        currentHighlightedCell = new Coord(x, y);

        labelPosition.setText(String.valueOf(currentHighlightedCell));

        showEliminationReasons();
        redrawBoard(); // redraw to wipe out any reason highlights
    }

    private void clearEliminationReasons()
    {
        lvEliminationSteps.getItems().clear();
        currentEliminationReason = null;
    }

    private void showEliminationReasons()
    {
        // Update list with elimination steps
        clearEliminationReasons();
        if (cbPencilMarks.isSelected() && currentHighlightedCell != null) {
            List<Explanation> reasons =
                    currentSolver.getEliminationReasons(currentHighlightedCell);
            if (null != reasons) {
                for (Explanation reason : reasons) {
                    lvEliminationSteps.getItems().add(reason.toString());
                }
            }
        }
    }

    public void doNextMove(ActionEvent actionEvent) {
        SolveStats stats = new SolveStats();
        Map.Entry<Coord, String> move = currentSolver.nextMove(stats);
        if (move != null) {
            Coord coord = move.getKey();

            myPuzzle.doMove(coord, move.getValue());
            currentHighlightedCell = coord;
            showEliminationReasons();

            redrawBoard(); // redraw to wipe out any reason highlights
        }
    }

    public void keyTyped(KeyEvent keyEvent) {
        System.out.println("Key: " + keyEvent);
    }

    public void hintAction(ActionEvent actionEvent) {
        SolveStats stats = new SolveStats();
        Map.Entry<Coord, String> move = currentSolver.nextMove(stats);
        if (move != null) {
            Coord coord = move.getKey();

            currentHighlightedCell = coord;
            clearEliminationReasons();

            redrawBoard(); // redraw to wipe out any reason highlights
        }
    }

    public void redoAction(ActionEvent actionEvent) {
        Map.Entry<Coord, String> move = myPuzzle.redoMove();
        if (move != null) {
            Coord coord = move.getKey();

            myPuzzle.doMove(coord, move.getValue());
            currentHighlightedCell = coord;
            showEliminationReasons();

            redrawBoard(); // redraw to wipe out any reason highlights
        }
    }
}
