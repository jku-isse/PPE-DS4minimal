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
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class SelectExpression<ST> extends IteratorExpression<Collection<ST>, ST, Boolean> {

	public SelectExpression(Expression<Collection<ST>> source, VariableExpression<ST> iterator1, VariableExpression<ST> iterator2, Expression<Boolean> body) {
		super(source, iterator1, iterator2, body);
		if (iterator2!=null) throw new ParsingException("select operator cannot have two iterators");
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
		if ((Boolean)bodyNode.resultValue) ((Collection)resultValue).add(iterator1Value);
	}

	@Override
	public String getARL() { return "SELECT(" + super.getARL()+")"; }

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
		return whitespaceBegin+this.source.getOriginalARL(indentation, isOnNewLine)+"\r\n"+
    			whitespace+"->select(" + super.getOriginalARL(indentation+2,false)+")"; 
	}

	@Override 
	public String getLocalARL() { return "SELECT";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		List values = evaluationNode.children[0].resultValue  != null ? new ArrayList( (Collection) evaluationNode.children[0].resultValue) : Collections.emptyList(); // if the collection is from accessing a collection property of an any call, this element might be null, hence the property null, and the evaluation result null
		RepairNode node = new AlternativeRepairNode(parent);
		if (expectedValue.getExpectedEvaluationResult() ) {
			//RepairNode childNode = new AlternativeRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN,
					true));
			for (int i = 0; i < values.size(); i++) {
				if (evaluationNode.children[i + 1].resultValue != expectedValue.getExpectedEvaluationResult()) {
					evaluationNode.children[i + 1].generateRepairTree(node, expectedValue);
					evaluationNode.incrementRepairGap();
				}
			}
		} else {									
			if (expectedValue.getValue() == null || expectedValue.getValue().equals(UnknownRepairValue.UNKNOWN)) {
				//RepairNode node = new SequenceRepairNode(parent);
				for (int i = 0; i < values.size(); i++) {					
					//RepairNode childNode = new AlternativeRepairNode(node);				
					if (evaluationNode.children[i + 1].resultValue != expectedValue.getExpectedEvaluationResult()) {
						evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.REMOVE, values.get(i),
								false));
						evaluationNode.children[i + 1].generateRepairTree(node, expectedValue);
						evaluationNode.decrementRepairGap();
					}					
				}
			} else {
				//RepairNode node = new AlternativeRepairNode(parent);
				for (int i = 0; i < values.size(); i++) {
					if (values.get(i).equals(expectedValue.getValue())) {// scoping to particular element in collection 
						if (evaluationNode.children[i + 1].resultValue != expectedValue.getExpectedEvaluationResult()) {
							evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.REMOVE, values.get(i),
									false));
							evaluationNode.children[i + 1].generateRepairTree(node, expectedValue);
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
		int selectionSize = ((Collection)resultValue).size();
		int childrenSize = (Collection)node.children[0].resultValue != null ? ((Collection)node.children[0].resultValue).size() : 0;
		int absGap = Math.abs(gap);
		String expl = String.format("Out of total %s element(s) %s meet the selection criteria, of which %s could be part of a repair", childrenSize, selectionSize, Math.min(absGap,  childrenSize));
//		if (gap > 0) {			
//			if (gap == childrenSize)
//				return String.format("None of the %s element(s) match the condition, but more could match", childrenSize);
//			else
//				return String.format("%s of %s element(s) dont match the condition, but more could match", Math.min(gap,  childrenSize), childrenSize);
//		} else if (gap == 0) {
//			if (childrenSize == 0)
//				return "No elements available to match, but more could match";
//			else
//				return String.format("All of the %s element(s) match the condition, which is desirable", childrenSize);
//		} else {
//			int absGap = Math.abs(gap);
//			if (absGap == childrenSize)
//				return String.format("All %s elements match the condition, but fewer could match", childrenSize);
//			else 
//				return String.format("%s of %s element(s) match the condition, but fewer could match", Math.min(absGap,  childrenSize), childrenSize);
//		}
		return expl;
	}
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) {
		if (this.parent.equals(processedExpr)) // Check if the control is coming from up
		{
			RestrictionNode bodyR=null;
			RestrictionNode sourceR=null;
			if(!(this.body.getrestGenerated()==1))
			{
				this.body.restGeneratedIncrement();
				bodyR = getBodyRestriction(null);
			}
			if(!(this.source.getrestGenerated()==1))
			{
				this.source.restGeneratedIncrement();
				sourceR = source.generateRestrictions(this);
			}
			if(bodyR == null && sourceR == null)
				return null;
			else if(sourceR ==null)
				return bodyR;
			if(bodyR == null)
				return sourceR;
			else
				return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);
		}
		else if (source.equals(processedExpr))
		{
			
		}
		else { // we come from the body part upwards, hence we navigated already
			this.parent.restGeneratedIncrement();
			return this.parent.generateRestrictions(this);
		}
		return null;
	}
	
