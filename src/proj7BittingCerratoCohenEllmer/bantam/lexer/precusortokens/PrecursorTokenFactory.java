package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import java.util.Stack;

public class PrecursorTokenFactory {

    public AbstractPrecursorToken createPrecursorToken(char initialChar, int lineNum, String filename) {
        Stack<Character> spellingStack = new Stack<>();
        spellingStack.push(initialChar);

        switch (initialChar) {
            case '/':
                return new PrecursorSlashToken(spellingStack, lineNum, filename);
            case '+':
            case '-':
            case '*':
            case '%':
            case '<':
            case '>':
                return new PrecursorMathToken(spellingStack, lineNum, filename);
            default:
                return null;
        }
    }
}
