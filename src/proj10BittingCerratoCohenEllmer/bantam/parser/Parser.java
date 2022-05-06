/*
 * Parser.java												2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 *
 *
 * Modified by Haoyu Song for a REVISED LL(1) version of Bantam Java
 * The parser is completely recursive descending
 * 		1)Give precedence to operators
 * 		2)Simplify the AST structures
 * 		3)Support more operations and special symbols
 * 		4)Add more keywords to the beginning of expressions and statements and thus make
 * 		the grammar mostly LL(1)
 *
 * Modified by Dale Skrien to clean up the code
 *
 * In the grammar below, the variables are enclosed in angle brackets and
 * "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * EMPTY indicates a rule with an empty right hand side.
 * All other symbols in the rules are terminals.
 */

package proj10BittingCerratoCohenEllmer.bantam.parser;

import proj10BittingCerratoCohenEllmer.bantam.ast.*;
import proj10BittingCerratoCohenEllmer.bantam.lexer.Scanner;
import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.treedrawer.Drawer;
import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;
import proj10BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.Reader;
import java.util.List;

import static proj10BittingCerratoCohenEllmer.bantam.lexer.Token.Kind.*;


public class Parser {
    // instance variables
    private Scanner scanner;
    private Token currentToken;
    private ErrorHandler errorHandler;


    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    //----------------------------------
    // checks whether the kind of the current token matches tokenKindExpected.
    // If so, fetches the next token.
    // If not, reports a syntactic bantam.error.

    private void advanceIfTokenMatches(Token.Kind tokenKindExpected) {
        if (currentToken.kind == COMMENT) {
            advance();
        }
        if (currentToken.kind == tokenKindExpected) {
            advance(); // move on to the next token
        } else if (currentToken.kind == VAR && tokenKindExpected == IDENTIFIER){
            advance(); // if we are expecting an Identifier and Get a Var, this is just a type declaration
                       // and we can continue parsing without issue, we pass any issues on to the semantic
                       // analyzer
        }else {
            reportSyntacticError(currentToken.position, tokenKindExpected.name(),
                    currentToken.spelling);
        }
    }

    // unconditionally fetch the next token
    private void advance() {
        do {
            currentToken = scanner.scan();
        } while (currentToken.kind == COMMENT);
    }


    //----------------------------------
    //register a SyntaxError and throw a CompilationException to exit from parsing
    private void reportSyntacticError(int position, String expectedToken,
                                      String metToken) {
        String message =
                "At line " + position + ", expected " + expectedToken + ", " + "got" +
                        " " + metToken + " instead.\n ";
        errorHandler.register(Error.Kind.PARSE_ERROR, scanner.getFilename(), position,
                message);
        // exit immediately because the parser can't continue
        throw new CompilationException(message, new Throwable());
    }

