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

public abstract class BinaryExpression<RT, AT, BT> extends Expression<RT> {

	protected Expression<AT> a;
	protected Expression<BT> b;
	public BinaryExpression(Expression<AT> a, Expression<BT> b) {
		super();
		this.a = a;
		this.b = b;
		this.a.setParent(this);
		this.b.setParent(this);
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode aNode = this.a.evaluate(scopeElements);
		EvaluationNode bNode = this.b.evaluate(scopeElements);
		return new EvaluationNode(this, check( (AT)aNode.resultValue, (BT)bNode.resultValue ), aNode, bNode);
	}

	abstract public RT check(AT argA, BT argB);
	abstract public String getPropertySet();
	abstract public boolean ispropertySetPresent(String it,Expression prev);
	@Override
	public RT evaluate(Expression<?> child)  {
		/*
		if (!disposed) {
			if (child == this.a) {
				this.aCache = (AT) child.getResultValue();
			} else {
				this.bCache = (BT) child.getResultValue();
			}
			resultValue = check(this.aCache, this.bCache);
		}
		return resultValue;

		 */
		return null;
	}

	@Override
	public String getARL() {
		return this.a.getARL() + "," + this.b.getARL();
	}
	
	
}
