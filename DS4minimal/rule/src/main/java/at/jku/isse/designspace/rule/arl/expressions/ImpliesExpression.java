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
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class ImpliesExpression extends BinaryExpression<Boolean, Boolean, Boolean> {

	public ImpliesExpression(Expression<Boolean> a, Expression<Boolean> b) {
		super(a, b);
		this.resultType = ArlType.BOOLEAN;
		if (a.resultType != ArlType.BOOLEAN || b.resultType != ArlType.BOOLEAN) throw new ParsingException("'implies' does not have boolean arguments");
	}

	@Override
	public Boolean check(Boolean argA, Boolean argB) { return (!argA) || argB; }

	@Override
	public String getARL() {
		return "IMPLIES(" + super.getARL() + ")";
	}

	@Override 
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
		return whitespaceBegin + this.a.getOriginalARL(indentation, false)+" implies \r\n" +
    			whitespace+this.b.getOriginalARL(indentation+2, true); 
	}
	
	@Override 
	public String getLocalARL() { return "IMPLIES";	}
	
	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode aNode = this.a.evaluate(scopeElements);
		EvaluationNode bNode = this.b.evaluate(scopeElements);
		if (!(Boolean)aNode.resultValue) return new EvaluationNode(this, true, aNode);		
		return new EvaluationNode(this, bNode.resultValue, aNode, bNode);
	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		boolean aValue = (Boolean) evaluationNode.children[0].resultValue;

		if(evaluationNode.children.length<2) {
			try {
				EvaluationNode bNode = this.b.evaluate(evaluationNode.scopeElements);
				evaluationNode.addNode(bNode);
			} catch (Exception ex) {
				ex.printStackTrace();
				EvaluationNode bNode = new EvaluationNode(this,false);
				evaluationNode.addNode(bNode);
			}
		}
		boolean bValue = (Boolean) evaluationNode.children[1].resultValue;
		// repair a OR b
		if (expectedValue.getExpectedEvaluationResult()) {
			if (aValue && !bValue) {
				RepairNode node = new AlternativeRepairNode(parent);
				evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, expectedValue.getValue(),!expectedValue.getExpectedEvaluationResult()));
				evaluationNode.children[1].generateRepairTree(node, expectedValue);
			}
		} else {
			RepairNode node = new SequenceRepairNode(parent);
			// repair b
			if (aValue && bValue)
				evaluationNode.children[1].generateRepairTree(node, expectedValue);
				// repair a AND b
			else if (!aValue && bValue) {
				evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, expectedValue.getValue(), !expectedValue.getExpectedEvaluationResult()));
				evaluationNode.children[1].generateRepairTree(node, expectedValue);
			}
			// repair a
			else if (!aValue && !bValue)
				evaluationNode.children[0].generateRepairTree(node, expectedValue);
		}


	}

	@Override
	public String getPropertySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean ispropertySetPresent(String it, Expression prev) {
		// TODO Auto-generated method stub
		return false;
	}



}
