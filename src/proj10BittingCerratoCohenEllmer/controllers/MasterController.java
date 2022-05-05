/*
 * File: MasterController.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj10BittingCerratoCohenEllmer.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import proj10BittingCerratoCohenEllmer.bantam.PrettyPrinterVisitor;
import proj10BittingCerratoCohenEllmer.bantam.ast.Program;
import proj10BittingCerratoCohenEllmer.bantam.parser.Parser;
import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;
import proj10BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
import proj10BittingCerratoCohenEllmer.model.SaveFailureException;
import proj10BittingCerratoCohenEllmer.model.SaveInformationShuttle;
import proj10BittingCerratoCohenEllmer.model.VimTab;
import proj10BittingCerratoCohenEllmer.view.DialogHelper;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


/**
 * MasterController class contains handler methods for buttons and menu items.
 */
public class MasterController {

    // Class DialogHelper handling all dialog instantiation
    private final DialogHelper dialogHelper = new DialogHelper();
    private final SimpleBooleanProperty isThreadActive = new SimpleBooleanProperty(false);
    // keep track of the content and where it was saved
    private final HashMap<Tab, String> savedContents = new HashMap<>();
    private final HashMap<Tab, String> savedPaths = new HashMap<>();
    @FXML
    private StyleClassedTextArea console;
    @FXML
    private Button checkButton, stopButton, pprintButton;
    @FXML
    private MenuItem undoMI, redoMI;
    @FXML
    private MenuItem selectAllMI, cutMI, copyMI, pasteMI;
    @FXML
    private MenuItem saveMI, saveAsMI, closeMI;
    @FXML
    private TabPane tabPane;
    private Thread processThread = null;

    /**
     * Exposes the exit handler's functionality to outside classes.
     */
    public void handleWindowExit() {
        handleExit(new ActionEvent());
    }


