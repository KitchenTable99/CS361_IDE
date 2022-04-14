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
        advanceToken();
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
        String parent = null;
        advanceToken();
        String name = currentToken.spelling;
        advanceToken();
        //handle possible ExtendsClause
        if(currentToken.kind == Token.Kind.EXTENDS){
            advanceToken();
            parent = currentToken.spelling;
        }
        advanceToken();
        //Create member list from class body
        if(currentToken.kind == Token.Kind.LCURLY){
            advanceToken();
            while(currentToken.kind != Token.Kind.RCURLY){
                memberList.addElement(parseMember());
            }
        }else{
            //create parse error
        }
        return new Class_(position, scanner.getFilename(), name, parent, memberList);

    }


    //Fields and Methods
    // <Member> ::= <Field> | <Method>
    // <Method> ::= <Type> <Identifier> ( <Parameters> ) <BlockStmt>
    // <Field> ::= <Type> <Identifier> <InitialValue> ;
    // <InitialValue> ::= EMPTY | = <Expression>
    private Member parseMember() {
    }


    //-----------------------------------
    //Statements
    // <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <VarDeclaration>
    //             | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
    private Stmt parseStatement() {
        Stmt stmt;

        switch (currentToken.kind) {
            case IF:
                stmt = parseIf();
                break;
            case LCURLY:
                stmt = parseBlock();
                break;
            case VAR:
                stmt = parseVarDeclaration();
                break;
            case RETURN:
                stmt = parseReturn();
                break;
            case FOR:
                stmt = parseFor();
                break;
            case WHILE:
                stmt = parseWhile();
                break;
            case BREAK:
                stmt = parseBreak();
                break;
            default:
                stmt = parseExpressionStmt();
        }

        return stmt;
    }


    // <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
    private Stmt parseWhile() {
        int position = currentToken.position;

        if (scanner.scan(true).kind != Token.Kind.LPAREN) {
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid While Statement");
            throw new CompilationException(
                    "Incomplete Statement: Return statement missing an opening '('",
                    new Throwable());
        }

        currentToken = scanner.scan(true);
        Expr predStmt = parseExpression();

        if (currentToken.kind != Token.Kind.RPAREN) {
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid While Statement");
            throw new CompilationException(
                    "Incomplete Statement: Return statement missing a closing ')'",
                    new Throwable());
        }

        currentToken = scanner.scan(true);
        Stmt bodyStmt = parseStatement();

        return new WhileStmt(position, predStmt, bodyStmt);

    }


    // <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
    private Stmt parseReturn() {
        int position = currentToken.position;
        Expr returnExpression = null;
        advanceToken();
        if(IncompleteStatement()){
            returnExpression = parseExpression();
        }
        if(IncompleteStatement()){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Return Statement");
            throw new CompilationException(
                "Incomplete Statement: Return statement missing an ending ';'",
                new Throwable());
        }else{ // if at semicolon move to next token for next parsing
            advanceToken();
        }
        return new ReturnStmt(position, returnExpression);
    }


    // <BreakStmt> ::= BREAK ;
    private Stmt parseBreak() {
        int position = currentToken.position;

        if (scanner.scan(true).kind != Token.Kind.SEMICOLON) {
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Break Statement");
            throw new CompilationException(
                    "Invalid Statement: Break statement must end with ';'",
                    new Throwable());
        }

        return new BreakStmt(position);
    }


    // <ExpressionStmt> ::= <Expression> ;
    private ExprStmt parseExpressionStmt() {
        int position = currentToken.position;
        advanceToken();
        Expr expression = parseExpression();
        if(IncompleteStatement()){
            // create parser exception
        }
        return new ExprStmt(position, expression);
    }


    // <VarDeclaration> ::= VAR <Id> = <Expression> ;
    // Every local variable must be initialized
    private Stmt parseVarDeclaration() {
        int position = currentToken.position;
        String varType = currentToken.spelling;

        String varID = scanner.scan(true).spelling;
        Token equalsToken = scanner.scan(true);
        if (equalsToken.kind != Token.Kind.ASSIGN) {
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Declaration Statement");
            throw new CompilationException(
                    "Incomplete Statement: Declaration statement missing a '='",
                    new Throwable());
        }

        // parse the expression
        currentToken = scanner.scan(true);
        Expr expr = parseExpression(); // will move through all tokens and currentToken should be semicolon

        // todo: figure out if this is helpful or if already handled
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Declaration Statement");
            throw new CompilationException(
                    "Incomplete Statement: Declaration statement missing an ending ';'",
                    new Throwable());
        }
        currentToken = scanner.scan(true); // ensure invariant

        DeclStmt declStmt = new DeclStmt(position, varID, expr);
        declStmt.setType(varType);
        return declStmt;
    }


    // <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
    // <Start> ::=     EMPTY | <Expression>
    // <Terminate> ::= EMPTY | <Expression>
    // <Increment> ::= EMPTY | <Expression>
    private Stmt parseFor() {
        int position = currentToken.position;
        if (scanner.scan(true).kind != Token.Kind.LPAREN) {
            // todo raise error
        }

        currentToken = scanner.scan(true);
        Expr initExpr;
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            initExpr = parseExpression();    // todo: if this doesn't consume the ending semicolon update logic
        } else {
            initExpr = null;
            currentToken = scanner.scan(true);
        }

        Expr termExpr;
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            termExpr = parseExpression();    // todo: if this doesn't consume the ending semicolon update logic
        } else {
            termExpr = null;
            currentToken = scanner.scan(true);
        }

        Expr updateExpr;
        if (currentToken.kind != Token.Kind.SEMICOLON) {
            updateExpr = parseExpression();    // todo: if this doesn't consume the ending semicolon update logic
        } else {
            updateExpr = null;
            currentToken = scanner.scan(true);
        }

        if (scanner.scan(true).kind != Token.Kind.RPAREN) {
            // todo raise error
        }

        currentToken = scanner.scan(true);
        Stmt bodyStmt = parseStatement();

        return new ForStmt(position, initExpr, termExpr, updateExpr, bodyStmt);
    }


    // <BlockStmt> ::= { <Body> }
    // <Body> ::= EMPTY | <Stmt> <Body>
    private Stmt parseBlock() {
        int lineNum = currentToken.position;

        if (currentToken.kind != Token.Kind.LCURLY){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Block Statement");
                throw new CompilationException(
                        "Incomplete Statement: Block statement missing a '{'",
                        new Throwable());
        }

        StmtList stmtList = new StmtList(lineNum);
        currentToken = scanner.scan(true); // either a Body or a '}'
        while (currentToken.kind != Token.Kind.RCURLY){ // add statements until we are at our next '}'
            if (currentToken.kind == Token.Kind.EOF){ // if we reach end of file, we have an error
                errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Block Statement");
                throw new CompilationException(
                        "Incomplete Statement: Block statement missing a '}'",
                        new Throwable());
            }
            stmtList.addElement(parseStatement()); // add the statement
            currentToken = scanner.scan(true);
        }

        return new BlockStmt(lineNum, stmtList);

    }


    // <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
    private Stmt parseIf() {
        int lineNum = currentToken.position;
        if(currentToken.kind != Token.Kind.IF){
            errorHandler.register(Error.Kind.PARSE_ERROR,"Invalid If Statement");
            throw new CompilationException(
                    "Incomplete Statement: If statement missing IF", new Throwable()
            );
        }
        currentToken = scanner.scan(true); // (
        if (currentToken.kind != Token.Kind.LPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR,"Invalid If Statement");
            throw new CompilationException(
                    "Incomplete Statement: If statement missing (", new Throwable()
            );
        }

        currentToken = scanner.scan(true); // expr
        Expr predExpr = parseExpression();
        currentToken = scanner.scan(); // )
        if (currentToken.kind != Token.Kind.RPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR,"Invalid If Statement");
            throw new CompilationException(
                    "Incomplete Statement: If statement missing )", new Throwable()
            );
        }

        currentToken = scanner.scan();
        Stmt thenStmt = parseStatement();
        currentToken = scanner.scan();
        if(currentToken.kind == Token.Kind.ELSE){
            currentToken = scanner.scan();
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
       currentToken = scanner.scan();
       if(currentToken.kind == Token.Kind.ASSIGN ) {
           Expr right = parseExpression();
           String name = currentToken.getSpelling();
           return new AssignExpr(lineNum, refName, name, right);
       }
       currentToken = scanner.scan();
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

    }


    // <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
    //                      <RelationalExpr>
    // <equalOrNotEqual> ::=  = | !=
    private Expr parseEqualityExpr() {
    }


    // <RelationalExpr> ::= <AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
    // <ComparisonOp> ::= < | > | <= | >=
    private Expr parseRelationalExpr() {
    }


    // <AddExpr>::＝ <MultExpr> <MoreMultExpr>
    // <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
    private Expr parseAddExpr() {
    }


    // <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
    // <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
    //               / <NewCastOrUnary> <MoreNCU> |
    //               % <NewCastOrUnary> <MoreNCU> |
    //               EMPTY
    private Expr parseMultExpr() {
    }

    // <NewCastOrUnary> ::= <NewExpression> | <CastExpression> | <UnaryPrefix>
    private Expr parseNewCastOrUnary() {
        Expr expr;
        switch(currentToken.kind){
            case NEW: // NewExpression
                currentToken = scanner.scan(true);
                expr = parseNew();
                break;
            case CAST: // CastExpression
                currentToken = scanner.scan(true);
                expr = parseCast();
                break;
            default: // UnaryPrefix
                expr = parseUnaryPrefix();
                break;
        }
        return expr;
    }


    // <NewExpression> ::= NEW <Identifier> ( )
    private Expr parseNew() {
        int lineNum = currentToken.position;

        currentToken = scanner.scan(true); // gets Identifier
        String type = parseIdentifier();

        currentToken = scanner.scan(true); // gets '(' or this is an error
        if (currentToken.kind != Token.Kind.LPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid New Statement");
                throw new CompilationException(
                        "Incomplete Statement: New statement missing a '('",
                        new Throwable());
        }

        currentToken = scanner.scan(true); // gets ')' or this is an error
        if (currentToken.kind != Token.Kind.RPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid New Statement");
                throw new CompilationException(
                        "Incomplete Statement: New statement missing a ')'",
                        new Throwable());
        }

        return new NewExpr(lineNum, type);

    }


    // <CastExpression> ::= CAST ( <Type> , <Expression> )
    private Expr parseCast() {
        int lineNum = currentToken.position;

        currentToken = scanner.scan(true); // gets '(' or this is an error
        if (currentToken.kind != Token.Kind.LPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Cast Statement");
                throw new CompilationException(
                        "Incomplete Statement: Cast statement missing a '('",
                        new Throwable());
        }
        
        currentToken = scanner.scan(true); // gets type
        String type = parseType();

        currentToken = scanner.scan(true); // gets ',' or this is an error
        if (currentToken.kind != Token.Kind.COMMA){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Cast Statement");
                throw new CompilationException(
                        "Incomplete Statement: Cast statement missing a ','",
                        new Throwable());
        }

        currentToken = scanner.scan(true); // gets expression
        Expr expr = parseExpression();

        currentToken = scanner.scan(true); // gets ')' or this is an error
        if (currentToken.kind != Token.Kind.RPAREN){
            errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Cast Statement");
                throw new CompilationException(
                        "Incomplete Statement: Cast statement missing a ')'",
                        new Throwable());
        }

        return new CastExpr(lineNum, type, expr);
    }


    // <UnaryPrefix> ::= <PrefixOp> <UnaryPreFix> | <UnaryPostfix>
    // <PrefixOp> ::= - | ! | ++ | --
    private Expr parseUnaryPrefix() {
        int lineNum = currentToken.position; // stores line number

        Expr prefix;
        switch(currentToken.kind){
            case PLUSMINUS: // -
                currentToken = scanner.scan(true);
                prefix = new UnaryNegExpr(lineNum, parseUnaryPrefix());
                break;
            case UNARYNOT: // !
                currentToken = scanner.scan(true);
                prefix = new UnaryNotExpr(lineNum, parseUnaryPrefix());
                break;
            case UNARYINCR: // ++
                currentToken = scanner.scan(true);
                prefix = new UnaryIncrExpr(lineNum, parseUnaryPrefix(), false);
                break;
            case UNARYDECR: // --
                currentToken = scanner.scan(true);
                prefix = new UnaryDecrExpr(lineNum, parseUnaryPrefix(), false);
                break;
            default:
                prefix = parseUnaryPostfix();
        }

        return prefix;
    }


    // <UnaryPostfix> ::= <Primary> <PostfixOp>
    // <PostfixOp> ::= ++ | -- | EMPTY
    private Expr parseUnaryPostfix() {
        int lineNum = currentToken.position; // stores line number
        Expr primary = parsePrimary(); // gets the primary
        currentToken = scanner.scan(true); // gets the postfix operator

        Expr postfix; // gets our expression to return
        switch(currentToken.kind){
            case UNARYINCR:
                postfix = new UnaryIncrExpr(lineNum, primary, true); // ++
                break;
            case UNARYDECR:
                postfix = new UnaryDecrExpr(lineNum, primary, true); // --
                break;
            default:
                postfix = primary; // empty, so just the primary
        }

        return postfix;
    }


    // <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
    //                              <StringConst> | <VarExpr>
    // <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
    // <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
    // <VarExprSuffix> ::= ( <Arguments> ) | EMPTY
    private Expr parsePrimary() {
    }


    // <Arguments> ::= EMPTY | <Expression> <MoreArgs>
    // <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
    private ExprList parseArguments() {
        ExprList arguments = new ExprList(currentToken.position); // makes empty ExprList

        while (currentToken.kind != Token.Kind.RPAREN){
            arguments.addElement(parseFormal()); // Add Expresion to ExprList
            
            currentToken = scanner.scan(true); // either a ',' or a ')' or we have an error
            if (currentToken.kind != Token.Kind.COMMA && currentToken.kind != Token.Kind.RPAREN){
                errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Parameter Statement");
                throw new CompilationException(
                        "Incomplete Statement: Parameter statement missing a ')' or ','",
                        new Throwable());
            }

            if (currentToken.kind == Token.Kind.COMMA){ // if it's a ',' we ignore it and go to the next
                currentToken = scanner.scan(true);      // argument to parse
            }
        }

        return arguments;
    }


    // <Parameters> ::=  EMPTY | <Formal> <MoreFormals>
    // <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
    private FormalList parseParameters() {
        FormalList parameters = new FormalList(currentToken.position); // makes empty FormalList

        while (currentToken.kind != Token.Kind.RPAREN){
            parameters.addElement(parseFormal()); // Add Formal to FormalList
            
            currentToken = scanner.scan(true); // either a ',' or a ')' or we have an error
            if (currentToken.kind != Token.Kind.COMMA && currentToken.kind != Token.Kind.RPAREN){
                errorHandler.register(Error.Kind.PARSE_ERROR, "Invalid Parameter Statement");
                throw new CompilationException(
                        "Incomplete Statement: Parameter statement missing a ')' or ','",
                        new Throwable());
            }

            if (currentToken.kind == Token.Kind.COMMA){ // if it's a ',' we ignore it and go to the next
                currentToken = scanner.scan(true);      // formal parameter to parse
            }
        }

        return parameters;
    }


    // <Formal> ::= <Type> <Identifier>
    private Formal parseFormal() {
        int lineNum = currentToken.position;
        String type = parseType();
        String name = parseIdentifier();
        return new Formal(lineNum, type, name);
    }


    // <Type> ::= <Identifier>
    private String parseType() {
        return parseIdentifier();
    }


    //----------------------------------------
    //Terminals

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
        advanceToken();
        return operator;
    }


    private String parseIdentifier() {
        if(currentToken.kind != Token.Kind.IDENTIFIER){
            errorHandler.register(Error.Kind.PARSE_ERROR, 
            "Invalid valid identifier");
            throw new CompilationException(
                    "Expected a valid identifier name",
                    new Throwable());
        }
        String identifier = currentToken.getSpelling();
        advanceToken();
        return identifier;
    }


    private ConstStringExpr parseStringConst() {
        //...save the currentToken's string to a local variable...
        //...advance to the next token...
        //...return a new ConstStringExpr containing the string...
        if(currentToken.kind != Token.Kind.STRCONST){
            errorHandler.register(Error.Kind.PARSE_ERROR, 
            "Invalid String Value");
            throw new CompilationException(
                    "Expected a valid string value",
                    new Throwable());
        }
        int position = currentToken.position;
        String constant = currentToken.getSpelling();
        advanceToken();
        return new ConstStringExpr(position, constant);
    }


    private ConstIntExpr parseIntConst() {
        if(currentToken.kind != Token.Kind.INTCONST){
            errorHandler.register(Error.Kind.PARSE_ERROR, 
            "Invalid Integer Value");
            throw new CompilationException(
                    "Expected Integer value in range 0-2147483647",
                    new Throwable());
        }
        int position = currentToken.position;
        String constant = currentToken.getSpelling();
        advanceToken();
        return new ConstIntExpr(position, constant);
    }


    private ConstBooleanExpr parseBoolean() {
        if(currentToken.kind != Token.Kind.BOOLEAN){
            errorHandler.register(Error.Kind.PARSE_ERROR, 
            "Invalid Boolean Value");
            throw new CompilationException(
                    "Expected 'true' or 'false as a boolean value",
                    new Throwable());
        }
        int position = currentToken.position;
        String constant = currentToken.getSpelling();
        advanceToken();
        return new ConstBooleanExpr(position, constant);
    }

    private void advanceToken(){
        // todo: this method should not be used
        do{
            currentToken = scanner.scan(true);
        // "If a comment, throw it away!"
        }while(currentToken.kind == Token.Kind.COMMENT);
    }

    private boolean IncompleteStatement(){
        return currentToken.kind != Token.Kind.SEMICOLON;
    }

}

