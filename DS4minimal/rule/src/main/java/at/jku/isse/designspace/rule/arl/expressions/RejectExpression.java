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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class RejectExpression<ST> extends IteratorExpression<Collection<ST>, ST, Boolean> {

	public RejectExpression(Expression<Collection<ST>> source, VariableExpression<ST> iterator1, VariableExpression<ST> iterator2, Expression<Boolean> body) {
		super(source, iterator1, iterator2, body);
		if (iterator2!=null) throw new ParsingException("reject operator cannot have two iterators");
	}

	@Override
	public void initialize() {
		if (source.resultType.collection== ArlType.CollectionKind.LIST)
			resultValue = new ArrayList<>();
		else if (source.resultType.collection==ArlType.CollectionKind.SET)
			resultValue = new HashSet<>();
		else if (source.resultType.collection==ArlType.CollectionKind.MAP)
			resultValue = new HashMap<>();
		else
			throw new EvaluationException("unknown collection type found '%s'", source.resultType);
	}

	@Override
	public void process() {
		if (!(Boolean)bodyNode.resultValue) ((Collection)resultValue).add(iterator1Value);
	}

	@Override
	public String getARL() { return "REJECT(" + super.getARL()+")"; }
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { String whitespace = createWhitespace(indentation);
	String whitespaceBegin = isOnNewLine ? whitespace : "";
	return whitespaceBegin+this.source.getOriginalARL(indentation, isOnNewLine)+"\r\n"+
			whitespace+"->reject(" + super.getOriginalARL(indentation+2,false)+")"; 
}

	@Override 
	public String getLocalARL() { return "REJECT";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		List values = evaluationNode.children[0].resultValue  != null ? new ArrayList( (Collection) evaluationNode.children[0].resultValue) : Collections.emptyList(); // if the collection is from accessing a collection property of an any call, this element might be null, hence the property null, and the evaluation result null
		RepairSingleValueOption invertedValue = new RepairSingleValueOption(expectedValue.operator, expectedValue.getValue(), !expectedValue.getExpectedEvaluationResult());
		if (expectedValue.getExpectedEvaluationResult() ) {

			RepairNode childNode = new AlternativeRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(childNode, new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN,
					false));
			for (int i = 0; i < values.size(); i++) {
				if (evaluationNode.children[i + 1].resultValue == expectedValue.getExpectedEvaluationResult()) {
					evaluationNode.children[i + 1].generateRepairTree(childNode, invertedValue);
					evaluationNode.incrementRepairGap();
				}
			}
		} else {			
			if (expectedValue.getValue() == null || expectedValue.getValue().equals(UnknownRepairValue.UNKNOWN)) {
				RepairNode node = new SequenceRepairNode(parent);
				for (int i = 0; i < values.size(); i++) {					
						RepairNode childNode = new AlternativeRepairNode(node);				
						if (evaluationNode.children[i + 1].resultValue == expectedValue.getExpectedEvaluationResult()) {
							evaluationNode.children[0].generateRepairTree(childNode, new RepairSingleValueOption(Operator.REMOVE, values.get(i),
									false));
							evaluationNode.children[i + 1].generateRepairTree(childNode, invertedValue);
							evaluationNode.decrementRepairGap();
						}					
				}
			} else {
				for (int i = 0; i < values.size(); i++) {
					RepairNode node = new AlternativeRepairNode(parent);
					if (values.get(i).equals(expectedValue.getValue())) {// scoping to particular element in collection 
						if (evaluationNode.children[i + 1].resultValue == expectedValue.getExpectedEvaluationResult()) {
							evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.REMOVE, values.get(i),
									false));
							evaluationNode.children[i + 1].generateRepairTree(node, invertedValue);
							evaluationNode.decrementRepairGap();
						}
					} 
				}
			}	
		}
	}

	@Override
	protected Object explainInternally(EvaluationNode node) {
		int gap = node.getRepairGap();
		int childrenSize = (Collection)node.children[0].resultValue != null ? ((Collection)node.children[0].resultValue).size() : 0;
		if (gap > 0) {			
			return String.format("%s of %s element(s) match the condition, but fewer could match", Math.min(gap,  childrenSize), childrenSize);
		} else if (gap == 0) {
			if (childrenSize == 0)
				return "No elements available to match";
			else
				return String.format("None of the %s element(s) match the condition, which is desirable", childrenSize);			
		} else {
			int absGap = Math.abs(gap);
			return String.format("%s of %s element(s) don't match the condition, but more could match", Math.min(absGap,  childrenSize), childrenSize);
		}
	}
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) { 
		if (this.parent.equals(processedExpr)) {
			
		}
		else if (source.equals(processedExpr)) {
			
		}
		else { // we come from the body part upwards, hence we navigated already
			return this.parent.generateRestrictions(this);
		}
		return null;
	}
	
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) { 
		// if restrictions from above/parent, then first source then body
		// if restriction from source, first body, then parent,
		// if restrictions from body, then just parent
		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
		
		if (this.parent.equals(processedExpr)) {
			RestrictionNode sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			RestrictionNode bodyR = new RestrictionNode.NotNode(getBodyRestriction(evalNode));
			return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);
//			// somehow get property of the source id not the previous expr was the source
		} else if (source.equals(processedExpr)) { // if restriction from source, then body, then parent,
				RestrictionNode bodyR = new RestrictionNode.NotNode(getBodyRestriction(evalNode));			
				RestrictionNode parentR = this.parent.generateRestrictions(parentEN, this);
				if (parentR != null && bodyR != null)
					return new RestrictionNode.AndNode(bodyR, parentR);
				if (parentR == null)
					return bodyR;
//				if (bodyR == null)
//					return parentR;
				else return null;
		} else { // we come from the body part upwards, hence we navigated already
			if(this.source.inconsistency_origin!=null)
			{
				RestrictionNode sR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
				return sR;
			}
			else
				return this.parent.generateRestrictions(parentEN, this);
		}
	}
	
	private RestrictionNode getBodyRestriction(EvaluationNode evalNode) {
		RestrictionNode rest=null;
		if(evalNode==null)
			return body.generateRestrictions(this);
		else {
			// other wise
			// for now lets just use the first evalnode that matches
			for (EvaluationNode child : evalNode.children) {
				if (child.expression.equals(body)) {
					rest=body.generateRestrictions(child, this);
				}
			}
		} 
		if(rest==null)
		{
			return body.generateRestrictions(this);
		}
		return rest;
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
