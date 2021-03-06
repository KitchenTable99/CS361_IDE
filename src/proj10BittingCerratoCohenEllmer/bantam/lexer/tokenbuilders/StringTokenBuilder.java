/*
 * File: StringTokenBuilder.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.tokenbuilders;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.HashSet;
import java.util.Stack;

/**
 * If some token starts with a ", this PrecursorToken contains all the logic needed
 * to tokenize that string.
 */
public class StringTokenBuilder extends TokenBuilder {

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

    public StringTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        int stackSize = spellingStack.size();
        if (stackSize > 1) {
            // check if string is closed
            if (c == '"' && !charIsEscaped()) {
                containsCompleteToken = true;
            }
            // check if EOF to find unterminated string
            if (c == '\u0000') {
                tokenErrors.add(new Error(Error.Kind.LEX_ERROR,
                        filename,
                        startingLineNumber,
                        "Unterminated String Constant!"));
                containsCompleteToken = true;
            }
            // make sure the escaped character is valid
            if (charIsEscaped() && !validEscapedCharacter.contains(c)) {
                tokenErrors.add(new Error(Error.Kind.LEX_ERROR,
                        filename,
                        startingLineNumber,
                        "Invalid Escaped Character \\" + spellingStack.peek() + "!"));
            }
        }
    }

    /**
     * Simple helper method that checks the second to last character to determine if the
     * last character is escaped.
     *
     * @return whether the last character is escaped
     */
    private boolean charIsEscaped() {
        return spellingStack.get(spellingStack.size() - 2) == '\\';
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        if (startingLineNumber != currentLineNumber) {
            tokenErrors.add(new Error(Error.Kind.LEX_ERROR,
                    filename,
                    currentLineNumber,
                    "Multiline String found! Starting @ line: " + startingLineNumber));
        }
        if (spellingStack.size() > 5000) {
            tokenErrors.add(new Error(Error.Kind.LEX_ERROR,
                    filename,
                    startingLineNumber,
                    "String Exceeds 5000 Characters!"));
        }

        Token.Kind tokenKind;
        if (tokenErrors.size() != 0) {
            tokenKind = Token.Kind.ERROR;
        } else {
            tokenKind = Token.Kind.STRCONST;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
