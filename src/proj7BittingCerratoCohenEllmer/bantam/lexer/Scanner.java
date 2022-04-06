package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Stack;

/**
 * This class reads characters from a file or a Reader
 * and breaks it into Tokens.
 */
public class Scanner {
    /**
     * the source of the characters to be broken into tokens
     */
    private final SourceFile sourceFile;
    /**
     * collector of all errors that occur
     */
    private final ErrorHandler errorHandler;

    private char skippedLastToken;
    private final HashSet<Character> validSolo = new HashSet<>() {{
        add('{');
        add('}');
        add('(');
        add(')');
        add(';');
        add('.');
        add('[');
        add(']');
    }};
    private final HashSet<String> alphaNumeric = new HashSet<>() {{
        add("a");
        add("b");
        add("c");
        add("d");
        add("e");
        add("f");
        add("g");
        add("h");
        add("i");
        add("j");
        add("k");
        add("l");
        add("m");
        add("n");
        add("o");
        add("p");
        add("q");
        add("r");
        add("s");
        add("t");
        add("u");
        add("v");
        add("w");
        add("x");
        add("y");
        add("z");

        add("0");
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
    }};

    /**
     * creates a new scanner for the given file
     *
     * @param filename the name of the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(filename);
        skippedLastToken = '\0';
    }

    /**
     * creates a new scanner for the given file
     *
     * @param reader reader object for the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        skippedLastToken = '\0';
    }

    /**
     * read characters and collect them into a Token.
     * It ignores white space unless it is inside a string or a comment.
     * It returns an EOF Token if all characters from the sourceFile have
     * already been read.
     *
     * @return the Token containing the characters read
     */
    public Token scan() {
        Stack<Character> spellingStack = new Stack<>();
        if (skippedLastToken != '\0') {
            spellingStack.push(skippedLastToken);
        }

        do {
            try {
                char letter = sourceFile.getNextChar();
                if (!Character.isWhitespace(letter) || !spellingStack.empty()) {
                    spellingStack.push(letter);
                }
            } catch (IOException e) {
                // if there are no more character then check to see if the final token is invalid
                e.printStackTrace(); // todo: make this elegant
            }

        } while (!isCompleteToken(spellingStack));
//        skippedLastToken = '\0'; // todo look at this
        return createToken(spellingStack);
        // todo: add all the token types to the logic below
        // todo: implement EOF token ASAP so we can test the rest of the tokens
    }

    // todo: elegance improvement: switch spelling to a stack. We do a lot of peeking
    // todo: elegance improvement: create an object that holds a reference to the spelling string and can do all this validation

    /**
     * This method checks to see if the spelling stack contains a valid token. If a valid
     * token can be constructed from the complete stack, this method return true. In the
     * case that a valid token can be made from all but the last char in the stack, this
     * method pops that character into skippedLastToken and returns true.
     *
     * @param spellingStack containing all the character read so far
     * @return whether a valid token can be created
     */
    private boolean isCompleteToken(Stack<Character> spellingStack) {
        if (spellingStack.size() == 0) {
            return false;
        }
        char leadingChar = spellingStack.lastElement();
        if (validSolo.contains(leadingChar)) {
            return true;
        } else if (isEOF(spellingStack)) {
            return true;
        } else if (leadingChar == '/') {
            return isCompleteSlash(spellingStack); // checks comments and divided by
        } else if (leadingChar == '+' || leadingChar == '-'
                || leadingChar == '*' || leadingChar == '%'
                || leadingChar == '<' || leadingChar == '>') {
            return isCompleteMath(spellingStack);
        } else if (leadingChar == '=') {
            return isCompleteEquals(spellingStack); // make sure to handle === should return == and then =
        } else if (Character.isDigit(leadingChar)) {
            return isCompleteInt(spellingStack);
        } else if (leadingChar == '\'') {
            return isCompleteString(spellingStack); // make sure to handle the case in the above line "\" is not a valid string
        } else {
            return isCompleteIdentifier(spellingStack); // todo: method assumes only alphabetic characters land here. verify this
        }
    }

    private boolean isEOF(Stack<Character> spellingStack) {
        return spellingStack.lastElement() == '\u0000';
    }

    private boolean isCompleteSlash(Stack<Character> spellingStack) {
        if (spellingStack.size() >= 2 && spellingStack.get(1) == '/') {
            return isCompleteComment(spellingStack);
        } else if (spellingStack.size() >= 2 && spellingStack.get(1) == '*') {
            return isCompleteComment(spellingStack);
        } else {
            return isCompleteMath(spellingStack);
        }
    }

    private boolean isCompleteComment(Stack<Character> spellingStack) {
        char secondChar = spellingStack.get(1);
        char lastChar = spellingStack.peek();
        if (secondChar == '/') {
            return lastChar == '\n' || lastChar == '\r';
        } else if (secondChar == '*') {
            char secondToLastChar = spellingStack.get(spellingStack.size() - 1);
            return secondToLastChar == '*' && lastChar == '/';
        }
        return false;
    }

    private boolean isCompleteMath(Stack<Character> spellingStack) {
        char lastChar = spellingStack.peek();
        if (lastChar == ' ' || lastChar == '=') {
            // set token type
            skippedLastToken = '\0';
            return true;
        } else if (Character.isAlphabetic(lastChar) || Character.isDigit(lastChar)) {
            // set token type
            skippedLastToken = lastChar;
            return true;
        } else if (makeStackString(spellingStack).equals("++")
                || makeStackString(spellingStack).equals("--")) {
            // set token type
            skippedLastToken = '\0';
            return true;
        } else if (lastChar == ';') {
            // set token type
            skippedLastToken = spellingStack.pop();
            return true;
        } else {
            return false;
        }
    }

