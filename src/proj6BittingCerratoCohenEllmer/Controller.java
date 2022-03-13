/*
 * File: Controller.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj6BittingCerratoCohenEllmer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;


/**
 * Controller class contains handler methods for buttons and menu items.
 */
public class Controller {

    @FXML private StyleClassedTextArea console;
    @FXML private Button compileButton, compileRunButton, stopButton;
    @FXML private MenuItem undoMI, redoMI;
    @FXML private MenuItem selectAllMI, cutMI, copyMI, pasteMI;
    @FXML private MenuItem saveMI, saveAsMI, closeMI;
    @FXML private CodeTabController tabController;

    // Class DialogHelper handling all dialog instantiation
    private final DialogHelper dialogHelper = new DialogHelper();

    private Thread processThread = null;

    private final SimpleBooleanProperty isThreadActive = new SimpleBooleanProperty(false);

    /**
     * Exposes the exit handler's functionality to outside classes.
     */
    public void handleWindowExit(){
        handleExit(new ActionEvent());
    }


    /**
     * Sets up listeners to disable/enable menu items +
     * connects existing close boxes to the created close MenuItems
     */
    @FXML
    private void initialize() {

        tabController.makeNewTab();

        // disable appropriate menu items when no tabs are open
        // TODO: delegate disabling to some helper class
        closeMI.disableProperty().bind(hasNoTabs());
        saveMI.disableProperty().bind(hasNoTabs());
        saveAsMI.disableProperty().bind(hasNoTabs());
        undoMI.disableProperty().bind(hasNoTabs());
        redoMI.disableProperty().bind(hasNoTabs());
        selectAllMI.disableProperty().bind(hasNoTabs());
        cutMI.disableProperty().bind(hasNoTabs());
        copyMI.disableProperty().bind(hasNoTabs());
        pasteMI.disableProperty().bind(hasNoTabs());

        // Bind compile buttons so that they are disabled when a process is running
        compileButton.disableProperty().bind(Bindings.or(isThreadActive, hasNoTabs()));
        compileRunButton.disableProperty().bind(Bindings.or(isThreadActive, hasNoTabs()));
        stopButton.disableProperty().bind(Bindings.or(isThreadActive.not(), hasNoTabs()));
    }

    /**
     * Calls the noTabs method of the tabController
     * @see CodeTabController#noTabs
     * @return the binding indicating whether there are tabs
     */
    private BooleanBinding hasNoTabs() {
        return tabController.noTabs();
    }


    /**
     * Handles menu bar item About. Shows a dialog that contains program information.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        Dialog<ButtonType> dialog = dialogHelper.getAboutDialog();
        dialog.showAndWait();
    }


    /**
     * Creates a new tab.
     *
     * @see CodeTabController#makeNewTab
     */
    @FXML
    private void handleNew() {
        tabController.makeNewTab();
    }

    /**
     * Handles menu bar item Open.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @see CodeTabController#openFile
     */
    @FXML
    private void handleOpen(ActionEvent event) {
        tabController.openFile();
    }


    /**
     * Handles menu bar item Close
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @see CodeTabController#closeSelectedTab
     */
    @FXML
    public void handleClose(ActionEvent event) {
        tabController.closeSelectedTab(SaveReason.CLOSING);
    }

    /**
     * Handler method for menu bar item Exit.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @see CodeTabController#closeAllTabs
     */
    @FXML
    private void handleExit(ActionEvent event) {
        tabController.closeAllTabs();
    }


    /**
     * Handler method for menu bar item Save.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @see CodeTabController#saveCurrentTab
     * @return whether the save was successful
     */
    @FXML
    private boolean handleSave(ActionEvent event) {
        return tabController.saveCurrentTab();
    }

    /**
     * Handles menu bar item Save as....
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @see CodeTabController#saveCurrentTab
     *
     * @return whether the save was successful
     */
    @FXML
    private boolean handleSaveAs(ActionEvent event) {
        return tabController.saveCurrentTabAs();
    }

