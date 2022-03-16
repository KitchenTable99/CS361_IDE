package proj6BittingCerratoCohenEllmer.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import proj6BittingCerratoCohenEllmer.model.JavaCodeArea;
import proj6BittingCerratoCohenEllmer.model.SaveFailureException;
import proj6BittingCerratoCohenEllmer.model.SaveInformationShuttle;
import proj6BittingCerratoCohenEllmer.view.DialogHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class CodeTabController {

    @FXML private TabPane tabPane;
    // keep track of the content and where it was saved
    private final HashMap<Tab,String> savedContents = new HashMap<>();
    private final HashMap<Tab,String> savedPaths = new HashMap<>();

    private final DialogHelper dialogHelper = new DialogHelper();

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
    * Gets the currently selected tab in tabPane
    *
    * @return the selected tab
    */
    public Tab getSelectedTab () {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Creates a new tab and adds it to the tabPane. The name of the tab will be the
     * lowest "Untitled" available.
     *
     * @see #getNextAvailableUntitled
     */
    public void makeNewTab() {
        // calls helper method for untitled tabName generation
        String newTabName = getNextAvailableUntitled();

        // creates tab and sets close behavior
        Tab newTab = new Tab(newTabName);
        newTab.setOnCloseRequest(closeEvent -> {
            tabPane.getSelectionModel().select(newTab);
            closeSelectedTab(SaveReason.CLOSING, new SaveInformationShuttle());
            closeEvent.consume();
        });

        // place toolTip
        Tooltip tabToolTip = new Tooltip(newTab.getText());
        newTab.setTooltip(tabToolTip);

        // create a code area
        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        newTab.setContent(new VirtualizedScrollPane<>(codeArea));

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
            } catch ( IOException e ) {
                throw new SaveFailureException(e.getMessage(), e.getCause());
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
        if(selectedTabIsDirty()) {
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
     * Ensures that the tab has been saved and prepares a ProcessBuilder to actually
     * compile the selected tab.
     *
     * @see #readyForCompile
     * @return ProcessBuilder that will compile the current tab
     */
    public ProcessBuilder prepareCompileProcess() {
        // guard against the empty tab and no save prior to compilation
        if (!readyForCompile()) {
            return null;
        }

        // new process builder for compilation
        String filepath = savedPaths.get(getSelectedTab());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("javac", filepath);

        return processBuilder;
    }

    /**
     * Prepare a ProcessBuilder for running the selected tab
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
        // TODO: remove the return for this method and pass the shuttle up the chain
        // If selected tab is dirty, calls handleSave
        if (selectedTabIsDirty()) {
            // Creates new dialog
            String tabText = getSelectedTab().getText();
            Dialog<ButtonType> saveDialog =
                    dialogHelper.getSavingDialog(tabText, SaveReason.COMPILING);
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
                    String body = "Compilation was canceled because you " +
                            "aborted saving the file";
                    dialogHelper.getAlert("Compilation Canceled", body).show();
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
                            "Please save before compiling.";
                    dialogHelper.getAlert("Unable to Compile", body).show();
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
        for(Tab tab : tabs){
            String tabTitle = tab.getText();
            if(tabTitle.startsWith("Untitled")) {
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