    private boolean isCompleteEquals(Stack<Character> spellingStack) {
        if (spellingStack.size() < 2) {
            return false;
        } else if (spellingStack.size() == 2 && spellingStack.peek() == '=') {
            skippedLastToken = '\0';
            return true;
        } else if (spellingStack.size() == 2 && spellingStack.peek() != '=') {
            skippedLastToken = spellingStack.pop();
            return true;
        } else {
            // something went wrong
            return false;
        }
    }

    private boolean isCompleteInt(Stack<Character> spellingStack) {
        if (Character.isDigit(spellingStack.peek())) {
            return false;
        } else {
            skippedLastToken = spellingStack.pop();
            return true;
        }
    }

    /**
     * checks if string contains valid characters
     * <p>
     * String constants start and end with double quotes.
     * They may contain the following special symbols:
     * \n (newline),
     * \t (tab),
     * \" (double quote),
     * \\ (backslash), and
     * \f (form feed).
     * A string constant cannot exceed 5000 characters and cannot span multiple lines.
     *
     * @param spellingStack the stack containing the characters
     * @return returns true if the stack contains a complete string
     */
    private boolean isCompleteString(Stack<Character> spellingStack) {
        int stackSize = spellingStack.size();
        if (stackSize > 1 && stackSize <= 5000) {
            return spellingStack.peek() == '"' && spellingStack.get(stackSize - 1) != '\\';
        } else if (stackSize > 5000) {
            //raise error;
            return true; // todo figure out if this should be true or false
        } else {
            return false;
        }
    }

    /**
     * checks if some string is an Identifier
     *
     * @param spellingStack the stack containing the characters that have been read
     * @return returns true if an identifier
     */
    private boolean isCompleteIdentifier(Stack<Character> spellingStack) {
        if (spellingStack.size() == 1) {
            return false;
        } else if (Character.isAlphabetic(spellingStack.peek()) ||
                Character.isDigit(spellingStack.peek()) ||
                spellingStack.peek() == '_') {
            return false;
        } else {
            skippedLastToken = spellingStack.pop();
            return true;
        }
    }

    private Token createToken(Stack<Character> spellingStack) {
        if (isEOF(spellingStack)) {
            return new Token(Kind.EOF, "bye bitch", 999);
        } else {
            return new Token(Kind.IDENTIFIER, makeStackString(spellingStack), sourceFile.getCurrentLineNumber());
        }
        /*
        if (isCompleteString(spellingStack)) {
            return new Token(Kind.STRCONST, spellingStack,
                    sourceFile.getCurrentLineNumber());
        }

        if (isCompleteIdentifier(spellingStack)) {
            return new Token(Kind.IDENTIFIER, spellingStack,
                    sourceFile.getCurrentLineNumber());
        }

        if (isValidInt(spellingStack)) {
            return new Token(Kind.INTCONST, spellingStack,
                    sourceFile.getCurrentLineNumber());
        }

        if (isEOF(spellingStack)) {
            return new Token(Kind.EOF, spellingStack,
                    sourceFile.getCurrentLineNumber());
        }

        if (isCompleteComment(spellingStack)) {
            return new Token(Kind.COMMENT, spellingStack,
                    sourceFile.getCurrentLineNumber());
        }

        return new Token(Kind.ERROR, "missed: " + spellingStack, 999);
        */
    }

    /**
     * checks if string contains a valid int
     *
     * @param integer the current spelling of the token to check
     * @return returns true if an integer
     */
    private boolean isValidInt(String integer) {
        try {
            Integer.parseInt(integer);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * checks if some string is an Identifier
     *
     * @param spelling the current spelling of the token to check
     * @return returns true if an identifier
     */
    private boolean isIdentifier(String spelling) {
        //must start with a letter
        char start = spelling.charAt(0);
        if (start >= 'A' && start <= 'Z' ||
                start >= 'a' && start <= 'z') {
            // only contains letters, numbers, and underscores
            for (char character : spelling.toCharArray()) {
                if (character < '0' ||
                        character > '9' && character < 'A' ||
                        character > 'Z' && character < '_' ||
                        character > '_' && character < 'a' ||
                        character > 'z') {
                    return false;
                }
            }
            return true;
        } // doesnt start with letter
        return false;
    }

    private boolean isSlash(String spelling) {
        return spelling.startsWith("/'");
    }

    private boolean isString(String spelling) {
        return spelling.startsWith("\"");
    }

    private Kind specialSymbolToKind(String symbol) {
        switch (symbol) {
            case "(":
                return Kind.LPAREN;
            case ")":
                return Kind.RPAREN;
            case "{":
                return Kind.LCURLY;
            case "}":
                return Kind.RCURLY;
            case ";":
                return Kind.SEMICOLON;
            case "+":
            case "-":
                return Kind.PLUSMINUS;
            case "++":
                return Kind.UNARYINCR;
            case "==":
                return Kind.COMPARE;
            case "&":
            case "|":
            case "&&":
            case "||":
                return Kind.BINARYLOGIC;
            case "--":
                return Kind.UNARYDECR;
            case "!":
                return Kind.UNARYNOT;
            case ".":
                return Kind.DOT;
            case ":":
                return Kind.COLON;
            case ",":
                return Kind.COMMA;
            case "*":
            case "/":
                return Kind.MULDIV;
        }
        return null;
    }

    private String makeStackString(Stack<Character> spellingStack) {
        char[] charArray = new char[spellingStack.size()];
        for (int i = spellingStack.size() - 1; i >= 0; i--) {
            charArray[i] = spellingStack.pop();
        }

        return new String(charArray);
    }
}
