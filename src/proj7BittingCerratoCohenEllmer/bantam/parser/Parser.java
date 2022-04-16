/*
 * Authors: Haoyu Song and Dale Skrien
 * Latest change: Oct. 5, 2021
 *
 * In the grammar below, the variables are enclosed in angle brackets and
 * "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable
 * (but note that "||" is an operator).
 * EMPTY indicates a rule with an empty right hand side.
 * All other symbols in the rules are terminals.
 */
package proj7BittingCerratoCohenEllmer.bantam.parser;

import proj7BittingCerratoCohenEllmer.bantam.ast.*;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Scanner;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.util.Arrays;
import java.util.List;

public class Parser {
    // instance variables
    private Scanner scanner; // provides the tokens
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler; // collects & organizes the error messages

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * parse the given file and return the root node of the AST
     *
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {
        // initialize scanner for each file
        errorHandler = new ErrorHandler();
        scanner = new Scanner(filename, errorHandler);
        currentToken = scanner.scan(true);
        return parseProgram();

    }


    // <Program> ::= <Class> | <Class> <Program>
    private Program parseProgram() {
        int position = currentToken.position;
        ClassList clist = new ClassList(position);

        while (currentToken.kind != Token.Kind.EOF) {
            Class_ aClass = parseClass();
            clist.addElement(aClass);
        }

        return new Program(position, clist);
    }


    // <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
    // <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
    // <MemberList> ::= EMPTY | <Member> <MemberList>
    private Class_ parseClass() {
        int position = currentToken.position;
        MemberList memberList = new MemberList(position);

        // get class name
        currentToken = scanner.scan(true);
        ensureTokenType("Class name is not valid", Token.Kind.IDENTIFIER);
        String name = parseIdentifier();

        // check if 'extends'
        String parent;
        if (currentToken.kind == Token.Kind.EXTENDS) {
            currentToken = scanner.scan(true); // todo check identifier
            parent = parseIdentifier();
        } else {
            parent = null;
        }

        // enter class body
        ensureTokenType("Class definition must begin with '{'", Token.Kind.LCURLY);
        currentToken = scanner.scan(true);

        // parse all members. when EMPTY token will be '}'
        while (currentToken.kind != Token.Kind.RCURLY) {

            // if we reach end of file, we have an error
            if (currentToken.kind == Token.Kind.EOF) {
                registerAndThrow("Incomplete Class Declaration: EOF occured before closing '}'");
            }
            memberList.addElement(parseMember());
        }

        // ensure invariants
        currentToken = scanner.scan(true);
        return new Class_(position, scanner.getFilename(), name, parent, memberList);
    }


    //Fields and Methods
    // <Member> ::= <Field> | <Method>
    // <Method> ::= <Type> <Identifier> ( <Parameters> ) <BlockStmt>
    // <Field> ::= <Type> <Identifier> <InitialValue> ;
    // <InitialValue> ::= EMPTY | = <Expression>
    private Member parseMember() {
        int lineNum = currentToken.position;
        String type = parseType();

        String identifier = parseIdentifier();


        if(currentToken.kind.equals(Token.Kind.LPAREN)){ //Is a method
            currentToken = scanner.scan(true);
            FormalList params = parseParameters();
            if(currentToken.kind.equals(Token.Kind.RPAREN)){
                currentToken = scanner.scan(true);
                Stmt blockStmt = parseBlock();
                StmtList stmtList = ((BlockStmt)blockStmt).getStmtList();
                return new Method(lineNum, type, identifier, params, stmtList);
            }else{
//                TODO:errorhandler stuff, not sure what you wanted for errors
            }
        }else{  // Is a field
            if(currentToken.getSpelling().equals("=")){
                currentToken = scanner.scan(true);
                Expr expr = parseExpression();
                if(currentToken.kind.equals(Token.Kind.SEMICOLON)){
                    currentToken = scanner.scan(true);
                }

                return new Field(lineNum, type, identifier, expr);
            }else{

//                TODO: error handler
            }
        }

        return null;

    }


    //-----------------------------------
    //Statements
    // <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <VarDeclaration>
    //             | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
    private Stmt parseStatement() {
        switch (currentToken.kind) {
            case IF:
                return parseIf();
            case LCURLY:
                return parseBlock();
            case VAR:
                return parseVarDeclaration();
            case RETURN:

                return parseReturn();
            case FOR:
                return parseFor();
            case WHILE:
                return parseWhile();
            case BREAK:
                return parseBreak();
            default:
                return parseExpressionStmt();
        }
    }


