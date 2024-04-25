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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.PropertyNode;

/**
 * The basic element for the Abstract Rule Language. It contains the basic concepts
 * of first order rule languages. RT represents the result type of this expression.
 */
public abstract class Expression<V>  {

	protected ArlType resultType;
	protected Expression parent;
	protected List<Expression<?>> children=new ArrayList();

	//added
	protected EvaluationNode origin; // might omit later on
	protected EvaluationNode inconsistency_origin;
	protected int restGenerated=0; // 0 not generated, 1 generated, 2 generated second time
	protected boolean isValue=false;
	//till here

	public Expression() {	}

	// added

	public boolean isValue() {
		return isValue;
	}

	public void setValue(boolean isValue) {
		this.isValue = isValue;
	}


	public EvaluationNode getOrigin() {
		return origin;
	}

	public void resetInconsistencyData()
	{
		this.inconsistency_origin = null;
		if(this.parent!=null)
			this.parent.inconsistency_origin=null;
		this.children.forEach(exp->exp.resetInconsistencyData());

	}
	public void setInconsistency_Origin(EvaluationNode io)
	{
		if(this!=null)
		{
			if(this.toString().equals(io.expression.toString()) && this.inconsistency_origin==null)
			{
				this.inconsistency_origin=io;
				Expression par=this.parent;
				while (par!=null)
				{
					if(par.inconsistency_origin!=null)
						break;
					par.inconsistency_origin=io;
					par.setInconsistency_Origin(io);
					par=par.parent;
				}
			}
			else
			{
				if(this.children.size()>0)
					this.children.forEach(exp->exp.setInconsistency_Origin(io));
			}
		}
	}

	public void setOrigin(EvaluationNode origin) {
		this.origin = origin;
		if(this.parent!=null)
			this.parent.origin=origin;
		this.children.forEach(exp->exp.setOrigin(origin));
	}

	public int getrestGenerated() {
		return restGenerated;
	}

	public void restGeneratedIncrement()
	{
		if(this.restGenerated==1)
		{
			this.restGenerated = 2;
			this.children.forEach(exp->exp.restGeneratedIncrement());
			//this.children.forEach(exp->exp.setrestGenerated(2));
		}
		else if(this.restGenerated==0)
		{
			this.restGenerated = 1;
		}
	}

	public void setrestGenerated(int count) {
		this.restGenerated = count<0 ? 0 : count;
		this.children.forEach(exp->exp.setrestGenerated(count<0 ? 0 : count));
	}
	/*
	 * Function to find the nearest self evaluation node
	 * */
	public EvaluationNode getSelfEvalNode(EvaluationNode eval)
	{
		if(eval.expression instanceof VariableExpression)
			return eval;
		for(EvaluationNode child:eval.children)
		{
			if(child.expression.toString().contains("self"))
			{
				return getSelfEvalNode(child);
			}
		}
		if(eval.parentEvalNode.expression.toString().contains("self"))
			return getSelfEvalNode(eval.parentEvalNode);
		return null;
	}
	// Added Functions for restrictions
	public String valueExtraction(Expression exp, Expression processesdExpression)
	{
		// Assumption: Origin must be of property call expression for now.
		/*
		 * We might not need this assumption considering that we are tracing the nearest self node from the origin.
		 * which will still work even in case where origin is not property call exp. However, for now the check stays until we come
		 * across a scenario where origin is not a property call expression. (Reminder: Found 1 constraint in freq data where
		 * the origin is an operation call exp. However for testing we have to come up with a similar scenario. TODO: generate a test case 
		 * where evaluation node is null as well as the origin is an operation call expression)
		 * */
		if(this.origin.expression instanceof PropertyCallExpression)
		{
			EvaluationNode eval=getSelfEvalNode(origin);
			// now track through the eval properties to find the eval node we are looking for.
			exp.setValue(false); // in order to avoid the loop.
			PropertyNode rest=(PropertyNode)exp.generateRestrictions(processesdExpression);
			exp.setValue(true);
			if(eval.resultValue instanceof Instance)  // self will always represent an instance
			{
				Instance ins=(Instance) eval.resultValue;
				while(rest.getNextNode()!=null && ins!=null)
				{
					ins=ins.getPropertyAsInstance(rest.getProperty().toString());
					rest=(PropertyNode) rest.getNextNode();

				}
				if(ins!=null)
					if(ins.getProperty(rest.getProperty()) instanceof SetProperty)
					{
						List<Instance> prop= new ArrayList<>(ins.getPropertyAsSet(rest.getProperty()));
						if(prop.size()==1)
						{
							return prop.get(0).name();
						}
						else
						{
							//TODO: Multiple Values returned. TC98 can be altered to have multiple values in the req of jiraA.
							return null;
						}
					}
					else
						return ins.getPropertyAsValue(rest.getProperty()).toString();
			}
		}
		return null;
	}

