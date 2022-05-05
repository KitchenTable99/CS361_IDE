/*
 * File: VimTab.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj10BittingCerratoCohenEllmer.model;

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
 */
public class VimTab extends Tab {

    // VIM command vs insert mode
    private boolean inVIMCommandMode;
    // set of characters used in current command
    private String vimCommands = "";
    // holds a clipboard like register
    private String yankRegister = "";
    //used for keeping track of caret position when traversing text area rows
    private int currentColumn = 0;
    private final CodeArea codeArea;

    /**
     * Constructs a new VimTab with a HighlightedCodeAreaWhichDoesNotExtendCodeArea.
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
        HighlightedCodeAreaWhichDoesNotExtendCodeArea hcawdneca = // this is obviously an abbreviation of the class name
                new HighlightedCodeAreaWhichDoesNotExtendCodeArea();
        codeArea = hcawdneca.getCodeArea();
        codeArea.addEventFilter(KeyEvent.ANY, vimHandler);
        setContent(new VirtualizedScrollPane<>(codeArea));
    }

    /**
     * routes the current vimCommand string to the appropriate
     * handler method via the first character in the command string.
     */
    private void dispatchVimCommand() {
        if (vimCommands.length() < 1) {
            return;
        }
        if (vimCommands.startsWith("i")) {
            handleLowerCaseI();
        } else if (vimCommands.startsWith("I")) {
            handleUpperCaseI();
        } else if (vimCommands.startsWith("a")) {
            handleLowerCaseA();
        } else if (vimCommands.startsWith("A")) {
            handleUpperCaseA();
        } else if (vimCommands.startsWith("h")) {
            handleH();
        } else if (vimCommands.startsWith("j")) {
            handleJ();
        } else if (vimCommands.startsWith("k")) {
            handleK();
        } else if (vimCommands.startsWith("l")) {
            handleL();
        } else if (vimCommands.startsWith("r")) {
            handleR();
        } else if (vimCommands.startsWith("x")) {
            handleX();
        } else if (vimCommands.startsWith("s")) {
            handleS();
        } else if (vimCommands.startsWith("d")) {
            handleD();
        } else if (vimCommands.startsWith("y")) {
            handleY();
        } else if (vimCommands.startsWith("p")) {
            handleLowerCaseP();
        } else if (vimCommands.startsWith("P")) {
            handleUpperCaseP();
        }
    }

    /**
     * "Put" VIM Command. Pastes the yank register at the current Caret Position.
     * Currently, unused as it is cannot accurately handle edge cases. Will be functional
     * in future versions.
     */
    private void handleP() {
        if (!isReadyForP()) {
            return;
        }

        // setup boolean expressions
        Character startingChar = vimCommands.charAt(0);
        boolean pasteNewLine = yankRegister.contains("\n");
        boolean capitalP = startingChar.equals('P');

        // initialize paste variables
        int insertPos;
        String insertText;
        if (pasteNewLine && capitalP) {
            insertPos = getStartOfLine() - 1;
            insertText = "\n" + yankRegister.trim();
        } else if (pasteNewLine && !capitalP) {
            insertPos = getEndOfLine();
            insertText = "\n" + yankRegister.trim();
        } else if (!pasteNewLine && capitalP) {
            insertPos = codeArea.getCaretPosition();
            insertText = yankRegister;
        } else {
            int textLength = codeArea.getContent().getLength();
            int caretPos = codeArea.getCaretPosition();
            insertPos = caretPos < textLength
                    ? caretPos + 1
                    : caretPos;

            insertText = yankRegister;
        }
        codeArea.insertText(insertPos, insertText);
        vimCommands = "";
    }

    /**
     * "Put" VIM Command
     * pastes the yank register before the current Caret Position. If there is a newline
     * character in the yanked register, it is placed such that the new line is pasted
     * before the line where the caret sits.
     *
     * @deprecated
     */
    private void handleUpperCaseP() {
        if (!isReadyForP()) {
            return;
        }
        int caretPos = codeArea.getCaretPosition();
        if (yankRegister.contains("\n")) {
            String trimmedYankRegister = yankRegister.trim();
            codeArea.insertText(getStartOfLine() - 1, "\n" + trimmedYankRegister);
        } else {
            codeArea.insertText(caretPos, yankRegister);
        }


        vimCommands = "";
    }

