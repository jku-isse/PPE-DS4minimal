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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.exception.RepairException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairRestriction;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.OperationNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.ValueNode;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;

public class OperationCallExpression<RT, E> extends Expression<RT> {

	protected Expression<RT> source;
	protected String operation;
	protected List<Expression<ArlType>> args;


	static {
		//string
		new OperationDeclaration(ArlType.INTEGER, "size", ArlType.STRING);
		new OperationDeclaration(ArlType.STRING, "concat", ArlType.STRING, ArlType.STRING);
		new OperationDeclaration(ArlType.STRING, "substring", ArlType.STRING, ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.BOOLEAN, "startsWith", ArlType.STRING, ArlType.STRING);
		new OperationDeclaration(ArlType.BOOLEAN, "contains", ArlType.STRING, ArlType.STRING);
		new OperationDeclaration(ArlType.STRING, "toUpper", ArlType.STRING);
		new OperationDeclaration(ArlType.STRING, "toLower", ArlType.STRING);
		new OperationDeclaration(ArlType.INTEGER, "indexOf", ArlType.STRING, ArlType.STRING);
		new OperationDeclaration(ArlType.BOOLEAN, "equalsIgnoreCase", ArlType.STRING, ArlType.STRING);
		new OperationDeclaration(ArlType.STRING, "at", ArlType.STRING, ArlType.INTEGER);
		new OperationDeclaration(ArlType.LIST, "characters", ArlType.STRING);
		new OperationDeclaration(ArlType.INTEGER, "toInteger", ArlType.STRING);
		new OperationDeclaration(ArlType.REAL, "toReal", ArlType.STRING);
		new OperationDeclaration(ArlType.BOOLEAN, "toBoolean", ArlType.STRING);

		//boolean
		new OperationDeclaration(ArlType.STRING, "toString", ArlType.BOOLEAN);

		//integer
		new OperationDeclaration(ArlType.INTEGER, "abs", ArlType.INTEGER);
		new OperationDeclaration(ArlType.INTEGER, "div", ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.INTEGER, "mod", ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.INTEGER, "max", ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.INTEGER, "min", ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.REAL, "toReal", ArlType.INTEGER);
		new OperationDeclaration(ArlType.STRING, "toString", ArlType.INTEGER);

		//real
		new OperationDeclaration(ArlType.REAL, "abs", ArlType.REAL);
		new OperationDeclaration(ArlType.INTEGER, "floor", ArlType.REAL);
		new OperationDeclaration(ArlType.INTEGER, "round", ArlType.REAL);
		new OperationDeclaration(ArlType.REAL, "max", ArlType.REAL, ArlType.REAL);
		new OperationDeclaration(ArlType.REAL, "min", ArlType.REAL, ArlType.REAL);
		new OperationDeclaration(ArlType.INTEGER, "toInteger", ArlType.REAL);
		new OperationDeclaration(ArlType.STRING, "toString", ArlType.REAL);

		//collection
		new OperationDeclaration(ArlType.BOOLEAN, "includes", ArlType.COLLECTION, ArlType.ANY);
		new OperationDeclaration(ArlType.BOOLEAN, "excludes", ArlType.COLLECTION, ArlType.ANY);
		new OperationDeclaration(ArlType.BOOLEAN, "includesAll", ArlType.COLLECTION, ArlType.COLLECTION);
		new OperationDeclaration(ArlType.BOOLEAN, "excludesAll", ArlType.COLLECTION, ArlType.COLLECTION);
		new OperationDeclaration(ArlType.BOOLEAN, "isEmpty", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.INTEGER, "size", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.INTEGER, "count", ArlType.COLLECTION, ArlType.ANY);
		new OperationDeclaration(ArlType.NUMBER, "sum", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.NUMBER, "max", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.NUMBER, "min", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.ANY, "any", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.SET, "asSet", ArlType.COLLECTION);
		new OperationDeclaration(ArlType.LIST, "asList", ArlType.COLLECTION);

		//list
		new OperationDeclaration(ArlType.LIST, "union", ArlType.LIST, ArlType.LIST);
		new OperationDeclaration(ArlType.LIST, "append", ArlType.LIST, ArlType.ANY);
		new OperationDeclaration(ArlType.LIST, "prepend", ArlType.LIST, ArlType.ANY);
		new OperationDeclaration(ArlType.LIST, "insertAt", ArlType.LIST, ArlType.INTEGER, ArlType.ANY);
		new OperationDeclaration(ArlType.LIST, "subList", ArlType.LIST, ArlType.INTEGER, ArlType.INTEGER);
		new OperationDeclaration(ArlType.ANY, "at", ArlType.LIST, ArlType.INTEGER);
		new OperationDeclaration(ArlType.INTEGER, "indexOf", ArlType.LIST, ArlType.ANY);
		new OperationDeclaration(ArlType.ANY, "first", ArlType.LIST);
		new OperationDeclaration(ArlType.ANY, "last", ArlType.LIST);
		new OperationDeclaration(ArlType.LIST, "including", ArlType.LIST, ArlType.ANY);
		new OperationDeclaration(ArlType.LIST, "excluding", ArlType.LIST, ArlType.ANY);
		new OperationDeclaration(ArlType.LIST, "reverse", ArlType.LIST);
		new OperationDeclaration(ArlType.LIST, "sort", ArlType.LIST);

		//set
		new OperationDeclaration(ArlType.SET, "union", ArlType.SET, ArlType.SET);
		new OperationDeclaration(ArlType.SET, "intersection", ArlType.SET, ArlType.SET);
		new OperationDeclaration(ArlType.SET, "difference", ArlType.SET, ArlType.SET);
		new OperationDeclaration(ArlType.SET, "including", ArlType.SET, ArlType.ANY);
		new OperationDeclaration(ArlType.SET, "excluding", ArlType.SET, ArlType.ANY);
		new OperationDeclaration(ArlType.SET, "symmetricDifference", ArlType.SET, ArlType.SET);

		//instance
		new OperationDeclaration(ArlType.BOOLEAN, "isDefined", ArlType.INSTANCE);
	}

	public OperationCallExpression(Expression<RT> source, String operation, List<Expression<ArlType>> args) {
		super();
		this.source = source;
		this.operation = operation;
		this.args = args;

		source.setParent(this);
		for (Expression arg : args) arg.setParent(this);

		OperationDeclaration operationDeclaration = OperationDeclaration.findOperationDeclaration(operation, source.resultType, (List<Expression>) (Object) args);
		if (operationDeclaration==null) throw new ParsingException("operation '"+operation+"' does not apply to type '%s' or has wrong arguments", source.resultType);
		this.resultType = operationDeclaration.returnType;
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements) {
		EvaluationNode sourceNode = this.source.evaluate(scopeElements);
		EvaluationNode[] nodes = new EvaluationNode[args.size()+1];
		List argsValues = new ArrayList();

		int i=1;
		for (Expression arg : this.args) {
			EvaluationNode argNode = arg.evaluate(scopeElements);
			nodes[i++] = argNode;
			argsValues.add(argNode.resultValue);
		}
		nodes[0] = sourceNode;
		return new EvaluationNode(this, check((E)sourceNode.resultValue, argsValues), nodes);
	}

	public RT check(E source, List<RT> args) {
		Object result=null;
		// Added
		/*if(source instanceof Instance)
		{
			switch(operation.toLowerCase())
			{
			case "tostring":

				result=((Instance)source).toString();
				break;
			}
		}
		else */
		// till here
		if (source instanceof String) {
			switch (operation.toLowerCase()) {
			case "size":
				result = (long)((String)source).length();
				break;
			case "concat":
				result = ((String)source)+((String)args.get(0));
				break;
			case "substring":
				int start =  Math.toIntExact((Long)args.get(0))-1; // from 1 based ARL to 0 based position in java
				int end = Math.toIntExact((Long) args.get(1));
				String str = (String)source;
				if (start >= end) throw new EvaluationException("indexes of substring operation are invalid: start > end");
				if (str.length() == 0) {
					result = "";
				} else	if (start <((String)source).length()) {
					if ((end-1)<((String)source).length()) {
						result = str.substring(start, end);
					} else { // end is earlier than end of range, return up to end
						result = str.substring(start);
					}
				} else { // string shorter than the start of the range
					result = "";
				}
				break;
			case "startswith":
				if (source == null) 
					result = Boolean.FALSE;
				else
					result = ((String)source).startsWith((String)args.get(0));
				break;
			case "contains":
				if (source == null) 
					result = Boolean.FALSE;
				else
					result = ((String)source).indexOf((String)args.get(0)) >= 0;
					break;						
			case "toupper":
				result = ((String)source).toUpperCase();
				break;
			case "tolower":
				result = ((String)source).toLowerCase();
				break;
			case "indexof":
				result = (long)((String)source).indexOf((String)args.get(0))+1;
				break;
			case "equalsignorecase":
				result = ((String)source).equalsIgnoreCase((String)args.get(0));
				break;
			case "at":
				if ((Long)args.get(0)<=((String)source).length())
					result = ""+((String)source).charAt((int) ((Long)args.get(0)-1));
				else
					throw new EvaluationException("index of at operation is invalid");
				break;
			case "characters":
				result = ((String)source).chars().mapToObj(e -> ""+((char)e)).collect(Collectors.toList());
				break;
			case "tointeger":
				result = Long.valueOf((String)source);
				break;
			case "toreal":
				result = Double.valueOf((String)source);
				break;
			case "toboolean": 
				result = Boolean.valueOf((String)source);
				break;
			default:
				throw new EvaluationException("Illegal string operation "+operation);
			}
		}
		else if (source instanceof Boolean) {
			switch (operation.toLowerCase()) {
			case "tostring":
				result = "" + ((Boolean) source);
				break;
			default:
				throw new EvaluationException("Illegal boolean operation " + operation);
			}
		}
		else if (source instanceof Long) {
			switch (operation.toLowerCase()) {
			case "abs":
				result = Long.valueOf(Math.abs((Long)source));
				break;
			case "div":
				result = Long.valueOf(((Long)source)/((Long)args.get(0)));
				break;
			case "mod":
				result = Long.valueOf(((Long)source)%((Long)args.get(0)));
				break;
			case "max":
				result = Long.valueOf(Math.max((Long)source, (Long)args.get(0)));
				break;
			case "min":
				result = Long.valueOf(Math.min((Long)source, (Long)args.get(0)));
				break;
			case "toreal":
				result = ((Long)source).doubleValue();
				break;
			case "tostring":
				result = ""+source;
				break;
			default:
				throw new EvaluationException("Illegal integer operation "+operation);
			}
		}
		else if (source instanceof Double) {
			switch (operation.toLowerCase()) {
			case "abs":
				result = Double.valueOf(Math.abs((Double) source));
				break;
			case "floor":
				result = Long.valueOf(""+(int) Math.floor((Double) source));
				break;
			case "round":
				result = Long.valueOf(""+(int) Math.round((Double) source));
				break;
			case "max":
				result = Double.valueOf(Math.max((Double) source, (Double) args.get(0)));
				break;
			case "min":
				result = Double.valueOf(Math.min((Double) source, (Double) args.get(0)));
				break;
			case "tointeger":
				result = ((Double)source).longValue();
				break;
			case "tostring":
				result = "" + source;
				break;
			default:
				throw new EvaluationException("Illegal double operation " + operation);
			}
		}
		else if (source instanceof Collection) {
			switch (operation.toLowerCase()) {
			case "excludes":
				result = !((Collection) source).contains(args.get(0));
				break;
			case "excludesall":
				result = Collections.disjoint((Collection) source, (Collection) args.get(0));
				break;
			case "includes":
				result = ((Collection) source).contains(args.get(0));
				break;
			case "includesall":
				result = ((Collection) source).containsAll((Collection) args.get(0));
				break;
			case "isempty":
				result = ((Collection) source).size()==0;
				break;
			case "size":
				result = (long)((Collection) source).size();
				break;
			case "count":
				result = (long)Collections.frequency((Collection) source, args.get(0));
				break;
			case "sum":
				try {
					double sum = 0.0;
					for (Number n : (Collection<Number>) source) {
						sum += n.doubleValue();
					}
					result = sum;
				} catch (Exception ex) {
					throw new EvaluationException("sum operation needs to be provided with numbers. The actual values are " + source.toString());
				}
				break;
			case "max":
				try {
					double max = Double.MIN_VALUE;
					for (Number n : (Collection<Number>) source) {
						double val = n.doubleValue();
						if (max < val) max = val;
					}
					result = max;
				} catch (Exception ex) {
					throw new EvaluationException("max operation needs to be provided with numbers. The actual values are " + source.toString());
				}
				break;
			case "min":
				try {
					double min = Double.MAX_VALUE;
					for (Number n : (Collection<Number>) source) {
						double val = n.doubleValue();
						if (min > val) min = val;
					}
					result = min;
				} catch (Exception ex) {
					throw new EvaluationException("min operation needs to be provided with numbers. The actual values are " + source.toString());
				}
				break;
			case "any":
				if (((Collection) source).size()==0)
					result = null;
				else
					result = ((Collection) source).iterator().next();
				break;
			case "asset":
				result = new HashSet((Collection) source);
				break;
			case "aslist":
				result = new ArrayList((Collection) source);
				break;
			default:
			}
			if (source instanceof List) {
				switch (operation.toLowerCase()) {
				case "union":
					result = new ArrayList((Collection) source);
					((List) result).addAll((Collection) args.get(0));
					break;
				case "append":
					result = new ArrayList((Collection) source);
					((List) result).add(args.get(0));
					break;
				case "prepend":
					result = new ArrayList((Collection) source);
					((List) result).add(0, args.get(0));
					break;
				case "insertat":
					result = new ArrayList((Collection) source);
					((List) result).add(((Long)args.get(0)).intValue(), args.get(1));
					break;
				case "sublist":
					result = new ArrayList((Collection) source);
					((List) result).subList(((Long)args.get(0)).intValue(), ((Long)args.get(1)).intValue());
					break;
				case "at":
					if (((List) source).isEmpty())
						result = null;
					else
						result=((List) source).get(((Long)args.get(0)).intValue());
					break;
				case "indexof":
					result=(long)((List) source).indexOf(args.get(0));
					break;
				case "first":
					if (((List) source).isEmpty())
						result = null;
					else
						result=((List) source).get(0);
					break;
				case "last":
					if (((List) source).isEmpty())
						result = null;
					else
						result=((List) source).get(((List) source).size() - 1);
					break;
				case "including":
					result = new ArrayList((Collection) source);
					if (args.get(0) instanceof Collection)
						((List) result).addAll((Collection)args.get(0));
					else
						((List) result).add(args.get(0));
					break;
				case "excluding":
					result = new ArrayList((Collection) source);
					if (args.get(0) instanceof Collection)
						((List) result).removeAll((Collection)args.get(0));
					else
						((List) result).remove(args.get(0));
					break;
				case "reverse":
					result = new ArrayList((Collection) source);
					Collections.reverse(((List) result));
					break;
				case "sort":
					result = new ArrayList((Collection) source);
					Collections.sort(((List) result));
					break;
				default:
				}
			}
			else if (source instanceof Set) {
				Set arg0 = null;
				if (!args.isEmpty() && args.get(0) != null) {
					if(args.get(0) instanceof Collection)
						arg0 = (Set) args.get(0);
				} else{
					arg0 = Collections.emptySet();
				}
				switch (operation.toLowerCase()) {
				case "union":
					result = Sets.union((Set) source, arg0);
					break;
				case "intersection":
					result = Sets.intersection((Set) source, arg0);
					break;
				case "difference":
					result = Sets.difference((Set) source, arg0);
					break;
				case "including":
					result = new HashSet((Collection) source);
					if (args.get(0) instanceof Collection)
						((Set) result).addAll(arg0);
					else
						((Set) result).add(arg0);
					break;
				case "excluding":
					result = new HashSet((Collection) source);
					if (args.get(0) instanceof Collection)
						((Set) result).removeAll(arg0);
					else
						((Set) result).remove(arg0);
					break;
				case "symmetricdifference":
					result = Sets.symmetricDifference((Set) source, arg0);
					break;
				default:
				}
			}
		}
		else {
			switch (operation.toLowerCase()) {
			case "isdefined":
				result = source != null;
				break;
				// TO EXTEND: if we have some null values, we need to handle these as well where it makes sense:
			case "size": result = 0l; break;
			case "substring": result = ""; break;
			case "contains": result = false; break;
			case "startswith": result = false; break;
			case "indexof": result = -1l; break;
			case "toboolean": result = false; break;
			case "equalsignorecase": result = false; break;
			case "excludes": result = true; break; // a null collection always doesn't include anything else
			case "excludesall": result = true; break; // a null collection always doesn't include anything else
			case "includes": result = false; break; // a null collection never includes anything
			case "includesall": result = false; break; // a null collection never includes anything
			case "isempty": result = true; break;
			case "count": result = 0l; break;
			case "sum": result = 0l; break;

			default:
			}
		}
		return (RT)result;
	}

