/*
 * File: PrettyPrinter.java
 * Names: Ricky Peng, Andy Xu, Alex Yu
 * Class: CS 361
 * Project 10
 * Date: May 6, 2022
 */

package proj10BittingCerratoCohenEllmer.bantam;

import proj10BittingCerratoCohenEllmer.bantam.ast.*;
import proj10BittingCerratoCohenEllmer.bantam.parser.Parser;
import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;
import proj10BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
import proj10BittingCerratoCohenEllmer.bantam.visitor.Visitor;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;


public class PrettyPrinterVisitor extends Visitor {

    // stores the pretty printed content in a string
    private String outString;
    // records the current level of indentation
    private int indentationLevel;

    public String generateOutputString(ASTNode rootNode) {
        outString = "";
        indentationLevel = 0;
        rootNode.accept(this);
        return outString;
    }

    public String getOutString() {
        return outString;
    }

    public void setOutString(String newCode) {
        this.outString = newCode;
    }

    @Override
    public String visit(Class_ node) {
        outString += "\n";
        outString += "class " + node.getName() + " ";
        if (! node.getParent().equals("Object")) {
           outString += "extends " + node.getParent() + " ";
        }
        startBlock();
        endBlock();
        return null;
    }

    @Override
    public Object visit(Field node) {
        addIndentation();
        outString += node.getType() + " " + node.getName();
        if (node.getInit() != null) {
            outString += " = ";
            node.getInit().accept(this);
        }
        outString += "; \n";
        return null;
    }

    @Override
    public Object visit(Method node) {
        outString += "\n";
        addIndentation();
        outString += node.getReturnType() + " " + node.getName() + "(";
        node.getFormalList().accept(this);
        // remove the last two chars, ie. "," and " ", if formal list is not empty
        int strLength = outString.length();
        if (outString.charAt(strLength-1) != '(') {
            outString = outString.substring(0, strLength - 2);
        }
        outString += ") ";
        startBlock();
        node.getStmtList().accept(this);
        endBlock();
        return null;
    }

    @Override
    public Object visit(Formal node) {
        outString += node.getType() + " " + node.getName() + ", ";
        return null;
    }

    @Override
    public Object visit(DeclStmt node) {
        addIndentation();
        outString += "var " + node.getName() + " = ";
        node.getInit().accept(this);
        outString += "; \n";
        return null;
    }

    @Override
    public Object visit(ExprStmt node) {
        addIndentation();
        node.getExpr().accept(this);
        outString += "; \n";
        return null;
    }

    @Override
    public Object visit(IfStmt node) {
        addIndentation();
        outString += "if (";
        node.getPredExpr().accept(this);
        outString += ") ";
        startBlock();
        node.getThenStmt().accept(this);
        endBlock();
        if (node.getElseStmt() != null) {
            addIndentation();
            outString += "else ";
            startBlock();
            node.getElseStmt().accept(this);
            endBlock();
        }
        return null;
    }

    @Override
    public Object visit(WhileStmt node) {
        addIndentation();
        outString += "while (";
        node.getPredExpr().accept(this);
        outString += ") ";
        startBlock();
        node.getBodyStmt().accept(this);
        endBlock();
        return null;
    }

    @Override
    public Object visit(ForStmt node) {
        addIndentation();
        outString += "for (";
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        outString += "; ";
        if (node.getPredExpr() != null) {
            node.getPredExpr().accept(this);
        }
        outString += "; ";
        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
        outString += ") ";
        startBlock();
        node.getBodyStmt().accept(this);
        endBlock();
        return null;
    }

    @Override
    public Object visit(BreakStmt node) {
        addIndentation();
        outString += "break; \n";
        return null;
    }

    @Override
    public Object visit(ReturnStmt node) {
        addIndentation();
        outString += "return";
        if (node.getExpr() != null) {
            outString += " ";
            node.getExpr().accept(this);
        }
        outString += "; \n";
        return null;
    }

    @Override
    public Object visit(DispatchExpr node) {
        if (node.getRefExpr() != null) {
            node.getRefExpr().accept(this);
            outString += ".";
        }
        outString += node.getMethodName() + "(";
        node.getActualList().accept(this);
        // remove the ending ", " if the actual list is not empty
        if (outString.charAt(outString.length()-1) != '(') {
            int strLength = outString.length();
            outString = outString.substring(0, strLength - 2);
        }
        outString += ")";
        return null;
    }

    @Override
    public Object visit(ExprList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
            ((Expr) it.next()).accept(this);
            outString += ", ";
        }
        return null;
    }

    @Override
    public Object visit(NewExpr node) {
        outString += "new " + node.getType() + "()";
        return null;
    }

    @Override
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        outString += " instanceof " + node.getType();
        return null;
    }

    @Override
    public Object visit(CastExpr node) {
        outString += "cast(" + node.getType() + ", ";
        node.getExpr().accept(this);
        outString += ")";
        return null;
    }

    @Override
    public Object visit(AssignExpr node) {
        if (node.getRefName() != null) {
            outString += node.getRefName() + ".";
        }
        outString += node.getName() + " = ";
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompEqExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompNeExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompLeqExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryArithTimesExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        outString += " " + node.getOpName() + " ";
        node.getRightExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(UnaryNegExpr node) {
        outString += node.getOpName();
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(UnaryNotExpr node) {
        outString += node.getOpName();
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(UnaryIncrExpr node) {
        if (node.isPostfix()) {
            node.getExpr().accept(this);
            outString += node.getOpName();
        } else {
            outString += node.getOpName();
            node.getExpr().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(UnaryDecrExpr node) {
        if (node.isPostfix()) {
            node.getExpr().accept(this);
            outString += node.getOpName();
        } else {
            outString += node.getOpName();
            node.getExpr().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(VarExpr node) {
        if (node.getRef() != null) {
            node.getRef().accept(this);
            outString += ".";
        }
        outString += node.getName();
        return null;
    }


    @Override
    public Object visit(ConstIntExpr node) {
        outString += node.getIntConstant();
        return null;
    }

    @Override
    public Object visit(ConstBooleanExpr node) {
        outString += node.getConstant();
        return null;
    }

    @Override
    public Object visit(ConstStringExpr node) {
        outString += node.getConstant();
        return null;
    }


    /* Add the right level of indentation at the start of a new line. */
    public void addIndentation() {
        outString += new String(new char[indentationLevel]).replace("\0", "\t");
    }

    /* Start a new block by print out a left curly brace and switch to a new line. */
    public void startBlock() {
        Desktop desk = Desktop.getDesktop();
        try {
            desk.browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
            System.out.println("Note: This pretty printer was written by Andy and crew then" +
                    " inserted into our project just to obfuscate the Rick Roll");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        outString += "{ \n";
        indentationLevel++;
    }

    /* Add the right curly brace at the right level of indentation. */
    public void endBlock() {
        indentationLevel--;
        addIndentation();
        outString += "} \n";
    }


    // * for testing purpose
    public static void main(String[] args) {
        PrettyPrinterVisitor prettyPrinterVisitor = new PrettyPrinterVisitor();
        for (String filename: args) {
            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);
            try {
                Program root = parser.parse(filename);
                String result = prettyPrinterVisitor.generateOutputString(root);
                System.out.println(result);
            } catch (CompilationException ex) {
                for (Error error : errorHandler.getErrorList()) {
                    System.out.println(error.toString());
                    ex.printStackTrace();
                }
            }
        }
    }
}
