package proj5BittingLoverudeXu;

import javafx.scene.control.*;
import javafx.scene.control.Dialog;

public class
DialogHelper {

    public Dialog<ButtonType> getAboutDialog() {
        // create a new dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setContentText("This is a code editor! \n\n "
                + "Authors: Caleb Bitting, Jasper Loverude, and Andy Xu");
        // add a close button so that dialog closing rule is fulfilled
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        return dialog;
    }

    public Alert getAlert(String title, String alertBody) {
        // TODO: overload method and use an alert as the second parameter?
        Alert alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setHeaderText(title);
        alertBox.setContentText(alertBody);

        return alertBox;
    }

    public Dialog<ButtonType> getSavingDialog(String fileName, SaveReason reason) {
        Dialog<ButtonType> dialog = new Dialog<>();
        // TODO: pass a tab instead of a string to extract the fileName?

        String promptText;
        switch (reason) {
            case CLOSING:
                promptText = String.format("Do you want to save %s before closing it?",
                                            fileName);
                break;

            case EXITING:
                promptText = String.format("Do you want to save %s before exiting?",
                                            fileName);
                break;

            case COMPILING:
                promptText = String.format("Do you want to save %s before compiling?",
                                            fileName);
                break;

            default:
                promptText = "How did we get here?";
                break;
        }

        dialog.setContentText(promptText);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        return dialog;
    }

}