	public int analyzeExpressions(Expression expA,Expression expB, Expression processesdExpression)
	{
		// Only dealing with cases where both sides are of property expressions. 
		if(expA instanceof PropertyCallExpression && expB instanceof PropertyCallExpression)
		{
			// Case 1: Where only one side contains self
			if(expA.toString().contains("self") && !expB.toString().contains("self"))
			{
				expA.setValue(true);
				expB.setValue(false);
				return 0;
			}
			else if(!expA.toString().contains("self") && expB.toString().contains("self"))
			{
				expB.setValue(true);
				expA.setValue(false);
				return 1;
			}
			// Case 2: Where both sides contains self.
			else if(expA.toString().contains("self") && expB.toString().contains("self"))
			{
				return 2;
			}
			// Case 3: Where neither side contains self
			else
			{
				//TODO: here we have to check even if entry point is not self it might be same. they might be under the same structure.
				//TODO: before implementing we have to come up with such a scenario if possible.
				return -1;
			}
		}
		else if(expA instanceof VariableExpression)
		{
			expA.setValue(false);
			expB.setValue(true);
		}
		else if(expB instanceof VariableExpression)
		{
			expA.setValue(true);
			expB.setValue(false);
		}
		return -1;
	}
	public void setVariable(EvaluationNode evalA, EvaluationNode evalB)
	{
		if(evalB!=null)
		{
			//Case 1: where one side is a literal expression
			if(evalB.expression instanceof LiteralExpression)
			{
				evalA.setisVariable(true);
			}
			else if(evalA.expression instanceof LiteralExpression)
			{
				evalB.setisVariable(true);
			}
			//Case 2: Only one side contains self. 
			//TODO: see when it executes
			else if(evalA.expression.toString().contains("self") && !evalB.expression.toString().contains("self"))
			{
				evalB.setisVariable(true);
			}
			else if(evalB.expression.toString().contains("self") && !evalA.expression.toString().contains("self"))
			{
				evalA.setisVariable(true);
			}
			//Case 3: neither literal nor self or self on both sides. In this case we will compare with values of the origin
			else 
			{
				// Step 1: Generate a Value set from the origin
				List<String> valueSet=Collections.synchronizedList(new LinkedList<String>());
				valueSet=originValueSet(this.origin, valueSet);
				// Step 2: Check which branch have the value from the origin value set
				if(comparingValueSet(evalA,valueSet))
				{
					evalA.setisVariable(true);
				}
				else if(comparingValueSet(evalB,valueSet))
				{
					evalB.setisVariable(true);
				}
				else
				{
					System.out.println("Neither evalA nor evalB contain the values");
				}
			}
		}
	}

	public boolean comparingValueSet(EvaluationNode eval,List<String> valueSet)
	{
		if(eval.resultValue instanceof Instance)
		{
			Instance ins=(Instance) eval.resultValue;
			if(valueSet.contains(ins.name()))
			{
				return true;
			}
		}
		else if(eval.resultValue instanceof String)
		{
			if(valueSet.contains(eval.resultValue))
			{
				return true;
			}
		}
		else if(eval.resultValue instanceof Collection)
		{
			Collection resultColl=(Collection)eval.resultValue;
			for(int i=0;i<resultColl.size();i++)
			{
				if(resultColl.toArray()[i] instanceof Boolean)
					return false;
				else
				{
					Instance ins=(Instance)resultColl.toArray()[i];
					if(valueSet.contains(ins.name()))
						return true;
				}
			}
		}
		return false;
	}

	public List<String> originValueSet(EvaluationNode eval,List<String> valueSet )
	{
		//if(!(eval.expression instanceof IteratorExpression))
		//	{
		//Adding values
		if(eval.resultValue instanceof Instance)
		{
			Instance ins=(Instance) eval.resultValue;
			valueSet.add(ins.name());
		}
		else if(eval.resultValue instanceof Collection)
		{
			Collection resultColl=(Collection)eval.resultValue;
			for(int i=0;i<resultColl.size();i++)
			{
				if(resultColl.toArray()[i] instanceof Instance)
				{
					Instance ins=(Instance)resultColl.toArray()[i];
					valueSet.add(ins.name());
				}
			}
		}
		// Only in case when evalNode represents origin.
		if(eval.equals(this.origin))
		{
			// Adding values of origin immediate child
			for(EvaluationNode child:eval.children)
			{
				valueSet=originValueSet(child, valueSet);
			}
			// Adding values of origin immediate parent
			EvaluationNode parentE=eval.parentEvalNode;
			valueSet=originValueSet(parentE, valueSet);
		}
		//	}
		return valueSet;
	}



	// till here			

	/**
	 * maintaining parent and child nodes of this expression
	 */
	public void setParent(Expression<?> parent) {
		if (this.parent!=null) this.parent.children.remove(this);
		this.parent = parent;
		if (parent!=null) parent.children.add(this);
	}


