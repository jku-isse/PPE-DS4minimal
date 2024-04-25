package at.jku.isse.designspace.rule.arl.evaluator;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.RotationNode.TreePrefix;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;
import lombok.Data;

@Data
public class EvaluationRow {

	private final String constraint;
	//private final EvalInput input;
	private final Object result;
	private final EvaluationNode evalNode;

	
	public String printEvalRow(int identation, EvaluationRow parent, TreePrefix treePrefix) {		
		String prefix = treePrefix == TreePrefix.left ? "L " : "R ";
		StringBuffer sb = new StringBuffer(String.format(prefix+"%1$"+identation+"s", ""));
		String parentConstr = parent != null ? parent.getConstraint() : "";
		String shortConstr = constraint;		
		if (constraint.startsWith(parentConstr)) 
			shortConstr = shortConstr.substring(parentConstr.length());
		sb.append(shortConstr);
		sb.append(" | ");
		
		String onPath = evalNode.isMarkedAsOnRepairPath() ? "ONPATH" : "ok";
		sb.append(onPath);
		sb.append(" | ");
		
		sb.append(evalNode.expression.explain(evalNode));
		sb.append(" | ");
		
		sb.append(evalNode.expression.getClass().getSimpleName());
		
		evalNode.getRepairs().forEach(ra -> printRepair(ra, sb, identation + 8));
		
		return sb.toString();
	}
	
	private void printRepair(AbstractRepairAction ra, StringBuffer sb, int intendation) {
		sb.append(String.format("\r\n%1$"+intendation+"s", ""));
		RestrictionNode rootNode =  ra.getValue()==UnknownRepairValue.UNKNOWN && ra.getRepairValueOption().getRestriction() != null ? ra.getRepairValueOption().getRestriction().getRootNode() : null;
		if (rootNode != null) {
			sb.append(EvaluationRow.compileRestrictedRepair(ra,rootNode.printNodeTree(false,40)));
		} else
			sb.append(ra.toString());
	}
	
	public static String compileRestrictedRepair(AbstractRepairAction ra, String restriction) {
		String target = ra.getElement() != null ? ((Instance)ra.getElement()).name() : "";
		StringBuffer list = new StringBuffer();
		switch(ra.getOperator()) {
		case ADD:							 
			list.append(String.format("Add to %s of ", ra.getProperty()));
			list.append(target);
			list.append(restriction);
			break;
		case MOD_EQ:
		case MOD_GT:
		case MOD_LT:
		case MOD_NEQ:				
			list.append(String.format("Set the %s of ", ra.getProperty()));
			list.append(target);			
			list.append(" to");
			list.append(restriction);
			break;
		case REMOVE:					
			list.append(String.format("Remove from %s of ", ra.getProperty()));
			list.append(target);
			list.append(restriction);
			break;
		default:
			break;		
		}
		return list.toString();
	}

//	@Data 
//	public static class EvalInput {
//		private final Object subject;
//		private final String path;
//		private final List<Object> values;
//	
//
//		@Override
//		public String toString() {
//			if (getValues()==null) 
//				return getSubject()+getPath();
//			else if (getValues().size() == 1)
//				return getSubject()+getPath()+" = "+getValues().get(0);
//			else
//				return getSubject()+getPath()+" = [ "+getValues().size()+" elements ]";
//		}
//
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			EvalInput other = (EvalInput) obj;
//			return Objects.equals(path, other.path) && Objects.equals(subject, other.subject);
//		}
//
//
//		@Override
//		public int hashCode() {
//			return Objects.hash(path, subject);
//		}
//
//
//	}

}