    /**
     * Sets up listeners to disable/enable menu items +
     * connects existing close boxes to the created close MenuItems
     */
    @FXML
    private void initialize() {

        makeNewTab();

        // disable appropriate menu items when no tabs are open
        // TODO: delegate disabling to some helper class
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
        checkButton.disableProperty().bind(Bindings.or(isThreadActive, noTabs()));
        stopButton.disableProperty().bind(Bindings.or(isThreadActive.not(), noTabs()));
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

    @FXML
    private void handlePPrint(ActionEvent event) {
        ErrorHandler bantamErrorHandler = new ErrorHandler();
        Parser bantamParser = new Parser(bantamErrorHandler);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String filename = savedPaths.get(selectedTab);
        PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
        Program currentProgram = null;
        try {
            currentProgram = bantamParser.parse(filename);
        } catch (CompilationException e) {
            System.out.println(e);
        }
        System.out.println(currentProgram);
        String newConsole = ppv.generateOutputString(currentProgram);
        getSelectedTextBox().replaceText(newConsole);
    }

    /**
     * Creates a new tab.
     */
    @FXML
    private void handleNew() {
        makeNewTab();
    }

    /**
     * Handles menu bar item Open.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleOpen(ActionEvent event) {
        openFile();
    }


    /**
     * Handles menu bar item Close
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        closeSelectedTab(SaveReason.CLOSING, new SaveInformationShuttle());
    }

    /**
     * Handler method for menu bar item Exit.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        closeAllTabs();
    }


    /**
     * Handler method for menu bar item Save.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        SaveInformationShuttle saveShuttle = new SaveInformationShuttle();
        try {
            saveCurrentTab(saveShuttle);
        } catch (SaveFailureException e) {
            dialogHelper.getAlert("Unable to save file", e.getMessage()).show();
        }
    }

    /**
     * Handler method for menu bar item Save. Behaves like Save as... if the text
     * has never been saved before. Otherwise, save the text to its corresponding
     * text file.
     *
     * @throws SaveFailureException if the file cannot be saved
     */
    public void saveCurrentTab(SaveInformationShuttle shuttle)
            throws SaveFailureException {
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

                // store shuttle information
                shuttle.setSuccessfulSave();
            } catch (IOException e) {
                throw new SaveFailureException(e.getMessage(), e.getCause());
            }
        }
        // if text in selected tab was not loaded from a file nor ever saved to a file
        else {
            saveCurrentTabAs(shuttle);
        }
    }

    /**
     * Gets the currently selected tab in tabPane
     *
     * @return the selected tab
     */
    public Tab getSelectedTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Gets the text box in the selected tab
     *
     * @return CodeArea the text box in the selected tab
     */
    public CodeArea getSelectedTextBox() {
        Tab currentTab = getSelectedTab();
        VirtualizedScrollPane scrollPane;
        scrollPane = (VirtualizedScrollPane) currentTab.getContent();
        return (CodeArea) scrollPane.getContent();
    }

    /**
     * Handles menu bar item Save as....  a dialog appears in which the user is asked for
     * to save a file with four permitted extensions: .java, .txt, .fxml, and .css.
     *
     * @throws SaveFailureException if the file cannot be saved
     */
    public void saveCurrentTabAs(SaveInformationShuttle shuttle)
            throws SaveFailureException {
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
                fw.write(getSelectedTextBox().getText());
                fw.close();

                // update savedContents field and tab text
                savedContents.put(getSelectedTab(), getSelectedTextBox().getText());
                savedPaths.put(getSelectedTab(), fileToSave.getPath());
                getSelectedTab().setText(fileToSave.getName());
                getSelectedTab().getTooltip().setText(fileToSave.getPath());

                // store information in the shuttle object
                shuttle.setSuccessfulSave();
            } catch (IOException e) {
                throw new SaveFailureException(e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * Handles menu bar item Save as....
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSaveAs(ActionEvent event) {
        SaveInformationShuttle saveShuttle = new SaveInformationShuttle();
        try {
            saveCurrentTabAs(saveShuttle);
        } catch (SaveFailureException e) {
            dialogHelper.getAlert("Unable to save file", e.getMessage()).show();
        }
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

    @FXML
    private void handleCheck(ActionEvent event) {
        boolean saved = readyForCompile();
        if (!saved) {
            return;
        }
        ErrorHandler bantamErrorHandler = new ErrorHandler();
        Parser bantamParser = new Parser(bantamErrorHandler);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String filename = savedPaths.get(selectedTab);
        try {
            Program currentProgram = bantamParser.parse(filename);
            Platform.runLater(() -> {
                console.appendText("\nFinished checking code. No errors!");
            });
        } catch (CompilationException e) {
            List<Error> errors = bantamErrorHandler.getErrorList();
            StringBuilder toDisplay = new StringBuilder("Errors occurred while checking code:\n");
            for (Error error : errors) {
                toDisplay.append("\t").append(error.toString()).append("\n");
            }
            Platform.runLater(() -> {
                console.appendText(toDisplay.toString());
            });
        }

    }

    /**
     * Handles the stop button. Forcefully stops the thread and resets the processThread
     * to null.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     * @deprecated
     */
    @FXML
    private void handleStop(ActionEvent event) {
        if (processThread != null) {
            processThread.stop(); // TODO: do this in a non-depricated manner
            isThreadActive.set(false);
            processThread = null;
        }
    }

    /**
     * Returns a true BooleanBinding if there are no more tabs and a false one if there
     * is at least one tab.
     *
     * @return a BooleanBinding demonstrating if there are no more tabs
     */
    public BooleanBinding noTabs() {
        return Bindings.isEmpty(tabPane.getTabs());
    }

    /**
     * Creates a new tab and adds it to the tabPane. The name of the tab will be the
     * lowest "Untitled" available.
     *
     * @see #getNextAvailableUntitled
     */
    private void makeNewTab() {
        // calls helper method for untitled tabName generation
        String newTabName = getNextAvailableUntitled();

        // creates tab and sets close behavior
        VimTab newTab = new VimTab(newTabName);
        newTab.setOnCloseRequest(closeEvent -> {
            tabPane.getSelectionModel().select(newTab);
            closeSelectedTab(SaveReason.CLOSING, new SaveInformationShuttle());
            closeEvent.consume();
        });

        // place toolTip
        Tooltip tabToolTip = new Tooltip(newTab.getText());
        newTab.setTooltip(tabToolTip);

        // add new tab to the tabPane and sets as topmost
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().selectLast();
    }

    /**
     * Asks user to choose a file. Acceptable extensions are .java, .txt, .fxml, and .css
     * Creates a new tab and dumps the contents of the selected file into the editor.
     */
    public void openFile() {
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
                makeNewTab();
                // set text/name of the tab to the filename
                getSelectedTab().setText(selectedFile.getName());
                getSelectedTextBox().replaceText(content);
                // update savedContents field
                savedContents.put(getSelectedTab(), content);
                savedPaths.put(getSelectedTab(), selectedFile.getPath());
                getSelectedTab().getTooltip().setText(selectedFile.getPath());
            } catch (IOException e) {
                dialogHelper.getAlert("File Opening Error", e.getMessage()).show();
            }
        }
    }

    /**
     * Prompts the user to save if tab is dirty. If the user chooses to save the changes,
     * the changes are saved and the tab is closed. If the tab is clean or the user
     * chooses not to save the dirty tab, the tab is summarily closed.
     *
     * @param reason  the reason that the closeTab method is being invoked
     * @param shuttle the shuttle object used to pass out the save status and button type
     *                if the tab is closed, the shuttle's buttonType field will be empty
     */
    public void closeSelectedTab(SaveReason reason, SaveInformationShuttle shuttle) {
        // If selectedTab is dirty, opens dialog to ask user if they would like to save
        if (selectedTabIsDirty()) {
            String fileName = getSelectedTab().getText();
            Dialog<ButtonType> dialog = dialogHelper.getSavingDialog(fileName, reason);

            Optional<ButtonType> result = dialog.showAndWait();
            shuttle.setButtonType(result);
            // save if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                try {
                    saveCurrentTab(shuttle);
                } catch (SaveFailureException e) {
                    dialogHelper.getAlert("Unable to save file", e.getMessage()).show();
                }
                // don't close the tab if it is still dirty
                if (selectedTabIsDirty()) {
                    return;
                }
            }
            // quit the dialog and keep selected tab if user chooses CANCEL
            else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        // remove tab from tabPane if text is saved or user chooses NO
        savedContents.remove(getSelectedTab());
        savedPaths.remove(getSelectedTab());
        tabPane.getTabs().remove(getSelectedTab());
        shuttle.setButtonType(Optional.empty());
    }

    /**
     * Attempts to close all the tabs. If the user clicks cancel in any tab during the
     * closing process, this method stops.
     */
    public void closeAllTabs() {
        // TODO fix bug when clicking red 'X' with no tabs open -- NOT A BUG IN OUR CODE
        tabPane.getSelectionModel().selectLast();
        while (tabPane.getTabs().size() > 0) {
            // try close the currently selected tab
            SaveInformationShuttle shuttle = new SaveInformationShuttle();
            closeSelectedTab(SaveReason.EXITING, shuttle);
            // if the user chooses Cancel at any time, then the exiting is canceled,
            // and the application stays running.
            Optional<ButtonType> result = shuttle.getButtonType();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        // exit if all tabs are closed
        System.exit(0);
    }

    /**
     * Ensures that the tab has been saved and prepares a ProcessBuilder to actually
     * compile the selected tab. Sets the field of the passed shuttle to the new
     * ProcessBuilder
     *
     * @param shuttle the object used to store the ProcessBuilder
     * @see #readyForCompile
     */
    public void prepareCompileProcess(ProcessBuilderShuttle shuttle) {
        // guard against the empty tab and no save prior to compilation
        if (!readyForCompile()) {
            return;
        }

        // new process builder for compilation
        String filepath = savedPaths.get(getSelectedTab());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("javac", filepath);
        shuttle.setProcessBuilder(processBuilder);
    }

    /**
     * Prepare a ProcessBuilder for running the selected tab
     *
     * @return ProcessBuilder that will run the current tab
     */
    public ProcessBuilder prepareRunningProcess() {
        String fullpath = savedPaths.get(getSelectedTab()).replace(".java", "");
        int splitIndex = fullpath.split(File.separator).length - 1;
        String classname = fullpath.split(File.separator)[splitIndex];
        String classpath = fullpath.replace(File.separator + classname, "");

        // new process builder for running with java interpreter
        ProcessBuilder processBuilder = new ProcessBuilder();
        return processBuilder.command("java", "-cp", classpath, classname);
    }

    /**
     * Checks to see if the current tab is dirty. If it is, asks user if they want to
     * save. If they do, saves the tab and returns true. If the tab is dirty, the user
     * doesn't want to save, returns whether the file has been saved before. If user
     * cancels or does not want to save, returns false.
     *
     * @return whether the selected tab is ready for compilation
     */
    private boolean readyForCompile() {
        // If selected tab is dirty, calls handleSave
        if (selectedTabIsDirty()) {
            // Creates new dialog
            String tabText = getSelectedTab().getText();
            Dialog<ButtonType> saveDialog =
                    dialogHelper.getSavingDialog(tabText, SaveReason.CHECKING);
            Optional<ButtonType> result = saveDialog.showAndWait();

            // call handleSave() if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                SaveInformationShuttle shuttle = new SaveInformationShuttle();
                try {
                    saveCurrentTab(shuttle);
                } catch (SaveFailureException e) {
                    dialogHelper.getAlert("Unable to save file", e.getMessage()).show();
                }
                if (shuttle.isSaveSuccessful()) {
                    return true;
                } else {
                    String body = "Checking was canceled because you " +
                            "aborted saving the file";
                    dialogHelper.getAlert("Checking Canceled", body).show();
                    return false;
                }
            }
            // No compilation if user chooses CANCEL
            else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return false;
            } else if (result.isPresent() && result.get() == ButtonType.NO) {
                // if user chooses NO and check if current tab has been saved before
                if (savedPaths.containsKey(getSelectedTab())) {
                    return true;
                } else {
                    // make an alert box
                    String body = "Current tab has not been saved." +
                            "Please save before checking.";
                    dialogHelper.getAlert("Unable to Check", body).show();
                    return false;
                }
            }
        } else {
            return true;
        }
        return false;
    }


    /**
     * Helper method that checks if the text in the selected tab is saved.
     *
     * @return boolean whether the text in the selected tab is dirty (unsaved changes).
     */
    private boolean selectedTabIsDirty() {
        // Gets current contents of tab and its hashed contents (Null if unsaved)
        String currentContents = getSelectedTextBox().getText();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String savedContent = savedContents.get(selectedTab);

        // If no saved contents, and tab is empty, contents not dirty
        if (savedContent == null && currentContents.equals("")) {
            return false;
        }
        // If no saved contents, but code area not empty, contents are dirty
        else if (savedContent == null) {
            return true;
        }
        // Otherwise, returns false (not dirty) if contents equal, or true if they aren't
        else {
            return !savedContent.equals(currentContents);
        }

    }

    /**
     * Helper method that generates a string that is the lowest "Untitled-x" available.
     * If some file is literally named "Untitled" or "Untitled-XX" where the XX is some
     * integer, this method will treat that tab as if it were created as a default name.
     * If the open tabs are all loaded from files named "Untitled," "Foo," "Bar,"
     * "Untitled-1," and "Baz" this will return "Untitled-2"
     *
     * @return String name for newTab
     */
    private String getNextAvailableUntitled() {
        ObservableList<Tab> tabs = tabPane.getTabs();
        HashSet<Integer> untitledSet = new HashSet<>();

        // iterate over each tab to check if it is an "Untitled" tab
        for (Tab tab : tabs) {
            String tabTitle = tab.getText();
            if (tabTitle.startsWith("Untitled")) {
                String[] splitTitle = tabTitle.split("Untitled");
                if (splitTitle.length == 0) {
                    // this only happens if the title of the tab is "Untitled"
                    untitledSet.add(0);
                } else {
                    // the array is of the form ["Untitled", "-XX"] where XX is the
                    // value we want to avoid duplicating
                    int negativeUntitled = Integer.parseInt(splitTitle[1]);
                    untitledSet.add(-1 * negativeUntitled);
                }
            }
        }

        // return the lowest untitled string
        if (!untitledSet.contains(0)) {
            return "Untitled";
        }

        for (int i = 1; i < tabs.size(); i++) {
            if (!untitledSet.contains(i)) {
                return "Untitled-" + i;
            }
        }

        return "Untitled-" + tabs.size();
    }
}