	@Override
	public RT evaluate(Expression<?> child) {
		/*
		if (!disposed) {
			if (child != null) {
				if (child.equals(source)) {
					this.contextCache = (E) child.getResultValue();
					if (this.args.contains(child)) {
						int index = this.args.child.its(inde);
						this.argCache.set(index, (RT) child.getResultValue());
					}
				}
			}
			resultValue = check(this.contextCache, this.argCache);
		}
		return resultValue;

		 */
		return null;
	}

	@Override
	public String getARL() {
		StringBuilder sb = new StringBuilder();
		sb.append(operation.toUpperCase());
		sb.append("(");
		sb.append(this.source.getARL());
		for (Expression arg : this.args) {
			sb.append(",");
			sb.append(arg.getARL());
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation);
		String whitespaceBegin = isOnNewLine ? whitespace : "";
		StringBuilder sb = new StringBuilder(whitespaceBegin);
		sb.append(this.source.getOriginalARL(indentation, isOnNewLine));
		sb.append("\r\n");
		sb.append(whitespace);
		sb.append(".");
		sb.append(operation);
		sb.append("(");
		sb.append(	this.args.stream()
				.map(arg -> arg.getOriginalARL(indentation+2, false))
				.collect(Collectors.joining(", ")));
		sb.append(")");
		return sb.toString();
	}

	@Override 
	public String getLocalARL() { return operation.toUpperCase();	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		EvaluationNode sourceNode = evaluationNode.children[0];

		if (sourceNode.expression.resultType.equals(ArlType.STRING)) {
			//if(sourceNode.resultValue instanceof String) {
			switch (operation.toLowerCase()) {
			case "size":
				generateRepairTreeStringSize(parent,expectedValue,evaluationNode);
				break;
			case "concat":
				generateRepairTreeStringConcat(parent,expectedValue,evaluationNode);
				break;
			case "contains":
				generateRepairTreeStringContains(parent,expectedValue,evaluationNode);
				break;	
			case "substring":
				generateRepairTreeStringSubString(parent,expectedValue,evaluationNode);
				break;
			case "equalsignorecase":
				generateRepairTreeStringEqualsIgnoreCase(parent,expectedValue,evaluationNode);
				break;
			case "startswith":
				generateRepairTreeStringStartsWith(parent,expectedValue,evaluationNode);
				break;
			case "indexof":					
			case "toupper":
			case "tolower":				
			case "at":
			case "characters":
			case "tointeger":
			case "toreal":
			case "toboolean":
				throw new RepairException("generateRepairTree not implemented");

			}
		}
		else if (sourceNode.resultValue instanceof Boolean) {
			switch (operation.toLowerCase()) {
			case "tostring":
				throw new RepairException("generateRepairTree not implemented");
			}
		}
		else if (sourceNode.resultValue instanceof Long) {
			switch (operation.toLowerCase()) {
			case "abs":
			case "div":
			case "mod":
			case "max":
			case "min":
			case "tostring":
				throw new RepairException("generateRepairTree not implemented");
			}
		}
		else if (sourceNode.resultValue instanceof Double) {
			switch (operation.toLowerCase()) {
			case "abs":
			case "floor":
			case "round":
			case "max":
			case "min":
			case "tostring":
				throw new RepairException("generateRepairTree not implemented");
			}
		}  else if (sourceNode.resultValue instanceof Collection ||
				sourceNode.expression.resultType.equals(ArlType.COLLECTION) ||
				sourceNode.expression.resultType.equals(ArlType.SET) ||
				sourceNode.expression.resultType.equals(ArlType.LIST) ||
				sourceNode.expression.resultType.equals(ArlType.MAP) 
				) {		
			switch (operation.toLowerCase()) {
			case "any":
				generateRepairTreeNaiveAny(parent, expectedValue, evaluationNode);
				break;
			case "sum":
			case "max":
			case "min":
			case "asset":
			case "aslist":
				evaluationNode.children[0].generateRepairTree(parent, expectedValue);
				break;
			case "isempty":
				generateRepairTreeCollectionIsEmpty(parent,expectedValue,evaluationNode);
				break;
			case "size":
				generateRepairTreeCollectionSize(parent,expectedValue,evaluationNode);
				break;
			case "count":
				generateRepairTreeCollectionCount(parent,expectedValue,evaluationNode);
				break;
			case "includes":
				generateRepairTreeCollectionIncludes(parent,expectedValue,evaluationNode);
				break;
			case "includesall":
				generateRepairTreeCollectionIncludesAll(parent,expectedValue,evaluationNode);
				break;
			case "excludes":
				generateRepairTreeCollectionIncludes(parent,new RepairSingleValueOption(expectedValue.operator,expectedValue.getValue(),!expectedValue.getExpectedEvaluationResult()),evaluationNode);
				break;
			case "excludesall":
				generateRepairTreeCollectionExcludesAll(parent,expectedValue,evaluationNode);
				break;
			}
			if (sourceNode.resultValue instanceof List 
					|| sourceNode.expression.resultType.equals(ArlType.LIST) ) {
				switch (operation.toLowerCase()) {
				case "union":
					generateRepairTreeCollectionUnion(parent,expectedValue,evaluationNode);
					break;
				case "append":
				case "prepend":
				case "insertat":
				case "sublist":
				case "reverse":
				case "excluding":
				case "sort":
				case "at":
				case "indexof":
				case "first":
				case "last":
				case "including":
					evaluationNode.children[0].generateRepairTree(parent, expectedValue);

				}
			}
			else if (sourceNode.resultValue instanceof Set
					|| sourceNode.expression.resultType.equals(ArlType.SET)) {
				switch (operation.toLowerCase()) {
				case "union":
					generateRepairTreeCollectionUnion(parent,expectedValue,evaluationNode);
					break;
				case "intersection":
					generateRepairTreeCollectionIntersection(parent, expectedValue, evaluationNode);
					break;
					/*evaluationNode.children[0].generateRepairTree(parent, expectedValue);
					evaluationNode.children[1].generateRepairTree(parent, expectedValue);*/
				case "difference":
				case "including":
				case "excluding":
				case "symmetricdifference":
					evaluationNode.children[0].generateRepairTree(parent, expectedValue);
				}

			}
		}
		else {
			switch (operation.toLowerCase()) {
			case "isdefined":
				generateRepairTreeIsDefined(parent,expectedValue,evaluationNode);
				break;
			default:
			}


		}


	}