    /**
     * parse the given file and return the root node of the AST
     *
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {

        //set up scanner
        scanner = new Scanner(filename, errorHandler);

        // start scanning and parsing
        advance();
        return parseProgram();
    }

    // parse the characters in the reader and return the AST
    public Program parse(Reader reader) {

        //set up scanner
        scanner = new Scanner(reader, errorHandler);

        // start scanning and parsing
        advance();
        return parseProgram();
    }


    //------------------------------
    //Begin Parsing

    //<Program> ::= <Class> | <Class> <Program>
    private Program parseProgram() {

        int position = currentToken.position;
        ClassList clist = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            clist.addElement(aClass);
        }

        return new Program(position, clist);
    }

    //-----------------------------
    //<Class> ::= CLASS <Identifier> <Extension> { <MemberList> }
    //<Extension> ::= EXTENDS <Identifier> | EMPTY
    //<MemberList> ::= EMPTY | <Member> <MemberList>
    private Class_ parseClass() {

        Class_ aClass;
        int position = currentToken.position;

        advanceIfTokenMatches(CLASS);
        Token className = currentToken;
        advanceIfTokenMatches(IDENTIFIER);
        String parentName = null;
        if (currentToken.kind == EXTENDS) {
            advance();
            parentName = parseIdentifier();
        } else {
            parentName = "Object";
        }

        MemberList memberList = new MemberList(currentToken.position);
        advanceIfTokenMatches(LCURLY);
        while (currentToken.kind != RCURLY && currentToken.kind != EOF) {
            Member member = parseMember();
            memberList.addElement(member);
        }
        advanceIfTokenMatches(RCURLY);

        aClass = new Class_(position, scanner.getFilename(), className.spelling,
                parentName, memberList);
        return aClass;
    }


    //-----------------------------------
    //Fields and Methods

    //<Member> ::= <Field> | <Method>
    //<Method> ::= <Type> <Identifier> ( <Parameters> ) <BlockStmt>>
    //<Field> ::= <Type> <Identifier> <OptInitialValue> ;
    //<OptInitialValue> ::= EMPTY | = <Expression>
    private Member parseMember() {
        Method method;
        String type = parseType();

        String id = parseIdentifier();
        BlockStmt stmt;
        int position = currentToken.position;

        if (currentToken.kind == LPAREN) // it is a method
        {
            advance();
            FormalList parameters = parseParameters();
            advanceIfTokenMatches(RPAREN);
            stmt = (BlockStmt) parseBlock();
            method = new Method(position, type, id, parameters, stmt.getStmtList());
            return method;
        } else {
            Expr init = null;

            if (currentToken.kind == ASSIGN) {
                advance();
                init = parseExpression();
            }

            advanceIfTokenMatches(SEMICOLON);

            return new Field(position, type, id, init);
        }

    }


    //-----------------------------------
    //<Stmt>::= <IfStmt> | <BlockStmt> | <DeclStmt> | <ReturnStmt>
    //          <ForStmt> | <WhileStmt> | <BreakStmt> | <ExpressionStmt>
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
                stmt = parseDeclStmt();
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


    //<WhileStmt>::= WHILE ( <Expression> ) <Stmt>
    private Stmt parseWhile() {
        int position = currentToken.position;

        advance(); // past "while"
        advanceIfTokenMatches(LPAREN);
        Expr expression = parseExpression();
        advanceIfTokenMatches(RPAREN);
        Stmt execution = parseStatement();

        return new WhileStmt(position, expression, execution);
    }


    //<ReturnStmt>::= RETURN <Expression> ; | RETURN ;
    // private Stmt parseReturn() {
    //     int position = currentToken.position;
    //     Expr expr = null;
    //
    //     advance(); // accept the RETURN token
    //
    //     if (currentToken.kind != SEMICOLON) {
    //         expr = parseExpression();
    //     }
    //     advanceIfMatches(SEMICOLON);
    //
    //     return new ReturnStmt(position, expr);
    // }
    private Stmt parseReturn() {
        // currentToken is “return" at the start of this method
        int position = currentToken.position;
        Expr expr = null;

        advance();

        if (currentToken.kind == SEMICOLON) {
            advance();
            return new ReturnStmt(position, null);
        } else {
            expr = parseExpression();
            advanceIfTokenMatches(SEMICOLON);
            return new ReturnStmt(position, expr);
        }
    }


    //<BreakStmt>::= BREAK ;
    private Stmt parseBreak() {
        Stmt stmt = new BreakStmt(currentToken.position);
        advance();
        advanceIfTokenMatches(SEMICOLON);
        return stmt;
    }


    //<ExpressionStmt>::= <Expression> ;
    private ExprStmt parseExpressionStmt() {
        int position = currentToken.position;
        Expr expr = parseExpression();
        advanceIfTokenMatches(SEMICOLON);
        return new ExprStmt(position, expr);
    }


    //<DeclStmt>::= VAR <Id> = <Expression>;
    //This makes sure that every local variable is initialized
    private Stmt parseDeclStmt() {

        int position = currentToken.position;
        DeclStmt stmt;
        String type = currentToken.spelling;
        advance(); // the keyword var

        String id = parseIdentifier();
        advanceIfTokenMatches(ASSIGN);
        Expr value = parseExpression();

        stmt = new DeclStmt(position, id, value);
        stmt.setType(type);
        advanceIfTokenMatches(SEMICOLON);

        return stmt;
    }


    //<ForStmt>::=FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
    //<Start>::= EMPTY | <Expression>
    //<Terminate>::= EMPTY | <Expression>
    //<Increment>::= EMPTY | <Expression>
    private Stmt parseFor() {

        int position = currentToken.position;
        Expr start = null;
        Expr terminate = null;
        Expr increment = null;
        Stmt execute;
        advance();

        advanceIfTokenMatches(LPAREN);

        //allow the possibility that start,terminate and increment are null
        if (currentToken.kind != SEMICOLON) {
            start = parseExpression();
        }
        advanceIfTokenMatches(SEMICOLON);

        if (currentToken.kind != SEMICOLON) {
            terminate = parseExpression();
        }
        advanceIfTokenMatches(SEMICOLON);

        if (currentToken.kind != RPAREN) {
            increment = parseExpression();
        }
        advanceIfTokenMatches(RPAREN);

        execute = parseStatement();

        return new ForStmt(position, start, terminate, increment, execute);
    }


    //<BlockStmt>::=  { <Body> }
    //<Body>::= EMPTY | <Stmt> <Body>
    private Stmt parseBlock() {

        int position = currentToken.position;
        StmtList stmtList = new StmtList(position);
        advanceIfTokenMatches(LCURLY);

        while (currentToken.kind != RCURLY) {
            stmtList.addElement(parseStatement());
        }
        advanceIfTokenMatches(RCURLY);

        return new BlockStmt(position, stmtList);
    }


    //<IfStmt>::= IF (<Expr>) <Stmt> | IF (<Expr>) <Stmt> ELSE <Stmt>
    private Stmt parseIf() {

        int position = currentToken.position;
        Expr condition;
        Stmt thenStmt;
        Stmt elseStmt = null;

        advance();
        advanceIfTokenMatches(LPAREN);
        condition = parseExpression();
        advanceIfTokenMatches(RPAREN);
        thenStmt = parseStatement();

        if (currentToken.kind == ELSE) {
            advance();
            elseStmt = parseStatement();
        }

        return new IfStmt(position, condition, thenStmt, elseStmt);
    }


    //==============================================
    //Expressions
    //Here we introduce the precedence to operations

    //<Expression>::= <LogicalOrExpr> <OptionalAssignment>
    // <OptionalAssignment>::=  = <Expression> | EMPTY
    private Expr parseExpression() {
        Expr result;
        int position = currentToken.position;

        result = parseOrExpr();
        if (currentToken.kind == ASSIGN && result instanceof VarExpr) {
            advance();
            VarExpr lhs = (VarExpr) result;
            Expr lhsRef = lhs.getRef();
            if (lhsRef != null && (!(lhsRef instanceof VarExpr)
                    || ((VarExpr) lhsRef).getRef() != null))
                reportSyntacticError(position, "a name or a name.name", "expr.name.name");
            Expr right = parseExpression();
            String lhsName = lhs.getName();
            String lhsRefName = (lhs.getRef() == null ? null :
                    ((VarExpr) lhs.getRef()).getName());
            result = new AssignExpr(position, lhsRefName, lhsName, right);
        }

        return result;
    }


    //<LogicalOR>::= <logicalAND> <LogicalORRest>
    //<LogicalORRest>::= || <LogicalAND> <LogicalORRest> | EMPTY
    private Expr parseOrExpr() {
        int position = currentToken.position;
        Expr left;

        left = parseAndExpr();
        while (currentToken.spelling.equals("||")) {
            advance();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    //<LogicalAND>::=<ComparisonExpr> <LogicalANDRest>
    //<LogicalANDRest>::= && <ComparisonExpr> <LogicalANDRest> | EMPTY
    private Expr parseAndExpr() {
        int position = currentToken.position;
        Expr left = parseComparisonExpr();
        while (currentToken.spelling.equals("&&")) {
            advance();
            Expr right = parseComparisonExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    //<ComparisonExpr>::= <RelationalExpr> <EqualOrNotEqual> <RelationalExpr> |
    //                     <RelationalExpr>
    //<EqualOrNotEqual>::=   == | !=
    private Expr parseComparisonExpr() {
        int position = currentToken.position;
        Expr left = parseRelationalExpr();

        if (currentToken.spelling.equals("==")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompEqExpr(position, left, right);
        } else if (currentToken.spelling.equals("!=")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompNeExpr(position, left, right);
        }

        return left;
    }


    //<RelationalExpr>::= <AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
    //<ComparisonOp>::= < | > | <= | >= | INSTANCEOF
    private Expr parseRelationalExpr() {
        int position = currentToken.position;
        Expr left, right;

        left = parseAddExpr();
        switch (currentToken.spelling) {
            case "<":
                advance();
                right = parseAddExpr();
                return new BinaryCompLtExpr(position, left, right);
            case "<=":
                advance();
                right = parseAddExpr();
                return new BinaryCompLeqExpr(position, left, right);
            case ">":
                advance();
                right = parseAddExpr();
                return new BinaryCompGtExpr(position, left, right);
            case ">=":
                advance();
                right = parseAddExpr();
                return new BinaryCompGeqExpr(position, left, right);
            case "instanceof":
                advance();
                String type = parseType();
                return new InstanceofExpr(position, left, type);
        }

        return left;
    }


    //<AddExpr>::＝ <MultExpr> <MoreMult>
    //<MoreMult>::= + <MultExpr> <MoreMult> | - <MultiExpr> <MoreMult> | EMPTY
    private Expr parseAddExpr() {
        int position = currentToken.position;
        Expr left = parseMultExpr();

        while (currentToken.kind == PLUSMINUS) {
            if (currentToken.spelling.equals("+")) {
                advance();
                Expr right = parseMultExpr();
                left = new BinaryArithPlusExpr(position, left, right);
            } else {
                advance();
                Expr right = parseMultExpr();
                left = new BinaryArithMinusExpr(position, left, right);
            }
        }

        return left;
    }


    //<MultiDiv>::= <NewCastOrUnary> <MoreNCU>
    //<MoreNCU>::= * <NewCastOrUnary> <MoreNCU> |
    //             / <NewCastOrUnary> <MoreNCU> |
    //             % <NewCastOrUnary> <MoreNCU> |
    //             EMPTY
    private Expr parseMultExpr() {
        int position = currentToken.position;
        Expr left, right;


        left = parseNewCastOrUnary();
        while (currentToken.kind == MULDIV) {
            switch (currentToken.spelling) {
                case "/":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithDivideExpr(position, left, right);
                    break;
                case "*":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithTimesExpr(position, left, right);
                    break;
                case "%":
                    advance();
                    right = parseNewCastOrUnary();
                    left = new BinaryArithModulusExpr(position, left, right);
                    break;
            }
        }

        return left;
    }

    //<NewCastOrUnary>::= <NewExpression> | <CastExpression> | <UnaryPrefix>
    private Expr parseNewCastOrUnary() {
        Expr result;

        switch (currentToken.kind) {
            case NEW:
                result = parseNew();
                break;
            case CAST:
                result = parseCast();
                break;
            default:
                result = parseUnaryPrefix();
        }

        return result;
    }


    //<NewExpression>::= NEW <Identifier>()
    private Expr parseNew() {
        int position = currentToken.position;
        advance();
        String type = parseIdentifier();
        advanceIfTokenMatches(LPAREN);
        advanceIfTokenMatches(RPAREN);
        return new NewExpr(position, type);
    }

    //<CastExpression>::= CAST ( <Type> , <Expression> )
    private Expr parseCast() {

        Expr castExpression;
        int position = currentToken.position;
        advance();

        advanceIfTokenMatches(LPAREN);
        String type = parseType();
        advanceIfTokenMatches(COMMA);
        Expr expression = parseExpression();
        advanceIfTokenMatches(RPAREN);

        castExpression = new CastExpr(position, type, expression);
        return castExpression;
    }


    //<UnaryPrefix>::= <PrefixOp> <UnaryPreFix> | <UnaryPostfix>
    //<PrefixOp>::= - | ! | ++ | --
    private Expr parseUnaryPrefix() {
        int position = currentToken.position;
        Token.Kind kind = currentToken.kind;

        if (currentToken.spelling.equals("-") || kind == UNARYDECR || kind == UNARYINCR ||
                kind == UNARYNOT) {
            advance();
            Expr expr = parseUnaryPrefix();
            if (kind == PLUSMINUS) {
                return new UnaryNegExpr(position, expr);
            } else if (kind == UNARYDECR) {
                return new UnaryDecrExpr(position, expr, false);
            } else if (kind == UNARYINCR) {
                return new UnaryIncrExpr(position, expr, false);
            } else // kind == UNARYNOT
            {
                return new UnaryNotExpr(position, expr);
            }
        } else {
            return parseUnaryPostfix();
        }

    }


    //<UnaryPostfix>::= <Primary> <PostfixOp>
    //<PostfixOp>::= ++ | -- | EMPTY
    private Expr parseUnaryPostfix() {

        Expr unary;
        int position = currentToken.position;

        unary = parsePrimary();
        if (currentToken.kind == UNARYINCR) {
            unary = new UnaryIncrExpr(position, unary, true);
            advance();
        } else if (currentToken.kind == UNARYDECR) {
            unary = new UnaryDecrExpr(position, unary, true);
            advance();
        }

        return unary;
    }


    /*
     * <Primary> ::= ( <Expression> ) <Suffix> | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> <Suffix> | <Identifier> <Suffix>
     * <Suffix> ::=    . <Identifier> <Suffix>
     *               | ( <Arguments> ) <Suffix>
     *               | EMPTY
     */

