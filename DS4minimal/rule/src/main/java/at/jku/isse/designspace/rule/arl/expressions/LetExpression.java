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
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class LetExpression<RT> extends Expression<RT> {

	protected VariableExpression<?> variableExpression;
	protected Expression<RT> inExpression;

	EvaluationNode variableNode;
	EvaluationNode inNode;

	public LetExpression(VariableExpression<?> variable, Expression in) {
		super();
		this.variableExpression = variable;
		this.inExpression = in;

		this.variableExpression.setParent(this);
		this.inExpression.setParent(this);

		this.resultType = inExpression.resultType;
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements)  {
		variableNode = this.variableExpression.evaluate(scopeElements);
		inNode = this.inExpression.evaluate(scopeElements);
		return new EvaluationNode(this, inNode.resultValue, variableNode, inNode);
	}

	@Override
	public RT evaluate(Expression child) {
		/*
		if (!disposed) {
			if (child == this.initExpression) {
				this.initCache = this.initExpression.getResultValue();
				List<Expression> occurences = new ArrayList<Expression>(this.variableOccurences);
				this.variableOccurences.clear();
				for (Expression exp : occurences) {
					exp.evaluate();
					exp.getParent().evaluate(exp);
				}

			}
			this.inCache = this.inExpression.getResultValue();
			resultValue = check(this.inCache);
		}
		return resultValue;

		 */
		return null;
	}

	@Override
	public Object getValueForVariable(Expression variable) {
		if (((VariableExpression<?>) variable).name.equals(this.variableExpression.name))
			return this.variableNode.resultValue;
		else
			return parent.getValueForVariable(variable);
	}

	@Override
	public String getARL() {
		return "LET(" + this.variableExpression.name + " IN " + this.inExpression.getARL()+")";
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
		return whitespaceBegin + "let " + this.variableExpression.name + " : <"+this.variableExpression.resultType+"> in \r\n" +
    			whitespace+this.inExpression.getOriginalARL(indentation+2, true);
	}
	
	@Override 
	public String getLocalARL() { return "LET / IN";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new SequenceRepairNode(parent);
		evaluationNode.children[1].generateRepairTree(node,expectedValue);
	}
}