/*	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr, RestrictionNode prev) {
		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
		if (this.parent.equals(processedExpr)) // control is coming from up
		{
			RestrictionNode bodyR=getBodyRestriction(evalNode,prev);
			RestrictionNode sourceR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this,prev);
			if(bodyR == null && sourceR == null)
				return null;
			else if(sourceR ==null)
				return bodyR;
			if(bodyR == null)
				return sourceR;
			else
				return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);
		}
		else if (source.equals(processedExpr)) { // if restriction from source/below, then body, then parent,
			RestrictionNode bodyR=getBodyRestriction(evalNode,prev);
			prev.setNextNode(bodyR);
			RestrictionNode parentR=this.parent.generateRestrictions(parentEN, this,prev);
			if (parentR != null && bodyR != null)
			{
				return new RestrictionNode.AndNode(bodyR, parentR);
			}
		}
		else { 
			
		}
		return null;
	}
	*/
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {
		// if restrictions from above/parent, then first source then body
		// if restriction from source, first body, then parent,
		// if restrictions from body, then just parent
		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
		
		if (this.parent.equals(processedExpr)) // Check if the control is coming from up
		{
			RestrictionNode bodyR=null;
			RestrictionNode sourceR=null;
			if(!(this.body.getrestGenerated()==1))
			{
				this.body.restGeneratedIncrement();
				bodyR = getBodyRestriction(evalNode);
			}
			if(!(this.source.getrestGenerated()==1))
			{
				this.source.restGeneratedIncrement();
				sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			}
			if(bodyR == null && sourceR == null)
				return null;
			else if(sourceR ==null)
				return bodyR;
			if(bodyR == null)
				return sourceR;
			else
			{
				if(this.parent instanceof OperationCallExpression 
						&& this.origin.expression instanceof PropertyCallExpression
						&& ((OperationCallExpression)this.parent).operation.equals("intersection")
						&& this.source instanceof PropertyCallExpression
						&& ((PropertyCallExpression)this.source).a instanceof VariableExpression
						/*&& ((VariableExpression)((PropertyCallExpression)this.source).a).name.equals(
								((VariableExpression)this.origin.children[0].expression).name)*/)
				{
					return bodyR;
				}
				else
					return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);
			}
			
		} else if (source.equals(processedExpr)) { // if restriction from source/below, then body, then parent,
			RestrictionNode bodyR=null;
			RestrictionNode parentR=null;
			if(this.parent.getrestGenerated()==0)
			{
				parentR = this.parent.generateRestrictions(parentEN, this);
				this.parent.restGeneratedIncrement();
			}
			if(this.body.getrestGenerated()==0)
			{
				this.body.restGeneratedIncrement();
				bodyR = getBodyRestriction(evalNode);
			}
			if (parentR != null && bodyR != null)
			{
				/*if(this.parent instanceof OperationCallExpression)
				{
					if(!((OperationCallExpression)this.parent).operation.equals("union"))
					{
						return new RestrictionNode.SubtreeCombinatorNode(parentR, bodyR);
					}
					else
					{
						return parentR;
					}
				}
				else*/
					return new RestrictionNode.AndNode(bodyR, parentR);
			}
			if (bodyR == null)
				return parentR;
			if(parentR == null)
			{
				if(!(source.getrestGenerated()==1) && source instanceof PropertyCallExpression)
				{
					this.source.setrestGenerated(1);
					RestrictionNode sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
					if(sourceR!=null)
					{
						RestrictionNode temp=sourceR;
						while(sourceR.getNextNode()!=null)
							sourceR=sourceR.getNextNode();
						sourceR.setNextNode(bodyR);
						return temp;
					}
					else
						return bodyR;
				}
				else 
					return bodyR;
			}
			else 
				return null;
		} else { // we come from the body part upwards, hence we navigated already
			
			if(this.source.inconsistency_origin!=null)
			{
				RestrictionNode sR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
				return sR;
			}
			else
			{
				this.parent.setrestGenerated(1);
				return this.parent.generateRestrictions(parentEN, this);
			}
		}
	}

	/*private RestrictionNode getBodyRestriction(EvaluationNode evalNode, RestrictionNode prev) {
		RestrictionNode rest=null;
		if(evalNode!=null) 
		{
			// for now lets just use the first eval node that matches
			for (EvaluationNode child : evalNode.children) 
			{
				if (child.expression.equals(body)) {
					rest= body.generateRestrictions(child, this,prev);
				}
			}
		}
		if(rest==null) // no child matches the body which means the eval node for the restriction is null.
		{
			rest=body.generateRestrictions(this);
		}
		return rest;
	}*/
	
	private RestrictionNode getBodyRestriction(EvaluationNode evalNode) {
		RestrictionNode rest=null;
		if(evalNode!=null) 
		{
			// for now lets just use the first eval node that matches
			for (EvaluationNode child : evalNode.children) 
			{
				if (child.expression.equals(body)) {
					rest= body.generateRestrictions(child, this);
				}
			}
		}
		if(rest==null) // no child matches the body which means the eval node for the restriction is null.
		{
			rest=body.generateRestrictions(this);
		}
		return rest;
	}

	@Override
	public String getPropertySet() {
		for(Expression child : this.children)
		{
			if(child instanceof BinaryExpression)
			{
				return ((BinaryExpression)child).getPropertySet();
			}
			else if(child instanceof IteratorExpression)
				return ((IteratorExpression)child).getPropertySet();
		}
		return null;
	}

	@Override
	public boolean ispropertySetPresent(String it, Expression prev) {
		// TODO Auto-generated method stub
		return false;
	}
}