    /*
     * <Primary> ::= ( <Expression> ) <ExprSuffix> | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> <IdSuffix> | <Identifier> <Suffix>
     * <IdSuffix>    ::=  . <Identifier> <Suffix> | EMPTY
     * <DispSuffix>  ::=  ( <Arguments> ) <IdSuffix> | EMPTY
     * <ExprSuffix>  ::=  <IdSuffix> | <IndexSuffix>
     * <Suffix>      ::=  <IdSuffix> | <DispSuffix> | <IndexSuffix>
     */
    private Expr parsePrimary() {
        Expr primary;

        switch (currentToken.kind) {
            case INTCONST:
                return parseIntConst();
            case BOOLEAN:
                return parseBoolean();
            case STRCONST:
                primary = parseStringConst();
                break;
            case LPAREN:
                advance();
                primary = parseExpression();
                advanceIfTokenMatches(RPAREN);
                if (currentToken.kind == LPAREN) //cannot have ( expr )( args )
                    reportSyntacticError(currentToken.position,
                            "something other than \"(\"",
                            currentToken.kind.name());
                break;
            default:
                String id = parseIdentifier();
                primary = new VarExpr(currentToken.position, null, id);
        }
        // now add the suffixes
        while (currentToken.kind == DOT
                || currentToken.kind == LPAREN && primary instanceof VarExpr) {
            if (currentToken.kind == LPAREN) {
                advance();
                ExprList args = parseArguments();
                advanceIfTokenMatches(RPAREN);
                VarExpr varExpr = (VarExpr) primary;
                // if no ref, then add "this" as the ref
                if (varExpr.getRef() == null) {
                    varExpr = new VarExpr(varExpr.getLineNum(),
                            new VarExpr(-1, null, "this"),
                            varExpr.getName());
                }
                primary = new DispatchExpr(primary.getLineNum(), varExpr.getRef(),
                        varExpr.getName(), args);
            } else { // currentToken is a DOT
                advance();
                String id = parseIdentifier();
                primary = new VarExpr(currentToken.position, primary, id);
            }
        }

        return primary;
    }


