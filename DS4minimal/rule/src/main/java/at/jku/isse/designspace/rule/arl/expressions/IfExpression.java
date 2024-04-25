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
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class IfExpression<RT> extends Expression<RT> {

	private Expression<Boolean> condition;
	private Expression<RT> thenExpression;
	private Expression<RT> elseExpression;

	public IfExpression(Expression<Boolean> condition, Expression<RT> thenExpression, Expression<RT> elseExpression) {
		super();
		this.condition = condition;
		this.thenExpression = thenExpression;
		this.elseExpression = elseExpression;
		condition.setParent(this);
		thenExpression.setParent(this);
		elseExpression.setParent(this);

		this.resultType = thenExpression.resultType.commonSuperType(elseExpression.resultType);
		if (condition.resultType!= ArlType.BOOLEAN) throw new ParsingException("if condition '%s' is not boolean", condition);
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode conditionNode = this.condition.evaluate(scopeElements);
		if ((boolean)conditionNode.resultValue) {
			EvaluationNode thenNode = thenExpression.evaluate(scopeElements);
			return new EvaluationNode(this, thenNode.resultValue, conditionNode, thenNode);
		}
		else {
			EvaluationNode elseNode = elseExpression.evaluate(scopeElements);
			return new EvaluationNode(this, elseNode.resultValue, conditionNode, elseNode);
		}
	}

	@Override
	public RT evaluate(Expression child)  {
		/*
		if (!disposed) {
			if (child.equals(condition)) {
				Boolean oldConditionResult = this.conditionCache;
				this.conditionCache = this.condition.getResultValue();
				if (!this.conditionCache.equals(oldConditionResult)) {
					if (conditionCache) {
						this.thenCache = this.thenExpression.evaluate();
					} else {
						this.elseCache = this.elseExpression.evaluate();
					}
				}
			} else if (child.equals(this.thenExpression)) {
				this.thenCache = this.thenExpression.getResultValue();
			} else if (child.equals(this.elseExpression)) {
				this.elseCache = this.elseExpression.getResultValue();
			}
			resultValue = check(conditionCache, thenCache, elseCache);
		}
		return resultValue;

		 */
		return null;
	}

	@Override
	public String getARL() { return "IF(" + condition.getARL() + " THEN " + thenExpression.getARL() + " ELSE " + elseExpression.getARL()+")"; }

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
		return whitespaceBegin+"if(" + condition.getOriginalARL(indentation, false) + "\r\n" +
				whitespace + "then" + thenExpression.getOriginalARL(indentation+2, true) + "\r\n" +
				whitespace + "else " + elseExpression.getOriginalARL(indentation+2, true)+")"; 
	}

	@Override 
	public String getLocalARL() { return "IF / THEN / ELSE";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new SequenceRepairNode(parent);
		evaluationNode.children[1].generateRepairTree(node, expectedValue);

	}
}
