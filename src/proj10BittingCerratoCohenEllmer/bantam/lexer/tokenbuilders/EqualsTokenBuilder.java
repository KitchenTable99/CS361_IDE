/*
 * File: EqualsTokenBuilder.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.tokenbuilders;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

/**
 * If some token starts with '=', this PrecursorToken contains all the logic required
 * to tokenize the string
 */
public class EqualsTokenBuilder extends TokenBuilder {

    public EqualsTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (spellingStack.size() >= 2) {
            containsCompleteToken = true;
        }

        if (containsCompleteToken && spellingStack.get(1) != '=') {
            popLastBeforeCreation = true;
        }

    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }
        Token.Kind tokenKind;
        if (spellingStack.size() == 1) {
            tokenKind = Token.Kind.ASSIGN;
        } else {
            tokenKind = Token.Kind.COMPARE;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
