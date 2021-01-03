package ottop.sudoku.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import ottop.sudoku.Coord;
import ottop.sudoku.PossibilitiesContainer;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.SudokuSolver;
import ottop.sudoku.explain.EliminationReason;
import ottop.sudoku.explain.IntersectionRadiationEliminationReason;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.group.SingleCellGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.ArrayList;
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
    public TextArea notes;
    public Label labelPosition;
    public ChoiceBox<String> cbPuzzleDB;
    public Button nextMoveButton;
    public Button explainButton;
    public ListView lvEliminationSteps;

    private IPuzzle myPuzzle;
    private String currentActionButtonValue = null;
    private Coord currentHighlightedCell = null;
    private List<AbstractGroup> currentHighlightedGroups = null;
    private Set<Coord> currentHighlightedSubArea = null;
    private SudokuSolver currentSolver = null;
    private List<EliminationReason> currentEliminationReasons = null;

    public Controller() {
        theController = this;
    }

    public void initialize() {
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                int selectionIdx = lvEliminationSteps.getSelectionModel().getSelectedIndex();
                if (currentEliminationReasons != null && selectionIdx >= 0 && selectionIdx<currentEliminationReasons.size()) {
                    EliminationReason reason = currentEliminationReasons.get(selectionIdx);
                    currentHighlightedGroups = reason.getHighlightGroups();
                    currentHighlightedSubArea = reason.getHighlightSubArea();
                    updateWholeDisplay(currentHighlightedCell);
                }
            }
        };
        lvEliminationSteps.getSelectionModel().selectedItemProperty().removeListener(listener);
        lvEliminationSteps.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public void symbolClicked(ActionEvent actionEvent) {
        currentActionButtonValue = ((Button) actionEvent.getSource()).getText();
    }

    // TODO: support arrow keys move around in canvas
    // TODO: also support pressing keys 1-9

    public void setPuzzle(IPuzzle initPuzzle) {
        setPuzzle(initPuzzle, null);
    }

    public void setPuzzle(IPuzzle initialPuzzle, Coord highlight) {
        myPuzzle = initialPuzzle;

        currentSolver = (new SudokuSolver(myPuzzle))
                .setEliminateIntersectionRadiation(cbRadiation.isSelected())
                .setEliminateNakedPairs(cbNakedPairs.isSelected())
                .setEliminateXWings(cbXWings.isSelected());
        currentSolver.eliminatePossibilities();

        currentEliminationReasons = null;
        currentHighlightedGroups = null;
        currentHighlightedSubArea = null;
        lvEliminationSteps.getItems().clear();

        updateWholeDisplay(highlight);
    }

    private void updateWholeDisplay(Coord highlight) {
        this.currentHighlightedCell = highlight;

        // List of puzzles
        try {
            String[] puzzles = PuzzleDB.getPuzzles();
            cbPuzzleDB.getItems().setAll(puzzles);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Puzzle name and type
        cbPuzzleDB.setValue(myPuzzle.getName());
        undoButton.setDisable(!myPuzzle.canUndo());

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
        myPuzzle.drawPuzzleOnCanvas(gameCanvas, highlight, currentHighlightedSubArea);

        // Cursor
        labelPosition.setText("");

        // Hints & help
        showPuzzleHints();

        // Highlight last move
        if (highlight != null) {
            GraphicsContext gc = gameCanvas.getGraphicsContext2D();
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            myPuzzle.drawGroup(gameCanvas, new SingleCellGroup(highlight, myPuzzle));
        }

    }

    public void hintsAction(ActionEvent actionEvent) {
        setPuzzle(myPuzzle, currentHighlightedCell); // will re-do eliminations and reset puzzle solver
    }

    public void showPuzzleHints() {
        if (myPuzzle.isSolved()) {
            notes.setText("Complete\n");
            nextMoveButton.setDisable(true);
        } else if (myPuzzle.isInconsistent()) {
            notes.setText("Inconsistent\n");
            nextMoveButton.setDisable(true);
        } else {
            notes.setText("");
            nextMoveButton.setDisable(false);
        }

        if (!myPuzzle.isSolved()) {
            if (currentHighlightedCell != null && !myPuzzle.isOccupied(currentHighlightedCell)) {
                notes.appendText(currentHighlightedCell + ":\n");

                boolean hasPossibleMoves = false;
                PossibilitiesContainer possibilitiesContainer = currentSolver.getPossibilitiesContainer();
                if (null != possibilitiesContainer) {
                    Map<Coord, String> nakedSingles = possibilitiesContainer.getAllNakedSingles();
                    if (nakedSingles != null && nakedSingles.size()>0) {
                        String symbol = nakedSingles.get(currentHighlightedCell);
                        if (symbol != null) {
                            notes.appendText("Naked Single: " + symbol + "\n");
                            hasPossibleMoves = true;
                        }
                    }

                    Map<Coord, Map.Entry<String, List<AbstractGroup>>> uniqueValues = possibilitiesContainer.getAllUniqueValues();
                    if (uniqueValues != null && uniqueValues.size()>0) {
                        Map.Entry<String, List<AbstractGroup>> symbol = uniqueValues.get(currentHighlightedCell);
                        if (symbol != null) {
                            notes.appendText("Unique Value: " + symbol.getKey() + " in " + symbol.getValue() + "\n");
                            hasPossibleMoves = true;
                        }
                    }
                }
                if (!hasPossibleMoves) {
                    notes.appendText("Candidates: " +
                            possibilitiesContainer.getCandidatesAtCell(currentHighlightedCell) + "\n");
                }
            } else {
                notes.appendText("Naked Singles: " + currentSolver.getNakedSingles() + "\n");
                notes.appendText("Unique Values: " + currentSolver.getUniqueValues() + "\n");
            }

            if (cbPencilMarks.isSelected()) {
                for (Coord c : myPuzzle.getAllCells()) {
                    if (!myPuzzle.isOccupied(c)) {
                        Set<Integer> candidates = currentSolver.getPossibilitiesContainer().getCandidatesAtCell(c);
                        myPuzzle.drawPossibilities(gameCanvas, c, candidates);
                    }
                }
            }

            // Highlighted groups
            GraphicsContext gc = gameCanvas.getGraphicsContext2D();
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(3);
            if (currentHighlightedGroups != null) {
                for (AbstractGroup g : currentHighlightedGroups) {
                    myPuzzle.drawGroup(gameCanvas, g);
                }
            }
        }
        explainButton.setDisable(!cbPencilMarks.isSelected());
    }

    public void moreHelpAction(ActionEvent actionEvent) {
        System.out.println("More help...");
    }

    public void undoAction(ActionEvent actionEvent) {
        IPuzzle prevPuzzle = myPuzzle.undoMove();
        if (prevPuzzle != null) {
            setPuzzle(prevPuzzle);
        }
    }

    public void canvasMouseMove(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth() * mouseEvent.getX() / gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight() * mouseEvent.getY() / gameCanvas.getHeight());
        if (x >= 0 && x <= myPuzzle.getWidth()) {
            if (y >= 0 && y <= myPuzzle.getHeight()) {
                labelPosition.setText("" + new Coord(x, y));
            }
        }
    }

    public void puzzleSelectAction(ActionEvent actionEvent) {
        String puzzleName = String.valueOf(cbPuzzleDB.getValue());
        if (!puzzleName.equals(myPuzzle.getName())) { // event triggers very often
            IPuzzle p;
            try {
                p = PuzzleDB.getPuzzleByName(puzzleName);
                if (p != null) {
                    setPuzzle(p);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void canvasMouseClick(MouseEvent mouseEvent) {
        int x = (int) Math.floor(myPuzzle.getWidth() * mouseEvent.getX() / gameCanvas.getWidth());
        int y = (int) Math.floor(myPuzzle.getHeight() * mouseEvent.getY() / gameCanvas.getHeight());
        Coord coord = new Coord(x, y);
        labelPosition.setText(""+coord);

        if (null != currentActionButtonValue) {
            if ("?".equals(currentActionButtonValue)) {
                // explain
                if (!myPuzzle.isOccupied(coord)) {
                    PossibilitiesContainer possibilitiesContainer = currentSolver.getPossibilitiesContainer();

                    currentEliminationReasons = possibilitiesContainer.getEliminationReasons(coord);
                    lvEliminationSteps.getItems().clear();
                    currentHighlightedGroups = null;
                    currentHighlightedSubArea = null;
                    lvEliminationSteps.setDisable(null == currentEliminationReasons || currentEliminationReasons.size() == 0);
                    if (null != currentEliminationReasons) {
                        for (EliminationReason reason : currentEliminationReasons) {
                            lvEliminationSteps.getItems().add(reason);
                        }
                    }
                } else {
                    currentEliminationReasons = null;
                    lvEliminationSteps.getItems().clear();
                    lvEliminationSteps.setDisable(true);
                }
                updateWholeDisplay(coord); // highlight
            } else {
                // do move

                IPuzzle newPuzzle = myPuzzle.doMove(coord, currentActionButtonValue);
                if (newPuzzle != null) {
                    setPuzzle(newPuzzle, coord);
                }

            }
        }
    }

    public void doNextMove(ActionEvent actionEvent) {
        Map.Entry<Coord, String> move = currentSolver.nextMove();
        if (move != null) {
            Coord coord = move.getKey();
            IPuzzle newPuzzle = myPuzzle.doMove(coord, move.getValue());
            if (newPuzzle != null) {
                setPuzzle(newPuzzle, coord);
            }
        }
    }

    public void keyTyped(KeyEvent keyEvent) {
        System.out.println("Key: " + keyEvent);
    }

    public void explainClicked(ActionEvent actionEvent) {
        currentActionButtonValue = "?";
    }

    public void eliminationStepsClicked(MouseEvent mouseEvent) {
        //System.out.println("Click elimination steps");
    }
}
