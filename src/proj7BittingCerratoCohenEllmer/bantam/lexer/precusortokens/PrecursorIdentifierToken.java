package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

public class PrecursorIdentifierToken extends AbstractPrecursorToken {

    public PrecursorIdentifierToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!Character.isAlphabetic(c) &&
                !Character.isDigit(c) &&
                c != '_') {
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber) throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(Token.Kind.IDENTIFIER, makeStackString(false), currentLineNumber);
    }
}
