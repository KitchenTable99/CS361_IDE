package proj6BittingCerratoCohenEllmer.model;

public class SaveInformationShuttle {

    private boolean saveSuccessful;

    public SaveInformationShuttle() {
        saveSuccessful = false;
    }

    public void setSuccessfulSave() {
        saveSuccessful = true;
    }

    public boolean isSuccessful() {
        return saveSuccessful;
    }
}
