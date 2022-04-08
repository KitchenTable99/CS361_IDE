package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

public class PrecursorIntegerToken extends AbstractPrecursorToken {

    public PrecursorIntegerToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!Character.isDigit(c)) {
            containsCompleteToken = true;
            popLastBeforeCreation = true;
        }
    }

    @Override
    public Token getFinalToken() throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        // make sure the int is small enough
        Token.Kind tokenKind;
        long currentNumber = Long.parseLong(makeStackString(true));
        if (currentNumber <= Integer.MAX_VALUE) {
            tokenKind = Token.Kind.INTCONST;
        } else {
            tokenError = new Error(Error.Kind.LEX_ERROR, filename, lineNumber, "Integer Constant too large!");
            tokenKind = Token.Kind.ERROR;
        }

        return new Token(tokenKind, makeStackString(false), lineNumber);
    }
}
