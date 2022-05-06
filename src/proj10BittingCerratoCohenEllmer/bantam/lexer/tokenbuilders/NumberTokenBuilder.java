/*
 * File: NumberTokenBuilder.java
 * Author: cbitting, matt cerrato
 * Date: 5/5/2022
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.tokenbuilders;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.List;
import java.util.Stack;

/**
 * If some token starts with a digit, this PrecursorToken contains all the logic needed
 * to tokenize the string.
 */
public class NumberTokenBuilder extends TokenBuilder {

    private final List<Character> validDoubleChars = List.of('d', 'e', 'E', 'f', '+', '-', '.');
    private boolean isDouble = false;

    public NumberTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!Character.isDigit(c) && validDoubleChars.contains(c)) {
            // 12e4.8f5d4 is allowed by this if-else tree, but the
            // invalid number will be caught in the getFinalToken method
            isDouble = true;
        } else if (!Character.isDigit(c)) {
            containsCompleteToken = true;
            popLastBeforeCreation = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(getTokenKind(currentLineNumber), makeStackString(false),
                currentLineNumber);
    }

    private Token.Kind getTokenKind(int currentLineNumber) {
        String stackString = makeStackString(true);
        try {
            if (isDouble) {
                Double.parseDouble(stackString);
                return Token.Kind.DBLCONST;
            } else {
                Integer.parseInt(stackString);
                return Token.Kind.INTCONST;
            }
        } catch (NumberFormatException e) {
            if (isDouble) {
                tokenErrors.add(new Error(Error.Kind.LEX_ERROR, filename,
                        currentLineNumber,
                        "Number constant not valid"));
                return Token.Kind.ERROR;
            } else {
                isDouble = true;
                return getTokenKind(currentLineNumber);
            }
        }
    }
}
