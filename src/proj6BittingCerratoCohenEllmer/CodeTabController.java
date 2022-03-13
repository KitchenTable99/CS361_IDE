package proj6BittingCerratoCohenEllmer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

public class CodeTabController {

    @FXML private TabPane tabPane;
    // list of saved tabs and their content
    private final HashMap<Tab,String> savedContents = new HashMap<>();
    // list of saved tabs and their saving path
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
     * Handles menu bar item New. Creates a new tab and adds it to the tabPane.
     */
    public void makeNewTab() {
        // calls helper method for untitled tabName generation
        String newTabName = getNextAvailableUntitled();

        // creates tab and sets close behavior
        Tab newTab = new Tab(newTabName);
        newTab.setOnCloseRequest(closeEvent -> {
            tabPane.getSelectionModel().select(newTab);
            closeSelectedTab(SaveReason.CLOSING);
            closeEvent.consume();
        });

        // installs toolTip
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
     * Asks user to choose a file. Acceptable extentions are .java, .txt, .fxml, and .css
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
     * @return whether the save was successful
     */
    public boolean saveCurrentTab() {
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
            return saveCurrentTabAs();
        }

    }

    /**
     * Handles menu bar item Save as....  a dialog appears in which the user is asked for
     * to save a file with four permitted extensions: .java, .txt, .fxml, and .css.
     *
     * @return whether the save was successful
     */
    public boolean saveCurrentTabAs() {
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
     * Prompts the user to save if tab is dirty. If the user chooses to save the changes,
     * the changes are saved and the tab is closed. If the tab is clean or the user
     * chooses to save the dirty tab, the tab is closed.
     *
     * @param reason the reason that the closeTab method is being invoked
     *
     * @return Optional the Optional object returned by dialog.showAndWait().
     *                  returns null if tab text is already saved.
     */
    public Optional<ButtonType> closeSelectedTab(SaveReason reason) {
        // If selectedTab is unsaved, opens dialog to ask user whether they would like to save
        if(selectedTabIsDirty()) {
            String fileName = getSelectedTab().getText();
            Dialog<ButtonType> saveDialog = dialogHelper.getSavingDialog(fileName, reason);

            Optional<ButtonType> result  = saveDialog.showAndWait();
            // save if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                saveCurrentTab();
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
        savedContents.remove(getSelectedTab());
        savedPaths.remove(getSelectedTab());
        tabPane.getTabs().remove(getSelectedTab());
        return Optional.empty();
    }

    /**
     * When exit item of the menu bar is clicked, the application quits if all tabs in
     * the tabPane are closed properly. Attempts to close all of the tabs.
     */
    public void closeAllTabs() {
        // TODO fix bug when clicking red 'X' with no tabs open
        tabPane.getSelectionModel().selectLast();
        while (tabPane.getTabs().size() > 0) {
            // try close the currently selected tab
            Optional<ButtonType> result = closeSelectedTab(SaveReason.EXITING);
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
     * helper function to get the text box in the selected tab
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
     * If the tab is dirty, asks user to save. If user chooses to save, the changes are
     * saved and the tab is compiled. If user chooses not to save, the currently saved
     * version of the file is compiled (the unsaved changes are ignored). If the user
     * cancels the dialog, no compilation is performed.
     *
     */
    // TODO update the javadoc and rename the method. This doesn't actually compile anything
    public ProcessBuilder compileTab() {
        // guard against the empty tab and no save prior to compilation
        if (!saveBeforeCompile() || getSelectedTextBox().getText().equals("")) {
            return null;
        }

        // new process builder for compilation
        String filepath = savedPaths.get(getSelectedTab());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("javac", filepath);

        return processBuilder;
    }

    private boolean saveBeforeCompile() {
        // TODO: rename this method. if the tab is not saved, BUT HAS BEEN BEFORE, this returns true. That behavior seems misleading
        // If selected tab is dirty, calls handleSave
        if (selectedTabIsDirty()) {
            // Creates new dialog
            Dialog<ButtonType> saveDialog = dialogHelper.getSavingDialog(getSelectedTab().getText(), SaveReason.COMPILING);
            Optional<ButtonType> result = saveDialog.showAndWait();
            // call handleSave() if user chooses YES
            if (result.isPresent() && result.get() == ButtonType.YES) {
                boolean saved = saveCurrentTab();
                if (saved) {
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
                    String body = "Current tab has not been saved. Please save before compiling.";
                    dialogHelper.getAlert("Unable to Compile", body).show();
                    return false;
                }
            }
        } else {
            return true;
        }
        return false; // TODO figure out how this ever gets called. added to allow compilation
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
     * Helper method that generates a string that is the lowest "Untitled-x" available.
     *
     * @return String name for newTab
     */
    private String getNextAvailableUntitled() {
        // TODO refactor this method
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
}
