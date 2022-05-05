/*
 * File: NumberTokenBuilder.java
 * Author: cbitting, matt cerrato
 * Date: 5/5/2022
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.List;
import java.util.Stack;

enum type {DOUBLE, INT}

/**
 * If some token starts with a digit, this PrecursorToken contains all the logic needed
 * to tokenize the string.
 */
public class NumberTokenBuilder extends TokenBuilder {

    private type tokenType = type.INT;
    private final List<Character> letterChars = List.of('d', 'e', 'E', 'f');
    private final List<Character> mathChars = List.of('+', '-', '.');

    public NumberTokenBuilder(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!Character.isDigit(c)) {
            if (letterChars.contains(c) || mathChars.contains(c)) {
                pushDoubleChar(c);
            } else {
                containsCompleteToken = true;
                popLastBeforeCreation = true;
            }
        }
    }

    public void pushDoubleChar(char c) {
        tokenType = type.DOUBLE;
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }


        Token.Kind tokenKind = Token.Kind.ERROR;

        // decide if number is integer or double
        try {
            String stack = makeStackString(true);
            if (tokenType.equals(type.DOUBLE)) {
                Double.parseDouble(stack);
                tokenKind = Token.Kind.DBLCONST;
            } else if (tokenType.equals(type.INT)) {
                Integer.parseInt(stack);
                tokenKind = Token.Kind.INTCONST;
            }
        } catch (NumberFormatException e) {
            tokenError.add(new Error(Error.Kind.LEX_ERROR, filename,
                    currentLineNumber,
                    "Number Constant not valid"));
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
