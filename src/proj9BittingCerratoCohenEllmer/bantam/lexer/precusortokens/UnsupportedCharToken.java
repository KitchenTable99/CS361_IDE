/*
 * File: UnsupportedCharToken.java
 * Author: cbitting
 * modified: ecohen
 * Date: 4/14/2021
 */
package proj9BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj9BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj9BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

/**
 * If some token is complete as a single symbol, this PrecursorToken contains all the
 * logic needed to tokenize that string.
 */
public class UnsupportedCharToken extends AbstractPrecursorToken {

    public UnsupportedCharToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
        containsCompleteToken = true;
    }

    @Override
    public void pushChar(char c) {
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        tokenError.add(new Error(Error.Kind.LEX_ERROR, filename,
            currentLineNumber,
            "Unsupported Character : " + spellingStack.peek()));
        return new Token(Token.Kind.ERROR, makeStackString(false), currentLineNumber);
    }
}
