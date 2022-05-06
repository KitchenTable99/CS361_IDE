/**
 * File: ClassMapBuilderVisitor
 * Authors: cbitting
 * Date: 4/23/2022
 */
package proj10BittingCerratoCohenEllmer.bantam.semant;

import proj10BittingCerratoCohenEllmer.bantam.ast.ASTNode;
import proj10BittingCerratoCohenEllmer.bantam.ast.Class_;
import proj10BittingCerratoCohenEllmer.bantam.ast.Field;
import proj10BittingCerratoCohenEllmer.bantam.ast.Method;
import proj10BittingCerratoCohenEllmer.bantam.visitor.Visitor;

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
        boolean noParameters = (node.getFormalList().getSize() == 0);
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
