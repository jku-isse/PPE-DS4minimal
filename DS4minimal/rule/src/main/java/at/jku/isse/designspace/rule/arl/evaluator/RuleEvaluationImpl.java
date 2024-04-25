/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 * <p>
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 * <p>
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.expressions.RootExpression;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.checker.ConsistencyUtils;

public class RuleEvaluationImpl<E> implements RuleEvaluation<E> {

    protected E contextElement;
    protected RuleDefinition< E> ruleDefinition;
    protected Object result;
    protected String error;
    protected EvaluationNode evaluationTree;
    protected Set scopeElements;
    protected Sets.SetView addedScopeElements;
    protected Sets.SetView removedScopeElements;

    public RuleEvaluationImpl(RuleDefinition<E> ruleDefinition, E contextElement) {
        this.contextElement = contextElement;
        this.result = null;
        this.evaluationTree = null;
        this.ruleDefinition = ruleDefinition;
        this.scopeElements =new HashSet();
        this.error = null;
    }


    //**************************************************************************************************
    //*** Evaluation
    //**************************************************************************************************

    @Override public E getContextElement() {
        return this.contextElement;
    }
    @Override public RuleDefinition< E> getRuleDefinition() { return ruleDefinition; }

    @Override
    public Object evaluate() {
        if (getRuleDefinition().getRuleError()==null) {
            HashSet scopeElements =new HashSet();
            try {
                this.error = null;
                this.evaluationTree = ((RootExpression)getRuleDefinition().getSyntaxTree()).evaluate(contextElement, scopeElements);
                ConsistencyUtils.printEvaluationTree(evaluationTree);
                this.result = this.evaluationTree.resultValue;
            } catch (ParsingException pe){
                getRuleDefinition().setRuleError(pe.toString());
                this.result = null;
                this.error = pe.toString();
                System.out.println(pe);
            } catch (Exception ex) {
                //if (RuleService.logger.getLevel()==ch.qos.logback.classic.Level.DEBUG) 
                ex.printStackTrace();
                this.result = null;
                this.error = "evaluation caused an exception: " + ex.toString();
            }

            this.addedScopeElements = Sets.difference(scopeElements, this.scopeElements);
            this.removedScopeElements = Sets.difference(this.scopeElements, scopeElements);
            this.scopeElements = scopeElements;

        } else {
            this.result = null;
            this.error = "evaluation was not possible because the rule definition is invalid";
        }
        return this.result;
    }

    @Override
    public void delete() {
        this.ruleDefinition = null;
        this.contextElement = null;
        this.evaluationTree = null;
        this.result = null;
        this.scopeElements = null;
        this.error = null;
    }

    public Object getResult() { return this.result; }

    @Override public Set getScopeElements() { return scopeElements; }
    @Override public Sets.SetView getAddedScopeElements() { return addedScopeElements; }
    @Override public Sets.SetView getRemovedScopeElements() { return removedScopeElements; }

    @Override public EvaluationNode getEvaluationTree() {
        return this.evaluationTree;
    }
    @Override public RepairNode getRepairTree(Object cre) {
        this.evaluationTree.generateRepairs(cre);
        // sort tree based on alphabetical order
        if(this.evaluationTree.repairTree!=null)
        {
        this.evaluationTree.repairTree=sortTree(this.evaluationTree.repairTree,1);
        //System.out.println("Sorted Tree in Alphabetical order");
        //ConsistencyUtils.printRepairTree(sortedTree);
        }
      return this.evaluationTree.repairTree;
    }

    // Code to sort the tree in alphabetical order
    public RepairNode sortTree(RepairNode node, int position) {
    	Collections.sort(node.getChildren(),Comparator.comparing(RepairNode::toString));
    	for (RepairNode child : node.getChildren()) {
    		sortTree(child, position + 1);
		}
    	return node;
    }
// Sorting code End here
    
    @Override public String getError() {
        return this.error;
    }


    @Override public String toString() {
        return result + "/" + contextElement.toString();
    }
   
    
    
}
