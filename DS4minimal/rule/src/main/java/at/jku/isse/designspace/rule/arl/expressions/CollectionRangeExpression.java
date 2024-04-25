/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jku.isse.designspace.rule.arl.expressions;

import java.util.ArrayList;
import java.util.Collection;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.exception.RepairException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;

public class CollectionRangeExpression extends BinaryExpression<Collection<Long>, Long, Long> {

	public CollectionRangeExpression(Expression<Long> a, Expression<Long> b) {
		super(a, b);
		this.resultType = ArlType.INTEGER;
		if (a.resultType!=ArlType.INTEGER && b.resultType!=ArlType.INTEGER) throw new ParsingException("collection range '%s' needs integer values", this);
	}

	@Override
	public Collection<Long> check(Long argA, Long argB) {
		Collection<Long> result = new ArrayList<Long>();
		result.add(argA);
		result.add(argB);
		return result;
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation);
		return whitespace+this.a.getOriginalARL(indentation, false) + " .. " + this.b.getOriginalARL(indentation, false);
	}
	
	@Override
	public String getARL() {
		return "..(" + super.getARL()+")";
	}

	@Override 
	public String getLocalARL() { return "..";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		throw new RepairException("generateRepairTree not implemented");
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
