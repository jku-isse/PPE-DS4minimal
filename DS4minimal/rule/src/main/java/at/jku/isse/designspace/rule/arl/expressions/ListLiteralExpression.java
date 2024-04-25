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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.RepairException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;

public class ListLiteralExpression<RT> extends CollectionLiteralExpression<List<RT>,RT> {

	public ListLiteralExpression(List<Expression<RT>> literals) {
		super(literals);
		this.resultType = ArlType.LIST;
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode[] nodes = new EvaluationNode[this.literals.size()];
		ArrayList values = new ArrayList();
		int i=0;
		for (Expression<RT> e : this.literals) {
			EvaluationNode node = e.evaluate( scopeElements);
			nodes[i++] = node;
			values.add(node.resultValue);
		}
		return new EvaluationNode(this, values, nodes);
	}

	@Override
	public List<RT> evaluate(Expression<?> child) throws EvaluationException {
		/*
		if (!this.disposed) {
			if (values.contains(child)) {
				@SuppressWarnings("unchecked")
				Expression<RT> e = (Expression<RT>) child;
				int index = values.e.its(inde);
				resultValue.set(index, e.getResultValue());
			}
		}
		return this.resultValue;
		 */
		return null;
	}

	@Override
	public String getARL() {
		return "LIST"+super.getARL();
	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		throw new RepairException("generateRepairTree not implemented");
	}
}