    /**
     * "put" VIM Command
     * pastes the yank register after the current Caret Position. If there is a newline
     * character in the yanked register, it is placed such that the new line is pasted
     * after the line where the caret sits.
     *
     * @deprecated
     */
    private void handleLowerCaseP() {
        if (!isReadyForP()) {
            return;
        }
        int caretPos = codeArea.getCaretPosition();
        if (yankRegister.contains("\n")) {
            String trimmedYankRegister = yankRegister.trim();
            codeArea.insertText(getEndOfLine(), "\n" + trimmedYankRegister);
        } else if (caretPos < codeArea.getContent().getLength()) {
            codeArea.insertText(caretPos + 1, yankRegister);
        } else {
            codeArea.insertText(caretPos, yankRegister);
        }

        vimCommands = "";
    }

    /**
     * Checks if the put/Put command makes sense to use.
     *
     * @return boolean representing whether "put/Put" can be used
     */
    private boolean isReadyForP() {
        return yankRegister.equals("");
    }

    /**
     * Only set to handle the yy Vim Command
     * This yanks the current line from the start of the line or file to "\n"
     */
    private void handleY() {
        if (!isReadyForYY()) {
            return;
        }
        int start = getYankStart();
        int end = getYankEnd();
        if (start != 0 || end != codeArea.getContent().getLength()) {
            start++;
        }
        yank(start, end);

        yank(getYankStart(), getYankEnd());
        vimCommands = "";
    }

    /**
     * Checks if the vimCommand is "yy"
     *
     * @return boolean true if vimCommand is "yy"
     */
    private boolean isReadyForYY() {
        return "yy".equals(vimCommands);
    }

    /**
     * Only set to handle the dd Vim Command
     * This yanks the current line from the start of the line or file to "\n" and then
     * deletes the same selection.
     */
    private void handleD() {
        if (!isReadyForDD()) {
            return;
        }

        String content = codeArea.getContent().getText();
        int start = getYankStart();
        int end = getYankEnd();
        if (start != 0 || end != content.length()) {
            start++;
        }
        yank(start, end);
        String preYank = content.substring(0, start);
        String postYank = content.substring(end);
        codeArea.replaceText(preYank + postYank);
        updateColumnTracker();
        vimCommands = "";
    }

    /**
     * Checks if the vimCommand is "dd"
     *
     * @return boolean true if vimCommand is "dd"
     */
    private boolean isReadyForDD() {
        return "dd".equals(vimCommands);
    }

    /**
     * Finds the start of the current line for yank
     * either "\n" or start of the text
     *
     * @return int position in code area
     */
    private int getYankStart() {
        codeArea.getContent().getText();
        int startYank = getStartOfLine();
        if (startYank > 0) {
            startYank--;
        }
        return startYank;
    }

    /**
     * Finds the end of the current line for yank
     * either "\n" or end of the text
     *
     * @return int position in code area
     */
    private int getYankEnd() {
        String content = codeArea.getContent().getText();
        int endYank = getEndOfLine();
        if (endYank < content.length()) {
            endYank++;
        }
        return endYank;
    }

