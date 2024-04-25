/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 *
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluation;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;

public class RootExpression<V> extends Expression<V> {

	public Expression body;
	public RuleEvaluation evaluation;
	public Object contextElement;

	public RootExpression(Expression body) {
		super();
		this.parent = null;
		this.body = body;
		this.body.setParent(this);
		this.resultType = body.resultType;
	}

	public EvaluationNode evaluate(Object contextElement, HashSet scopeElements)  {
		this.contextElement = contextElement;
		return this.body.evaluate(scopeElements);
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements)  {
		throw new EvaluationException("evaluate root expression by calling evaluate(contextElement)");
	}

	@Override
	public V evaluate(Expression child)  {
		throw new EvaluationException("evaluate root expression by calling evaluate(contextElement)");
	}

	@Override
	public Object getValueForVariable(Expression variable) {
		if (((VariableExpression<?>) variable).name.equals("self"))
			return this.contextElement;
		else
			throw new EvaluationException("value for variable '%s' not found", ((VariableExpression<?>) variable).name);	//variable should be found below root expression
	}

	@Override
	public String getARL() {
		return this.body.getARL();
	}
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		return this.body.getOriginalARL(0, false);
	}
	
	@Override 
	public String getLocalARL() { return this.body.getARL();	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		this.body.generateRepairTree(parent, expectedValue,evaluationNode);
	}
}