	public Expression<?> getParent() { return this.parent; }
	public List<Expression<?>> getChildren() { return this.children; }

	/**
	 * generates string representation of this expression, including its sub expressions
	 */
	abstract public String getARL();


	/**
	 * generates semantically equivalent string representation of the originally used syntax, including subexpression
	 * @param indentation TODO
	 * @param isOnNewLine TODO
	 */
	abstract public String getOriginalARL(int indentation, boolean isOnNewLine);
	
	
	protected String createWhitespace(int indentation) {
		return String.join("", Collections.nCopies(indentation, " "));
	}
	
	/**
	 * generates string representation of this expression, without its sub expressions
	 */
	abstract public String getLocalARL();
	
	/**
	 * Returns the resulting type of its evaluation.
	 */
	public ArlType getResultType() { return this.resultType; }

	/**
	 * perform a top-down initial evaluation this expression (usually starts at the root expression)
	 */
	abstract public EvaluationNode evaluate(HashSet scopeElements);

	/**
	 * perform a bottom-up evaluation this expression (usually starts at a leaf expression)
	 * @param child
	 *            The child expression that changed its evaluation result and
	 *            triggers a re-evaluation of its parent (this expression)
	 * @return evaluation result
	 */
	abstract public V evaluate(Expression<?> child);

	/**
	 * looks of the current value of a variable needed during evaluation.
	 * usually travels up to parent nodes where variables were defined
	 */
	public Object getValueForVariable(Expression<?> variable) {
		return this.parent.getValueForVariable(variable);
	}

	public String toString() { return getARL(); }

	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode){

	};

	public void generateRepairTree(RepairNode parent, String property, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		generateRepairTree(parent, expectedValue,evaluationNode);
	}

	public Expression getRootExpression(){
		if(this instanceof RootExpression)
			return this;
		return this.getParent().getRootExpression();
	}

	public String getRootProperty(){
		if(this instanceof PropertyCallExpression) {
			if(!this.children.isEmpty() && this.children.get(0) instanceof PropertyCallExpression) {
				return getChildProperty(this.children.get(0),((PropertyCallExpression<?, ?, ?>) this).property);
			}else
				return ((PropertyCallExpression<?, ?, ?>) this).property;
		}
		if(this.children.isEmpty())
			return null;
		else
			return this.children.get(0).getRootProperty();
	}

	private String getChildProperty(Expression e, String property){
		property = ((PropertyCallExpression<?, ?, ?>) e).property+"."+property;
		if(e.children != null && !e.children.isEmpty() && e.children.get(0) instanceof PropertyCallExpression)
			return getChildProperty((Expression) e.children.get(0),property);

		return property;
	}

	/**
	 * generates restriction tree on repair value
	 * */
/*	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr, RestrictionNode prev) {
		return null;
	}*/
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {
		if (parent != null && !parent.equals(processedExpr)) {
			EvaluationNode parentEN = null;
			if (evalNode != null && evalNode.expression.equals(this) && evalNode.parentEvalNode != null && evalNode.parentEvalNode.expression.equals(parent)) {
				parentEN = evalNode.parentEvalNode;
			}
			//this.parent.restGeneratedIncrement();
			return parent.generateRestrictions(parentEN, this);
		} 
		/*else if(parent!=null && parent.equals(processedExpr)) //originating from parent
		{
			return evalNode.expression.generateRestrictions(evalNode, this);
		}*/
		else
			return null;
	}

	/**
	 * generates restriction where the evaluation node is null
	 * */

	public RestrictionNode generateRestrictions(Expression processedExpr) {
		return null;
	}

	/**
	 * assists in traversing the evaluation tree
	 * @param evalNode
	 * @return the parent evaluation node only if the expressions are the same, otherwise null
	 */
	public EvaluationNode getParentIfExpressionMatches(EvaluationNode evalNode) {
		if (evalNode != null && evalNode.expression.equals(this) && evalNode.parentEvalNode != null && evalNode.parentEvalNode.expression.equals(parent)) {
			return evalNode.parentEvalNode;
		} else
			return null;
	}

	/**
	 * for expressions that return boolean and for evaluation node results not being the expected value, returns a more descriptive reason why it is fulfilled or not
	 * for other expressions returns the result value
	 * ASSUMPTION: this requires the repair tree being created first to have each evaluation tagged with expected value
	 */
	public Object explain(EvaluationNode node) {
		if (node == null) return null;
		if (node.expression != this) return null; // mismatch of this expression and the evaluation node
		if (!node.isMarkedAsOnRepairPath()) return node.resultValue; // no repair generation yet called or not part of a repair, hence no explaining necessary, just return value
		return explainInternally(node);			
	}

	protected Object explainInternally(EvaluationNode node) {
		return node.resultValue;
	};
}

