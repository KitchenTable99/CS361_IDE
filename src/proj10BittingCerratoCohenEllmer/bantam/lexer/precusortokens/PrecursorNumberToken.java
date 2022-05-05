/*
 * File: PrecursorIntegerToken.java
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
public class PrecursorNumberToken extends AbstractPrecursorToken {

    private boolean onePoint;

    public PrecursorNumberToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
        this.onePoint = false;
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!Character.isDigit(c)) {
             if(!this.onePoint && c == '.'){
                this.onePoint = true;
            }else{
                 containsCompleteToken = true;
                 popLastBeforeCreation = true;
             }
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        // decide if number is integer or double
        Token.Kind tokenKind;
        try{
            String stack = makeStackString(true);
            if(stack.contains(".")){
                Double.parseDouble(stack);
                tokenKind = Token.Kind.DBLCONST;
            }else {
                Integer.parseInt(stack);
                tokenKind = Token.Kind.INTCONST;
            }
        }catch(NumberFormatException e){
            tokenError.add(new Error(Error.Kind.LEX_ERROR, filename,
                    currentLineNumber,
                    "Number Constant too large!"));
            tokenKind = Token.Kind.ERROR;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);
    }
}
