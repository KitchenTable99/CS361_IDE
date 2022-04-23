/**
 * File: ClassMapBuilderVisitor
 * Authors: cbitting
 * Date: 4/23/2022
 */
package proj7BittingCerratoCohenEllmer.bantam.semant;

import proj7BittingCerratoCohenEllmer.bantam.ast.ASTNode;
import proj7BittingCerratoCohenEllmer.bantam.ast.Class_;
import proj7BittingCerratoCohenEllmer.bantam.ast.Field;
import proj7BittingCerratoCohenEllmer.bantam.ast.Method;
import proj7BittingCerratoCohenEllmer.bantam.visitor.Visitor;

public class MainMethodVisitor extends Visitor {

    /**
     * Internal used to register whether there is a valid method
     */
    private boolean hasAppropriateMainMethod;

    /**
     * Returns true if there is a void method with no parameters named 'main' in a class
     * named 'Main' else returns false
     *
     * @param rootNode node to start searching from. Should be the root node.
     * @return whether there is a valid method
     */
    public boolean hasMain(ASTNode rootNode) {
        hasAppropriateMainMethod = false;
        rootNode.accept(this);
        return hasAppropriateMainMethod;
    }

    @Override
    public Object visit(Class_ node) {
        if ("Main".equals(node.getName())) {
            node.getMemberList().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(Method node) {
        boolean nameMain = "main".equals(node.getName());
        boolean voidMethod = "void".equals(node.getReturnType());
        boolean noParameters = null == node.getFormalList();

        if (nameMain && voidMethod && noParameters) {
            hasAppropriateMainMethod = true;
        }
        return null;
    }

    @Override
    public Object visit(Field node) {
        return null;
    }
}
