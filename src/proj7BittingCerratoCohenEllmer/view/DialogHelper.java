/*
 * File: DialogHelper.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 5
 * Date: March 7
 */

package proj7BittingCerratoCohenEllmer.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import proj7BittingCerratoCohenEllmer.controllers.SaveReason;

import java.util.HashMap;

public class DialogHelper {

    private final HashMap<SaveReason, String> reasonMap;

    public DialogHelper() {
        reasonMap = new HashMap<>();
        reasonMap.put(SaveReason.CLOSING, "closing it?");
        reasonMap.put(SaveReason.EXITING, "exiting?");
        reasonMap.put(SaveReason.COMPILING, "compiling?");
    }

    public Dialog<ButtonType> getAboutDialog() {
        // create a new dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setContentText("This is a code editor! \n\n "
                + "Authors: Caleb Bitting, Matt Cerrato, Erik Cohen, and Ian Ellmer");
        // add a close button so that dialog closing rule is fulfilled
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        return dialog;
    }

    public Alert getAlert(String title, String alertBody) {
        Alert alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setHeaderText(title);
        alertBox.setContentText(alertBody);

        return alertBox;
    }

    public Dialog<ButtonType> getSavingDialog(String fileName, SaveReason reason) {
        String promptText = String.format("Do you want to save %s before ", fileName);
        promptText += reasonMap.get(reason);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setContentText(promptText);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        return dialog;
    }

}
