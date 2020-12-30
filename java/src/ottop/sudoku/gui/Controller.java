package ottop.sudoku.gui;

import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import ottop.sudoku.*;
import ottop.sudoku.group.AbstractGroup;
import ottop.sudoku.puzzle.IPuzzle;

import java.util.AbstractMap;
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
    public CheckBox cbBasicElimination;
    public Button buttonMoreHelp;
    public TextArea notes;
    public Label labelPosition;
    public ChoiceBox cbPuzzleDB;
    public Button nextMoveButton;
    public Button explainButton;

    private IPuzzle myPuzzle;
    private String currentCellSymbol = null;
    //private Map<Coord, Set<Integer>> currentPencilMarks;
    private SolutionContainer currentSolutions;
    private PossibilitiesContainer currentPossibilities;

    public Controller() {
        theController = this;
    }

    public void symbolClicked(ActionEvent actionEvent) {
        currentCellSymbol = ((Button) actionEvent.getSource()).getText();
    }

    // TODO: support arrow keys move around in canvas
    // TODO: also support pressing keys 1-9

    public void setPuzzle(IPuzzle initPuzzle) {
        setPuzzle(initPuzzle, null);
    }

    public void setPuzzle(IPuzzle initPuzzle, Coord highlight) {
        myPuzzle = initPuzzle;
        updateWholeDisplay(highlight);
    }

    private void updateWholeDisplay(Coord highlight) {

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

        // Possible values
        digitButtonBar.getButtons().removeAll(digitButtonBar.getButtons());
        for (int symbolCode = 1; symbolCode < myPuzzle.getSymbolCodeRange(); symbolCode++) {
            Button symbolButton = new Button();
            symbolButton.setText(myPuzzle.symbolCodeToSymbol(symbolCode));
            symbolButton.setStyle("-fx-font-size:8; -fx-font-weight: bold");
            symbolButton.setPrefSize((digitButtonBar.getPrefWidth() - digitButtonBar.getPadding().getLeft() - digitButtonBar.getPadding().getRight()) / (myPuzzle.getSymbolCodeRange() - 1), digitButtonBar.getPrefHeight());
            symbolButton.setOnAction(actionEvent -> symbolClicked(actionEvent));
            digitButtonBar.getButtons().add(symbolButton);
        }

        // Puzzle itself
        myPuzzle.drawPuzzleOnCanvas(gameCanvas, highlight);

        // Cursor
        labelPosition.setText("");

        // Hints & help
        showPuzzleHints();
    }

    public void hintsAction(ActionEvent actionEvent) {
        if ((cbRadiation.isSelected() || cbNakedPairs.isSelected() || cbXWings.isSelected()) & !cbBasicElimination.isSelected()) {
            cbBasicElimination.setSelected(true);
        }
        updateWholeDisplay(null);
    }

    public void showPuzzleHints() {
        currentPossibilities = null;
        currentSolutions = null;

        if (myPuzzle.isSolved()) {
            notes.setText("Complete");
            nextMoveButton.setDisable(true);
        } else if (myPuzzle.isInconsistent()) {
            notes.setText("Inconsistent");
            nextMoveButton.setDisable(false);
        } else {
            nextMoveButton.setDisable(false);
            SudokuSolver sv = new SudokuSolver(myPuzzle);

            if (cbRadiation.isSelected()) {
                sv.eliminateByRadiationFromIntersections();
            }
            if (cbNakedPairs.isSelected()) {
                sv.eliminateNakedPairs();
            }
            if (cbXWings.isSelected()) {
                sv.eliminateByXWings();
            }

            if (cbBasicElimination.isSelected()) {
                currentPossibilities = sv.getPossibilities();

                for (Coord c : myPuzzle.getAllCells()) {
                    if (!myPuzzle.isOccupied(c)) {
                        myPuzzle.drawPossibilities(gameCanvas, c, currentPossibilities.getPossibilities(c));
                    }
                }

                currentSolutions = sv.getMoves();
                notes.setText("Lone numbers: " + currentSolutions.getLoneSymbols() + "\n");
                notes.appendText("Unique values: " + currentSolutions.getUniqueSymbols() + "\n");
            } else {
                notes.setText("");
            }
        }
        explainButton.setDisable(!cbBasicElimination.isSelected());
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
        if (x >= 0 && x <= 9) {
            if (y >= 0 && y <= 9) {
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

        if (null != currentCellSymbol) {
            if ("?".equals(currentCellSymbol)) {
                // explain
                notes.setText("Explain " + coord + "\n");

                if (null != currentSolutions) {
                    String s = currentSolutions.getLoneSymbolAt(coord);
                    if (null != s) {
                        notes.appendText("Lone symbol: " + s + "\n");
                    }
                    // TODO: consider highlighting the groups
                    AbstractMap.SimpleEntry<String, List<AbstractGroup>> s2 = currentSolutions.getUniqueSymbolAt(coord);
                    if (null != s2) {
                        notes.appendText("Unique symbol: " + s2.getKey() + " in " + s2.getValue() + "\n");
                    }
                }
                notes.appendText("Possible values: " + currentPossibilities.getPossibilities(coord) + "\n");

                // TODO: highlight the reasons in the board
                // TODO: collect the simple eliminations
                List<EliminationReason> reasons = currentPossibilities.getEliminations(coord);
                if (null != reasons) {
                    for (EliminationReason reason : reasons) {
                        notes.appendText(reason + "\n");
                    }
                }
            } else {
                IPuzzle newPuzzle = myPuzzle.doMove(coord, currentCellSymbol);
                if (newPuzzle != null) {
                    setPuzzle(newPuzzle, coord);
                }
            }
        }
    }

    public void doNextMove(ActionEvent actionEvent) {
        SudokuSolver sv = new SudokuSolver(myPuzzle);
        int level = 0;
        if (cbBasicElimination.isSelected()) {
            level += SudokuSolver.EliminationMethods.BASICRADIATION.code();
        }
        if (cbRadiation.isSelected()) {
            level += SudokuSolver.EliminationMethods.INTERSECTION.code();
        }
        if (cbNakedPairs.isSelected()) {
            level += SudokuSolver.EliminationMethods.NAKEDPAIRS.code();
        }
        if (cbXWings.isSelected()) {
            level += SudokuSolver.EliminationMethods.XWINGS.code();
        }
        Map.Entry<Coord, String> move = sv.nextMove(level);
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
        currentCellSymbol = "?";
    }
}
