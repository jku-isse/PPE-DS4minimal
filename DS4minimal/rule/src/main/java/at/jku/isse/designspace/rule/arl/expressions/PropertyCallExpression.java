/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 * <p>
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 * <p>
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.expressions;

import java.util.Collection;
import java.util.HashSet;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.evaluator.ModelAccess;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairRestriction;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.PropertyNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.ValueNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class PropertyCallExpression<RT,AT,P> extends UnaryExpression<RT,AT> {

	public String property;
	private HashSet scopeElements=null;

	public PropertyCallExpression(Expression<AT> a, String property) {
		super(a);
		this.property = property;
		this.resultType = a.resultType.propertyType(property);
	}

	public Object check(Object element) {
		Object scopeElement;
		if (element instanceof Collection)
			scopeElement = ((Collection<?>) element).stream().findFirst().get();
		else
			scopeElement = element;
		scopeElements.add(ModelAccess.instance.scopeElement(scopeElement, property));
		return (RT) ModelAccess.instance.propertyValueOfInstance(scopeElement, property);
	}


	@Override
	public EvaluationNode evaluate(HashSet scopeElements)  {
		this.scopeElements=scopeElements;
		return super.evaluate(scopeElements);
	}

	@Override
	public RT evaluate(Expression<?> child) throws EvaluationException {
		if (child == null) {
			child = this.a;
		}
		try {
			return super.evaluate(child);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getARL() {
		return super.getARL() + "." + this.property;
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		return whitespace+super.getOriginalARL(indentation, isOnNewLine) + "." + this.property;
	}

	@Override 
	public String getLocalARL() { return "." + this.property;	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		generateRepairTree(parent,property,expectedValue,evaluationNode);
	}

	@Override
	public void generateRepairTree(RepairNode parent, String property, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode)  {
		Instance instance = evaluationNode.getInstanceValue();
		if(instance==null) {
			if (evaluationNode.getInstanceValue() !=null) // prevents throwing exception when instance is null (instance may be null due to select or collect)
				new ConsistencyRepairAction(parent,property,evaluationNode.getInstanceValue(),new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
		}else {
			if (expectedValue.getValue() == UnknownRepairValue.UNKNOWN && instance.hasProperty(property)) { //distinguish between a NULL value and an UNKNOWN value
				if(!instance.getProperty(property).propertyType().cardinality().equals(Cardinality.SINGLE)) {
					new ConsistencyRepairAction(parent, property, instance,  expectedValue.duplicate().setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
				} else {
					new ConsistencyRepairAction(parent, property, instance, new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
				}
			} else
				new ConsistencyRepairAction(parent, property, instance,  expectedValue, evaluationNode);
		}
		if(evaluationNode.children != null) { // repairs for child nodes
			RepairSingleValueOption childExpectedValue;
			RepairNode childNode;
			childExpectedValue = expectedValue;
			childNode = parent;
			if(evaluationNode.children[0].expression instanceof PropertyCallExpression) { // if the property is from self, then the parent repairNode should be the root
				childExpectedValue = new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN);
				if(evaluationNode.children[0].expression.children.size() > 0 &&
						evaluationNode.children[0].expression.children.get(0) instanceof VariableExpression &&
						((VariableExpression) evaluationNode.children[0].expression.children.get(0)).name.equalsIgnoreCase("self"))
					childNode = new AlternativeRepairNode(parent.getRoot()); //TODO: discuss with Luciano why this needs to be ROOT!?
			}
			evaluationNode.children[0].generateRepairTree(childNode,childExpectedValue);
		}
	}

	/*@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode,Expression processedExpr,RestrictionNode prev)
	{
		PropertyNode pn = new PropertyNode(this.property, this.resultType); 
		PropertyNode firstNode =  parent.equals(processedExpr) ? walkPropertyChain(pn, true,processedExpr) : pn;
		if(this.parent==null || this.parent instanceof RootExpression || processedExpr.equals(this.parent))
		{
			if(evalNode!=null)
			{
				if(evalNode.isVariable()) // Means the node cannot be a value node
				{
					evalNode.setisVariable(false);
					return firstNode;
				}
				else // Could be value node or could be not
				{
					//TODO: shorten out the code. A function can be generated in the evaluation node and reused again.
					if(evalNode.resultValue instanceof Instance)
					{
						Instance ins=(Instance) evalNode.resultValue;
						return new ValueNode(ins.name());
					}
					else if(evalNode.resultValue instanceof Collection )
					{
						return firstNode;
					}
					else if(evalNode.resultValue==null)
					{
						return firstNode;
					}
					else
					{	
						String val = evalNode.resultValue != null ? evalNode.resultValue.toString() : "Undefined";
						return new ValueNode(val);

					}
				}
			}
			else
				return firstNode;
		}
		else
		{
			EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
			if(prev!=null)
				prev.setNextNode(pn);
			else
				prev=pn;
			RestrictionNode pR;
			if(parent instanceof EqualsExpression)
			{
				pR=new RestrictionNode.OnlyComparatorNode(Operator.MOD_EQ);
				pR.setNextNode(parent.generateRestrictions(parentEN, this,prev));
			}
			else
			{
				pR=parent.generateRestrictions(parentEN, this,prev);
			}
			if(this.getrestGenerated()==2) //T4, T5, T42, T46, T47
			{
				return pR;
			}
			pn.setNextNode(pR);
			return pn;
		}
	}*/

	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode,Expression processedExpr)
	{
		PropertyNode pn = new PropertyNode(this.property, this.resultType);    
		PropertyNode firstNode = parent.equals(processedExpr) ? walkPropertyChain(pn, true,processedExpr) : pn;
		if(this.parent==null || this.parent instanceof RootExpression || processedExpr.equals(this.parent))
		{
			//	this.restGeneratedIncrement();
			if(evalNode!=null)
			{
				if(evalNode.isVariable()) // Means the node cannot be a value node
				{
					evalNode.setisVariable(false);
					return firstNode;
				}
				else // Could be value node or could be not
				{
					//TODO: shorten out the code. A function can be generated in the evaluation node and reused again.
					if(evalNode.resultValue instanceof Instance)
					{
						Instance ins=(Instance) evalNode.resultValue;
						return new ValueNode(ins.name());
					}
					else if(evalNode.resultValue instanceof Collection )
					{
						if(evalNode.parentEvalNode.expression instanceof OperationCallExpression)
						{
							Collection resultColl=(Collection)evalNode.resultValue;
							RestrictionNode rest=evalNode.generateValueBasedRestriction();
							if(rest!=null)
								return rest;
							else return firstNode;
						}
						else return firstNode;
					}
					else if(evalNode.resultValue==null)
					{
						return firstNode;
					}
					else
					{	
						String val = evalNode.resultValue != null ? evalNode.resultValue.toString() : "Undefined";
						return new ValueNode(val);

					}
				}
			}
			else
				return firstNode;
		}
		else
		{
			EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
			this.setrestGenerated(1);
			RestrictionNode pR=parent.generateRestrictions(parentEN, this);
			if(this.getrestGenerated()==2) //T4, T5, T42, T46, T47
			{
				return pR;
			}
			pn.setNextNode(pR);
			return pn;
		}
	}

	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr)
	{
		PropertyNode pn = new PropertyNode(this.property, this.resultType);    
		RestrictionNode firstNode =  (parent.equals(processedExpr) ? walkPropertyChain(pn, true,processedExpr) : pn);
		if(firstNode instanceof ValueNode)
			return firstNode;
		else if(this.isValue())
		{
			String val=valueExtraction(this, processedExpr);
			if(val==null)
				return firstNode;
			return new ValueNode(val);
		}
		else
		{
			return firstNode;
		}
	}


	private int isOriginSame()
	{
		if(this.origin.expression.getClass() == this.getClass())
		{
			PropertyCallExpression or=(PropertyCallExpression)this.origin.expression;
			if(this.equals(or))
			{
				return 0;
			}
			else if(this.property.equals(or.property))
			{
				if(this.a instanceof VariableExpression && or.a instanceof VariableExpression)
				{
					VariableExpression aV=(VariableExpression) this.a;
					VariableExpression oV=(VariableExpression) or.a;
					if(aV.name.contains(oV.name) || oV.name.contains(aV.name))
						return 1;
					return -1;
				}
			}
			return -1;
		}
		else
			return -1;

	}

	private boolean containRest(PropertyNode node, PropertyNode tocheck)
	{
		//if(node instanceof PropertyNode)
		//{
			while(node!=null)
			{
				if(((PropertyNode)node).getProperty().equalsIgnoreCase(tocheck.getProperty()))
				{
					return true;
				}
				else
					node=(PropertyNode) node.getNextNode();

			}
			return false;
		/*}
		else
		{
			while(node!=null)
			{
				((ValueNode)node).get
			}
			return false;
		}*/
	}

	private PropertyNode walkPropertyChain(PropertyNode currentNode, boolean isRoot,Expression processedExpression) 
	{
		int originStatus=isOriginSame();
		if(originStatus==0) // Coming from the same branch
		{
			PropertyNode pn = new PropertyNode(this.property, this.resultType);
			return pn;
		}
		else if(originStatus==1) //Coming in from different branch but same context
		{
			return currentNode;
		}
		if (a instanceof PropertyCallExpression) {
			PropertyNode priorNode = (PropertyNode)((PropertyCallExpression)a).walkPropertyChain(currentNode, false,processedExpression);
			if(this.parent instanceof PropertyCallExpression)
			{
				PropertyNode pn = new PropertyNode(this.property, this.resultType);
				pn.setNextNode(priorNode);
				return pn;
			}
			else
				if (!containRest((PropertyNode)priorNode, currentNode)) {
					PropertyNode temp=(PropertyNode)priorNode;
					while(priorNode.getNextNode()!=null)
						priorNode=(PropertyNode) priorNode.getNextNode();
					priorNode.setNextNode(currentNode);
					return temp;
				}
				else
					return priorNode;
		}
		/*if(a instanceof VariableExpression && eval!=null)
		{
			VariableExpression var=(VariableExpression)this.a;
			ValueNode val=new ValueNode(((Instance)(eval.children[0].resultValue)).getPropertyAsValue("name").toString());
			val.setNextNode(currentNode);
			return val;
		}*/
		if (!isRoot) {
			PropertyNode pn = new PropertyNode(this.property, this.resultType);
			pn.setNextNode(currentNode);
			currentNode = pn;
			return currentNode;
		}
		return currentNode;
	}
}
