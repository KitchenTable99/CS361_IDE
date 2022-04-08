package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

public class PrecursorEqualsToken extends AbstractPrecursorToken {

    public PrecursorEqualsToken(Stack<Character> sc, int n, String s) {
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
    public Token getFinalToken() throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }
        Token.Kind tokenKind;
        if (spellingStack.size() == 1) {
            tokenKind = Token.Kind.ASSIGN;
        } else {
            tokenKind = Token.Kind.COMPARE;
        }

        return new Token(tokenKind, makeStackString(false), lineNumber);
    }
}
