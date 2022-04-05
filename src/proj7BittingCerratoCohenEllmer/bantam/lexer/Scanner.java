package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;

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
    private String skippedLastToken;
    private boolean endOfToken;
    private final ArrayList<String> nonValidSpecialStringSymbs = new ArrayList<>(){{
        add("\b");
        add("\r");
        add("\'");

    }};
    private final HashSet<String> validSolo = new HashSet<>() {{
        add("{");
        add("}");
        add("(");
        add(")");
        add(";");
        add(".");
        add("[");
        add("]");
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
        skippedLastToken = "";
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
        skippedLastToken = "";
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
        String spelling = skippedLastToken;
        boolean startedToken = false;
        endOfToken = false;
        skippedLastToken = "";
        while (!isCompleteToken(spelling)){
            try {
                String letter = String.valueOf(sourceFile.getNextChar());
                if(startedToken && isWhiteSpace(letter)){
                    endOfToken = true;
                }
                if (!isWhiteSpace(letter) 
                    || isComment(spelling)
                    || isString(spelling)) {
                    startedToken = true;
                    spelling += letter;
                }
            } catch (IOException e) {
                // if there are no more character then check to see if the final token is invalid
                e.printStackTrace(); // todo: make this elegant
            }
        }
        if( spelling.length() > 1 && validSolo.contains(lastSymbol(spelling))
            && (!isComment(spelling) || !isString(spelling))
        ){
            endOfToken = true;
            skippedLastToken = lastSymbol(spelling);
            return createToken(dropLastSymbol(spelling));
        }
        return createToken(spelling);
        // todo: add all the token types to the logic below
        // todo: implement EOF token ASAP so we can test the rest of the tokens
    }

    /**
     * checks if letter is whitespace, a carriage, or a tab
     *
     * @param letter the current letter being checked
     */
    private boolean isWhiteSpace(String letter) {
        return Character.isWhitespace(letter.charAt(0));
    }

    // todo: elegance improvement: switch spelling to a stack. We do a lot of peeking
    // todo: elegance improvement: create an object that holds a reference to the spelling string and can do all this validation
    private boolean isCompleteToken(String spelling) {
        if(spelling.length()<1){
            return false;
        }
        if (validSolo.contains(spelling) || validSolo.contains(lastSymbol(spelling))){
            return true;
        } else if (isEOF(spelling)) {
            return true;
        } else if (spelling.startsWith("/")) {
            return isCompleteSlash(spelling); // checks comments and divided by
        } else if (spelling.startsWith("+") || spelling.startsWith("-")
                || spelling.startsWith("*") || spelling.startsWith("%")
                || spelling.startsWith("<") || spelling.startsWith(">")) {
            return isCompleteMath(spelling);
        } else if (spelling.startsWith("=")) {
            return isCompleteEquals(spelling); // make sure to handle === should not return == and then =
        } else if (isInt(spelling)) {
            return isCompleteInt(spelling);
        } else if (spelling.startsWith("\"")) {
            return isCompleteString(spelling); // make sure to handle the case in the above line "\" is not a valid string
        } else {
            return isCompleteIdentifier(spelling);
        }
    }

    private boolean isEOF(String spelling) {
        return spelling.equals(String.valueOf('\u0000'));
    }

    private boolean isCompleteSlash(String spelling) {
        if (spelling.startsWith("//")) {
            return isCompleteComment(spelling);
        } else if (spelling.startsWith("/*")) {
            return isCompleteComment(spelling);
        } else {
            return isCompleteMath(spelling);
        }
    }

    private boolean isCompleteComment(String spelling) {
        if (spelling.startsWith("//")) {
            return (spelling.endsWith("\n") || spelling.endsWith("\r"));
        } else if (spelling.startsWith("/*")) {
            return spelling.endsWith("*/");
        }
        return false;
    }

    private boolean isCompleteMath(String spelling) {
        String lastCharacter = spelling.substring(spelling.length() - 1);
        if (lastCharacter.equals(" ") || lastCharacter.equals("=")) {
            // set token type
            skippedLastToken = "";
            return true;
        } else if (alphaNumeric.contains(spelling.substring(spelling.length() - 1))) {
            // set token type
            skippedLastToken = lastCharacter;
            return true;
        } else if (spelling.equals("++") || spelling.equals("--")) {
            // set token type
            skippedLastToken = "";
            return true;
        } else if (lastCharacter.equals(";")) {
            // set token type
            skippedLastToken = ";";
            return true;
        } else {
            return false;
        }
    }

    private String lastSymbol(String spelling){
        return spelling.substring(spelling.length() - 1);
    }

    private String dropLastSymbol(String spelling){
        return spelling.substring(0, spelling.length() - 1);
    }

    private boolean isCompleteEquals(String spelling) {
        return false;
    }

    private boolean isCompleteInt(String spelling) {
        return false;
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
     * @param spelling the current spelling of the token to check
     * @return returns true if valid string
     */
    private boolean isCompleteString(String spelling) {
        if (spelling.length() > 1 && // prevent " from triggering "valid string"
                spelling.startsWith("\"") && spelling.endsWith("\"")) {
            if (spelling.length() <= 5000) {
                for(String sym: nonValidSpecialStringSymbs){
                    if(spelling.contains(sym)){
                        return false;
                    }
                }
                // TODO: needs regex? idk how to check for symbols
                return true;
            }
        }
        // not encased in double quote
        return false;
    }

    /**
     * checks if some string is an Identifier
     *
     * @param spelling the current spelling of the token to check
     * @return returns true if an identifier
     */
    private boolean isCompleteIdentifier(String spelling) {
        if(endOfToken){
            //must start with a letter
            char start = spelling.charAt(0);
            if(start >= 'A' && start <= 'Z' ||
                    start >= 'a' && start <= 'z'){
                // only contains letters, numbers, and underscores
                for (char character : spelling.toCharArray()) {
                    if(character < '0' ||
                            character > '9' && character < 'A' ||
                            character > 'Z' && character < '_' ||
                            character > '_' && character < 'a' ||
                            character > 'z'){
                        errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                                sourceFile.getCurrentLineNumber(),
                                "Unsupported Symbol, " + character + " in: " + spelling);
                        return false;
                    }
                }
                return true;
            } 
        }
        return false;
    }

    private Token createToken(String spelling) {
        //System.out.println("create token: " + spelling);

        if (specialSymbolToKind(spelling) != null) {
            return new Token(specialSymbolToKind(spelling), spelling,
                    sourceFile.getCurrentLineNumber());
        }

        if (isCompleteString(spelling)) {
            return new Token(Kind.STRCONST, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        if (isCompleteIdentifier(spelling)) {
            return new Token(Kind.IDENTIFIER, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        if (isInt(spelling)) {
            return new Token(Kind.INTCONST, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        if (isEOF(spelling)) {
            return new Token(Kind.EOF, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        if (isCompleteComment(spelling)) {
            if(spelling.endsWith("\n") || spelling.endsWith("\r")){
                spelling = spelling.substring(0, spelling.length()-1);
            }
            return new Token(Kind.COMMENT, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        return new Token(Kind.ERROR, "missed: " + spelling, 999);
        // todo
    }

    /**
     * checks if string contains a valid int
     *
     * @param integer the current spelling of the token to check
     * @return returns true if an integer
     */
    private boolean isInt(String integer) {
        try {
            Integer.parseInt(integer);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isSlash(String spelling) {
        return spelling.startsWith("/") && spelling.length() > 0;
    }

    private boolean isComment(String spelling) {
        return spelling.startsWith("//") || spelling.startsWith("/*")
                && spelling.length() > 1;
    }

    private boolean isString(String spelling) {
        return spelling.startsWith("\"") && spelling.length() > 1;
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
}
