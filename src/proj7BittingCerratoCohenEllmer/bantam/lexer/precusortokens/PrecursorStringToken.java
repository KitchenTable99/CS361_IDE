package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.HashSet;
import java.util.Stack;

public class PrecursorStringToken extends AbstractPrecursorToken {

    /**
     * Holds legal escaped characters in strings
     */
    private final HashSet<Character> validEscapedCharacter = new HashSet<>() {{
        add('n');
        add('t');
        add('"');
        add('\\');
        add('f');
    }};

    public PrecursorStringToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        int stackSize = spellingStack.size();
        if (stackSize > 1) {
            // check if string is closed
            if (spellingStack.peek() == '"' && !charIsEscaped()) {
                containsCompleteToken = true;
            }
            //check if EOF triggers untermintated string
            if (spellingStack.peek() == '\u0000') {
                tokenError = new Error(Error.Kind.LEX_ERROR, filename, startingLineNumber, "Unterminated String Constant!");
                containsCompleteToken = true;
            }
            if (charIsEscaped() && !validEscapedCharacter.contains(spellingStack.peek())) {
                tokenError = new Error(Error.Kind.LEX_ERROR, filename, startingLineNumber, "Invalid Escaped Character \\" + spellingStack.peek() + "!");
            }
        }
    }

    private boolean charIsEscaped() {
        return spellingStack.get(spellingStack.size() - 1) == '\\';
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        if (startingLineNumber != currentLineNumber) {
            tokenError = new Error(Error.Kind.LEX_ERROR, filename, currentLineNumber, "Multiline String found! Starting @ line: " + startingLineNumber);
        }
        if (spellingStack.size() > 5000) {
            tokenError = new Error(Error.Kind.LEX_ERROR, filename, startingLineNumber, "String Exceeds 5000 Characters!");
        }

        Token.Kind tokenKind;
        if (tokenError != null) {
            tokenKind = Token.Kind.ERROR;
        } else {
            tokenKind = Token.Kind.STRCONST;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
