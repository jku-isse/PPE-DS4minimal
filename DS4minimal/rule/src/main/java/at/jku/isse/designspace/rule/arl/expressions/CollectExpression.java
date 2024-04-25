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
import java.util.LinkedList;
import java.util.List;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.PropertyNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class CollectExpression<ST> extends IteratorExpression<Collection<ST>, ST, Object> {

	public CollectExpression(Expression<Collection<ST>> source, VariableExpression<ST> iterator1, VariableExpression<ST> iterator2, Expression<Object> body) {
		super(source, iterator1, iterator2, body);
		if (iterator2!=null) throw new ParsingException("collect operator cannot have two iterators");
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
		if (bodyNode.resultValue instanceof Collection)
			((Collection)resultValue).addAll((Collection)bodyNode.resultValue);
		else if (bodyNode.resultValue != null)
			((Collection)resultValue).add(bodyNode.resultValue);
	}

	@Override
	public String getARL() {
		return "COLLECT(" + super.getARL()+")";
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
		return this.source.getOriginalARL(indentation, true)+"\r\n"+whitespace+"->collect(" + super.getOriginalARL(indentation+2, false)+")"; }
	
	@Override 
	public String getLocalARL() { return "COLLECT";	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new AlternativeRepairNode(parent);
		if (expectedValue.getValue() == null || expectedValue.getValue().equals(UnknownRepairValue.UNKNOWN)) {		
			for (int i = 0; i < evaluationNode.children.length; i++) {
				//evaluationNode.children[i].generateRepairTree(new AlternativeRepairNode(parent), expectedValue);
				evaluationNode.children[i].generateRepairTree(node, expectedValue);
			}
		} else {
			List values = evaluationNode.children[0].resultValue  != null ? new ArrayList( (Collection) evaluationNode.children[0].resultValue) : Collections.emptyList(); // if the collection is from accessing a collection property of an any call, this element might be null, hence the property null, and the evaluation result null
			for (int i = 0; i < values.size(); i++) {	
				if (values.get(i).equals(expectedValue.getValue())) { 
					evaluationNode.children[i+1].generateRepairTree(new AlternativeRepairNode(parent), expectedValue);
				}
			}

			//			for (int i = 0; i < evaluationNode.children.length; i++) {
			//				if (values.size() <= i) // lets avoid an overflow
			//					System.out.println("Cannot access index: "+i);
			//				else	
			//					if (values.get(i).equals(expectedValue.getValue())) { //TODO: this throws an Indexp out opf bounds exception for some tests
			//						evaluationNode.children[i].generateRepairTree(new AlternativeRepairNode(parent), expectedValue);
			//					}
			//			}
		}
	}

	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr)
	{
		if(this.parent.equals(processedExpr))
		{
			RestrictionNode sourceR=source.generateRestrictions(this);
			RestrictionNode bodyR=body.generateRestrictions(this);
			return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);
		}
		else if(this.source.equals(processedExpr))
		{
			
		}
		else
		{
			return this.parent.generateRestrictions(this);
		}
		return null;
	}
	
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) { 
		EvaluationNode parentEN=getParentIfExpressionMatches(evalNode);
		if(this.parent.equals(processedExpr))
		{
			RestrictionNode sourceR=null;
			RestrictionNode bodyR=null;
			if(source.equals(evalNode.children[0].expression))
			{
				sourceR = source.generateRestrictions(evalNode != null & evalNode.children.length>0 ? evalNode.children[0] : null, this);
				bodyR = body.generateRestrictions(evalNode != null & evalNode.children.length>1? evalNode.children[1] : null, this);
			}
			else
			{
				sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[1] : null, this);
				bodyR = body.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			}
			if(sourceR!=null && bodyR!=null)
			{
			return new RestrictionNode.SubtreeCombinatorNode(bodyR, sourceR);
			}
			else if(bodyR==null)
				return sourceR;
		}
		else if(this.source.equals(processedExpr))
		{
			RestrictionNode bodyR=getBodyRestriction(evalNode);
			//this.restGeneratedIncrement();
			RestrictionNode parentR = this.parent.generateRestrictions(parentEN, this);
			this.parent.restGeneratedIncrement();
			if (parentR != null && bodyR!=null)
			{
				if(this.restGenerated==2)
				{
					this.setrestGenerated(1);
					return new RestrictionNode.AndNode(bodyR, parentR);
					//return parentR;
				}
				else
					//return parentR;
					return new RestrictionNode.SubtreeCombinatorNode(bodyR, parentR);
			}
			/*Case: where parent does contain the element in context but one of body or 
			 * parent is null which makes the other meaningless hence no restriction.
			 */
			if(checkParentEv_forOrigin(parentEN))
			{
				return null;
			}
			//else if(bodyR!=null && !(this.body instanceof PropertyCallExpression)) // Might need the check for PCE as well in some scenario
			else if(bodyR!=null && !(bodyR instanceof PropertyNode))
			{// since parent does not contain the element in context. and bodyR is not null return the bodyR restriction
				return bodyR;
			}
			else return parentR;
		}
		else
		{
			/*if(this.source.inconsistency_origin!=null)
			{
				RestrictionNode sR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
				return sR;
			}
			else if(this.source.inconsistency_origin==null && this.parent instanceof OperationCallExpression)
			{
				if(!((OperationCallExpression)this.parent).operation.equals("intersection"))
				{
					return this.parent.generateRestrictions(parentEN, this);
				}
				else
				{
					RestrictionNode sR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
					return sR;
				}
			}
			else
				return this.parent.generateRestrictions(parentEN, this);*/
			if(this.source.inconsistency_origin!=null)
			{
				RestrictionNode sR=source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
				return sR;
			}
			else
				return this.parent.generateRestrictions(parentEN, this);
			//return this.parent.generateRestrictions(parentEN, this);
		}
		return null;
	}

	private boolean checkParentEv_forOrigin(EvaluationNode parent) {
		for(EvaluationNode child: parent.children)
		{
			if(!child.expression.toString().equals(this.toString()) && child.expression.toString().contains(this.origin.expression.toString()))
				return true;

		}
		return false;
	}

	private RestrictionNode getBodyRestriction(EvaluationNode evalNode) {
		if(evalNode==null)
			return body.generateRestrictions(this);
		else if (evalNode.children.length == 1) { // empty iterator
			return body.generateRestrictions(evalNode, this);
		} 
		else {
			for (EvaluationNode child : evalNode.children) {
				if (child.expression.equals(body)) {
					child.expression.restGeneratedIncrement();
					List<String> valueSet=Collections.synchronizedList(new LinkedList<String>());
					valueSet=originValueSet(this.origin, valueSet);
					if(child.resultValue == null)
					{
						child.setisVariable(true);
						return body.generateRestrictions(child,this);
					}
					else if((child.resultValue instanceof Instance) && comparingValueSet(child,valueSet))
					{
						child.setisVariable(true);
						return body.generateRestrictions(child, this);
					}
					else if(!(child.resultValue instanceof Instance))
					{
						child.setisVariable(true);
						return body.generateRestrictions(child, this);
					}
					else if(!comparingValueSet(child, valueSet) && child.resultValue instanceof Instance)
					{
						child.setisVariable(true);
						return body.generateRestrictions(child, this);
					}

				}
			}
		}
		return null;
	}
	/*
	private List<RestrictionNode> getBodyRestriction(EvaluationNode evalNode) {
		List<RestrictionNode> rest=Collections.synchronizedList(new LinkedList<RestrictionNode>());
		if (evalNode == null || evalNode.children.length == 1) { // empty iterator
			rest.add(body.generateRestrictions(evalNode, this));
		} else {// As it's collect expression and it's body might have more than one instances

			for (EvaluationNode child : evalNode.children) {
				//TODO: the check of restriction being generated already or not might be needed.
				if (child.expression.equals(body)) {
					child.expression.restGeneratedIncrement();
					List<String> valueSet=Collections.synchronizedList(new LinkedList<String>());
					valueSet=originValueSet(this.origin, valueSet);
					if(child.resultValue == null)
					{
						child.setisVariable(true);
						RestrictionNode temp=body.generateRestrictions(child,this);
						if(!findRestrictionInList(rest, temp))
							rest.add(temp);
					}
					else if(comparingValueSet(child,valueSet))
					{
					rest.add(body.generateRestrictions(child, this));
					}

				}
			}
		}
		return rest;
	}*/

	private boolean findRestrictionInList(List<RestrictionNode> rest, RestrictionNode rn)
	{
		for(RestrictionNode r:rest)
		{
			if(r.matches(rn))
				return true;
		}
		return false;
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

	/*
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) { 		
		// if restrictions from above/parent, then first source then body
		// if restriction from source, first body, then parent,
		// if restrictions from body, then just parent

		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);	
		if (this.parent.equals(processedExpr)) {
			RestrictionNode sourceR = source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			RestrictionNode bodyR = body.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			return new RestrictionNode.SubtreeCombinatorNode(sourceR, bodyR);	
		} else if (source.equals(processedExpr)) { // if restriction from source, then body, then parent,
			//RestrictionNode bodyR = body.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
			RestrictionNode bodyR=getBodyRestriction(evalNode);
			RestrictionNode parentR = this.parent.generateRestrictions(parentEN, this);
			if (parentR != null && bodyR != null)
				return new RestrictionNode.SubtreeCombinatorNode(bodyR, parentR);
			if (parentR == null)
				return null; // if the parent provides no further restriction, then the body restriction is pointless (its just a collect, but not a constraint)
			if (bodyR == null)
				return parentR;
			else return null;
		} else { // we come from the body part upwards, hence we navigated already
			return this.parent.generateRestrictions(parentEN, this);
		}	
	}
	 */

}
