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
import java.util.List;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class ForAllExpression<ST> extends IteratorExpression<Boolean, ST, Boolean> {



	public ForAllExpression(Expression<Collection<ST>> source, VariableExpression<ST> iterator1, VariableExpression<ST> iterator2, Expression<Boolean> body) {
		super(source, iterator1, iterator2, body);
		this.resultType= ArlType.BOOLEAN;
	}

	@Override
	public void initialize() {
		this.resultValue = true;	//if collection is empty then result is true
	}

	@Override
	public void process() {
		//false once the first body=false (e.g., not all elements match body)
		resultValue = (Boolean)resultValue && (Boolean)bodyNode.resultValue;
	}

	@Override
	public String getARL() {
		return "FORALL(" + super.getARL()+")";
	}
	
	

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
		return whitespaceBegin+this.source.getOriginalARL(indentation, isOnNewLine)+"\r\n"+
    			whitespace+"->forAll(" + super.getOriginalARL(indentation+2, false)+")"; }

	@Override 
	public String getLocalARL() { return "FORALL";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		if(expectedValue.getExpectedEvaluationResult()){
			RepairNode node = new SequenceRepairNode(parent);
			List<Instance> instances = new ArrayList<>((Collection)evaluationNode.children[0].resultValue);
			for (int i = 1; i < evaluationNode.children.length; i++) {
				if (!(Boolean)evaluationNode.children[i].resultValue) {
					RepairNode childNode = new AlternativeRepairNode(node);
					if(instances.size()>=i-1) {
						evaluationNode.children[0].generateRepairTree(childNode, new RepairSingleValueOption(Operator.REMOVE, instances.get(i-1), false));
						evaluationNode.incrementRepairGap();
					} else{
						evaluationNode.children[0].generateRepairTree(childNode, new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN, false));
						evaluationNode.incrementRepairGap();
					}
					evaluationNode.children[i].generateRepairTree(childNode, expectedValue);
				}
			}
		}else{
			RepairNode node = new AlternativeRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN));
			evaluationNode.decrementRepairGap();
			for (int i = 1; i < evaluationNode.children.length; i++) {
				evaluationNode.children[i].generateRepairTree(node, expectedValue);

			}
		}
	}

	@Override
	protected Object explainInternally(EvaluationNode node) {
		if (node.getRepairGap() > 0) {
			int childrenSize = (Collection)node.children[0].resultValue != null ? ((Collection)node.children[0].resultValue).size() : 0;
			if (node.getRepairGap() == childrenSize)
				return String.format("None of the %s element(s) match the condition", childrenSize);
			else
				return String.format("%s of %s element(s) dont match the condition", node.getRepairGap(), childrenSize);
		} else	// add a non matching element			
			return "All elements match the condition";							
	}

	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) { 
		if (this.parent.equals(processedExpr)) {
			RestrictionNode sourceR=null;
				sourceR = source.generateRestrictions(this);
			RestrictionNode bodyR = getBodyRestriction(null);
			List<RestrictionNode> restList=new ArrayList<>();
			if(sourceR!=null)
			{
				restList.add(sourceR);
				restList.add(bodyR);
				RestrictionNode forAllR = new RestrictionNode.OperationNode("forall",restList);
				//forAllR.setNextNodeFluent(sourceR.setNextNodeFluent(bodyR));
				return forAllR;
			}
			else
			{
				restList.add(bodyR);
				RestrictionNode forAllR = new RestrictionNode.OperationNode("forall",restList);
				//forAllR.setNextNodeFluent(bodyR);
				return forAllR;
			}
		/*	forAllR.setNextNode(bodyR);
			if(sourceR!=null)
				return new RestrictionNode.SubtreeCombinatorNode(sourceR, forAllR);
			else
				return forAllR;*/
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
			RestrictionNode sourceR=null;
			if(!source.toString().equals(this.origin.expression.toString()))
			{
				if(source instanceof IteratorExpression)
				{
					String it=((IteratorExpression) source).getPropertySet();
					if(this.parent instanceof BinaryExpression && ((BinaryExpression)this.parent).ispropertySetPresent(it,this))
					{
						sourceR=null;
					}
					else
					{
						sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
					}
				}
				
			}
			RestrictionNode bodyR = getBodyRestriction(evalNode);
			List<RestrictionNode> restList=new ArrayList<>();
			if(sourceR!=null)
			{
				restList.add(sourceR);
				restList.add(bodyR);
				RestrictionNode forAllR = new RestrictionNode.OperationNode("forall",restList);
				//forAllR.setNextNodeFluent(sourceR.setNextNodeFluent(bodyR));
				return forAllR;
			}
			else
			{
				/*restList.add(bodyR);
				RestrictionNode forAllR = new RestrictionNode.OperationNode("forall",restList);
				return forAllR;*/
				return bodyR;
			}
			/*forAllR.setNextNode(bodyR);
			if(sourceR!=null)
				return new RestrictionNode.SubtreeCombinatorNode(sourceR, forAllR);
			else
				return forAllR;*/
		} else if (source.equals(processedExpr)) { // if restriction from source, then body, then parent,
			RestrictionNode forAllR = new RestrictionNode.OperationNode("forall"); //sort of a hack, as forall is not an operator
			RestrictionNode bodyR = getBodyRestriction(evalNode);
			forAllR.setNextNode(bodyR);
			RestrictionNode parentR = this.parent.generateRestrictions(parentEN, this);
			if (parentR != null && bodyR != null)
				return new RestrictionNode.AndNode(forAllR, parentR);
			if (parentR == null)
				return forAllR;
			if (bodyR == null)
				return parentR;
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
		if(evalNode!=null)
		{
			for (EvaluationNode child : evalNode.children) {
				if (child.expression.equals(body)) {
					rest= body.generateRestrictions(child, this);
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
