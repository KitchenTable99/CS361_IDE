/*
 * File: UnsupportedCharTokenBuilder.java
 * Author: cbitting
 * modified: ecohen
 * Date: 4/14/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.tokenbuilders;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

/**
 * If some token is complete as a single symbol, this PrecursorToken contains all the
 * logic needed to tokenize that string.
 */
public class UnsupportedCharTokenBuilder extends TokenBuilder {

    public UnsupportedCharTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
        containsCompleteToken = true;
    }

    @Override
    public void pushChar(char c) {
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        tokenErrors.add(new Error(Error.Kind.LEX_ERROR, filename,
                currentLineNumber,
                "Unsupported Character : " + Character.getNumericValue(spellingStack.peek())));
        return new Token(Token.Kind.ERROR, makeStackString(false), currentLineNumber);
    }
}
