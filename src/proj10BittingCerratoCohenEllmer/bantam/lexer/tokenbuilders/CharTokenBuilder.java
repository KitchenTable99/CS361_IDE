/*
 * File: PrecursorStringToken.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.tokenbuilders;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

/**
 * If some token starts with a ", this PrecursorToken contains all the logic needed
 * to tokenize that string.
 */
public class CharTokenBuilder extends TokenBuilder {

    public CharTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);
        if (spellingStack.size() > 1 && c == '\'') {
            containsCompleteToken = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        Token.Kind tokenKind;
        if (spellingStack.size() != 3) {
            tokenErrors.add(new Error(Error.Kind.LEX_ERROR, filename,
                    currentLineNumber,
                    "Char constant too large! A char is a single character"));
            tokenKind = Token.Kind.ERROR;
        } else {
            tokenKind = Token.Kind.CHRCONST;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