    /**
     * Handler method for menu bar item Undo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleUndo(ActionEvent event) {
        tabController.getSelectedTextBox().undo();
    }

    /**
     * Handler method for menu bar item Redo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleRedo(ActionEvent event) {
        tabController.getSelectedTextBox().redo();
    }

    /**
     * Handler method for menu bar item Cut.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCut(ActionEvent event) {
        tabController.getSelectedTextBox().cut();
    }

    /**
     * Handler method for menu bar item Copy.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCopy(ActionEvent event) {
        tabController.getSelectedTextBox().copy();
    }

    /**
     * Handler method for menu bar item Paste.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handlePaste(ActionEvent event) {
        tabController.getSelectedTextBox().paste();
    }

    /**
     * Handler method for menu bar item Select all.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSelectAll(ActionEvent event) {
        tabController.getSelectedTextBox().selectAll();
    }


    /**
     * Handler method for Compile button.
     * If the tab is dirty, asks user to save. If user chooses to save, the changes are
     * saved and the tab is compiled. If user chooses not to save, the currently saved
     * version of the file is compiled (the unsaved changes are ignored). If the user
     * cancels the dialog, no compilation is performed.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    // TODO update the javadoc
    @FXML
    private void handleCompile(ActionEvent event) {
        ProcessBuilder processBuilder = tabController.compileTab();
        if (processBuilder == null) {
            return;
        }
        try {
            Process process = processBuilder.start();
            int exitValue = process.waitFor();
            // if an error occurs
            if ( process.getErrorStream().read() != -1 ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = reader.readLine();
                while (line != null) {
                    console.appendText(line + "\n");
                    line = reader.readLine();
                }
            }
            // if compilation process exits successfully
            if ( exitValue == 0 ) {
                console.appendText("\nCompilation was successful.\n");
            }
        } catch (IOException | InterruptedException e) {
            dialogHelper.getAlert("Compilation Failed", e.getMessage()).show();
        }
    }


    private void putOnConsole(String toDisplay) throws IOException {
        Platform.runLater(() -> console.appendText(toDisplay + "\n"));
    }



    /**
     * Handler method for Compile & Run button.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCompileRun(ActionEvent event) throws InterruptedException {
        ProcessBuilder processBuilder = tabController.compileTab();
        if (processBuilder == null) {
            return;
        }
        // prepare running in a new thread
        processThread = new Thread(() -> {
            try {
                Process process = processBuilder.start();

                // get outStream and inStream
                InputStream inStream = process.getInputStream();
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                OutputStream outStream = process.getOutputStream();

                // make console listen for key presses
                console.setOnKeyReleased(new EventHandler<>() {
                    String userInput = "";

                    public void handle(KeyEvent event) {
                        // get the key that is pressed and add it
                        userInput += event.getText();
                        // if user presses enter
                        if (event.getCode() == KeyCode.ENTER) {
                            try {
                                outStream.write(userInput.getBytes(StandardCharsets.UTF_8));
                                outStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            userInput = "";         // start user input over
                        }
                    }
                });

                // print to the new std.out while the program is running
                String line;
                while (process.isAlive()) {
                    line = inputReader.readLine();
                    if (line != null) {
                        putOnConsole(line);
                    }
                }
                outStream.close();

                // if compilation process exits successfully
                Platform.runLater(() -> {
                    console.appendText(String.format("\nProcess finished with exit code %d.\n", process.exitValue()));
                });
            }
            catch (IOException ex) {
                Platform.runLater(() -> {
                    dialogHelper.getAlert("Runtime Error", ex.getMessage()).show();
                });
            }
            // after the thread is done running, it should set the internal field back to null so that
            // the bindings can recognize that there is no process running
            this.processThread = null;
            this.isThreadActive.set(false);
        });
        this.isThreadActive.set(true);
        processThread.start();
    }

    /**
     * Handles the stop button. Forcefully stops the thread and resets the processThread
     * to null.
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @deprecated
     */
    @FXML
    private void handleStop(ActionEvent event) {;
        if (processThread != null) {
            processThread.stop(); // TODO: do this in a non-depricated manner
            this.isThreadActive.set(false);
            processThread = null;
        }
    }
}
