/*
 * File: VimTab.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj6BittingCerratoCohenEllmer.model;

import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.HashSet;
import java.util.Set;

/**
 * VimTab class implements a basic set of Vim commands
 * 
 */
public class VimTab extends Tab {

    // VIM command vs insert mode
    private boolean inVIMCommandMode;
    // set of character used in current command
    private String vimCommands = "";
    // holds a clipboard like register
    private String yankRegister = "";
    //used for keeping place when traversing text area rows 
    private int currentColumn = 0;

    /**
     * Constructs a new VimTab with a JavaCodeArea.
     * 
     * @param tabName String to name the tab
     */
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

                // Determine VIM mode
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

        // set tab content
        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        codeArea.addEventFilter(KeyEvent.ANY, vimHandler);
        setContent(new VirtualizedScrollPane<>(codeArea));
    }

    /**
     * routes the current vimCommand string to the appropriate
     * handler method
     */
    private void dispatchVimCommand() {
        if(vimCommands.length() < 1){
            return;
        }
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

    /**
     * "Put" VIM Command
     * pastes the yank register at the current Caret Position
     */
    private void handleUpperCaseP() {
        if (notReadyForP()) {
            return;
        }
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        codeArea.insertText(caretPos, yankRegister);

        vimCommands = "";
    }

    /**
     * "put" VIM Command
     * pastes the yank register after the current Caret Position
     */
    private void handleLowerCaseP() {
        if (notReadyForP()) {
            return;
        }
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        codeArea.insertText(caretPos + 1, yankRegister);

        vimCommands = "";
    }

    /**
     * Checks if the put/Put command makes sense to use.
     * 
     * @return boolean representing whether "put/Put" can be used
     */
    private boolean notReadyForP() {
        return yankRegister.equals("");
    }

    /**
     * Only set to handle the yy Vim Command
     * This yanks the current line from "\n" to "\n"
     */
    private void handleY() {
        if( isReadyForyy()){
            yank(getYankStart() , getYankEnd());
            vimCommands = "";
        }
    }

    /**
     * Checks if the vimCommand is "yy"
     * @return boolean true if vimCommand is "yy"
     */
    private boolean isReadyForyy() {
        return "yy".equals(vimCommands);
    }

    /**
     * Only set to handle the dd Vim Command
     * This yanks the current line from "\n" to "\n"
     * and then deletes the current line from "\n" to "\n"
     */
    private void handleD() {
        if( isReadyFordd()){
            CodeArea codeArea = getCodeArea();
            String content = codeArea.getContent().getText();
            yank(getYankStart() , getYankEnd());
            String preYank = content.substring(0, getYankStart());
            String postYank = content.substring(getYankEnd());
            codeArea.replaceText(preYank+postYank);
            vimCommands = "";
        }
    }

    /**
     * Checks if the vimCommand is "dd"
     * @return boolean true if vimCommand is "dd"
     */
    private boolean isReadyFordd() {
        return "dd".equals(vimCommands);
    }

    /**
     * Finds the start of the current line for yank
     * either "\n" or start of the text
     * @return int position in code area
     */
    private int getYankStart(){
        CodeArea codeArea = getCodeArea();
        codeArea.getContent().getText();
        int startYank = getStartOfLine();
        if(startYank > 0){
            startYank--;
        }
        return startYank;
    }

    /**
     * Finds the end of the current line for yank
     * either "\n" or end of the text
     * @return int position in code area
     */
    private int getYankEnd(){
        CodeArea codeArea = getCodeArea();
        String content = codeArea.getContent().getText();
        int endYank = getEndOfLine();
        if(endYank < content.length()){
            endYank++;
        }
        return endYank;
    }

    /**
     * Yanks text from the code area
     * @param start an int position to start the yank
     * @param end an int position to end the yank
     */
    private void yank(int start, int end){
        CodeArea codeArea = getCodeArea();
        yankRegister = codeArea.getContent().getText().substring(start, end);
    }

    /**
     * yanks the current character and enters insert mode
     */
    private void handleS() {
        handleX();
        inVIMCommandMode = false;
    }

    /**
     * yanks the current character 
     */
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

    /**
     * replaces the current character with last character 
     * in VimCommands string
     */
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

    /**
     * checks if ready to replace the current character 
     * @return if VIMcommand length is 2 characters
     */
    private boolean isReadyForR() {
        return vimCommands.length() == 2;
    }

    //right
    private void handleL() {
        CodeArea codeArea = getCodeArea();
        int caretPos = codeArea.getCaretPosition();
        // move right if not the last character 
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
        if(notLastLine()){
            // Move to start of next line 
            codeArea.moveTo(getEndOfLine() + 1);
            // Move to correct collumn
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
        // if not first character move left
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

