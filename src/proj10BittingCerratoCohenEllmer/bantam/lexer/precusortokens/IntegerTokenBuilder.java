/*
 * File: IntegerTokenBuilder.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

/**
 * If some token starts with a digit, this PrecursorToken contains all the logic needed
 * to tokenize the string
 */
public class IntegerTokenBuilder extends TokenBuilder {

    public IntegerTokenBuilder(Stack<Character> sc, int n, String s) {
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
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        // make sure the int is small enough
        Token.Kind tokenKind;
        try{
            Integer.parseInt(makeStackString(true));
            tokenKind = Token.Kind.INTCONST;
        }catch(NumberFormatException e){
            tokenError.add(new Error(Error.Kind.LEX_ERROR, filename,
                currentLineNumber,
                "Integer Constant too large!"));
            tokenKind = Token.Kind.ERROR;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}