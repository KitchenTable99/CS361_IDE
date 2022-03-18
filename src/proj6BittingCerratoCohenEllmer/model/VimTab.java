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
    private int currentColumn = 0;
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
                    updateColumnTracker();
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
        // is ready method == "yy" and should take \n + line + \n into the register
    }

    private void handleD() {
        // TODO only implement the dd functionality of d
        // is ready method == "yy" and should take \n + line + \n into the register and delete it
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
    //right
    private void handleL() {
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        if(caretPos < codeArea.getContent().getLength()){
            codeArea.moveTo(caretPos + 1);
        }
        vimCommands = "";
        updateColumnTracker();
    }

    //up
    private void handleK() {
        CodeArea codeArea = getCodeArea();
        // Move to previous line
        int previous_line = getStartOfLine() - 1;
        if(previous_line > 0){
            codeArea.moveTo(previous_line);
        }
        // Move to correct collumn
        int lineLength = getEndOfLine() - getStartOfLine();
        if(currentColumn < lineLength){
            codeArea.moveTo(getStartOfLine() + currentColumn);
        }else{
            codeArea.moveTo(getStartOfLine() + lineLength);
        }
        vimCommands = "";
    }

    //down
    private void handleJ() {
        CodeArea codeArea = getCodeArea();
        // Move to start of next line then add
        if(notLastLine()){
            codeArea.moveTo(getEndOfLine() + 1);
            int lineLength = getEndOfLine() - getStartOfLine();
            if(currentColumn < lineLength){
                codeArea.moveTo(codeArea.getCaretPosition() + currentColumn);
            }else{
                codeArea.moveTo(codeArea.getCaretPosition() + lineLength);
            }
        }
        vimCommands = "";
    }

    private boolean notLastLine(){
        CodeArea codeArea = getCodeArea();
        // Move to start of next line then add
        if(getEndOfLine() + 1 < codeArea.getContent().getLength()){
            return true;
        }
        return false;
    }


    //left
    private void handleH() {
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        if(caretPos > 0){
            codeArea.moveTo(caretPos - 1);
        }
        vimCommands = "";
        updateColumnTracker();
    }

    private void handleUpperCaseA() {
        if(vimCommands.startsWith("A")){
            CodeArea codeArea = getCodeArea();
            codeArea.moveTo(getEndOfLine());
            inVIMCommandMode = false;
        }
    }

    private void handleLowerCaseA() {
        if(vimCommands.startsWith("a")){
            CodeArea codeArea = getCodeArea();
            int caretPos = codeArea.getCaretPosition();
            String content = codeArea.getContent().getText();
            if(caretPos < content.length()
                && ! "\n".equals(content.substring(caretPos, caretPos + 1))){
                caretPos++;
            }
            codeArea.moveTo(caretPos);
            inVIMCommandMode = false;
        }
    }

    private void handleUpperCaseI() {
        if(vimCommands.startsWith("I")){
            CodeArea codeArea = getCodeArea();
            codeArea.moveTo(getStartOfLine());
            inVIMCommandMode = false;
        }
    }

    private void handleLowerCaseI() {
        if(vimCommands.startsWith("i")){
            CodeArea codeArea = getCodeArea();
            int caretPos = codeArea.getCaretPosition();
            String content = codeArea.getContent().getText();
            if(caretPos > 0 && ! "\n".equals(content.substring(caretPos - 1, caretPos))){
                caretPos--;
            }
            codeArea.moveTo(caretPos);
            inVIMCommandMode = false;
        }
    }

    private int getEndOfLine(){
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        String content = codeArea.getContent().getText();
        while(caretPos < content.length()
            && ! "\n".equals(content.substring(caretPos, caretPos + 1))){
            caretPos++;
        }
        return caretPos;
    }

    private int getStartOfLine(){
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        String content = codeArea.getContent().getText();
        while(caretPos > 0 && ! "\n".equals(content.substring(caretPos - 1, caretPos))){
            caretPos--;
        }
        return caretPos;
    }


    private CodeArea getCodeArea() {
        return ((VirtualizedScrollPane<CodeArea>) getContent()).getContent();
    }

    private void updateColumnTracker(){
        currentColumn = getCodeArea().getCaretColumn();
    }

}

