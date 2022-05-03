/*
 * File: PrecursorEqualsToken.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

/**
 * If some token starts with a letter, this PrecursorToken contains all the logic
 * used to tokenize that string.
 */
public class PrecursorIdentifierToken extends AbstractPrecursorToken {

    public PrecursorIdentifierToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (! (Character.isLetterOrDigit(c) || c == '_') ) {
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(Token.Kind.IDENTIFIER,
            makeStackString(false), currentLineNumber);
    }
}
