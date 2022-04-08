package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Optional;
import java.util.Stack;

public abstract class AbstractPrecursorToken {

    protected final Stack<Character> spellingStack;
    protected boolean popLastBeforeCreation = false;
    protected boolean containsCompleteToken = false;
    protected Error tokenError = null;
    protected int lineNumber;
    protected String filename;

    public AbstractPrecursorToken(Stack<Character> sc, int n, String s) {
        spellingStack = sc;
        lineNumber = n; // todo: have starting line number and ending line number
        filename = s;
    }

    /**
     * Getter method for the containsCompleteToken field.
     *
     * @return whether the internal stack contains a valid token
     */
    public boolean isComplete() {
        return containsCompleteToken;
    }

    /**
     * Exposes the pop functionality of the internal stack if the last character needs
     * to be removed.
     *
     * @return Optional wrapped char of the final stack value. Optional.empty if there
     * is no need for the final char to be popped.
     */
    public Optional<Character> getExtraChar() {
        if (popLastBeforeCreation) {
            return Optional.of(spellingStack.pop());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Error> getError() {
        if (tokenError != null) {
            return Optional.of(tokenError);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Converts the spelling stack to a string
     *
     * @param preserveStack whether to empty the stack
     * @return a string of the spelling stack
     */
    protected String makeStackString(boolean preserveStack) {
        if (preserveStack) {
            Stack<Character> spellingStackCopy = (Stack<Character>) spellingStack.clone();
            return emptyStackToString(spellingStackCopy);
        } else {
            return emptyStackToString(spellingStack);
        }
    }

    /**
     * Converts a stack to a string and empties stack
     *
     * @param spellingStack the spelling stack to be converted/ emptied
     * @return The spelling stack as a string
     */
    private String emptyStackToString(Stack<Character> spellingStack) {
        char[] charArray = new char[spellingStack.size()];
        for (int i = spellingStack.size() - 1; i >= 0; i--) {
            charArray[i] = spellingStack.pop();
        }
        return new String(charArray);
    }

    /**
     * Pushes the character onto the spelling stack. This method also updates the
     * properties of the precursor token popLastBeforeCreation if the last char should
     * not be included in the final token and containsCompleteToken if the stack contains
     * a complete token.
     *
     * @param c the char to push onto the stack
     */
    public abstract void pushChar(char c);

    /**
     * Creates the final token from the internal spelling stack. Must first check to see
     * if the flag for popLastBeforeCreation has been handled and raise a
     * MalformedSpellingStackException if it has not.
     *
     * @return Token with all the relevant information
     */
    public abstract Token getFinalToken() throws MalformedSpellingStackException;

}