    //<Arguments> ::= EMPTY | <Expression> <MoreArgs>
    //<MoreArgs> ::= EMPTY | , <Expression> <MoreArgs>
    private ExprList parseArguments() {
        int position = currentToken.position;

        ExprList ar = new ExprList(position);

        if (currentToken.kind == RPAREN) {
            return ar;
        } else {
            ar.addElement(parseExpression());
            while (currentToken.kind != RPAREN) {
                advanceIfTokenMatches(COMMA);
                ar.addElement(parseExpression());
            }
        }

        return ar;
    }


    //<Parameters> ::=  EMPTY | <Formal> <MoreFormals>
    //<MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
    private FormalList parseParameters() {
        int position = currentToken.position;

        FormalList parameters = new FormalList(position);

        if (currentToken.kind == RPAREN) {
            return parameters;
        } else {
            parameters.addElement(parseFormal());
            while (currentToken.kind != RPAREN) {
                advanceIfTokenMatches(COMMA);
                parameters.addElement(parseFormal());
            }
        }

        return parameters;
    }


    //<Formal> ::= <Type> <Identifier>
    private Formal parseFormal() {
        return new Formal(currentToken.position, parseType(), parseIdentifier());
    }


    //<Type> ::= <Identifier>
    private String parseType() {
        String id = parseIdentifier();
        return id;
    }


