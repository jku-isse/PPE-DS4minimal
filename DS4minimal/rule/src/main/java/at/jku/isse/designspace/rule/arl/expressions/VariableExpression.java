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

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairRestriction;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.ValueNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class VariableExpression<RT> extends Expression<RT> {

	public String name;
	public Expression<RT> initValue;

	public VariableExpression(String name, TypeExpression type, Expression<RT> value) {
		super();
		this.name = name;
		this.initValue = value;
		if (this.initValue!=null) this.initValue.setParent(this);

		this.resultType = type.value;
		if (this.initValue!=null && !this.resultType.conformsTo(value.resultType)) throw new ParsingException("Initial value of variable '%s' does not match type", name);
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		if (this.initValue!=null) {
			EvaluationNode initValueNode = (this.initValue.evaluate(scopeElements));
			return new EvaluationNode(this, initValueNode.resultValue, initValueNode);
		}
		else {
			return new EvaluationNode(this, getValueForVariable(this));
		}
	}

	@Override
	public RT evaluate(Expression<?> child) {
		return null;
	}

	@Override
	public String getARL() { return "VARIABLE("+this.name+")"; }

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespaceBegin = isOnNewLine ? createWhitespace(indentation) : "";
		return whitespaceBegin+this.name;
	}
	
	@Override 
	public String getLocalARL() { return getOriginalARL(0, false);	}

	@Override
	public void generateRepairTree(RepairNode parent, String property, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		Instance instance = evaluationNode.getInstanceValue();
		if (instance == null)
			new ConsistencyRepairAction(parent, getRootProperty(), evaluationNode.getInstanceValue(), new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);// prevents throwing exception when instance is null
		else {
			if(expectedValue.operator.equals(Operator.ADD) || expectedValue.operator.equals(Operator.REMOVE)) {
				if(!instance.getProperty(property).propertyType().cardinality().equals(Cardinality.SINGLE))
					new ConsistencyRepairAction(parent, property, instance, expectedValue.duplicate().setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			} else {
				if(instance.getProperty(property).propertyType().cardinality().equals(Cardinality.SINGLE)) {
					if (expectedValue.getValue() instanceof Boolean
							&& !instance.getProperty(property).getValue().getClass().equals(expectedValue.getValue().getClass()))
						new ConsistencyRepairAction(parent, property, instance, new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
					else
						new ConsistencyRepairAction(parent, property, instance, expectedValue.duplicate().setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
				}
			}
		}
	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		Instance contextElement = null;
		if(this.parent instanceof LetExpression)
			evaluationNode.children[0].generateRepairTree(parent,  expectedValue);
	}
	
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) {  
		if(!this.isValue())
		{
			//we are returning null because there is no value available here due to null eval node.
			return null;
		}
		else
		{
			return new ValueNode(this.valueExtraction(this, processedExpr));
		}
	}

	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {  
		if (parent.equals(processedExpr)) // lets avoid a loop
		{
			// if eval node is a variable then there will be no value for it hence returns null.
			if(evalNode.isVariable) 
				return null;
			else
			{
				if(evalNode.resultValue instanceof Instance)
				{
					Instance ins=(Instance) evalNode.resultValue;
					return new ValueNode(ins.name());
				} else {
					if (evalNode.resultValue != null)
						return new ValueNode((String)evalNode.resultValue);
					else
						return new ValueNode(null);
				}
			}
		}
		if (evalNode == null) { 
			return parent.generateRestrictions(null, processedExpr);
		} else {
			Expression parentExpr = evalNode.parentEvalNode.expression;
			return parentExpr.generateRestrictions(evalNode.parentEvalNode, this);
		}			 
	}

	//	 private RestrictionNode generateSubpropertyRestrictions(EvaluationNode evalNode, Expression processedExpr) {
	//		 if (parent.equals(processedExpr)) { // lets avoid a loop
	//			// we need to understand if (adding, removing, setting) some prior property is equivalent to this variable, 
	//			 // e.g., iterating over a collection, iterator is then representing one add/rem anonymous value in that collection 
	//			 return new RestrictionNode.PropertyNode(this.name, this.resultType);//TODO not quite correct as we have a variable here and not a property!!
	//		 } if (evalNode == null) { 
	//			 return parent.parent.generateRestrictions(null, parent);
	//		 } else {
	//			 Expression parentExpr = evalNode.parentEvalNode.parentEvalNode.expression;
	//			 return parentExpr.generateRestrictions(evalNode.parentEvalNode.parentEvalNode, parent);
	//		 }
	//	 }


}
