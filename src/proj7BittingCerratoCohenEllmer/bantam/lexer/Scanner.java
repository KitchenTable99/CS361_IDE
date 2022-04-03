package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
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
    }

    /**
     * checks if letter is whitespace, a carriage, or a tab
     *
     * @param letter the current letter being checked
     */
    private boolean isWhiteSpace(String letter){
        return Character.isWhitespace(letter.charAt(0));
    }

    /**
     * checks if some string is an Identifier
     * 
     * @param spelling the current spelling of the token to check
     * 
     * @return returns true if an identifier 
     */

    /**
     * checks if some string is an Identifier
     *
     * @param spelling the current spelling of the token to check
     *
     * @return returns true if an identifier
     */
    private boolean isIdentifier(String spelling){
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
                    return false;
                }
            }
            return true;
        } // doesnt start with letter
        return false;
    }

    /**
     * checks if string contains a valid int
     *
     * @param integer the current spelling of the token to check
     *
     * @return returns true if an integer
     */
    private boolean isValidInt(String integer){
        try{
            Integer.parseInt(integer);
            return true;
        }
        catch (NumberFormatException ex){
            return false;
        }
    }


    private boolean isValidString(String spelling){
        return false;
    }



    private Kind specialSymbolToKind(String symbol){
        if(symbol.equals("(")){
            return Kind.LPAREN;
        }else if(symbol.equals(")")){
            return Kind.RPAREN;
        }else if(symbol.equals("{")){
            return Kind.LCURLY;
        }else if(symbol.equals("}")){
            return Kind.RCURLY;
        }else if(symbol.equals(";")){
            return Kind.SEMICOLON;
        }else if(symbol.equals("+")){
            return Kind.PLUSMINUS;
        }else if(symbol.equals("-")){
            return Kind.PLUSMINUS;
        }else if(symbol.equals("++")){
            return Kind.UNARYINCR;
        }else if(symbol.equals("==")){
            return Kind.COMPARE;
        }else if(symbol.equals("&")){
            return Kind.BINARYLOGIC;
        }else if(symbol.equals("|")){
            return Kind.BINARYLOGIC;
        }else if(symbol.equals("&&")){
            return Kind.BINARYLOGIC;
        }else if(symbol.equals("||")){
            return Kind.BINARYLOGIC;
        }else if(symbol.equals("--")){
            return Kind.UNARYDECR;
        }else if(symbol.equals("!")){
            return Kind.UNARYNOT;
        }else if(symbol.equals(".")){
            return Kind.DOT;
        }else if(symbol.equals(":")){
            return Kind.COLON;
        }else if(symbol.equals(",")) {
            return Kind.COMMA;
        }else if(symbol.equals("*")){
            return Kind.MULDIV;
        }else if(symbol.equals("/")){
            return Kind.MULDIV;
        }
        return null;
    }

    private boolean isSpecialSymbol(String symbol){
        String[] specialSymbolsArray = { "(", ")", "{", "}", ";", "+", "-", "++", "==",
                "&", "|", "&&", "||", "--", "!", ".", ":", ",", "*", "/"};
        ArrayList<String> specialSymbols = new ArrayList<>(Arrays.asList(specialSymbolsArray));
        return specialSymbols.contains(symbol);
    }

    private Token createToken(String spelling){


        if(isSpecialSymbol(spelling)){
            return new Token(specialSymbolToKind(spelling), spelling, sourceFile.getCurrentLineNumber());
        }

        if(isIdentifier(spelling)){
            return new Token(Kind.IDENTIFIER, spelling,
                    sourceFile.getCurrentLineNumber());
        }
        if(isValidInt(spelling)){
            return new Token(Kind.INTCONST, spelling,
                    sourceFile.getCurrentLineNumber());
        }

        //else check if spelling starts and ends with double quotes
        /**String constants ("abc", etc.) String constants start and end with double quotes.
         They may contain the following special symbols:
         \n (newline), \t (tab), \" (double quote),
         \\ (backslash), and \f (form feed).
         A string constant cannot exceed 5000 characters and cannot span multiple lines. STRCONST */

        //else check if spelling is comment
        //Comments. include the "//" or the "/*" and "*/" delimiters.

        return new Token(Kind.ERROR, "missed: " + spelling , 999);
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

        do {
            try {
                String letter = String.valueOf(sourceFile.getNextChar());
                if (!isWhiteSpace(letter) || spelling.equals("")) {
                    spelling += letter;
                }
            } catch (IOException e) {
                // if there are no more character then check to see if the final token is invalid
                e.printStackTrace(); // todo: make this elegant
            }

        } while (!isCompleteToken(spelling));

        return new Token(Kind.BOOLEAN, spelling, -1);
        // todo: add all the token types to the logic below
        // todo: implement EOF token ASAP so we can test the rest of the tokens
    }

    private boolean isCompleteToken(String spelling) {
        if (validSolo.contains(spelling)) {
            return true;
        }
        // todo: elegance improvement: switch spelling to a stack. We do a lot of peeking
        // todo: elegance improvement: create an object that holds a reference to the spelling string and can do all this validation

        if (spelling.startsWith("/")) {
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
            return isCompleteIndentifier(spelling);
        }
    }

    private boolean isCompleteIndentifier(String spelling) {
        return false;
    }

    private boolean isCompleteString(String spelling) {
        return false;
    }

    private boolean isCompleteInt(String spelling) {
        return false;
    }

    private boolean isInt(String spelling) {
        return false;
    }

    private boolean isCompleteEquals(String spelling) {
        return false;
    }

    private boolean isCompleteSlash(String spelling) {
        if (spelling.length() > 1 && spelling.substring(1).equals("/")) {
            return isCompleteComment(spelling);
        } else if (spelling.length() > 1 && spelling.substring(1).equals("*")) {
            return isCompleteComment(spelling);
        } else {
            return isCompleteMath(spelling);
        }
    }

    private boolean isCompleteComment(String spelling) {
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


}
