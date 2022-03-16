package proj6BittingCerratoCohenEllmer.model;

import javafx.scene.control.ButtonType;

import java.util.Optional;


public class SaveInformationShuttle {

    private boolean saveSuccessful;
    private Optional<ButtonType> buttonType;

    public SaveInformationShuttle() {
        saveSuccessful = false;
        buttonType = Optional.empty();
    }

    public void setSuccessfulSave() {
        saveSuccessful = true;
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    public void setButtonType(Optional<ButtonType> bt) {
        buttonType = bt;
    }

    public Optional<ButtonType> getButtonType() {
        return buttonType;
    }
}
