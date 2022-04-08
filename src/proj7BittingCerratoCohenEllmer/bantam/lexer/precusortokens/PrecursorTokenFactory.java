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
            case '=':
                return new PrecursorEqualsToken(spellingStack, lineNum, filename);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return new PrecursorIntegerToken(spellingStack, lineNum, filename);
            default:
                return null;
        }
    }
}
