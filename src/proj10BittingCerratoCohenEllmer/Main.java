/*
 * File: Main.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj10BittingCerratoCohenEllmer;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proj10BittingCerratoCohenEllmer.controllers.MasterController;

import java.io.IOException;


/**
 * Main class that sets up the stage.
 */
public class Main extends Application {

    /**
     * Main method of the program that calls {@code launch} inherited from the
     * Application class
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the contents of the starting window.
     *
     * <p>BUG: When clicking the red-x in the corner, this method will throw a null pointer
     * exception. When binding the close request to the primaryStage, the tabController
     * field inside of the main controller is null. This happens despite the main controller
     * being fully initialized and internally having access to its tabController.
     * All functionality EXCEPT for this singular piece works as expected. This appears
     * to have been a bug in netbeans 8.0 as documented shown
     * <a href="https://stackoverflow.com/a/24387797">here</a>.
     * </p>
     *
     * @param primaryStage A Stage object that is created by the {@code launch}
     *                     method inherited from the Application class.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // TODO: handle the IOException--Dale and Caleb couldn't figure out an elegant way disregard until further notice

        // Load fxml file
        MasterController controller = new MasterController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/Main.fxml"));
        Parent root = fxmlLoader.load();

        // handle clicking close box of the window
        primaryStage.setOnCloseRequest(windowEvent -> {
            controller.handleWindowExit();
            windowEvent.consume();
        });

        // Load css files
        Scene scene = new Scene(root);
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.add(getClass().getResource("view/Main.css").toExternalForm());
        stylesheets.add(getClass().getResource("view/java-keywords.css").toExternalForm());
        primaryStage.setScene(scene);

        // Set the minimum height and width of the main stage
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setTitle("Project 6");

        // Show the stage
        primaryStage.show();
    }
}