    //----------------------------------------
    //Terminals


    private String parseOperator() {
        String op = currentToken.getSpelling();
        advance();
        return op;
    }


    private String parseIdentifier() {
        String name = currentToken.getSpelling();
        advanceIfTokenMatches(IDENTIFIER);
        return name;
    }


    private ConstStringExpr parseStringConst() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;
        advanceIfTokenMatches(STRCONST);
        return new ConstStringExpr(position, spelling);
    }


    private ConstIntExpr parseIntConst() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;
        advanceIfTokenMatches(INTCONST);
        return new ConstIntExpr(position, spelling);
    }


    private ConstBooleanExpr parseBoolean() {
        int position = currentToken.position;
        String spelling = currentToken.spelling;
        advanceIfTokenMatches(BOOLEAN);
        return new ConstBooleanExpr(position, spelling);
    }


    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);

        args = new String[]{"testsByDale/AAA.btm"};

        for (String inFile : args) {
            System.out.println("\n========== Results for " + inFile + " =============");
            try {
                errorHandler.clear();
                Program program = parser.parse(inFile);
                System.out.println("  Parsing was successful.");
                new Drawer().draw(inFile, program);
            } catch (CompilationException ex) {
                System.out.println("  There were errors:");
                List<Error> errors = errorHandler.getErrorList();
                for (Error error : errors) {
                    System.out.println("\t" + error.toString());
                }
            }
        }

    }

}

