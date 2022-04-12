/*
 * File: PrecursorMathToken.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

/**
 * If some token starts with a mathematical symbol, this PrecursorToken contains all the
 * logic needed to tokenize the string
 */
public class PrecursorMathToken extends AbstractPrecursorToken {

    public PrecursorMathToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (c == '+' || c == '-' || c == '&' || c == '|'|| c == '=') {
            containsCompleteToken = true;
        } else{
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(getTokenKind(), makeStackString(false), currentLineNumber);
    }

    /**
     * Gets the exact token type of the mathematical expression
     *
     * @return the Kind of the token
     */
    private Token.Kind getTokenKind() {
        String tokenString = makeStackString(true);
        switch (tokenString) {
            case ">":
            case "<":
            case ">=":
            case "<=":
                return Token.Kind.COMPARE;
            case "+":
            case "-":
                return Token.Kind.PLUSMINUS;
            case "*":
                return Token.Kind.MULDIV;
            case "%":
            case "&&":
            case "||":
                return Token.Kind.BINARYLOGIC;
            case "++":
                return Token.Kind.UNARYINCR;
            case "--":
                return Token.Kind.UNARYDECR;
            default:
                return null;
        }
    }
}
