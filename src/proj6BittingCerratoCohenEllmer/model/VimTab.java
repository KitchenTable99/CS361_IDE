package proj6BittingCerratoCohenEllmer.model;

import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.HashSet;
import java.util.Set;

public class VimTab extends Tab {

    private boolean inVIMCommandMode;
    private String vimCommands = "";
    private String yankRegister = "";

    public VimTab(String tabName) {
        super(tabName);

        EventHandler<KeyEvent> vimHandler = new EventHandler<>() {
            /**
             * Route VIM key press events to implemented methods
             *
             * @param key A KeyEvent object that gives information about a Key Stroke
             */
            @Override
            public void handle(KeyEvent key) {
                // don't do anything if keyboard shortcut pressed
                if (key.isShortcutDown()) {
                    return;
                }
                // set up set for keys that should do nothing in command mode
                Set<KeyCode> noActionKeys = new HashSet<>();
                noActionKeys.add(KeyCode.LEFT);
                noActionKeys.add(KeyCode.RIGHT);
                noActionKeys.add(KeyCode.DOWN);
                noActionKeys.add(KeyCode.UP);
                noActionKeys.add(KeyCode.SHIFT);
                noActionKeys.add(KeyCode.COLON); // this will change at some point down the road

                KeyCode eventKey = key.getCode();
                if (!inVIMCommandMode && eventKey.equals(KeyCode.ESCAPE)) {
                    inVIMCommandMode = true;
                } else if (inVIMCommandMode && eventKey.equals(KeyCode.ESCAPE)) {
                    vimCommands = "";
                    key.consume();
                } else if (inVIMCommandMode && !noActionKeys.contains(eventKey)) {
                    // must handle all key events to suppress keystroke in codeArea
                    // Only read key released to prevent duplicated commands
                    if (key.getEventType().equals(KeyEvent.KEY_RELEASED)) {
                        vimCommands += key.getText();
                        System.out.println("Vim Commands: " + vimCommands);
                        dispatchVimCommand();
                    }
                    key.consume();
                }
            }
        };

        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        codeArea.addEventFilter(KeyEvent.ANY, vimHandler);
        setContent(new VirtualizedScrollPane<>(codeArea));
    }

    private void dispatchVimCommand() {
        Character starting_char = vimCommands.charAt(0);
        if (starting_char.equals('i')) {
            handleLowerCaseI();
        } else if (starting_char.equals('I')) {
            handleUpperCaseI();
        } else if (starting_char.equals('a')) {
            handleLowerCaseA();
        } else if (starting_char.equals('A')) {
            handleUpperCaseA();
        } else if (starting_char.equals('h')) {
            handleH();
        } else if (starting_char.equals('j')) {
            handleJ();
        } else if (starting_char.equals('k')) {
            handleK();
        } else if (starting_char.equals('l')) {
            handleL();
        } else if (starting_char.equals('r')) {
            handleR();
        } else if (starting_char.equals('x')) {
            handleX();
        } else if (starting_char.equals('s')) {
            handleS();
        } else if (starting_char.equals('d')) {
            handleD();
        } else if (starting_char.equals('y')) {
            handleY();
        } else if (starting_char.equals('p')) {
            handleLowerCaseP();
        } else if (starting_char.equals('P')) {
            handleUpperCaseP();
        }
    }

    private void handleUpperCaseP() {
        if (notReadyForP()) {
            return;
        }
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        codeArea.insertText(caretPos, yankRegister);

        vimCommands = "";
    }

    private void handleLowerCaseP() {
        if (notReadyForP()) {
            return;
        }
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        codeArea.insertText(caretPos + 1, yankRegister);

        vimCommands = "";
    }

    private boolean notReadyForP() {
        return yankRegister.equals("");
    }

    private void handleY() {
        // TODO only implement the yy functionality of y
    }

    private void handleD() {
        // TODO only implement the dd functionality of d
    }

    private void handleS() {
        handleX();
        inVIMCommandMode = false;
    }

    private void handleX() {
        // no need to call an isReady method as 'x' is a single character command.
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        if (caretPos == 0) {
            caretPos = 1;
        }
        yankRegister = codeArea.getText(caretPos - 1, caretPos);
        codeArea.deleteText(caretPos - 1, caretPos);
        vimCommands = "";
    }

    private void handleR() {
        if (!isReadyForR()) {
            return;
        }
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        if (caretPos == 0) {
            caretPos = 1;
        }
        String replaceString = Character.toString(vimCommands.charAt(1));
        codeArea.replaceText(caretPos - 1, caretPos, replaceString);

        vimCommands = "";
    }

    private boolean isReadyForR() {
        return vimCommands.length() == 2;
    }

    private void handleL() {

    }

    private void handleK() {

    }

    private void handleJ() {

    }

    private void handleH() {

    }

    private void handleUpperCaseA() {

    }

    private void handleLowerCaseA() {

    }

    private void handleUpperCaseI() {

    }

    private void handleLowerCaseI() {
    }

    private CodeArea getCodeArea() {
        return ((VirtualizedScrollPane<CodeArea>) getContent()).getContent();
    }

}