	private void generateRepairTreeIsDefined(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new SequenceRepairNode(parent);
		Instance inst = evaluationNode.getInstanceValue();
		if (inst == null && evaluationNode.children.length > 0 && evaluationNode.children[0].children.length > 0) {
			// e.g., when an any is used, then suggest to add anything downstream, not quite correct as any is random and another element could have fulfilled it
			Map<Instance, String> coll = findFirstCollectionSource(evaluationNode.children[0].children[0]); 
			if (!coll.isEmpty()) {
				RepairNode childNode = new AlternativeRepairNode(node);
				coll.entrySet().forEach(entry -> {
					new ConsistencyRepairAction(childNode, entry.getValue(), entry.getKey(), new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
				});
			}
		}

		if (inst!=null) {
			boolean result;
			if(expectedValue.getValue() instanceof Boolean)
				result = expectedValue.getExpectedEvaluationResult();
			else
				result = expectedValue.getExpectedEvaluationResult();
			if (result) {
				evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN));
			} else {
				evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.MOD_EQ,UnknownRepairValue.UNKNOWN));
			}

		}
	}

	private void generateRepairTreeCollectionIsEmpty(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		String property = getRootProperty();
		Instance inst = evaluationNode.getInstanceValue();
		if(!inst.hasProperty(property)){
			inst = (Instance) evaluationNode.children[0].children[0].resultValue;
		}
		RepairNode node;
		// if expects true, remove all elements from collection 
		if (expectedValue.getExpectedEvaluationResult()) {
			node = new SequenceRepairNode(parent);
			// if there is a subExpression (e.g. select), then it generates repairs based on it
			//TODO: Change: Commented the IF 
			/*if(this.source.children.get(0) instanceof PropertyCallExpression )
				evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, false));
			else*/{
				Collection values = (Collection) evaluationNode.children[0].resultValue;
				for (Iterator i = values.iterator(); i.hasNext();) {
					evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.REMOVE, i.next(),false));
					evaluationNode.incrementRepairGap();
				}
			}

		} else {
			node = new AlternativeRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(Operator.ADD, null));
			evaluationNode.decrementRepairGap();
			if(this.source.children.get(0) instanceof PropertyCallExpression ) {// if there is a subExpression (e.g. select), then it generate repairs based on it
				evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, true));				
			}
		}
	}
	private void generateRepairTreeCollectionExcludesAll(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode alternativeRepairNode = new AlternativeRepairNode(parent);
		String property1 = null;
		String property2 = null;
		if(this.source instanceof PropertyCallExpression )
			property1 = ((PropertyCallExpression) this.source).property;
		if(this.args.get(0) instanceof PropertyCallExpression )
			property2 = ((PropertyCallExpression) this.args.get(0)).property;
		Set collection1 = new HashSet((Collection) evaluationNode.children[0].resultValue);
		Set collection2 = new HashSet((Collection) evaluationNode.children[1].resultValue);

		if (expectedValue.getExpectedEvaluationResult()) {
			RepairNode node = new AlternativeRepairNode(alternativeRepairNode);
			// if expects false, removes all elements from the children OR adds a new element to the other collection
			RepairNode childNode = new SequenceRepairNode(node);
			if(evaluationNode.children[1].children[0].resultValue instanceof  Instance)
				new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue, new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			for (Object o : collection2) {
				if(collection1.contains(o)) {
					if(evaluationNode.children[0].children[0].resultValue instanceof  Instance) {
						evaluationNode.decrementRepairGap();
						new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue, new RepairSingleValueOption(Operator.REMOVE, o), evaluationNode);
					}
				}
			}
		} else{
			RepairNode node = new SequenceRepairNode(alternativeRepairNode);
			// if expects true, adds all elements to the children OR remove all elements not included from the other side of the expression
			for (Object o : collection2) {
				if(!collection1.contains(o)) {
					RepairNode childNode = new AlternativeRepairNode(node);
					evaluationNode.incrementRepairGap();
					if(evaluationNode.children[0].children[0].resultValue instanceof  Instance)
						new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue, new RepairSingleValueOption(Operator.ADD, o), evaluationNode);
					if(evaluationNode.children[1].children[0].resultValue instanceof  Instance)
						new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue, new RepairSingleValueOption(Operator.REMOVE, o), evaluationNode);
				}
			}
		}
	}

	private void generateRepairTreeCollectionIncludesAll(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode alternativeRepairNode = new AlternativeRepairNode(parent);
		String property1 = null;
		String property2 = null;
		if(this.source instanceof PropertyCallExpression )
			property1 = getRootProperty();
		if(this.args.get(0) instanceof PropertyCallExpression )
			property2 = ((PropertyCallExpression) this.args.get(0)).property;
		Object col1 = evaluationNode.children[0].resultValue;
		Set collection1 = new HashSet();
		if(col1 != null)
			collection1 = new HashSet((Collection) col1);
		Object col2 = evaluationNode.children[1].resultValue;

		Set collection2 = new HashSet();
		if(col2 != null)
			collection2 = new HashSet((Collection) col2);

		if (expectedValue.getExpectedEvaluationResult()) {
			RepairNode node = new SequenceRepairNode(alternativeRepairNode);
			// if expects true, adds all elements to the children OR remove all elements not included from the other side of the expression
			for (Object o : collection2) {
				if(!collection1.contains(o)) {
					evaluationNode.incrementRepairGap();
					RepairNode childNode = new AlternativeRepairNode(node);
					if(evaluationNode.children[0].children[0].resultValue instanceof  Instance)
						new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue, new RepairSingleValueOption(Operator.ADD, o), evaluationNode);
					if(evaluationNode.children[1].children[0].resultValue instanceof  Instance)
						new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue, new RepairSingleValueOption(Operator.REMOVE, o), evaluationNode);
				}
			}
		} else{
			RepairNode node = new AlternativeRepairNode(alternativeRepairNode);
			// if expects false, removes all elements from the children OR adds a new element to the other collection
			RepairNode childNode = new AlternativeRepairNode(node);
			if(evaluationNode.children[1].children[0].resultValue instanceof  Instance)
				new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue, new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			for (Object o : collection2) {
				if(collection1.contains(o)) {
					evaluationNode.decrementRepairGap();
					if(evaluationNode.children[0].children[0].resultValue instanceof  Instance)
						new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue, new RepairSingleValueOption(Operator.REMOVE, o), evaluationNode);
				}
			}
		}
	}



	private void generateRepairTreeCollectionIncludes(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new SequenceRepairNode(parent);

		String property = getRootProperty();
		Instance inst = evaluationNode.getInstanceValue();
		if(!inst.hasProperty(property)){
			inst = (Instance) evaluationNode.children[0].children[0].resultValue;
		}
		if (expectedValue.getExpectedEvaluationResult()) {
			// if expects true, add the element to the children
			evaluationNode.incrementRepairGap();
			new ConsistencyRepairAction(node, property, inst, new RepairSingleValueOption(Operator.ADD, evaluationNode.children[1].resultValue), evaluationNode);

		} else{
			// if expects false, remove the element from the children
			new ConsistencyRepairAction(node,property, inst, new RepairSingleValueOption(Operator.REMOVE,evaluationNode.children[1].resultValue), evaluationNode);
			evaluationNode.decrementRepairGap();
		}
	}
	private void generateRepairTreeCollectionUnion(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new AlternativeRepairNode(parent);
		evaluationNode.children[0].generateRepairTree(node,expectedValue);
		evaluationNode.children[1].generateRepairTree(node,expectedValue);
		if (evaluationNode.children[0].isMarkedAsOnRepairPath())
			evaluationNode.incrementRepairGap();
		if (evaluationNode.children[1].isMarkedAsOnRepairPath())
			evaluationNode.decrementRepairGap();
	}
	private void generateRepairTreeCollectionIntersection(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode alt_node = new AlternativeRepairNode(parent);
		Object col0 = evaluationNode.children[0].resultValue;
		Set collection0 = new HashSet();
		if(col0 != null)
			collection0 = new HashSet((Collection) col0);

		Object col1 = evaluationNode.children[1].resultValue;
		Set collection1 = new HashSet();
		if(col1 != null)
			collection1 = new HashSet((Collection) col1);

		alt_node = new SequenceRepairNode(parent);
		evaluationNode.children[0].generateRepairTree(alt_node,expectedValue);
		evaluationNode.children[1].generateRepairTree(alt_node,expectedValue);
		if (evaluationNode.children[0].isMarkedAsOnRepairPath())
			evaluationNode.incrementRepairGap();
		if (evaluationNode.children[1].isMarkedAsOnRepairPath())
			evaluationNode.decrementRepairGap();
		
		/*if(collection0.isEmpty() && collection1.isEmpty()) // both childs are empty
		{
			alt_node = new SequenceRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(alt_node,expectedValue);
			evaluationNode.children[1].generateRepairTree(alt_node,expectedValue);
			if (evaluationNode.children[0].isMarkedAsOnRepairPath())
				evaluationNode.incrementRepairGap();
			if (evaluationNode.children[1].isMarkedAsOnRepairPath())
				evaluationNode.decrementRepairGap();
		}
		else if(!collection0.isEmpty() && !collection1.isEmpty())
		{
			evaluationNode.children[0].generateRepairTree(alt_node,expectedValue);
			evaluationNode.children[1].generateRepairTree(alt_node,expectedValue);
			if (evaluationNode.children[0].isMarkedAsOnRepairPath())
				evaluationNode.incrementRepairGap();
			if (evaluationNode.children[1].isMarkedAsOnRepairPath())
				evaluationNode.decrementRepairGap();
		}
		else
		{
			alt_node = new SequenceRepairNode(parent);
			evaluationNode.children[0].generateRepairTree(alt_node,expectedValue);
			evaluationNode.children[1].generateRepairTree(alt_node,expectedValue);
			if (evaluationNode.children[0].isMarkedAsOnRepairPath())
				evaluationNode.incrementRepairGap();
			if (evaluationNode.children[1].isMarkedAsOnRepairPath())
				evaluationNode.decrementRepairGap();
		}*/


		//	else
		/*else if(child0!=null) 
		{
			evaluationNode.children[1].generateRepairTree(node,expectedValue);
			if (evaluationNode.children[1].isMarkedAsOnRepairPath())
				evaluationNode.incrementRepairGap();
		}
		else if(child1!=null)
		{
			evaluationNode.children[0].generateRepairTree(node,expectedValue);
			if (evaluationNode.children[0].isMarkedAsOnRepairPath())
				evaluationNode.incrementRepairGap();
		}*/
		/*evaluationNode.children[0].generateRepairTree(node,expectedValue);
		evaluationNode.children[1].generateRepairTree(node,expectedValue);
		if (evaluationNode.children[0].isMarkedAsOnRepairPath())
			evaluationNode.incrementRepairGap();
		if (evaluationNode.children[1].isMarkedAsOnRepairPath())
			evaluationNode.decrementRepairGap();*/
	}

	private void generateRepairTreeCollectionCount(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode node = new SequenceRepairNode(parent);

		String property = getRootProperty();
		Instance inst = evaluationNode.getInstanceValue();
		if(!inst.hasProperty(property)){
			inst = (Instance) evaluationNode.children[0].children[0].resultValue;
		}
		if(expectedValue.operator == Operator.MOD_EQ){
			if((Long)evaluationNode.resultValue < (Long)expectedValue.getValue()){
				for (long i = (Long) evaluationNode.resultValue; i < (Long)expectedValue.getValue(); i++) {
					new ConsistencyRepairAction(node, property, inst, new RepairSingleValueOption(Operator.ADD,evaluationNode.children[1].resultValue), evaluationNode);
				}
			}
			else{
				for (long i = (Long)expectedValue.getValue(); i < (Long) evaluationNode.resultValue; i++) {
					new ConsistencyRepairAction(node, property, inst, new RepairSingleValueOption(Operator.REMOVE,evaluationNode.children[1].resultValue), evaluationNode);
				}
			}
		}else if(expectedValue.operator == Operator.MOD_NEQ){
			RepairNode childNode = new AlternativeRepairNode(node);
			new ConsistencyRepairAction(childNode, property, inst, new RepairSingleValueOption(Operator.ADD,evaluationNode.children[1].resultValue), evaluationNode);
			new ConsistencyRepairAction(childNode, property, inst, new RepairSingleValueOption(Operator.REMOVE,evaluationNode.children[1].resultValue), evaluationNode);
		}else if(expectedValue.operator == Operator.MOD_LT){
			for (long i = (Long) evaluationNode.resultValue; i >= (Long)expectedValue.getValue(); i--) {
				new ConsistencyRepairAction(node, property, inst, new RepairSingleValueOption(Operator.REMOVE,evaluationNode.children[1].resultValue), evaluationNode);
			}
		}else if(expectedValue.operator == Operator.MOD_GT){
			for (long i = (Long) evaluationNode.resultValue; i <= (Long)expectedValue.getValue(); i++) {
				new ConsistencyRepairAction(node, property, inst, new RepairSingleValueOption(Operator.ADD,evaluationNode.children[1].resultValue), evaluationNode);
			}
		}
	}

	private void generateRepairTreeStringSubString(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode childNode = new AlternativeRepairNode(parent);
		String property = getRootProperty();
		if(property != null && evaluationNode.children[0].children[0].resultValue instanceof  Instance)
			if(expectedValue.operator.equals(Operator.MOD_EQ))
				new ConsistencyRepairAction(childNode, property, (Instance) evaluationNode.children[0].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			else if(expectedValue.operator.equals(Operator.MOD_NEQ))
				new ConsistencyRepairAction(childNode, property, (Instance) evaluationNode.children[0].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_NEQ,evaluationNode.children[0].resultValue), evaluationNode);


	}

	private void generateRepairTreeStringContains(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode childNode = new AlternativeRepairNode(parent);
		String property = getRootProperty();
		if(property != null && evaluationNode.children[0].children[0].resultValue instanceof  Instance)
			if(expectedValue.operator.equals(Operator.MOD_EQ))
				new ConsistencyRepairAction(childNode, property, (Instance) evaluationNode.children[0].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			else if(expectedValue.operator.equals(Operator.MOD_NEQ))
				new ConsistencyRepairAction(childNode, property, (Instance) evaluationNode.children[0].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_NEQ,evaluationNode.children[0].resultValue), evaluationNode);
	}

	private void generateRepairTreeStringEqualsIgnoreCase(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode repairChildNode = new AlternativeRepairNode(parent);
		//		String property1 = null;
		//		String lhsValue = (String)evaluationNode.children[0].resultValue;
		//		String rhsValue = (String)evaluationNode.children[1].resultValue;
		//
		//		generateRepairTreeEqualsIgnoreCaseChildbranch(evaluationNode.children[0], repairChildNode, rhsValue, expectedValue.operator);
		//		generateRepairTreeEqualsIgnoreCaseChildbranch(evaluationNode.children[1], repairChildNode, lhsValue, expectedValue.operator);
		Operator op;
		if(expectedValue.getExpectedEvaluationResult())
			op = Operator.MOD_EQ;
		else
			op = Operator.MOD_NEQ;
		evaluationNode.children[0].generateRepairTree(repairChildNode,  new RepairSingleValueOption(op, evaluationNode.children[1].resultValue));
		evaluationNode.children[1].generateRepairTree(repairChildNode,  new RepairSingleValueOption(op, evaluationNode.children[0].resultValue));
		if (evaluationNode.children[0].isMarkedAsOnRepairPath())
			evaluationNode.incrementRepairGap();
		if (evaluationNode.children[1].isMarkedAsOnRepairPath())
			evaluationNode.decrementRepairGap();
	}

	//	private void generateRepairTreeEqualsIgnoreCaseChildbranch(EvaluationNode child, RepairNode repairChildNode, String compareValue, Operator op) {
	//		if (child.expression instanceof PropertyCallExpression
	//				&& child.children[0].resultValue instanceof Instance
	//				&& ((PropertyCallExpression)child.expression).property != null) {
	//			String prop = ((PropertyCallExpression)child.expression).property;
	//			Instance inst = (Instance) child.children[0].resultValue;
	//
	//			if(op.equals(Operator.MOD_EQ))
	//				new ConsistencyRepairAction(repairChildNode, prop, inst ,new RepairSingleValueOption(Operator.MOD_EQ,compareValue));
	//			else if(op.equals(Operator.MOD_NEQ))
	//				new ConsistencyRepairAction(repairChildNode, prop, inst,new RepairSingleValueOption(Operator.MOD_NEQ,child.resultValue));
	//
	//			// now if there is a navigation path to the property, we can repair on that path as well:
	//			if (child.children[0].children.length > 0) {
	//				generateModAnyRepairTreeOnPropertyTraversalPath(child.children[0], repairChildNode);
	//			}
	//		}// else we cant repair anything?!
	//	}

	private void generateRepairTreeStringStartsWith(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode repairChildNode = new AlternativeRepairNode(parent);
		String property1 = null;
		String lhsValue = (String)evaluationNode.children[0].resultValue;
		String rhsValue = (String)evaluationNode.children[1].resultValue;

		Operator op;
		if(expectedValue.getExpectedEvaluationResult())
			op = Operator.MOD_EQ;
		else
			op = Operator.MOD_NEQ;
		evaluationNode.children[0].generateRepairTree(repairChildNode,  new RepairSingleValueOption(op, evaluationNode.children[1].resultValue));
		evaluationNode.children[1].generateRepairTree(repairChildNode,  new RepairSingleValueOption(op, evaluationNode.children[0].resultValue));

	}
	//
	//	private void generateModAnyRepairTreeOnPropertyTraversalPath(EvaluationNode child, RepairNode repairChildNode) {
	//		if (child.expression instanceof PropertyCallExpression
	//				&& child.children[0].resultValue instanceof Instance
	//				&& ((PropertyCallExpression)child.expression).property != null) {
	//			String prop = ((PropertyCallExpression)child.expression).property;
	//			Instance inst = (Instance) child.children[0].resultValue;
	//			new ConsistencyRepairAction(repairChildNode, prop, inst,new RepairSingleValueOption(Operator.MOD_EQ, null).setRestriction(new RepairRestriction(child.expression, child, child.expression.parent))); // we dont want ANY but any fulfilling a particular condition
	//
	//			// recursive walk the traversal path
	//			if (child.children[0].children.length > 0) {
	//				generateModAnyRepairTreeOnPropertyTraversalPath(child.children[0], repairChildNode);
	//			}
	//		}
	//	}

	public void generateRepairTreeStringConcat(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		String property1 = null;
		String property2 = null;
		if(this.source instanceof PropertyCallExpression )
			property1 = ((PropertyCallExpression) this.source).property;
		if(this.args.get(0) instanceof PropertyCallExpression )
			property2 = ((PropertyCallExpression) this.args.get(0)).property;
		RepairNode childNode = new AlternativeRepairNode(parent);
		// add repairs for the first expression. If expects true modify it to ?, if expects false modify to something different then current value
		if(property1 != null && evaluationNode.children[0].children[0].resultValue instanceof  Instance)
			if(expectedValue.operator == Operator.MOD_EQ)
				new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			else
				new ConsistencyRepairAction(childNode, property1, (Instance) evaluationNode.children[0].children[0].resultValue, new RepairSingleValueOption(expectedValue.operator, evaluationNode.children[0].resultValue), evaluationNode);

		// add repairs for the second expression. If expects true modify it to ?, if expects false modify to something different then current value
		if(property2 != null && evaluationNode.children[1].children[0].resultValue instanceof  Instance)
			if(expectedValue.operator == Operator.MOD_EQ)
				new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue,new RepairSingleValueOption(Operator.MOD_EQ, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
			else
				new ConsistencyRepairAction(childNode, property2, (Instance) evaluationNode.children[1].children[0].resultValue, new RepairSingleValueOption(expectedValue.operator, evaluationNode.children[1].resultValue), evaluationNode);


	}

	public void generateRepairTreeStringSize(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		RepairNode node = new SequenceRepairNode(parent);
		String repairValue = (String) evaluationNode.children[0].resultValue;
		if(expectedValue.operator == Operator.MOD_EQ){
			if((Long)evaluationNode.resultValue < (Long)expectedValue.getValue()){
				for (long i = (Long) evaluationNode.resultValue; i < (Long)expectedValue.getValue(); i++) {
					repairValue = repairValue.concat("X");
				}
			}
			else{
				repairValue = repairValue.substring(0,((Long)expectedValue.getValue()).intValue());
			}

		}else if(expectedValue.operator == Operator.MOD_NEQ){
			repairValue = repairValue.substring(0,((Long)expectedValue.getValue()).intValue()-1);

		}else if(expectedValue.operator == Operator.MOD_LT){
			repairValue = repairValue.substring(0,((Long)expectedValue.getValue()).intValue());
		}else if(expectedValue.operator == Operator.MOD_GT){
			for (long i = (Long) evaluationNode.resultValue; i <= (Long)expectedValue.getValue(); i++) {
				repairValue = repairValue.concat("X");
			}

		}
		evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.MOD_EQ,repairValue));
	}
	public void generateRepairTreeNaiveAny(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		RepairNode altNode = new AlternativeRepairNode(parent.getRoot());
		for (int i = 0; i < evaluationNode.children.length; i++) {
			evaluationNode.children[i].generateRepairTree(altNode, expectedValue);
		}
		//		Map<Instance, String> coll = findFirstCollectionSource(evaluationNode.children[0]);
		//		if (!coll.isEmpty()) {
		//			coll.entrySet().forEach(entry -> {
		//				new ConsistencyRepairAction(altNode, entry.getValue(), entry.getKey(), RepairSingleValueOption.ADD_ANY);
		//			});
		//		}
	}

	public void generateRepairTreeCollectionSize(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		// in any case, we calc how many items to remove or add

		long toAdd = 0;
		long toRemove = 0;
		if(expectedValue.operator == Operator.MOD_EQ){
			if((Long)evaluationNode.resultValue < (Long)expectedValue.getValue()){
				toAdd = (Long)expectedValue.getValue() - (Long) evaluationNode.resultValue;				
			}
			else{
				toRemove = (Long) evaluationNode.resultValue - (Long)expectedValue.getValue();				
			}
		}else if(expectedValue.operator == Operator.MOD_NEQ){
			toAdd = 1;
			toRemove = 1;
		}else if(expectedValue.operator == Operator.MOD_LT){
			toRemove = (Long) evaluationNode.resultValue - (Long)expectedValue.getValue() + 1;
		}else if(expectedValue.operator == Operator.MOD_GT){
			toAdd = (Long)expectedValue.getValue() - (Long) evaluationNode.resultValue + 1;
		}
		for (int i = 0; i < toAdd; i++)
			evaluationNode.incrementRepairGap();
		for (int i = 0; i < toRemove; i++)
			evaluationNode.decrementRepairGap();
		// if equal, then we treat the resulting 0 as indicated to change the count in any direction

		String property = null;
		IteratorExpression iterExpression = null;
		PropertyCallExpression propExpression=null;
		if(this.getRootProperty() !=null )
			property = this.getRootProperty();

		if(property==null){
			// for special cases (e.g. Set{10,20,30,40} -> select(j : <Integer> | self.age > j)->size() < 1)
			for (int i = 0; i < toAdd; i++) {
				evaluationNode.children[0].generateRepairTree(new AlternativeRepairNode(parent),RepairSingleValueOption.TRUE);// generate repairs for evaluationNodes
			}
			for (int i = 0; i < toRemove; i++) {
				evaluationNode.children[0].generateRepairTree(new AlternativeRepairNode(parent),RepairSingleValueOption.TRUE);// generate repairs for evaluationNodes
			}
			return;
		}				

		if(evaluationNode.children[0].expression instanceof IteratorExpression){ // in case the expression is a `selectExpression`
			iterExpression = (IteratorExpression) evaluationNode.children[0].expression;
			generateRepairIterationSize(parent, expectedValue, evaluationNode, iterExpression, property, toRemove, toAdd);
		} else if (this.source instanceof OperationCallExpression) {
			if (((OperationCallExpression) this.source).operation.equalsIgnoreCase("asset")) {
				if(evaluationNode.children[0].children[0].expression instanceof IteratorExpression){ // in case the expression is a `selectExpression` or others
					iterExpression = (IteratorExpression) evaluationNode.children[0].children[0].expression;	
					evaluationNode = evaluationNode.children[0];
					generateRepairIterationSize(parent, expectedValue, evaluationNode, iterExpression, property, toRemove, toAdd);					
				} else if(evaluationNode.children[0].children[0].expression instanceof PropertyCallExpression) {
					// updated to handle the asset when it's applied on a propertycallExpression.
					generateRepairIterationSize(parent, expectedValue, evaluationNode, iterExpression, property, toRemove, toAdd);
				}
				else
				{// just skip the as set

				}
			} else if (((OperationCallExpression) this.source).operation.equalsIgnoreCase("symmetricdifference")) {
				// then the repair is either to remove or add an item from either side (limited to those in the result) as many times as diff of expected and evaluation node
				OperationCallExpression ope = (OperationCallExpression) this.source;
				EvaluationNode symDiffEN = evaluationNode.children[0];
				// in the result value of symDiffEN we have the symdiff elements
				// both children need to have resultTypes of type SET
				//int symDiffCount = ((Collection)symDiffEN.resultValue).size();
				RepairNode node = new SequenceRepairNode(parent); // all the following are needed, as we might have to add/remove multiple ones
				if (toAdd > 0 && toRemove > 0) { // we have two options, thus another level of alternative
					RepairNode parentNode = node;
					node = new AlternativeRepairNode(parentNode);
				}
				Set symDiff =  (Set) symDiffEN.resultValue;
				Set collA = symDiffEN.children[0].resultValue != null ? Set.copyOf((Collection) symDiffEN.children[0].resultValue) : Collections.emptySet();
				Set collB = symDiffEN.children[1].resultValue != null ? Set.copyOf((Collection)symDiffEN.children[1].resultValue) : Collections.emptySet();
				// for now just take first, later make this alternatives
				Optional<Map.Entry<Instance, String>> instA = findFirstCollectionSource(symDiffEN.children[0]).entrySet().stream().findAny(); //source(s) where elements of collA come from, we also need property FIXME: for now just one supported
				Optional<Map.Entry<Instance, String>> instB = findFirstCollectionSource(symDiffEN.children[1]).entrySet().stream().findAny(); //source(s) where elements from collB come from FIXME: for now just one supported

				for (int i = 0; i < toAdd; i++) {
					// increase the symmetric diff, i.e., add something to either side, 
					// 								     or remove anything from either side, except for what is already in the symdiff
					RepairNode altNode = new AlternativeRepairNode(node);
					if (instA.isPresent()) {
						new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode); // not quite accurate, as not ANY would result in successful repair
						if (collA.size() > 0)
							new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
					}
					if (instB.isPresent()) {
						new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
						if (collB.size() > 0)
							new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode);
					}
				}
				// as we are producing concrete repairs, we need to make sure that these are consistent (e.g., we cant suggest to remove the same element multiple times in an Sequential Node)
				Set usedElements = new HashSet<>();
				// Note that this may result in some viable repairs not being produced (e.g., if only one has to be removed but 3 are available, we dont suggest the others)
				for (int i = 0; i < toRemove; i++) {
					// reduce the symmetric diff: i.e., add something from the symdiff to side A from side B that is not existing in A yet and vice versa
					// 									or remove something from  either side that is in the symdiff
					RepairNode altNode = new AlternativeRepairNode(node);
					// both children need to have resultTypes of type SET
					// is there something to add to A from B that is not existing there yet?
					Optional elOpt = symDiff.stream()
							.filter(el -> !collA.contains(el))
							.filter(el -> !usedElements.contains(el))
							.findAny();
					final EvaluationNode evalRef = evaluationNode;
					elOpt.ifPresentOrElse(el -> { // suggest to add to A, or remove from B
						if (instA.isPresent())
							new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.ADD, el), evalRef);
						if (instB.isPresent())
							new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.REMOVE, el), evalRef);
						usedElements.add(el);
					}, () -> // if there are no more items to add to A or remove from B, then try inverse direction
					symDiff.stream()
					.filter(el -> !collB.contains(el))
					.filter(el -> !usedElements.contains(el))
					.findAny()
					.ifPresent(el -> { // suggest to add to B or remove from A
						if (instA.isPresent())
							new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.REMOVE, el), evalRef);
						if (instB.isPresent())
							new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.ADD, el), evalRef);
						usedElements.add(el);
					}));
					//TODO: actually, we can also fix symmetric difference by moving up to any select etc. but this would require knowing which ones up the tree need changing
				}
			} else if (((OperationCallExpression) this.source).operation.equalsIgnoreCase("union")) { 
				RepairNode node = new SequenceRepairNode(parent); // all the following are needed, as we might have to add/remove multiple ones
				if (toAdd > 0 && toRemove > 0) { // we have two options, thus another level of alternative
					RepairNode parentNode = node;
					node = new AlternativeRepairNode(parentNode);
				}
				// now we add the alternatives: for each required adding/removing of an entry create an OR node
				for (int i = 0; i < toAdd; i++) {
					// for too few elements, either repair one existing element or add a new one
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN,
							true));// generate repairs for evaluationNodes
				}
				for (int i = 0; i < toRemove; i++) {
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN,
							false));// generate repairs for evaluationNodes
				}
				//
				//				// add or remove from either part of the union
				//				for (int i = 0; i < toAdd; i++) {
				//					// for too few elements, either repair one existing element or add a new one
				//					RepairNode altNode = new AlternativeRepairNode(node);
				//					evaluationNode.children[0].generateRepairTree(altNode,RepairSingleValueOption.TRUE);// generate repairs for evaluationNodes
				//				}
				//				for (int i = 0; i < toRemove; i++) {
				//					RepairNode altNode = new AlternativeRepairNode(node);
				//					evaluationNode.children[0].generateRepairTree(altNode,RepairSingleValueOption.FALSE);// generate repairs for evaluationNodes
				//				}
				return;
			} 
			else if(((OperationCallExpression) this.source).operation.equalsIgnoreCase("intersection"))
			{
				OperationCallExpression ope = (OperationCallExpression) this.source;
				EvaluationNode intersectionEN = evaluationNode.children[0];
				RepairNode node = new AlternativeRepairNode(parent); // all the following are needed, as we might have to add/remove multiple ones
				if (toAdd > 0 && toRemove > 0) { // we have two options, thus another level of alternative
					RepairNode parentNode = node;
					node = new AlternativeRepairNode(parentNode);
				}
				Set symDiff =  (Set) intersectionEN.resultValue;
				Set collA = intersectionEN.children[0].resultValue != null ? Set.copyOf((Collection) intersectionEN.children[0].resultValue) : Collections.emptySet();
				Set collB = intersectionEN.children[1].resultValue != null ? Set.copyOf((Collection)intersectionEN.children[1].resultValue) : Collections.emptySet();
				// for now just take first, later make this alternatives
				Optional<Map.Entry<Instance, String>> instA = findFirstCollectionSource(intersectionEN.children[0]).entrySet().stream().findAny(); //source(s) where elements of collA come from, we also need property FIXME: for now just one supported
				Optional<Map.Entry<Instance, String>> instB = findFirstCollectionSource(intersectionEN.children[1]).entrySet().stream().findAny(); //source(s) where elements from collB come from FIXME: for now just one supported
				// Repairs With Restrictions
				for (int i = 0; i < toAdd; i++) {
					// for too few elements, either repair one existing element or add a new one
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN,
							true));// generate repairs for evaluationNodes
				}
				for (int i = 0; i < toRemove; i++) {
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN,
							false));// generate repairs for evaluationNodes
				}
				/*if(collA.isEmpty() && collB.isEmpty())
				{
				//generate repairs for evaluationNodes
				new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.ADD,UnknownRepairValue.UNKNOWN ).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode.children[0].children[0]); // not quite accurate, as not ANY would result in successful repair
					new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.ADD,UnknownRepairValue.UNKNOWN).setRestriction(new RepairRestriction(this, evaluationNode, this)), evaluationNode.children[0].children[1]); // not quite accurate, as not ANY would result in successful repair
					if (evaluationNode.children[0].isMarkedAsOnRepairPath())
						evaluationNode.incrementRepairGap();
					if (evaluationNode.children[1].isMarkedAsOnRepairPath())
						evaluationNode.decrementRepairGap();
				}*/
				if(!collA.isEmpty())
				{
					RepairNode altNode2 = new SequenceRepairNode(node);
					for (int i = 0; i < toAdd; i++) {
						RepairNode seqNode = new SequenceRepairNode(altNode2);
						if (instB.isPresent()) {
							RepairNode altNode = new AlternativeRepairNode(seqNode);
							for(int j=0;j<collA.toArray().length;j++)
							{
								if(!collB.contains(collA.toArray()[j]))
									new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.ADD,collA.toArray()[j] ), evaluationNode.children[0]); // not quite accurate, as not ANY would result in successful repair
							}
						}
					}
					for (int i = 0; i < toRemove; i++) {
						RepairNode seqNode = new SequenceRepairNode(altNode2);
						if (instB.isPresent()) {
							RepairNode altNode = new AlternativeRepairNode(seqNode);
							for(int j=0;j<collA.toArray().length;j++)
							{
								if(collB.contains(collA.toArray()[j]))
								{
								new ConsistencyRepairAction(altNode, instB.get().getValue(), instB.get().getKey(), new RepairSingleValueOption(Operator.REMOVE,collA.toArray()[j] ), evaluationNode.children[0]); // not quite accurate, as not ANY would result in successful repair
								}
							}
							
						}
					}
				}
				if(!collB.isEmpty())
				{
					RepairNode altNode2 = new SequenceRepairNode(node);
					for (int i = 0; i < toAdd; i++) {
						RepairNode seqNode = new SequenceRepairNode(altNode2);
						if (instA.isPresent()) {
							RepairNode altNode = new AlternativeRepairNode(seqNode);
							for(int j=0;j<collB.toArray().length;j++)
							{
								if(!collA.contains(collB.toArray()[j]))
									new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.ADD,collB.toArray()[j] ), evaluationNode.children[0]); // not quite accurate, as not ANY would result in successful repair
							}
						}
					}
					for (int i = 0; i < toRemove; i++) {
						RepairNode seqNode = new SequenceRepairNode(altNode2);
						if (instA.isPresent()) {
							RepairNode altNode = new AlternativeRepairNode(seqNode);
							for(int j=0;j<collB.toArray().length;j++)
							{
								if(collA.contains(collB.toArray()[j]))
								{
									new ConsistencyRepairAction(altNode, instA.get().getValue(), instA.get().getKey(), new RepairSingleValueOption(Operator.REMOVE,collB.toArray()[j] ), evaluationNode.children[0]); // not quite accurate, as not ANY would result in successful repair
								}
							}
							
						}
					}
				}
			}
			else {
				// not sure which other we can expect here, or would need to consider here
			}
		} else if (this.source instanceof PropertyCallExpression) {
			generateRepairIterationSize(parent, expectedValue, evaluationNode, iterExpression, property, toRemove, toAdd);
		}
	}

	private void generateRepairIterationSize(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode, IteratorExpression iterExpression, String property, long toRemove, long toAdd) {

		if (iterExpression != null && iterExpression.children.size() > 1) {
			// if the child expressions contain a select, then the repair could be not just adding/removing but also changing
			// any of the selected over elements to match the selection criteria
			// this means: analyzing the children elements,
			// if there are more than 1 grandchildren, we would need to check which of those fulfill the selection criteria
			// and only add those that dont support the expected value
			//				if(iterExpression instanceof SelectExpression) {
			//					RepairNode node = new SequenceRepairNode(parent); // all the following are needed, as we might have to add/remove multiple ones
			//					if (toAdd > 0 && toRemove > 0) { // we have two options, thus another level of alternative
			//						RepairNode parentNode = node;
			//						node = new AlternativeRepairNode(parentNode);
			//					}
			//					// now determine select results, how many items iterated over in the select fulfill, resp violate select criteria
			//					Set<Integer> fulfillingExp = new HashSet<>();
			//					Set<Integer> unfulfillingExp = new HashSet<>();
			//
			//					EvaluationNode selectNode = evaluationNode.children[0];
			//					for (int i = 1; i < selectNode.children.length; i++) { // from the second element to the end
			//						if (selectNode.children[i].expression instanceof OperationCallExpression
			//								&& selectNode.children[i].resultValue instanceof Boolean) {
			//							if ((Boolean) selectNode.children[i].resultValue)
			//								fulfillingExp.add(i);
			//							else
			//								unfulfillingExp.add(i);
			//						}
			//					}
			//
			//					//TODO: determine instance!!!
			//					// now we add the alternatives: for each required adding/removing of an entry create an OR node
			//					for (int i = 0; i < toAdd; i++) {
			//						RepairNode altNode = new AlternativeRepairNode(node);
			//						evaluationNode.children[0].generateRepairTree(altNode,RepairSingleValueOption.TRUE);// generate repairs for evaluationNodes
			//						// the OR/alternative node is either adding, or inverting fulfillment of selection criteria
			//						unfulfillingExp.stream()
			//								.map(index -> selectNode.children[index])
			//								.forEach(evalNode -> evalNode.expression.generateRepairTree(altNode, RepairSingleValueOption.TRUE, evalNode));
			//					}
			//					for (int i = 0; i < toRemove; i++) {
			//						RepairNode altNode = new AlternativeRepairNode(node);
			//						evaluationNode.children[0].generateRepairTree(altNode,RepairSingleValueOption.TRUE);// generate repairs for evaluationNodes
			//						// the OR/alternative node is either removing, or inverting fulfillment of selection criteria
			//						fulfillingExp.stream()
			//								.map(index -> selectNode.children[index])
			//								.forEach(evalNode -> evalNode.expression.generateRepairTree(altNode, RepairSingleValueOption.FALSE, evalNode));
			//					}
			//				}
			if(iterExpression instanceof SelectExpression || iterExpression instanceof CollectExpression || iterExpression instanceof RejectExpression) {
				RepairNode node = new SequenceRepairNode(parent); // all the following are needed, as we might have to add/remove multiple ones
				if (toAdd > 0 && toRemove > 0) { // we have two options, thus another level of alternative
					RepairNode parentNode = node;
					node = new AlternativeRepairNode(parentNode);
				}
				// now we add the alternatives: for each required adding/removing of an entry create an OR node
				for (int i = 0; i < toAdd; i++) {
					// for too few elements, either repair one existing element or add a new one
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.ADD, null,
							true));// generate repairs for evaluationNodes
				}
				for (int i = 0; i < toRemove; i++) {
					RepairNode altNode = new AlternativeRepairNode(node);
					evaluationNode.children[0].generateRepairTree(altNode,new RepairSingleValueOption(Operator.REMOVE, null,
							false));// generate repairs for evaluationNodes
				}
			} 
			else {
				//TODO: support other iterator Expressions
			}
		}
		else {
			Instance inst = evaluationNode.getInstanceValue();
			if(inst != null && !inst.hasProperty(property) && evaluationNode.children[0].children[0].resultValue instanceof Instance){
				inst = (Instance) evaluationNode.children[0].children[0].resultValue;
			}
			generateSimpleRepairTreeSize(parent, expectedValue, evaluationNode, property, inst);
		}
	}

	private Map<Instance, String> findFirstCollectionSource(EvaluationNode node) {
		Map<Instance, String> instMap = new HashMap<>();
		if (node.expression instanceof PropertyCallExpression) {
			instMap.put((Instance)node.children[0].resultValue, ((PropertyCallExpression)node.expression).property);
		} else if (node.expression instanceof CollectExpression) {
			if(node.children[0].resultValue==null || node.children.length<2)
			return findFirstCollectionSource(node.children[0]);
			else
				return findFirstCollectionSource(node.children[1]);
		} else if (node.expression instanceof SelectExpression) {
			return findFirstCollectionSource(node.children[0]);
		} else if (node.expression instanceof OperationCallExpression || node.expression instanceof AsTypeExpression) {
			// iterate through all children and collect
			for (EvaluationNode childNode : node.children) {
				//FIXME: filter out non relevant expressions that should not be further navigated!?
				instMap.putAll(findFirstCollectionSource(childNode));
			}
		}
		return instMap;
	}

	protected void generateSimpleRepairTreeSize(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode, String property, Instance inst) {
		RepairNode node = new SequenceRepairNode(parent);
		if(expectedValue.operator == Operator.MOD_EQ){
			if((Long)evaluationNode.resultValue < (Long)expectedValue.getValue()){
				for (long i = (Long) evaluationNode.resultValue; i < (Long)expectedValue.getValue(); i++) {
					evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN));
				}
			}
			else{
				for (long i = (Long)expectedValue.getValue(); i < (Long) evaluationNode.resultValue; i++) {
					evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN));
				}
			}
		}else if(expectedValue.operator == Operator.MOD_NEQ){
			RepairNode childNode = new AlternativeRepairNode(node);
			evaluationNode.children[0].generateRepairTree(childNode,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN));
			evaluationNode.children[0].generateRepairTree(childNode,new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN));
		}else if(expectedValue.operator == Operator.MOD_LT){
			for (long i = (Long) evaluationNode.resultValue; i >= (Long)expectedValue.getValue(); i--) {
				evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.REMOVE, UnknownRepairValue.UNKNOWN));
			}
		}else if(expectedValue.operator == Operator.MOD_GT){
			for (long i = (Long) evaluationNode.resultValue; i <= (Long)expectedValue.getValue(); i++) {
				evaluationNode.children[0].generateRepairTree(node,new RepairSingleValueOption(Operator.ADD, UnknownRepairValue.UNKNOWN));
			}
		}
	}

	@Override
	protected Object explainInternally(EvaluationNode node) {
		int gap = node.getRepairGap();
		switch (operation.toLowerCase()) {
		case "size":				
			if (gap > 0) {
				return "Increase by "+gap;
			} else if (gap < 0)
				return "Reduce by "+Math.abs(gap);
			else
				return "Increase or decrease by 1";	
		case "equalsignorecase": 								
			if (gap > 0) // left hand side is repairable
				return "Left hand side can be repaired";
			else if (gap == 0) // both sides are repairable
				return "Either side can be repaired";
			else { // right hand side is repairable
				int absGap = Math.abs(gap);
				return "Right hand side can be repaired";
			}
		case "isempty":
			if (gap > 0)  
				return String.format("Remove %s elements", gap);
			else 
				return String.format("Add at least one element", Math.abs(gap));
		case "includesall": //fallthrough
		case "excludesall":
			if (gap > 0)
				return String.format("Add %s elements to left collection from right collection or vice versa", gap);
			else if (gap == 0) 
				return "Add one element to the right hand collection";
			else { 
				int absGap = Math.abs(gap);
				return String.format("Remove %s elements from left hand collection, or add one element to the right hand collection", absGap);
			}							
		case "includes":
			if (gap > 0)  
				return String.format("Add the element", gap);
			else 
				return String.format("Remove the element", Math.abs(gap));
		case "union": 								
			if (gap > 0) // left hand side is repairable
				return "Left hand side can be repaired";
			else if (gap == 0) // both sides are repairable
				return "Either side can be repaired";
			else { // right hand side is repairable
				int absGap = Math.abs(gap);
				return "Right hand side can be repaired";
			}	
		}
		return super.explainInternally(node);
	}

	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) {
		switch(operation.toLowerCase())
		{
		case "startswith":
			if (this.source.equals(processedExpr)) 
			{
				RestrictionNode sR=null;
				RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
				for(Expression child:this.children)
				{
					if(child.equals(this.args.get(0)))
					{
						rn.setNextNode(this.args.get(0).generateRestrictions(this));
						return rn;
					}
				}
			} 
			else 
			{
				if(this.children.size()>1)
				{
					RestrictionNode c0 = this.children.get(0).generateRestrictions(this);
					RestrictionNode c1=this.children.get(1).generateRestrictions(this);
					RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
					c0.setNextNode(rn);
					rn.setNextNode(c1);
					return c0;
				}
				else
				{
					RestrictionNode sourceRn = this.children.get(0).generateRestrictions(this);
					RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
					rn.setNextNode(sourceRn);
					return rn;
				}
			}
			return null;
		case "isempty":
			if(this.parent.equals(processedExpr)) //from top down
			{
				RestrictionNode.OperationNode opNode = new OperationNode("isempty");    
				opNode.setNextNode(this.source.generateRestrictions(this));
				return opNode;
			}
			else
			{
				RestrictionNode.OperationNode opNode = new OperationNode("isempty");
				opNode.setNextNode(super.generateRestrictions(processedExpr));
				return opNode;
			}
		case "isdefined":
			if (parent.equals(processedExpr) && children.get(0) instanceof PropertyCallExpression) { // entry from above
				RestrictionNode pn = new RestrictionNode.PropertyNode(((PropertyCallExpression)children.get(0)).property, ((PropertyCallExpression)children.get(0)).resultType);
				pn.setNextNode(new RestrictionNode.OperationNode("isdefined"));
				return pn;
			} else {
				RestrictionNode selfNode = new RestrictionNode.OperationNode("isdefined");
				RestrictionNode parent = super.generateRestrictions(processedExpr); 
				if (parent != null) 
					return new RestrictionNode.AndNode(selfNode, parent);
				else return selfNode;
			}
		case "includes":
			if (this.parent.equals(processedExpr)){ // from top down	
				Expression expA=this.children.get(0);
				Expression expB=this.children.get(1);
				this.analyzeExpressions(expA,expB,this);
				RestrictionNode itemRn = this.args.get(0).generateRestrictions(this);
				RestrictionNode sourceRn = this.source.generateRestrictions(this); 
				RestrictionNode rn = new RestrictionNode.OperationNode("includedin");
				rn.setNextNode(sourceRn);
				if(itemRn!=null)
				{
					itemRn.setNextNode(rn);
					return itemRn;
				}
				else return rn;

			} else if (this.source.equals(processedExpr)) { // from lhs 
				RestrictionNode rn = new RestrictionNode.OperationNode("includes");
				rn.setNextNode(this.args.get(0).generateRestrictions(this));
				return rn;
			} else { // from right hand side
				RestrictionNode rn = new RestrictionNode.OperationNode("includedin");
				rn.setNextNode(this.source.generateRestrictions(this));
				return rn;
			}
		case "contains":
			Expression<?> expA=this != null ? this.children.get(0) : null;
			Expression<?> expB=this != null ? this.children.get(1) : null;
			this.analyzeExpressions(expA, expB, processedExpr);
			if(this.origin.expression.equals(processedExpr)) // Origin is a Operation Expression
			{
				RestrictionNode aNode = this.children.get(0).generateRestrictions(this);			 
				RestrictionNode bNode = this.children.get(1).generateRestrictions(this);
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				if(aNode instanceof ValueNode)
				{
					bNode.setNextNodeFluent(rn.setNextNodeFluent(aNode));
					return bNode;
				}
				else
				{
					aNode.setNextNodeFluent(rn.setNextNodeFluent(bNode));
					return aNode;
				}
			}
			else if (this.source.equals(processedExpr)) {
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				rn.setNextNode(this.args.get(0).generateRestrictions(this));
				return rn;
			} else {
				RestrictionNode sourceRn = expA.generateRestrictions(this);  
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				rn.setNextNode(expB.generateRestrictions(this));
				sourceRn.setNextNode(rn);
				return sourceRn;
			}
		case "size":			
			if (this.source.equals(processedExpr)) {
				if(this.parent instanceof RootExpression || this.parent==null) // incase we have reached the top
				{
					return null;
				}
				else 
				{//this.parent is not null here
					RestrictionNode value=null;
					BinaryExpression parExp=(BinaryExpression)this.parent; 
					RestrictionNode sR=super.generateRestrictions(processedExpr);
					/* Size Immediate Value node will only be generated in the scenario where the child of the size node 
					 * is a property call expression and is not the origin.*/
					if(!((Expression)this.children.get(0)).equals(origin.expression) 
							&& !(this.children.get(0) instanceof IteratorExpression))
					{
						if(sR!=null)
							return sR;
						/*if(parExp.a.equals(this)) // coming from side a
						{
							value=parExp.b.generateRestrictions(evalNode.parentEvalNode.children[1], parExp);
							return value;
						}
						else // coming from side b
						{
							value=parExp.a.generateRestrictions(evalNode.parentEvalNode.children[0], parExp);
							return value;
						}*/

					}
					return null;
				}
			} else if (this.parent.equals(processedExpr)){ // from top down
				RestrictionNode.OperationNode opNode = new OperationNode("size");
				opNode.setNextNode(this.source.generateRestrictions(this));
				return opNode;
			} else { //self
				RestrictionNode parentR =  this.parent.generateRestrictions(this);
				if (parentR != null) {
					RestrictionNode.OperationNode opNode = new OperationNode("size");
					opNode.setNextNode(parentR);
					return opNode;
				} else 
					return null;
			}
		default:				
			RestrictionNode.OperationNode opNode = new OperationNode(operation);
			opNode.setNextNode(super.generateRestrictions(processedExpr));
			return opNode;	
		}
	}

	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {    
		// all these are called on the path to such an expression
		switch (operation.toLowerCase()) {
		case "any":
			if(this.source.equals(processedExpr))
			{
				RestrictionNode.OperationNode opNode = new OperationNode(operation);
				EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
				opNode.setNextNode(this.parent.generateRestrictions(parentEN, this));
				return opNode;
			}
			else
				return this.source.generateRestrictions(evalNode.children[0], this);
		case "aslist":
			if(this.source.equals(processedExpr))
			{
				EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
				return this.parent.generateRestrictions(parentEN, this);
			}
			else
				return this.source.generateRestrictions(evalNode.children[0], this);
		case "first":
			if(this.source.equals(processedExpr))
			{
				EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
				return this.parent.generateRestrictions(parentEN, this);
			}
			else
				return this.source.generateRestrictions(evalNode.children[0], this);
		case "asset":
			if (!this.source.equals(processedExpr)) {
				return this.source.generateRestrictions(evalNode.children[0], this);
			} else
				return null;
		case "equalsignorecase":    	
			if (this.source.equals(processedExpr)) {
				RestrictionNode sR=null;
				RestrictionNode rn = new RestrictionNode.OperationNode("equalsignorecase");
				if(!(this.source.getrestGenerated()==1))
				{
					System.out.println("Operation Call Expression L:1464 Study in Detail");
					evalNode.setisVariable(true);
					sR=this.source.generateRestrictions(evalNode, this);
					RestrictionNode temp=sR;
					rn.setNextNode(this.args.get(0).generateRestrictions(evalNode, this));
					for(int i=0;i<sR.getNumberOfRestrictions();i++)
						sR=sR.getNextNode();
					sR.setNextNode(rn);
					return temp;
				}
				else
				{
					for(EvaluationNode child:evalNode.children)
					{
						if(child.expression.equals(this.args.get(0)))
						{
							rn.setNextNode(this.args.get(0).generateRestrictions(child, this));
							return rn;
						}
					}
				}

			}
			else {
				//TODO: See in which scenario it will execute
				EvaluationNode evalA=evalNode.children[0];
				EvaluationNode evalB=evalNode.children[1];
				this.setVariable(evalA, evalB);
				RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);    			
				RestrictionNode rn = new RestrictionNode.OperationNode("equalsignorecase");
				sourceRn.setNextNode(rn);
				rn.setNextNode(this.args.get(0).generateRestrictions(evalNode != null ? evalNode.children[1] : null, this));
				return sourceRn;
			}
		case "isdefined":
			if (parent.equals(processedExpr) && children.get(0) instanceof PropertyCallExpression) { // entry from above
				RestrictionNode pn = new RestrictionNode.PropertyNode(((PropertyCallExpression)children.get(0)).property, ((PropertyCallExpression)children.get(0)).resultType);
				pn.setNextNode(new RestrictionNode.OperationNode("isdefined"));
				return pn;
			} else {// we dont know that the property checked for isdefined is later reused, thus we need to add it to the restriction
				RestrictionNode selfNode = new RestrictionNode.OperationNode("isdefined");
				RestrictionNode parent = super.generateRestrictions(evalNode, processedExpr); 
				if (parent != null) 
					return new RestrictionNode.AndNode(selfNode, parent);
				else return selfNode;
			}
		case "includes":
			// this can contain a restriction on what to include, or where this should be included in
			// for implicity we go with the single to be included item first, then the collection in which it should be included
			if (this.parent.equals(processedExpr)){ // from top down
				this.setVariable(evalNode != null ? evalNode.children[0] : null, evalNode != null ? evalNode.children[1] : null);				
				RestrictionNode itemRn = this.args.get(0).generateRestrictions(evalNode != null ? evalNode.children[1] : null, this);
				RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this); 
				RestrictionNode rn = new RestrictionNode.OperationNode("includedin");
				rn.setNextNode(sourceRn);
				if(itemRn!=null)
				{
					itemRn.setNextNode(rn);
					return itemRn;
				}
				else return rn;

			} else if (this.source.equals(processedExpr)) { // from lhs 
				RestrictionNode rn = new RestrictionNode.OperationNode("includes");
				rn.setNextNode(this.args.get(0).generateRestrictions(evalNode, this));
				return rn;
			} else { // from right hand side
				RestrictionNode rn = new RestrictionNode.OperationNode("includedin");
				rn.setNextNode(this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this));
				return rn;
			}
		case "isempty":
			if(this.parent.equals(processedExpr)) //from top down
			{
				RestrictionNode.OperationNode opNode = new OperationNode("isempty");    
				this.source.setOrigin(this.getOrigin());
				opNode.setNextNode(this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this));
				return opNode;
			}
			else  // neither
			{
				RestrictionNode.OperationNode opNode = new OperationNode("isempty");
				super.setOrigin(this.getOrigin());
				opNode.setNextNode(super.generateRestrictions(evalNode, processedExpr));
				return opNode;
			}
		case "startswith":
			if(this.origin.expression.equals(processedExpr)) // Origin is a Operation Expression
			{
				EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
				EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
				this.setVariable(evalA, evalB);
				RestrictionNode aNode = this.children.get(0).generateRestrictions(evalA, this);			 
				RestrictionNode bNode = this.children.get(1).generateRestrictions(evalB, this);
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				if(aNode instanceof ValueNode)
				{
					rn.setNextNode(aNode);
					return rn;
				}
				else
				{
					rn.setNextNode(bNode);
					return rn;
				}
			}
			else
				if (this.source.equals(processedExpr)) {
					RestrictionNode sR=null;
					RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
					if(!(this.source.getrestGenerated()==1))
					{
						System.out.println("Operation Call Expression L:1464 Study in Detail");
						evalNode.setisVariable(true);
						sR=this.source.generateRestrictions(evalNode, this);
						RestrictionNode temp=sR;
						rn.setNextNode(this.args.get(0).generateRestrictions(evalNode, this));
						for(int i=0;i<sR.getNumberOfRestrictions();i++)
							sR=sR.getNextNode();
						sR.setNextNode(rn);
						return temp;
					}
					else
					{
						for(EvaluationNode child:evalNode.children)
						{
							if(child.expression.equals(this.args.get(0)))
							{
								rn.setNextNode(this.args.get(0).generateRestrictions(child, this));
								return rn;
							}
						}
					}

				} else {
					if(this.children.size()>1)
					{
						RestrictionNode c0 = evalNode.children[0].expression.generateRestrictions(evalNode.children[0],this);
						RestrictionNode c1=evalNode.children[1].expression.generateRestrictions(evalNode.children[1],this);
						if(c1==null)
						{
							System.out.println("Stop here");
						}
						RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
						c0.setNextNode(rn);
						rn.setNextNode(c1);
						return c0;
					}
					else
					{
						RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);  
						RestrictionNode rn = new RestrictionNode.OperationNode("startswith");
						rn.setNextNode(sourceRn);
						return rn;
					}
				}
		case "contains":
			if(this.origin.expression.equals(processedExpr)) // Origin is a Operation Expression
			{
				EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
				EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
				this.setVariable(evalA, evalB);
				RestrictionNode aNode = this.children.get(0).generateRestrictions(evalA, this);			 
				RestrictionNode bNode = this.children.get(1).generateRestrictions(evalB, this);
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				if(aNode instanceof ValueNode)
				{
					bNode.setNextNodeFluent(rn.setNextNodeFluent(aNode));
					return bNode;
				}
				else
				{
					aNode.setNextNodeFluent(rn.setNextNodeFluent(bNode));
					return aNode;
				}
			}
			else if (this.source.equals(processedExpr)) {
				RestrictionNode rn = new RestrictionNode.OperationNode("contains");
				rn.setNextNode(this.args.get(0).generateRestrictions(evalNode, this));
				return rn;
			} else {
				if(this.children.size()>1)
				{
					EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
					EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
					setVariable(evalA, evalB);
					RestrictionNode aNode = evalA.expression.generateRestrictions(evalA, this);			 
					RestrictionNode bNode = evalB.expression.generateRestrictions(evalB, this);
					RestrictionNode rn = new RestrictionNode.OperationNode("contains");
					if(aNode instanceof ValueNode)
					{
						bNode.setNextNode(rn);
						rn.setNextNode(aNode);
						return bNode;
					}
					else
					{
						aNode.setNextNode(rn);
						rn.setNextNode(bNode);
						return aNode;
					}
				}
				else
				{
					RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);  
					RestrictionNode rn = new RestrictionNode.OperationNode("contains");
					rn.setNextNode(this.args.get(0).generateRestrictions(evalNode, this));
					sourceRn.setNextNode(rn);
					return sourceRn;
				}
			}
		case "substring":
			String from = this.args.get(0).getOriginalARL(0, false);
			String to = this.args.get(1).getOriginalARL(0, false);			
			if (!this.source.equals(processedExpr)) {    			    		
				RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);  
				RestrictionNode rn2 = new RestrictionNode.OperationNode("substring");
				rn2.setNextNode(new RestrictionNode.ValueNode(from+","+to));
				sourceRn.setNextNode(rn2);
				return sourceRn;
			} else {
				RestrictionNode rn2 = new RestrictionNode.OperationNode("substring");
				rn2.setNextNode(new RestrictionNode.ValueNode(from+","+to));
				return rn2;
			}
		case "indexof":			
			String substr = this.args.get(0).getOriginalARL(0, false);			
			if (!this.source.equals(processedExpr)) {    			    		
				RestrictionNode sourceRn = this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);  
				RestrictionNode rn2 = new RestrictionNode.OperationNode("indexOf");
				rn2.setNextNode(new RestrictionNode.ValueNode(substr));
				sourceRn.setNextNode(rn2);
				return sourceRn;
			} else {
				RestrictionNode rn2 = new RestrictionNode.OperationNode("indexOf");
				rn2.setNextNode(new RestrictionNode.ValueNode(substr));
				return rn2;
			}
		case "size":			
			if (this.source.equals(processedExpr)) {
				if(this.parent instanceof RootExpression || this.parent==null) // incase we have reached the top
				{
					return null;
				}
				else 
				{//this.parent is not null here
					RestrictionNode value=null;
					BinaryExpression parExp=(BinaryExpression)this.parent; 
					EvaluationNode parEval=getParentIfExpressionMatches(evalNode);
					//RestrictionNode sR=super.generateRestrictions(evalNode, processedExpr);
					//RestrictionNode sR=super.generateRestrictions(evalNode,this);
					RestrictionNode sR=parent.generateRestrictions(parEval, this);
					/* Size Immediate Value node will only be generated in the scenario where the child of the size node 
					 * is a property call expression and is not the origin.*/
					if(!((Expression)this.children.get(0)).equals(origin.expression))
						return sR;

					/*if(!((Expression)this.children.get(0)).equals(origin.expression) 
							&& !(this.children.get(0) instanceof IteratorExpression))
					{
						if(sR!=null)
							return sR;*/
					/*if(parExp.a.equals(this)) // coming from side a
						{
							value=parExp.b.generateRestrictions(evalNode.parentEvalNode.children[1], parExp);
							return value;
						}
						else // coming from side b
						{
							value=parExp.a.generateRestrictions(evalNode.parentEvalNode.children[0], parExp);
							return value;
						}*/

					//}
					/*
					 * Executes for: T53,T54,T57,T58,T91,T92,T110,T111,T32,T107
					 * Return for: T53,T54,T57,T58,T110,T111,T107
					 * we are taking the grand parent because 
					 * 1) the immediate parent of size would be comparator operator
					 * 2) the Gparent will have the info of the other branch and if it contains the element in repair or not.
					 * */
					// possibility of another branch with the element in context
					if(this.parent.parent!=null && this.parent.parent instanceof BinaryExpression) 
					{
						BinaryExpression exp=(BinaryExpression) this.parent.parent;
						if(exp.a.equals(parExp)) // coming from a side
						{// check if the other branch contains the element in context
							if(exp.b.toString().contains(this.origin.expression.toString()))
							{
								return exp.b.generateRestrictions(evalNode.parentEvalNode.parentEvalNode.children[1], exp);
							}
						}
						else // coming from b side 
						{// check if the other branch contains the element in context
							if(exp.a.toString().contains(this.origin.expression.toString()))
							{
								return exp.a.generateRestrictions(evalNode.parentEvalNode.parentEvalNode.children[0], exp);
							}
						}
					}
					return null;
				}
			} else if (this.parent.equals(processedExpr)){ // from top down
				RestrictionNode.OperationNode opNode = new OperationNode("size");
				opNode.setNextNode(this.source.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this));
				return opNode;
			} else { //self
				RestrictionNode parentR =  this.parent.generateRestrictions(evalNode != null ? evalNode.parentEvalNode : null, this);
				if (parentR != null) {
					RestrictionNode.OperationNode opNode = new OperationNode("size");
					opNode.setNextNode(parentR);
					return opNode;
				} else 
					return null;
			}
		case "union":
			if(this.parent.equals(processedExpr)) // coming from up i.e. parent
			{
				EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
				EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
				this.setVariable(evalA, evalB);
				List<RestrictionNode> restList=new ArrayList<>();
				restList.add(evalA.expression.generateRestrictions(evalA, this));
				restList.add(evalB.expression.generateRestrictions(evalB, this));
				RestrictionNode.OperationNode opN=new OperationNode(operation,restList);
				/*RestrictionNode sR=super.generateRestrictions(evalNode, processedExpr);
					if(sR!=null && evalNode.parentEvalNode.expression instanceof OperationCallExpression 
							&& ((OperationCallExpression)evalNode.parentEvalNode.expression).operation.equals("size"))
					{
						RestrictionNode rest=new RestrictionNode.OperationNode("size");
						rest.setNextNodeFluent(opN.setNextNodeFluent(sR));
						return rest;
					}*/
				return opN;
			}
			// Coming from child
			else 
			{	
				RestrictionNode sR=super.generateRestrictions(evalNode, processedExpr);
				return sR;
			}
		case "intersection":
			List<RestrictionNode> restList=new ArrayList<>();
			if(this.parent.equals(processedExpr)) // coming from the parent
			{
				EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
				EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
				/*if(evalA.expression instanceof PropertyCallExpression 
						&& evalB.expression instanceof PropertyCallExpression)
				{
					evalA.setisVariable(true);
					RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
					RestrictionNode valA= evalA.getBaseItemValue();
					valA.setNextNodeFluent(restA);
					evalB.setisVariable(true);
					RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
					RestrictionNode valB= evalB.getBaseItemValue();
					valB.setNextNodeFluent(restB);
					restList.add(valA);
					restList.add(valB);
					return new OperationNode(operation,restList);
				}
				else
				{
					//TODO
					return null;
				}*/
				RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
				RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
				restList.add(restA);
				restList.add(restB);
				return new OperationNode(operation,restList);
			}
			else  // coming from one of the child nodes
			{
				EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
				EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
				if(processedExpr.toString().equals(evalA.expression.toString())) // coming from childA
				{
					if(evalB.expression instanceof PropertyCallExpression)
					{
						//evalB.setisVariable(true);
						//RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
						RestrictionNode val= evalB.getBaseItemValue();
						//val.setNextNodeFluent(restB);
						RestrictionNode restA=evalA.expression.generateRestrictions(evalA,this);
						restList.add(restA);
						restList.add(val);
						return new OperationNode(operation,restList);
					}
					else
					{
						RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
						if(evalB.resultValue instanceof Collection)
						{
							RestrictionNode rest=evalB.getBaseItemValue();
							//RestrictionNode rest=evalB.generateValueBasedRestriction();
							if(rest==null)
							{
								if(processedExpr instanceof PropertyCallExpression ||
										(evalB.expression instanceof PropertyCallExpression && 
												evalA.expression instanceof PropertyCallExpression)
										|| evalB.expression.inconsistency_origin!=null || 
										(processedExpr instanceof CollectExpression && evalB.expression instanceof SelectExpression))
								{
									return restB;
								}
								else
									return null;
							}
							else
							{
								if(processedExpr instanceof PropertyCallExpression ||
										(evalB.expression instanceof PropertyCallExpression && 
												evalA.expression instanceof PropertyCallExpression)
										|| evalB.expression.inconsistency_origin!=null || 
										(processedExpr instanceof CollectExpression && evalB.expression instanceof SelectExpression))
								{
									restList.add(restB);
									restList.add(rest);
									return new OperationNode(operation,restList);
									//return new RestrictionNode.OrNode(restB, rest);
								}
								else
								{
									RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
									restList.add(restA);
									restList.add(rest);
									return new OperationNode(operation,restList);
									//return new RestrictionNode.OrNode(restA, rest);
								}
							}
						}
					}

					/*RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
					if(restB instanceof ValueNode)
					{
						RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
						restList.add(restA);
						restList.add(restB);
						return new OperationNode(operation,restList);
						//return new RestrictionNode.OrNode(restA, restB);
					}
					else if(evalB.resultValue instanceof Collection)
					{
						//Collection resultColl=(Collection)evalB.resultValue;
						RestrictionNode rest=evalB.generateValueBasedRestriction();
						if(rest==null)
						{
							if(processedExpr instanceof PropertyCallExpression ||
									(evalB.expression instanceof PropertyCallExpression && 
											evalA.expression instanceof PropertyCallExpression)
									|| evalB.expression.inconsistency_origin!=null || 
									(processedExpr instanceof CollectExpression && evalB.expression instanceof SelectExpression))
							{
								return restB;
							}
							else
								return null;
						}
						else
						{
							if(processedExpr instanceof PropertyCallExpression ||
									(evalB.expression instanceof PropertyCallExpression && 
											evalA.expression instanceof PropertyCallExpression)
									|| evalB.expression.inconsistency_origin!=null || 
									(processedExpr instanceof CollectExpression && evalB.expression instanceof SelectExpression))
							{
								restList.add(restB);
								restList.add(rest);
								return new OperationNode(operation,restList);
								//return new RestrictionNode.OrNode(restB, rest);
							}
							else
							{
								RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
								restList.add(restA);
								restList.add(rest);
								return new OperationNode(operation,restList);
								//return new RestrictionNode.OrNode(restA, rest);
							}
						}
					}*/
				}
				else // coming from childB
				{
					if(evalA.expression instanceof PropertyCallExpression)
					{
						//evalA.setisVariable(true);
						//RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
						RestrictionNode val= evalA.getBaseItemValue();
						//val.setNextNodeFluent(restA);
						RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
						restList.add(restB);
						restList.add(val);
						return new OperationNode(operation,restList);
					}
					else
					{
						RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
						if(evalA.resultValue instanceof Collection)
						{
							RestrictionNode rest=evalA.getBaseItemValue();
							if(rest==null)
							{
								if(processedExpr instanceof PropertyCallExpression ||
										(evalB.expression instanceof PropertyCallExpression
												&& evalA.expression instanceof PropertyCallExpression) 
										|| evalA.expression.inconsistency_origin!=null
										|| 
										(processedExpr instanceof CollectExpression && evalA.expression instanceof SelectExpression))
								{
									return restA;
								}
								else
									return null;
							}
							else
							{
								if(processedExpr instanceof PropertyCallExpression ||
										(evalB.expression instanceof PropertyCallExpression
												&& evalA.expression instanceof PropertyCallExpression) 
										|| evalA.expression.inconsistency_origin!=null
										|| 
										(processedExpr instanceof CollectExpression && evalA.expression instanceof SelectExpression))
								{
									restList.add(restA);
									restList.add(rest);
									return new OperationNode(operation,restList);
									//return new RestrictionNode.OrNode(restA, rest);
								}
								else
								{
									RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
									restList.add(restB);
									restList.add(rest);
									return new OperationNode(operation,restList);
									//return new RestrictionNode.OrNode(restB, rest);
								}
							}

						}
					}


					/*RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
					if(restA instanceof ValueNode)
					{
						RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
						restList.add(restB);
						restList.add(restA);
						return new OperationNode(operation,restList);
						//return new RestrictionNode.OrNode(restB, restA);
					}
					else if(evalA.resultValue instanceof Collection)
					{
						Collection resultColl=(Collection)evalA.resultValue;
						RestrictionNode rest=evalA.generateValueBasedRestriction();
						if(rest==null)
						{
							if(processedExpr instanceof PropertyCallExpression ||
									(evalB.expression instanceof PropertyCallExpression
											&& evalA.expression instanceof PropertyCallExpression) 
									|| evalA.expression.inconsistency_origin!=null
									|| 
									(processedExpr instanceof CollectExpression && evalA.expression instanceof SelectExpression))
							{
								return restA;
							}
							else
								return null;
						}
						else
						{
							if(processedExpr instanceof PropertyCallExpression ||
									(evalB.expression instanceof PropertyCallExpression
											&& evalA.expression instanceof PropertyCallExpression) 
									|| evalA.expression.inconsistency_origin!=null
									|| 
									(processedExpr instanceof CollectExpression && evalA.expression instanceof SelectExpression))
							{
								restList.add(restA);
								restList.add(rest);
								return new OperationNode(operation,restList);
								//return new RestrictionNode.OrNode(restA, rest);
							}
							else
							{
								RestrictionNode restB=evalB.expression.generateRestrictions(evalB,this);
								restList.add(restB);
								restList.add(rest);
								return new OperationNode(operation,restList);
								//return new RestrictionNode.OrNode(restB, rest);
							}
						}

					}*/
				}
			}
		default:				
			RestrictionNode.OperationNode opNode = new OperationNode(operation);
			opNode.setNextNode(super.generateRestrictions(evalNode, processedExpr));
			return opNode;			
		}

	}

	private RestrictionNode getBodyRestriction(EvaluationNode evalNode, Expression processedExpr) {
		for (EvaluationNode child : evalNode.children) {
			if (child.expression.equals(processedExpr)) {
				if(!(child.expression.getrestGenerated()==1))
				{
					child.expression.restGeneratedIncrement();
					return child.expression.generateRestrictions(child, this);
				}
			}
		}
		return null;
	}

	public static class OperationDeclaration {

		static List<OperationDeclaration> operationDeclarations = new ArrayList();

		public String name;
		public ArlType returnType;
		public ArlType sourceType;
		public List<ArlType> parameterTypes;

		public OperationDeclaration(ArlType returnType, String name, ArlType sourceType, ArlType... parameterTypes) {
			this.name = name;
			this.returnType = returnType;
			this.sourceType = sourceType;
			this.parameterTypes = Arrays.asList(parameterTypes);
			operationDeclarations.add(this);
		}

		static public OperationDeclaration findOperationDeclaration(String name, ArlType actualSourceType, List<Expression> actualParametersTypes) {
			for (OperationDeclaration declaredOperation : operationDeclarations) {
				if (declaredOperation.name.equals(name)) {
					if (declaredOperation.meetsParameterTypes(actualSourceType, actualParametersTypes))
						return declaredOperation;
				}
			}
			return null;
		}

		public boolean meetsParameterTypes(ArlType actualSourceType, List<Expression> actualParameterTypes) {
			boolean meets = parameterTypes.size() == actualParameterTypes.size();
			if (meets) {
				for (Iterator actualIterator = actualParameterTypes.iterator(), declaredIterator = parameterTypes.iterator(); meets && declaredIterator.hasNext() && actualIterator.hasNext();) {
					final ArlType declaredType = (ArlType) declaredIterator.next();
					final ArlType actualType = ((Expression) actualIterator.next()).resultType;
					meets = actualType.conformsTo(declaredType);
				}
			}
			if (meets) {
				meets = actualSourceType.conformsTo(this.sourceType);
			}
			return meets;
		}

		@Override
		public String toString() {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(this.name.toUpperCase());
			stringBuilder.append("(");
			stringBuilder.append(this.parameterTypes);
			stringBuilder.append(") : ");
			stringBuilder.append(this.returnType);
			return stringBuilder.toString();
		}

	}

	public String getOperation() {
		return operation;
	}
}