    /**
     * Yanks text from the code area
     *
     * @param start an int position to start the yank
     * @param end   an int position to end the yank
     */
    private void yank(int start, int end) {
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
     *
     * @return if vimCommands length is 2 characters
     */
    private boolean isReadyForR() {
        return vimCommands.length() == 2;
    }

    /**
     * "l" VIM command
     * Moves caret to the right (if not at the end of the document)
     */
    private void handleL() {
        int caretPos = codeArea.getCaretPosition();
        // move right if not the last character 
        if (caretPos < codeArea.getContent().getLength()) {
            codeArea.moveTo(caretPos + 1);
        }
        vimCommands = "";
        updateColumnTracker();
    }

    /**
     * "k" VIM command
     * Moves caret up (if not in the first row)
     */
    private void handleK() {
        // Move to previous line
        int previousLine = getStartOfLine() - 1;
        if (previousLine > 0) {
            codeArea.moveTo(previousLine);
        }
        // Move to correct column
        int lineLength = getEndOfLine() - getStartOfLine();
        if (currentColumn < lineLength) {
            codeArea.moveTo(getStartOfLine() + currentColumn);
        } else {
            codeArea.moveTo(getStartOfLine() + lineLength);
        }
        vimCommands = "";
    }

    /**
     * "j" VIM command
     * Moves caret down (if not in the last row)
     */
    private void handleJ() {
        if (notLastLine()) {
            // Move to start of next line 
            codeArea.moveTo(getEndOfLine() + 1);
            // Move to correct column
            int lineLength = getEndOfLine() - getStartOfLine();
            if (currentColumn < lineLength) {
                codeArea.moveTo(codeArea.getCaretPosition() + currentColumn);
            } else {
                codeArea.moveTo(codeArea.getCaretPosition() + lineLength);
            }
        }
        vimCommands = "";
    }

    /**
     * "h" VIM command
     * Moves caret to the left (if not at the start of the document)
     */
    private void handleH() {
        int caretPos = codeArea.getCaretPosition();
        // if not first character move left
        if (caretPos > 0) {
            codeArea.moveTo(caretPos - 1);
        }
        vimCommands = "";
        updateColumnTracker();
    }

    /**
     * "A" VIM command
     * Moves caret to the end of the line and enter insert mode
     */
    private void handleUpperCaseA() {
        codeArea.moveTo(getEndOfLine());
        inVIMCommandMode = false;
    }

    /**
     * "a" VIM command
     * Enter insert mode after current caret position
     */
    private void handleLowerCaseA() {
        int caretPos = codeArea.getCaretPosition();
        if (nextCharNotNewline(caretPos)) {
            caretPos++;
        }
        codeArea.moveTo(caretPos);
        inVIMCommandMode = false;
    }

    /**
     * "I" VIM command
     * Moves caret to the start of the line and enter insert mode
     */
    private void handleUpperCaseI() {
        codeArea.moveTo(getStartOfLine());
        inVIMCommandMode = false;
    }

    /**
     * "i" VIM command
     * Enter insert mode before current caret position
     */
    private void handleLowerCaseI() {
        int caretPos = codeArea.getCaretPosition();
        if (prevCharNotNewline(caretPos)) {
            caretPos--;
        }
        codeArea.moveTo(caretPos);
        inVIMCommandMode = false;
    }

    /**
     * Checks character after caret to determine if it is the new line. If that
     * character doesn't exist or is a newline, returns false. In other cases, returns
     * ture
     *
     * @param caretPos the position of the caret in the text
     * @return whether the following character exists and is not \n
     */
    private boolean nextCharNotNewline(int caretPos) {
        String content = codeArea.getContent().getText();
        return caretPos < content.length()
                && !"\n".equals(content.substring(caretPos, caretPos + 1));
    }

    /**
     * Checks character preceding caret to determine if it is the new line. If that
     * character doesn't exist or is a newline, returns false. In other cases, returns
     * ture
     *
     * @param caretPos the position of the caret in the text
     * @return whether the previous character exists and is not \n
     */
    private boolean prevCharNotNewline(int caretPos) {
        String content = codeArea.getContent().getText();
        return caretPos > 0 && !"\n".equals(content.substring(caretPos - 1, caretPos));
    }

    /**
     * Find the end of the current line
     *
     * @return int representing the end of the current line
     */
    private int getEndOfLine() {
        int caretPos = codeArea.getCaretPosition();
        String content = codeArea.getContent().getText();
        while (caretPos < content.length()
                && !"\n".equals(content.substring(caretPos, caretPos + 1))) {
            caretPos++;
        }
        return caretPos;
    }

    /**
     * Find the start of the current line
     *
     * @return int representing the start of the current line
     */
    private int getStartOfLine() {
        int caretPos = codeArea.getCaretPosition();
        String content = codeArea.getContent().getText();
        while (caretPos > 0 && !"\n".equals(content.substring(caretPos - 1, caretPos))) {
            caretPos--;
        }
        return caretPos;
    }

    /**
     * Finds if in last line of document
     *
     * @return boolean representing if in the last line of document
     */
    private boolean notLastLine() {
        // Move to start of next line then add
        int codeAreaLength = codeArea.getContent().getLength();
        return getEndOfLine() + 1 < codeAreaLength;
    }

    /**
     * keeps track of the caret column position for moving
     * between rows
     */
    private void updateColumnTracker() {
        currentColumn = codeArea.getCaretColumn();
    }

}

