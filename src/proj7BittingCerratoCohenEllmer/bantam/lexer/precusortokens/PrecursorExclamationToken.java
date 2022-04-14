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

        if ( c == '=') {
            containsCompleteToken = true;
        } else {
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
        if(spellingStack.size() == 1){
            return new Token(Token.Kind.UNARYNOT,
                makeStackString(false), currentLineNumber);
        }
        return new Token(Token.Kind.COMPARE,
            makeStackString(false), currentLineNumber);
    }
}
