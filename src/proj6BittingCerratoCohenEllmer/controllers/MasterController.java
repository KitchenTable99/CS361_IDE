/*
 * File: MasterController.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj6BittingCerratoCohenEllmer.controllers;

import java.io.*;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyleClassedTextArea;
import proj6BittingCerratoCohenEllmer.view.DialogHelper;
import proj6BittingCerratoCohenEllmer.SaveReason;


/**
 * MasterController class contains handler methods for buttons and menu items.
 */
public class MasterController {

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


    // TODO add javadoc
    private void doCompiling(ProcessBuilder processBuilder) {
        if (processBuilder == null) {
            return;
        }
        // prepare running in a new thread
        processThread = new Thread(() -> {
            try {
                Process process = processBuilder.start();

                // interact with the console
                sendInputFromStreamToConsole(console, process.getErrorStream());

                // indicate the process is complete
                process.waitFor();
                if (process.exitValue() == 0) {
                    Platform.runLater(() -> {
                        console.appendText("Compilation successful");
                    });
                }
            }
            catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    dialogHelper.getAlert("Runtime Error", e.getMessage()).show();
                });
            }
            // after the thread is done running, it should set the internal field back to null so that
            // the bindings can recognize that there is no process running
            processThread = null;
            isThreadActive.set(false);
        });
        isThreadActive.set(true);
        processThread.start();
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
        ProcessBuilder processBuilder = tabController.prepareCompileProcess();
        doCompiling(processBuilder);
    }

    /**
     * Handler method for Compile & Run button.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCompileRun(ActionEvent event) {
        ProcessBuilder processBuilder = tabController.prepareCompileProcess();
        doCompiling(processBuilder);
        // TODO actually run the compiled file
    }

    /**
     * gets input typed into the Console and writes it to the given OutputStream
     * The characters are written to the OutputStream when \r or \n are typed.
     * @author dskrien
     *
     * @param ioConsole    the StyleClassedTextArea whose input is sent to the stream
     * @param outputStream the OutputStream where the Console input is sent
     */
    public void sendInputFromConsoleToStream(StyleClassedTextArea ioConsole,
                                             OutputStream outputStream) {
        ioConsole.setOnKeyTyped(new EventHandler<>()
        {
            String result = ""; // the text to sent to the stream

            @Override
            public void handle(KeyEvent event) {
                String ch = event.getCharacter();
                result += ch;
                if (ch.equals("\r") || ch.equals("\n")) {
                    try {
                        for (char c : result.toCharArray()) {
                            outputStream.write(c);
                        }
                        outputStream.flush();
                        result = "";
                    } catch (IOException e) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                                "Could not send input to the output stream."));
                    }
                }
            }

        });
    }

    /**
     * gets the input from an InputStream and writes it continuously to the ioConsole.
     * @author dskrien
     *
     * @param ioConsole   the StyleClassedTextArea where the data is written
     * @param inputStream the InputStream providing the data to be written
     */
    public void sendInputFromStreamToConsole(StyleClassedTextArea ioConsole,
                                             InputStream inputStream) throws IOException {
        // for a discussion of how to convert inputStream data to strings, see
        // [stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string]
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            String result = new String(buffer, 0, length);
            Platform.runLater(() -> {
                ioConsole.appendText(result);
                ioConsole.moveTo(ioConsole.getLength()); //move cursor to the end
                ioConsole.requestFollowCaret();
            });
        }
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
