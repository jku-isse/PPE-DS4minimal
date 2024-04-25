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
import java.util.HashSet;

import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;

public abstract class IteratorExpression<RT, ST, BT> extends Expression<RT> {

	protected Expression<Collection<ST>> source;
	protected Expression<BT> body;
	protected VariableExpression<ST> iterator1;
	protected VariableExpression<ST> iterator2;

	protected EvaluationNode bodyNode=null;
	protected Object iterator1Value=null;
	protected Object iterator2Value=null;
	protected Object resultValue=null;

	public IteratorExpression(Expression<Collection<ST>> source, VariableExpression<ST> iterator1, VariableExpression<ST> iterator2, Expression<BT> body) {
		super();
		this.source = source;
		this.body = body;
		this.iterator1 = iterator1;
		this.iterator2 = iterator2;

		this.source.setParent(this);
		this.body.setParent(this);

		//iterator type is a mixture of the source collection type and the element type
		this.resultType = ArlType.get(body.resultType.type, source.resultType.collection, body.resultType.nativeType);

		if (iterator1==null) throw new ParsingException("iterator '%s' does not declare a iterator variable.", source.getARL());
		//need not check iterator type because it  is derived from collection type
	}

	

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode sourceNode = this.source.evaluate(scopeElements);
		ArrayList nodes = new ArrayList();
		nodes.add(sourceNode);
		initialize();

		if (sourceNode.resultValue != null) { // might occur when the trying to iterate over a null collection
			if(sourceNode.resultValue instanceof Collection) {
				for (ST i1 : (Collection<ST>) sourceNode.resultValue) {
					iterator1Value = i1;
					if (iterator2 == null) {
						bodyNode = this.body.evaluate(scopeElements);
						nodes.add(bodyNode);
						process();
					} else {
						for (ST i2 : (Collection<ST>) sourceNode.resultValue) {
							iterator2Value = i2;
							bodyNode = this.body.evaluate(scopeElements);
							nodes.add(bodyNode);
							process();
						}
					}
				}
			}else{
				throw new ParsingException("Type of "+sourceNode.resultValue +" must be a collection.");
			}
		}
		return new EvaluationNode(this, resultValue, nodes);
	}

	abstract public void initialize();	// sets the result (needed in case the source is empty)
	abstract public void process(); // updates the dafault result (with every elemetn in the source). return true if no more updates are necessary
	abstract public String getPropertySet();
	abstract public boolean ispropertySetPresent(String it, Expression prev);
	
	
	@Override
	public Object getValueForVariable(Expression variable) {
		if (((VariableExpression<?>) variable).name.equals(iterator1.name))
			return iterator1Value;
		else if (iterator2!=null && ((VariableExpression<?>) variable).name.equals(iterator2.name))
			return iterator2Value;

		return parent.getValueForVariable(variable);
	}

	@Override
	public RT evaluate(Expression<?> child)  {
		/*
		if (!disposed) {
			Collection<ST> sourceValues = null;
			try {
				if (child == this.source) {
					sourceValues = this.source.getResultValue();
					if (sourceValues == null) {
						sourceValues = Collections.emptySet();
					}
					for (Expression bodyExpression : this.bodies) {
						bodyExpression.dispose();
					}
					this.bodies.clear();
					initialize(this.bodies, sourceValues, new ArrayList<VariableExpression<?,?>>(this.variables),
							Collections.emptyList());
					resultValue = check(this.bodies);
				} else {
					resultValue = check(this.bodies);
					if (!resultValue.equals(expectedValue)) {
						sourceValues = this.source.getResultValue();
						if (sourceValues == null) {
							sourceValues = Collections.emptySet();
						}
						for (Expression bodyExpression : new HashSet<Expression>(this.bodies)) {
							BodyHeadExpression<BT> exp = (BodyHeadExpression<BT>) bodyExpression;
							if (exp.getBody() != child) {
								bodyExpression.dispose();
								this.bodies.remove(bodyExpression);
							}
						}
						initialize(this.bodies, sourceValues,
								new ArrayList<VariableExpression<?,?>>(this.variables), Collections.emptyList());
						resultValue = check(this.bodies);
					}
				}
			} finally {
				sourceValues = null;
			}
		}
		return resultValue;

		 */
		return null;
	}

	@Override
	public String getARL() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.source.getARL());
		sb.append(" {");
		sb.append(iterator1.name);
		if (iterator2!=null) {
			sb.append(",");
			sb.append(iterator2.name);
		}
		sb.replace(sb.length() - 2, sb.length(), "} ");
		sb.append(body.getARL());
		return sb.toString();
	}
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation);
		StringBuilder sb = new StringBuilder();
		
		sb.append(" ");
		sb.append(iterator1.name);
		if (iterator1.resultType.nativeType != null) {// if not a simple type link String
			sb.append(" : <"); // type expression are lost, thus we need to ensure they can be recovered later
			sb.append(((InstanceType) iterator1.resultType.nativeType).getQualifiedName());
			sb.append(">");
		}
		if (iterator2!=null) {
			sb.append(", ");
			sb.append(iterator2.name);
			if (iterator2.resultType.nativeType != null) {// if not a simple type link String
				sb.append(" : <"); // type expression are lost, thus we need to ensure they can be recovered later
				sb.append(((InstanceType) iterator2.resultType.nativeType).getQualifiedName());
				sb.append(">");
			}
		}
		sb.append(" | \r\n");
		sb.append(body.getOriginalARL(indentation, true));
		return sb.toString();
	}
	
	@Override 
	public String getLocalARL() { return "Iterator";	}
}
