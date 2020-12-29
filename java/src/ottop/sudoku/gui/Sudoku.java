package ottop.sudoku.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ottop.sudoku.PuzzleDB;

public class Sudoku extends Application {

    private Controller myController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = FXMLLoader.load(getClass().getResource("SudokuUI.fxml"));

        myController = Controller.theController;
        myController.setPuzzle(PuzzleDB.emptyStandardPuzzle);

        primaryStage.setTitle("U-Kudos");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