    // <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
    private Stmt parseWhile() {
        int position = currentToken.position;

        // enter while body
        currentToken = scanner.scan(true);
        ensureTokenType("Incomplete Statement: While statement missing an opening '('", Token.Kind.LPAREN);

        // parse predicate body
        currentToken = scanner.scan(true);
        Expr predStmt = parseExpression();

        // ensure predicate finished
        ensureTokenType("Incomplete Statement: While statement missing a closing ')'", Token.Kind.RPAREN);

        // parse loop body
        currentToken = scanner.scan(true);
        Stmt bodyStmt = parseStatement();

        return new WhileStmt(position, predStmt, bodyStmt);

    }


    // <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
    private Stmt parseReturn() {
        int position = currentToken.position;

        // determine if empty return or return expression
        Expr returnExpression;
        currentToken = scanner.scan(true);
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            returnExpression = parseExpression();
        } else {
            returnExpression = null;
        }

        // ensure semicolon ending
        ensureTokenType("Invalid Return Statement: Missing an ending ';'", Token.Kind.SEMICOLON);

        currentToken = scanner.scan(true);
        return new ReturnStmt(position, returnExpression);
    }


    // <BreakStmt> ::= BREAK ;
    private Stmt parseBreak() {
        int position = currentToken.position;

        // ensure semicolon ending
        ensureTokenType("Invalid Break Statement: Break statement must end with ';'", Token.Kind.SEMICOLON);

        currentToken = scanner.scan(true);
        return new BreakStmt(position);
    }


    // <ExpressionStmt> ::= <Expression> ;
    private ExprStmt parseExpressionStmt() {
        int position = currentToken.position;

        // parse the expression
        Expr expression = parseExpression();

        // ensure semicolon ending
        ensureTokenType("Invalid Expression: Expression statement must end with ';'", Token.Kind.SEMICOLON);
        return new ExprStmt(position, expression);
    }


    // <VarDeclaration> ::= VAR <Id> = <Expression> ;
    // Every local variable must be initialized
    private Stmt parseVarDeclaration() {
        int position = currentToken.position;

        // get var name
        currentToken = scanner.scan(true);
        ensureTokenType("Invalid Variable Name", Token.Kind.IDENTIFIER);
        String varID = currentToken.spelling;

        // ensure assignment
        currentToken = scanner.scan(true);
        ensureTokenType("Invalid Declaration Statement: All variables must be initialized", Token.Kind.ASSIGN);

        // get assignment expression
        currentToken = scanner.scan(true);
        Expr expr = parseExpression();

        // ensure semicolon ending
        ensureTokenType("Invalid Declaration Statement: Missing ending ';'", Token.Kind.SEMICOLON);

        // ensure invariant
        currentToken = scanner.scan(true);

        return new DeclStmt(position, varID, expr); // todo: is this the right return?
    }


    // <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
    // <Start> ::=     EMPTY | <Expression>
    // <Terminate> ::= EMPTY | <Expression>
    // <Increment> ::= EMPTY | <Expression>
    private Stmt parseFor() {
        int position = currentToken.position;

        // ensure predicate stuff contained in parentheses
        currentToken = scanner.scan(true);
        ensureTokenType("Invalid For Loop: Predicates must begin with '('", Token.Kind.LPAREN);

        // start expr
        currentToken = scanner.scan(true);
        Expr initExpr;
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            initExpr = parseExpression();
        } else {
            initExpr = null;
        }
        ensureTokenType("Invalid For Loop: You must delimit predicates with a ';'", Token.Kind.SEMICOLON);

        // terminating expr
        currentToken = scanner.scan(true);
        Expr termExpr;
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            termExpr = parseExpression();
        } else {
            termExpr = null;
        }
        ensureTokenType("Invalid For Loop: You must delimit predicates with a ';'", Token.Kind.SEMICOLON);

        // increment expression
        currentToken = scanner.scan(true);
        Expr updateExpr;
        if (currentToken.kind != Token.Kind.RPAREN) {
            updateExpr = parseExpression();
        } else {
            updateExpr = null;
        }

        // ensure predicate closed
        ensureTokenType("Invalid For Loop: You must close the predicates with a ')'", Token.Kind.RPAREN);

        // get body statement
        currentToken = scanner.scan(true);
        Stmt bodyStmt = parseStatement();

        return new ForStmt(position, initExpr, termExpr, updateExpr, bodyStmt);
    }


    // <BlockStmt> ::= { <Body> }
    // <Body> ::= EMPTY | <Stmt> <Body>
    private Stmt parseBlock() {
        int position = currentToken.position;

        // open the block statement
        ensureTokenType("Invalid Block Statement: missing '{'", Token.Kind.LCURLY);

        // create statement list
        StmtList stmtList = new StmtList(position);
        currentToken = scanner.scan(true); // either a Body or a '}'
        while (currentToken.kind != Token.Kind.RCURLY) {
            // if we reach end of file, we have an error
            if (currentToken.kind == Token.Kind.EOF) {
                errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Block Statement");
                throw new CompilationException(
                        "Incomplete Statement: Block statement missing a '}'",
                        new Throwable());
            }

            stmtList.addElement(parseStatement()); // add the statement
        }

        // we know that we're looking at a '}' token, so just advance
        currentToken = scanner.scan(true);

        return new BlockStmt(position, stmtList);
    }


    // <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
    private Stmt parseIf() {
        int lineNum = currentToken.position;

        ensureTokenType("Incomplete Statement: If statement missing IF", Token.Kind.IF);

        currentToken = scanner.scan(true); // (
        if (currentToken.kind != Token.Kind.LPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR,"Invalid If Statement");
            throw new CompilationException(
                    "Incomplete Statement: If statement missing (", new Throwable()
            );
        }

        Expr predExpr = parseExpression();

        if (currentToken.kind != Token.Kind.RPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR,"Invalid If Statement");
            throw new CompilationException(
                    "Incomplete Statement: If statement missing )", new Throwable()
            );
        }


        Stmt thenStmt = parseStatement();
        if(currentToken.kind == Token.Kind.ELSE){
            currentToken = scanner.scan(true);
            Stmt elseStmt = parseStatement();
            return new IfStmt(lineNum,predExpr,thenStmt,elseStmt);
        }
        // TODO: Check if messed up Else and if we should throw an error

        return new IfStmt(lineNum, predExpr, thenStmt, null);

    }


    //-----------------------------------------
    // Expressions
    // Here we use different rules than the grammar on page 49
    // of the manual to handle the precedence of operations

    // <Expression> ::= <LogicalORExpr> <OptionalAssignment>
    // <OptionalAssignment> ::= EMPTY | = <Expression>
    private Expr parseExpression() {
        int lineNum = currentToken.position;
        Expr left = parseOrExpr();
        String refName = currentToken.getSpelling();
        if(currentToken.kind == Token.Kind.ASSIGN && (left instanceof VarExpr)) {
            Expr right = parseExpression();
            String name = currentToken.getSpelling();
            left = new AssignExpr(lineNum, refName, name, right);
            currentToken = scanner.scan(true);
        }
        return left;
    }


    // <LogicalOR> ::= <logicalAND> <LogicalORRest>
    // <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
    private Expr parseOrExpr() {
        int position = currentToken.position;
        Expr left;

        left = parseAndExpr();
        while (currentToken.spelling.equals("||")) {
            //...advance to the next token...
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    // <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
    // <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
    private Expr parseAndExpr() {
        int lineNum = currentToken.position;
        Expr left = parseEqualityExpr();
        while (currentToken.spelling.equals("&&")){
            Expr right = parseEqualityExpr();
            left = new BinaryLogicAndExpr(lineNum, left, right);
        }

        return left;

    }


    // <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
    //                      <RelationalExpr>
    // <equalOrNotEqual> ::=  == | !=
    private Expr parseEqualityExpr() {
        int lineNum = currentToken.position;

        Expr left = parseRelationalExpr();


        if(currentToken.kind.equals(Token.Kind.COMPARE)){


            String typeOfEq = currentToken.spelling;
            currentToken = scanner.scan(true);

            Expr right = parseRelationalExpr();

            if(typeOfEq.equals("==")){

                left = new BinaryCompEqExpr(lineNum, left,right);
            }else{

                left = new BinaryCompNeExpr(lineNum, left, right);
            }

        }


        //TODO: check if there is an error

        return left;
    }


    // <RelationalExpr> ::= <AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
    // <ComparisonOp> ::= < | > | <= | >=
    private Expr parseRelationalExpr() {
        int lineNum = currentToken.position;
        Expr left = parseAddExpr();

        List<String> opList = Arrays.asList("<",">","<=",">=");
<<<<<<< HEAD

        String op = currentToken.spelling;
        if(!opList.contains(op)){
=======
        if(!opList.contains(currentToken.getSpelling())){
>>>>>>> 60cc20e7a16ba0eae01c10fd37d505a92a3678ef
            return left;
        }
        String op = parseOperator();

<<<<<<< HEAD
        op = parseOperator();
            currentToken = scanner.scan();
=======
>>>>>>> 60cc20e7a16ba0eae01c10fd37d505a92a3678ef
        Expr right = parseAddExpr();
        switch (op){
            case "<":
                left = new BinaryCompLtExpr(lineNum,left,right);
                break;
            case ">":
                left = new BinaryCompGtExpr(lineNum,left,right);
                break;
            case "<=":
                left = new BinaryCompLeqExpr(lineNum,left,right);
                break;
            case ">=":
                left = new BinaryCompGeqExpr(lineNum, left, right);
                break;
        }

        return left;



    }


    // <AddExpr>::＝ <MultExpr> <MoreMultExpr>
    // <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
    private Expr parseAddExpr() {
        int lineNum = currentToken.position;
        Expr left = parseMultExpr();
        String op = currentToken.spelling;
        while(op.equals("+")||op.equals("-")){
            Expr right = parseMultExpr();
            if(op.equals("+")){
                left = new BinaryArithPlusExpr(lineNum, left, right);
            }else{
                left = new BinaryArithMinusExpr(lineNum,left,right);
            }
        }

        return left;
    }


    // <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
    // <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
    //               / <NewCastOrUnary> <MoreNCU> |
    //               % <NewCastOrUnary> <MoreNCU> |
    //               EMPTY
    private Expr parseMultExpr() {
        int lineNum = currentToken.position;
        Expr left = parseNewCastOrUnary();

        String op = currentToken.getSpelling();
        while(op.equals("*")||op.equals("/")||op.equals("%")){
            Expr right = parseNewCastOrUnary();
            switch (op){
                case "*":
                    left = new BinaryArithTimesExpr(lineNum,left,right);
                    break;
                case "/":
                    left = new BinaryArithDivideExpr(lineNum,left,right);
                    break;
                case "%":
                    left = new BinaryArithModulusExpr(lineNum,left,right);
                    break;
            }
        }

        return left;

    }

    // <NewCastOrUnary> ::= <NewExpression> | <CastExpression> | <UnaryPrefix>
    private Expr parseNewCastOrUnary() {
        switch(currentToken.kind){
            case NEW: // NewExpression
                currentToken = scanner.scan(true);
                return parseNew();
            case CAST: // CastExpression
                currentToken = scanner.scan(true);
                return parseCast();
            default: // UnaryPrefix
                return parseUnaryPrefix(); // todo: why does this not scan forward?
        }
    }


    // <NewExpression> ::= NEW <Identifier> ( )
    private Expr parseNew() {
        int lineNum = currentToken.position;

        //currentToken = scanner.scan(true); // gets Identifier
        ensureTokenType("Invalid New Statement: invalid identifier", Token.Kind.IDENTIFIER);
        String type = parseIdentifier();

        ensureTokenType("Incomplete Statement: New statement missing a '('", Token.Kind.LPAREN);

        currentToken = scanner.scan(true); // gets ')' or this is an error
        ensureTokenType("Incomplete Statement: New statement missing a ')'", Token.Kind.RPAREN);

        return new NewExpr(lineNum, type);

    }


    // <CastExpression> ::= CAST ( <Type> , <Expression> )
    private Expr parseCast() {
        int lineNum = currentToken.position;

        currentToken = scanner.scan(true); // gets '(' or this is an error
        ensureTokenType("Incomplete Statement: Cast statement missing a '('", Token.Kind.LPAREN);

        currentToken = scanner.scan(true); // gets type
        String type = parseType();

        currentToken = scanner.scan(true); // gets ',' or this is an error
        ensureTokenType("Incomplete Statement: Cast statement missing a ','", Token.Kind.COMMA);

        currentToken = scanner.scan(true); // gets expression
        Expr expr = parseExpression();

        currentToken = scanner.scan(true); // gets ')' or this is an error
        ensureTokenType("Incomplete Statement: Cast statement missing a ')'", Token.Kind.RPAREN);

        return new CastExpr(lineNum, type, expr);
    }


    // <UnaryPrefix> ::= <PrefixOp> <UnaryPreFix> | <UnaryPostfix>
    // <PrefixOp> ::= - | ! | ++ | --
    private Expr parseUnaryPrefix() {
        int position = currentToken.position;

        switch(currentToken.kind){
            case PLUSMINUS: // - or +
                currentToken = scanner.scan(true);
                return new UnaryNegExpr(position, parseUnaryPrefix());
            case UNARYNOT: // !
                currentToken = scanner.scan(true);
                return new UnaryNotExpr(position, parseUnaryPrefix());
            case UNARYINCR: // ++
                currentToken = scanner.scan(true);
                return new UnaryIncrExpr(position, parseUnaryPrefix(), false);
            case UNARYDECR: // --
                currentToken = scanner.scan(true);
                return new UnaryDecrExpr(position, parseUnaryPrefix(), false);
            default:
                return parseUnaryPostfix(); // todo: why does this not have a scanner.scan line?
        }
    }


    // <UnaryPostfix> ::= <Primary> <PostfixOp>
    // <PostfixOp> ::= ++ | -- | EMPTY
    private Expr parseUnaryPostfix() {
        int lineNum = currentToken.position;

        Expr primary = parsePrimary(); // gets the primary


//        currentToken = scanner.scan(true); // gets the postfix operator
        // returns expression
        switch (currentToken.kind) {
            case UNARYINCR:
                return new UnaryIncrExpr(lineNum, primary, true); // ++
            case UNARYDECR:
                return new UnaryDecrExpr(lineNum, primary, true); // --
            default:

                return primary; // empty, so just the primary
        }
    }


    // <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
    //                              <StringConst> | <VarExpr>
    // <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
    // <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
    // <VarExprSuffix> ::= ( <Arguments> ) | EMPTY
    private Expr parsePrimary() {
        int startPosition = currentToken.position;
        Expr expr;
        ExprList args;
        // handle ( <Expression> )
        if(currentToken.kind == Token.Kind.LPAREN){
            currentToken = scanner.scan(true); // @<Expression>

            expr = parseExpression(); // @ )
            ensureTokenType("Incomplete Expression: Unclosed Parenthesis", Token.Kind.RPAREN);
            currentToken = scanner.scan(true); // prep next token
            //handle integerConst
        }else if(currentToken.kind == Token.Kind.INTCONST){
            expr = parseIntConst(); // @ next token -- throws error
            //handle booleanConst
        }else if(currentToken.kind == Token.Kind.BOOLEAN){
            expr = parseBoolean(); // @ next token -- throws error
            // handle StringConst
        }else if(currentToken.kind == Token.Kind.STRCONST){
            expr = parseStringConst(); // @ next token -- throws error
            // handle VarExpr
        } else{// @<VarExprPrefix>::= SUPER . | THIS . | EMPTY
            Expr refExpr = null;
            String methodName;
            if("super".equals(currentToken.spelling)
                    || "this".equals(currentToken.spelling)){

                refExpr = parseExpression(); //currentToken -> '.'
                ensureTokenType("reference call missing seperator '.'", Token.Kind.DOT);

                currentToken = scanner.scan(true); // '.' -> <identifier>
            }

            methodName = parseIdentifier();
            if(currentToken.kind.equals(Token.Kind.LPAREN)) {

                // handle ( <Arguments> )
                currentToken = scanner.scan(true); // @<Expression>
                args = parseArguments(); // @ )
                ensureTokenType("Method Call Incomplete : Unclosed Parenthesis, Missing')",
                        Token.Kind.RPAREN);

                currentToken = scanner.scan(true); // prep next token
                expr = new DispatchExpr(startPosition, refExpr, methodName, args);
            }else{
                expr = new VarExpr(startPosition,refExpr,methodName);
            }
        }

        return expr;
    }


    // <Arguments> ::= EMPTY | <Expression> <MoreArgs>
    // <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
    // todo: this should be re-refactored after parsePrimary
    private ExprList parseArguments() {
        ExprList arguments = new ExprList(currentToken.position); // makes empty ExprList

        while (currentToken.kind != Token.Kind.RPAREN){
            arguments.addElement(parseFormal()); // Add Expresion to ExprList

            currentToken = scanner.scan(true); // either a ',' or a ')' or we have an error
            ensureTokenType("Incomplete Statement: Parameter statement missing a ')' or ','", Token.Kind.COMMA, Token.Kind.RPAREN);

            if (currentToken.kind == Token.Kind.COMMA){ // if it's a ',' we ignore it and go to the next
                currentToken = scanner.scan(true);      // argument to parse
            }
        }

        return arguments;
    }


    // <Parameters> ::=  EMPTY | <Formal> <MoreFormals>
    // <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
    // todo: this should be re-refactored after parsePrimary
    private FormalList parseParameters() {
        FormalList parameters = new FormalList(currentToken.position); // makes empty FormalList

        while (currentToken.kind != Token.Kind.RPAREN){
            parameters.addElement(parseFormal()); // Add Formal to FormalList

            currentToken = scanner.scan(true); // either a ',' or a ')' or we have an error
            ensureTokenType("Incomplete Statement: Parameter statement missing a ')' or ','", Token.Kind.COMMA, Token.Kind.RPAREN);

            if (currentToken.kind == Token.Kind.COMMA){ // if it's a ',' we ignore it and go to the next
                currentToken = scanner.scan(true);      // formal parameter to parse
            }
        }

        return parameters;
    }


    // <Formal> ::= <Type> <Identifier>
    private Formal parseFormal() {
        int position = currentToken.position;

        String type = parseType();
        String name = parseIdentifier();
        return new Formal(position, type, name);
    }


    // <Type> ::= <Identifier>
    private String parseType() {
        return parseIdentifier();
    }


    //----------------------------------------
    //Terminals

    // todo: refactor this method once everything is fully implemented
    private String parseOperator() {
        if( currentToken.kind != Token.Kind.BINARYLOGIC ||
                currentToken.kind != Token.Kind.PLUSMINUS ||
                currentToken.kind != Token.Kind.MULDIV ||
                currentToken.kind != Token.Kind.COMPARE ||
                currentToken.kind != Token.Kind.UNARYINCR ||
                currentToken.kind != Token.Kind.UNARYDECR ||
                currentToken.kind != Token.Kind.ASSIGN ||
                currentToken.kind != Token.Kind.UNARYNOT){
            errorHandler.register(Error.Kind.PARSE_ERROR,
                    "Invalid Operator");
            throw new CompilationException(
                    "Expected a valid Operator",
                    new Throwable());
        }
        String operator = currentToken.getSpelling();
        currentToken = scanner.scan(true);
        return operator;
    }


    private String parseIdentifier() {
        // ensure the correct type
        ensureTokenType("Invalid identifier token", Token.Kind.IDENTIFIER);

        // get the needed information
        String identifier = currentToken.getSpelling();

        // ensure invariant
        currentToken = scanner.scan(true);
        return identifier;
    }


    private ConstStringExpr parseStringConst() {
        // ensure correct token type
        ensureTokenType("Invalid String Value: Not a string", Token.Kind.STRCONST);

        // extract needed information
        int position = currentToken.position;
        String constant = currentToken.getSpelling();

        // ensure invariant
        currentToken = scanner.scan(true);
        return new ConstStringExpr(position, constant);
    }


    private ConstIntExpr parseIntConst() {
        // ensure correct token type
        ensureTokenType("Invalid Integer Value: Expected Integer value in range 0-2147483647", Token.Kind.INTCONST);

        // get needed information
        int position = currentToken.position;
        String constant = currentToken.getSpelling();

        // ensure invariant
        currentToken = scanner.scan(true);
        return new ConstIntExpr(position, constant);
    }


    private ConstBooleanExpr parseBoolean() {
        // ensure correct token type
        ensureTokenType("Invalid Integer Value: Expected 'true' or 'false' as a boolean value", Token.Kind.BOOLEAN);

        // get needed information
        int position = currentToken.position;
        String constant = currentToken.getSpelling();

        // ensure invariant
        currentToken = scanner.scan(true);
        return new ConstBooleanExpr(position, constant);
    }

    /**
     * Helper method that checks the current token for the passed types. If it does not
     * match one of the passed types, the method registers an error with the passed
     * message with the internal error handler and raises a compilation exception.
     *
     * @param errorMessage to register and raise if needed
     * @param kinds        one or more Token.Kind values against which to check the type of the
     *                     current token
     */
    private void ensureTokenType(String errorMessage, Token.Kind... kinds) {
        // see if current token is one of the passed types
        boolean currentTokenTypeFound = false;
        for (Token.Kind kind : kinds) {
            if (currentToken.kind == kind) {
                currentTokenTypeFound = true;
                break;
            }
        }

        if (!currentTokenTypeFound) {
            registerAndThrow(errorMessage);
        }
    }

    private void registerAndThrow(String errorMessage) {

        System.out.println(currentToken.position +", "+ currentToken.spelling);
        errorHandler.register(Error.Kind.PARSE_ERROR, errorMessage);
        throw new CompilationException(errorMessage, new Throwable());
    }

}

