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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
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
    @FXML private TabPane tabPane;
    @FXML private MenuItem undoMI, redoMI;
    @FXML private MenuItem selectAllMI, cutMI, copyMI, pasteMI;
    @FXML private MenuItem saveMI, saveAsMI, closeMI;

    // list of saved tabs and their content
    private final HashMap<Tab,String> savedContents = new HashMap<>();
    // list of saved tabs and their saving path
    private final HashMap<Tab,String> savedPaths = new HashMap<>();
    // Class DialogHelper handling all dialog instantiation
    private final DialogHelper dialogHelper = new DialogHelper();

    private Thread processThread = null;

    private SimpleBooleanProperty isThreadActive = new SimpleBooleanProperty(false);

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

        this.handleNew();

        // disable appropriate menu items when no tabs are open
        closeMI.disableProperty().bind(noTabs());
        saveMI.disableProperty().bind(noTabs());
        saveAsMI.disableProperty().bind(noTabs());
        undoMI.disableProperty().bind(noTabs());
        redoMI.disableProperty().bind(noTabs());
        selectAllMI.disableProperty().bind(noTabs());
        cutMI.disableProperty().bind(noTabs());
        copyMI.disableProperty().bind(noTabs());
        pasteMI.disableProperty().bind(noTabs());

        // Bind compile buttons so that they are disabled when a process is running
        compileButton.disableProperty().bind(Bindings.or(isThreadActive, noTabs()));
        compileRunButton.disableProperty().bind(Bindings.or(isThreadActive, noTabs()));
        stopButton.disableProperty().bind(Bindings.or(isThreadActive.not(), noTabs()));
    }

    /**
     * Returns a true BooleanBinding if there are no more tabs and a false one if there
     * is at least one tab.
     *
     * @return a BooleanBinding demonstrating if there are no more tabs
     */
    private BooleanBinding noTabs() {
        return Bindings.isEmpty(tabPane.getTabs());
    }


    /**
     * Gets the currently selected tab in tabPane
     *
     * @return the selected tab
     */
    private Tab getSelectedTab () {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * helper function to get the text box in the selected tab
     *
     * @return TextArea  the text box in the selected tab
     */
    private CodeArea getSelectedTextBox() {
        Tab currentTab = getSelectedTab();
        VirtualizedScrollPane scrollPane;
        scrollPane = (VirtualizedScrollPane) currentTab.getContent();
        return (CodeArea) scrollPane.getContent();
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
     * Handles menu bar item New. Creates a new tab and adds it to the tabPane.
     *
     *
     */
    @FXML
    private void handleNew() {

        // calls helper method for untitled tabName generation
        String newTabName = getNextAvailableUntitled();

        // creates tab and sets close behavior
        Tab newTab = new Tab(newTabName);
        newTab.setOnCloseRequest(closeEvent -> {
            tabPane.getSelectionModel().select(newTab);
            handleClose(new ActionEvent());
            closeEvent.consume();
        });

        // installs toolTip
        Tooltip tabToolTip = new Tooltip(newTab.getText());
        newTab.setTooltip(tabToolTip);

        // create a code area
        HighlightedCodeArea highlightedCodeArea = new HighlightedCodeArea();
        CodeArea codeArea = highlightedCodeArea.getCodeArea();
        newTab.setContent(new VirtualizedScrollPane<>(codeArea));
        // add new tab to the tabPane and sets as topmost
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().selectLast();
    }

    /**
     * Helper method that generates a string that is the lowest "Untitled-x" available.
     *
     * @return String name for newTab
     * @author Caleb Bitting, Jasper Loverude, Andy Xu
     */
    private String getNextAvailableUntitled() {

        // Stores whether "Untitled" is available in boolean, iterates through all tabs
        boolean defaultUntitledAvailable = true;

        ObservableList<Tab> allTabsList = this.tabPane.getTabs();

        for(Tab currTab : allTabsList){

            // If "Untitled", sets boolean to false,
            String currTabTitle = currTab.getText();
            if(currTabTitle.equals("Untitled")) {
                defaultUntitledAvailable = false;
                break;
            }
        }

        if(defaultUntitledAvailable) return "Untitled";

        // Iterates through every tab in hashSet, until the lowest "Untitled-x" is found
        int untitledNumber = 1;
        String lowestUntitledName = "Untitled-" + untitledNumber;

        for(int i = 0; i < allTabsList.size(); i++){

            String untitledName = allTabsList.get(i).getText();

            if(lowestUntitledName.equals(untitledName)){
                untitledNumber++;
                lowestUntitledName = "Untitled-" + untitledNumber;
                i = 0;
            }
        }
        // Returns "Untitled-x" with lowest x available
        return lowestUntitledName;

    }

    /**
     * Handles menu bar item Open. Shows a dialog and lets the user select a file to be
     * loaded into the text box.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleOpen(ActionEvent event) {
        // create a new file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Java Files", "*.java"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("FXML Files", "*.fxml"),
            new FileChooser.ExtensionFilter("CSS Files", "*.css"));
        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());

        // if user selects a file (instead of pressing cancel button
        if (selectedFile != null) {
            try {
                // reads the file content to a String
                String content = new String(Files.readAllBytes(
                        Paths.get(selectedFile.getPath())));
                // open a new tab
                this.handleNew();
                // set text/name of the tab to the filename
                this.getSelectedTab().setText(selectedFile.getName());
                getSelectedTextBox().replaceText(content);
                // update savedContents field
                this.savedContents.put(getSelectedTab(), content);
                this.savedPaths.put(getSelectedTab(), selectedFile.getPath());
                this.getSelectedTab().getTooltip().setText(selectedFile.getPath());
            } catch (IOException e) {
                dialogHelper.getAlert("File Opening Error", e.getMessage()).show();
            }
        }
    }


    /**
     * Handles menu bar item Close. Creates a dialog if the selected tab is unsaved and
     * closes the tab.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        closeSelectedTab(event, SaveReason.CLOSING);
    }


    /**
     * Helper method that closes tabs. Prompts the user to save if tab is dirty. If the
     * user chooses to save the changes, the changes are saved and the tab is closed.
     * If the tab is clean or the user chooses to save the dirty tab, the tab is closed.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @return Optional the Optional object returned by dialog.showAndWait().
     *                  returns null if tab text is already saved.
     */
    private Optional<ButtonType> closeSelectedTab(ActionEvent event, SaveReason reason) {

        // If selectedTab is unsaved, opens dialog to ask user whether they would like to save
        if(selectedTabIsDirty()) {
            String fileName = getSelectedTab().getText();
            Dialog<ButtonType> saveDialog = dialogHelper.getSavingDialog(fileName, reason);

            Optional<ButtonType> result  = saveDialog.showAndWait();
            // save if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                this.handleSave(event);
                // Keep the tab if the save is unsuccessful (eg. canceled)
                if (selectedTabIsDirty()) {
                    return result;
                }
            }
            // quit the dialog and keep selected tab if user chooses CANCEL
            else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return result;
            }
        }
        // remove tab from tabPane if text is saved or user chooses NO
        this.savedContents.remove(getSelectedTab());
        this.savedPaths.remove(getSelectedTab());
        tabPane.getTabs().remove(getSelectedTab());
        return Optional.empty();
    }


    /**
     * Handler method for menu bar item Exit. When exit item of the menu
     * bar is clicked, the application quits if all tabs in the tabPane are
     * closed properly.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        tabPane.getSelectionModel().selectLast();
        while (tabPane.getTabs().size() > 0) {
            // try close the currently selected tab
            Optional<ButtonType> result = closeSelectedTab(event, SaveReason.EXITING);
            // if the user chooses Cancel at any time, then the exiting is canceled,
            // and the application stays running.
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        // exit if all tabs are closed
        System.exit(0);
    }


    /**
     * Helper method that checks if the text in the selected tab is saved.
     *
     * @return boolean whether the text in the selected tab is dirty (unsaved changes).
     */
    private boolean selectedTabIsDirty() {

        // Gets current contents of tab and its hashed contents (Null if unsaved)
        String currentContents = getSelectedTextBox().getText();
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        String savedContent = this.savedContents.get(selectedTab);

        // If no saved contents, and tab is empty, contents not dirty
        if(savedContent == null && currentContents.equals("")) {
            return false;
        }
        // If no saved contents, but code area not empty, contents are dirty
        else if(savedContent == null) {
            return true;
        }
        // Otherwise, returns false (not dirty) if contents equal, or true if they aren't
        else return !savedContent.equals(currentContents);




    }

    /**
     * Handler method for menu bar item Save. Behaves like Save as... if the text
     * has never been saved before. Otherwise, save the text to its corresponding
     * text file.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @return whether the save was successful
     */
    @FXML
    private boolean handleSave(ActionEvent event) {
        // if the text has been saved before
        if (savedContents.containsKey(getSelectedTab())) {
            // create a File object for the corresponding text file
            File savedFile = new File(savedPaths.get(getSelectedTab()));
            try {
                // write the new content to the text file
                FileWriter writer = new FileWriter(savedFile);
                writer.write(getSelectedTextBox().getText());
                writer.close();
                // update savedContents field
                savedContents.put(getSelectedTab(), getSelectedTextBox().getText());

                return true;
            } catch (IOException e) {
                dialogHelper.getAlert("File Saving Error", e.getMessage()).show();

                return false;
            }
        }
        // if text in selected tab was not loaded from a file nor ever saved to a file
        else {
            return handleSaveAs(event);
        }
    }

    /**
     * Handles menu bar item Save as....  a dialog appears in which the user is asked for
     * to save a file with four permitted extensions: .java, .txt, .fxml, and .css.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @return whether the save was successful
     */
    @FXML
    private boolean handleSaveAs(ActionEvent event) {
        // create a new fileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Java Files", "*.java"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("FXML Files", "*.fxml"),
            new FileChooser.ExtensionFilter("CSS Files", "*.css"));
        File fileToSave = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        // if user did not choose CANCEL
        if (fileToSave != null) {
            try {
                // save file
                FileWriter fw = new FileWriter(fileToSave);
                fw.write(this.getSelectedTextBox().getText());
                fw.close();
                // update savedContents field and tab text
                this.savedContents.put(getSelectedTab(), getSelectedTextBox().getText());
                this.savedPaths.put(getSelectedTab(), fileToSave.getPath());
                this.getSelectedTab().setText(fileToSave.getName());
                this.getSelectedTab().getTooltip().setText(fileToSave.getPath());

                return true;
            } catch ( IOException e ) {
                dialogHelper.getAlert("File Saving Error", e.getMessage()).show();

                return false;
            }
        }
        return false;
    }

    /**
     * Handler method for menu bar item Undo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleUndo(ActionEvent event) {
        getSelectedTextBox().undo();
    }

    /**
     * Handler method for menu bar item Redo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleRedo(ActionEvent event) {
        getSelectedTextBox().redo();
    }

    /**
     * Handler method for menu bar item Cut.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCut(ActionEvent event) {
        getSelectedTextBox().cut();
    }

    /**
     * Handler method for menu bar item Copy.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCopy(ActionEvent event) {
        getSelectedTextBox().copy();
    }

    /**
     * Handler method for menu bar item Paste.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handlePaste(ActionEvent event) {
        getSelectedTextBox().paste();
    }

    /**
     * Handler method for menu bar item Select all.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSelectAll(ActionEvent event) {
        getSelectedTextBox().selectAll();
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
    @FXML
    private void handleCompile(ActionEvent event) throws InterruptedException {
        compileTab(event);
    }

    /**
     * Helper method for handleCompile().
     *
     * @see #handleCompile(ActionEvent)
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @return a boolean that indicates whether the compilation was successful.
     */
    private boolean compileTab(ActionEvent event) {
        if (getSelectedTextBox().getText().equals("")) {
            // in industry, we call this an easter egg
            dialogHelper.getAlert("You cannot compile an empty file", "dumbass...").show();
            return false;
        }
        // If selected tab is dirty, calls handleSave
        if (selectedTabIsDirty()) {
            // Creates new dialog
            Dialog<ButtonType> saveDialog = dialogHelper.getSavingDialog(getSelectedTab().getText(), SaveReason.COMPILING);
            Optional<ButtonType> result = saveDialog.showAndWait();
            // call handleSave() if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                boolean saved = handleSave(event);
                if (!saved) {
                    String body = "Compilation was canceled because you " +
                            "aborted saving the file";
                    dialogHelper.getAlert("Compilation Canceled", body).show();
                    event.consume();
                    return false;
                }
            }
            // No compilation if user chooses CANCEL
            else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
                return false;
            }
            else if (result.isPresent() && result.get() == ButtonType.NO) {
                // if user chooses NO and the current tab is not saved before
                if (!savedPaths.containsKey(getSelectedTab())) {
                    // make an alert box
                    String body = "Current tab has not been saved. Pleas save before compiling.";
                    dialogHelper.getAlert("Unable to Compile", body).show();
                    event.consume();
                    return false;
                }
            }
        }
        String filepath = this.savedPaths.get(getSelectedTab());
        // new process builder for compilation
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("javac", filepath);

        try {
            Process process = processBuilder.start();
            // if an error occurs
            if ( process.getErrorStream().read() != -1 ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = reader.readLine();
                while (line != null) {
                    console.appendText(line + "\n");
                    line = reader.readLine();
                }
            }
            int exitValue = 0;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                dialogHelper.getAlert("Compilation Failed", e.getMessage()).show();
            }
            // if compilation process exits successfully
            if ( exitValue == 0 ) {
                console.appendText("\nCompilation was successful.\n");
                return true;
            }
        }
        catch (IOException ex) {
            dialogHelper.getAlert("Compilation Failed", ex.getMessage()).show();
        }
        return false;
    }

    private void putOnConsole(String toDisplay) throws IOException {
        Platform.runLater(() -> {
            console.appendText(toDisplay + "\n");
        });
    }



    /**
     * Handler method for Compile & Run button.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCompileRun(ActionEvent event) throws InterruptedException {
        // run the program if compilation was successful
        if (this.compileTab(event)) {

            String fullpath = this.savedPaths.get(getSelectedTab()).replace(".java", "");
            String classname = fullpath.split(File.separator)[fullpath.split(File.separator).length - 1];
            String classpath = fullpath.replace(File.separator + classname, "");

            // new process builder for running with java interpreter
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("java", "-cp", classpath, classname);

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
