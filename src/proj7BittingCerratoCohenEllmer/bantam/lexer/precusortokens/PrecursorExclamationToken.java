/*
 * File: PrecursorSingleCharToken.java
 * Author: cbitting
 * Adapted: ecohen
 * Date: 4/9/2021
 */
package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

/**
 * If some token is contains an Exclamation (!), this PrecursorToken contains all the
 * logic needed to tokenize that string.
 */
public class PrecursorExclamationToken extends AbstractPrecursorToken {

    public PrecursorExclamationToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
        containsCompleteToken = false;
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (Character.isWhitespace(c) ||
                Character.isAlphabetic(c) ||
                Character.isDigit(c) ||
                c == ';') {
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        } else if ( c == '=') {
            containsCompleteToken = true;
        }
    }


    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(getTokenKind(), makeStackString(false), currentLineNumber);
    }

    /**
     * Gets the exact token type of the singular character
     *
     * @return the Kind of the token
     */
    private Token.Kind getTokenKind() {
        String tokenString = makeStackString(true);
        switch (tokenString) {
            case "!":
                return Token.Kind.UNARYNOT;
            case "!=":
                return Token.Kind.COMPARE;
            default:
                return null;
        }
    }
}
