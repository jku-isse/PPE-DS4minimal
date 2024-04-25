package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import at.jku.isse.designspace.rule.arl.expressions.AndExpression;
import at.jku.isse.designspace.rule.arl.expressions.CollectExpression;
import at.jku.isse.designspace.rule.arl.expressions.ExistsExpression;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.expressions.ForAllExpression;
import at.jku.isse.designspace.rule.arl.expressions.LiteralExpression;
import at.jku.isse.designspace.rule.arl.expressions.NotExpression;
import at.jku.isse.designspace.rule.arl.expressions.OperationCallExpression;
import at.jku.isse.designspace.rule.arl.expressions.OrExpression;
import at.jku.isse.designspace.rule.arl.expressions.RejectExpression;
import at.jku.isse.designspace.rule.arl.expressions.SelectExpression;
import at.jku.isse.designspace.rule.arl.expressions.XorExpression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RotationNode {
	@EqualsAndHashCode.Include @ToString.Include private final EvaluationNode node;
	private RotationNode parent;
	private boolean isRotated = false;
	private List<RotationNode> lhs = new LinkedList<>();
	private List<RotationNode> rhs = new LinkedList<>();
	private EvaluationRow row;
	private boolean isLocalTreeConsistent = false;
	private boolean hasBeenCheckedForRepairPath = false;
	
	public static enum TreePrefix {left, right};
	
	public RotationNode(@NonNull EvaluationNode node, @NonNull RotationNode parent) {
		this.node = node;
		this.parent = parent;		
		buildInitialTree();		
	}
	
	private RotationNode() { // used for ROOTNODE only!
		this.node = null;
		this.parent = null;
	}
	
	private void propagateParent(RotationNode parent) {
		this.parent = parent;
		lhs.stream().forEach(node -> node.propagateParent(this));
		rhs.stream().forEach(node -> node.propagateParent(this));
	}
	
	private void checkTreeConsistency() {
		boolean lhsOk = lhs.stream().allMatch(node -> node.getParent().equals(this));
		boolean rhsOk = rhs.stream().allMatch(node -> node.getParent().equals(this));
		isLocalTreeConsistent = lhsOk && rhsOk;
		if (!isLocalTreeConsistent)
			log.warn(String.format("Rotation Node %s has inconsistent local tree structure", this.getNode().toString()));
		else {
			lhs.stream().forEach(node -> node.checkTreeConsistency());
			rhs.stream().forEach(node -> node.checkTreeConsistency());
		}
	}
	
	private void buildInitialTree() {
		if (this.node.children.length > 0) {
			lhs.add(new RotationNode(node.children[0], this));
		}
		if (this.node.children.length > 1) {
			for (int i = 1; i<node.children.length; i++) {
				rhs.add(new RotationNode(node.children[i], this));
			}
		}		
	}
	
	public RotationNode rotateLeftSideClockwise(RotationNode newParent) {
		RotationNode newRootOfThisSubtree = null;
		if (isRotated) 
			newRootOfThisSubtree = this.parent;
		else {												
			if (lhs.isEmpty()) { // leaf node
				;// nothing to rotate, rhs will be empty as well
				newRootOfThisSubtree = this;
				if (!this.parent.equals(ROOTROTATION)) {
					this.rhs = new ArrayList<>(List.of(this.parent));
				}
				this.parent = newParent; 
			} else {				
				if (shouldNodeRotate()) {
		//			log.warn("FormerLeftClockwise: ROTATING - "+this.getNode().toString());
					RotationNode oldParent = this.parent;
					// before rotation, lhs can contain only a single element at most!
					RotationNode oldLeft = this.lhs.remove(0);

					this.parent = oldLeft;
					//this.lhs.addAll(this.rhs);

					newRootOfThisSubtree = oldLeft.rotateLeftSideClockwise(newParent);										
					oldLeft.rhs = new ArrayList<>(List.of(this)); // now set the this to be the lhs of the oldLeft 
					
					for (RotationNode formerRight : rhs) { 
						this.lhs.add(formerRight.rotateFormerRightSideClockwise());
					}
					this.rhs.clear();
					//MOVED TO CALLER OF rotateLeftSideClockwise
//					if (!oldParent.equals(ROOTROTATION)) {
//						this.rhs = new ArrayList<>(List.of(oldParent)); 
//					}
				}  else {
		//			log.warn("FormerLeftCockwise: NotRotating - "+this.getNode().toString());
					newRootOfThisSubtree = this;
					this.parent = newParent;
					// but we continue rotating children
					// left side stays left side but is rotate in clockwise
					RotationNode nonRotatedLeft = this.lhs.remove(0); 					
					this.lhs.add(nonRotatedLeft.rotateLeftSideClockwise(this));					 
					// right side stays right side but is also rotated clockwise
					List<RotationNode> nonRotatedRights = new ArrayList<>(this.rhs); 
					this.rhs.clear();
					for (RotationNode nonRotatedRight : nonRotatedRights) {
						this.rhs.add(nonRotatedRight.rotateLeftSideClockwise(this));
					}
				} 
			}
			isRotated = true;
			if (newParent.equals(RotationNode.ROOTROTATION)) { //the first call to rotation
				// hence new subtreeRoot is the new overall root
			//	newRootOfThisSubtree.propagateParent(RotationNode.ROOTROTATION);
				checkTreeConsistency();
			}
		}
		//checkTreeConsistency();
		return newRootOfThisSubtree;
	}


	public RotationNode rotateFormerRightSideClockwise() {
		RotationNode newSubTreeRoot = null;
		if (isRotated)
			return this;
		if (this.lhs.size() == 0) {// nothing to rotate 
			newSubTreeRoot = this;
		} else
		if (this.shouldNodeRotate()) {
		//	log.warn("FormerRightCockwise: ROTATING - "+this.getNode().toString());
			//log.warn("FormerRightCockwise: ROTATING - "+this.getNode().toString());
			// not sure we actually need to rotate here --> we do
			
			// remove this from current parent
			//this.parent.getLhs().remove(this);
			
			// whatever is lefthand side (after rotating lefthand side) becomes new parent
			// (we havent rotated lefthand side before, so there can only be one element a maximum)
			RotationNode formerLeft = this.getLhs().remove(0);
			RotationNode formerParent = this.parent;
			this.parent = formerLeft;
			newSubTreeRoot = formerLeft.rotateLeftSideClockwise(formerParent);
			formerLeft.rhs = new ArrayList<>(List.of(this));  // make this the rhs of the leftSideRotatedNode
			
			
			//formerParent.getLhs().add(newSubTreeRoot);

			// current right becomes new left			
			//this.getLhs().addAll(this.getRhs());
			for (RotationNode currentRight : this.getRhs()) { 
				this.lhs.add(currentRight.rotateFormerRightSideClockwise());
			}
			this.rhs.clear();
						
		}  else {
	//		log.warn("FormerRightCockwise: NOT Rotating - "+this.getNode().toString());
			// we are an AND, OR, or XOR node, just rotate rhs and lhs children clockwise
			//this.parent = newParent;
			// left side stays left side but is rotated clockwise
			RotationNode nonRotatedLeft = this.lhs.remove(0); 
			RotationNode newLeftRoot = nonRotatedLeft.rotateLeftSideClockwise(this); 
			this.lhs.add(newLeftRoot);
			// regularily rotateLeftSideClockwise will have the former left to include this node on its rhs, which we need to undo now
			nonRotatedLeft.rhs.clear();	//nonRotatedLeft.rhs.remove(this); // nonRotatedLeft is the former Left and now at the bottom of the subtree (and thus should not point up to here again

			// right side stays right side but is also rotated clockwise
			List<RotationNode> nonRotatedRights = new ArrayList<>(this.rhs); 
			this.rhs.clear();
			for (RotationNode nonRotatedRight : nonRotatedRights) {
				RotationNode newRightRoot = nonRotatedRight.rotateLeftSideClockwise(this); 
				this.rhs.add(newRightRoot);
				// as with left node, need to avoid having this node at the bottom being pointed to
				nonRotatedRight.rhs.clear(); //nonRotatedRight.rhs.remove(this);
			} 					
			newSubTreeRoot = this; // as we wont rotate
		} 
		//checkTreeConsistency();
		this.isRotated = true;
		return newSubTreeRoot;
	}
	
	public boolean isNodeOnRepairPath() {
		if (isLocalTreeConsistent && !hasBeenCheckedForRepairPath) { // safety measure
			hasBeenCheckedForRepairPath = true;
			boolean isLhs = this.getLhs().stream().anyMatch(child -> child.isNodeOnRepairPath());
			boolean isRhs = this.getRhs().stream().anyMatch(child -> child.isNodeOnRepairPath());

			if (this.node != null) {

				if (isLhs || isRhs) 
					this.node.markAsOnRepairPath();
				return isLhs || isRhs || this.node.isMarkedAsOnRepairPath();
			} else
				return isLhs || isRhs;
		} else if (hasBeenCheckedForRepairPath) {
			return this.node.isMarkedAsOnRepairPath();
		} else {
			return true; //default to show all elements and signal inconsistent tree
		}
	}
	
	private boolean shouldNodeRotate() {
		// No rotation for AND, OR, XOR, as they are combinators and have no natural order or necessity of reading
		// no rotation for NOT, as it maintains natural reading flow
		if (this.getNode().expression instanceof AndExpression 
				|| this.getNode().expression instanceof OrExpression 
				|| this.getNode().expression instanceof XorExpression
				|| this.getNode().expression instanceof NotExpression
				) {
			return false;
		} else { 
			return true;
		}
	}
	
	public boolean isCombinationNode() {
		Expression expr = this.getNode().expression;
		return expr instanceof AndExpression 
				|| expr instanceof OrExpression 
				|| expr instanceof XorExpression;
	}
	
	public boolean isCollectionOrCombinationNode() {			 
		Expression expr = this.getNode().expression;
		if (isCombinationNode()
			|| expr instanceof ForAllExpression 
			|| expr instanceof ExistsExpression
			|| expr instanceof SelectExpression
			|| expr instanceof RejectExpression
			|| expr instanceof CollectExpression
			|| isCollectionOperation(expr) 
			) {
			return true;
		}
		return false;
	}
	
	private boolean isCollectionOperation(Expression exp) {
		if (exp instanceof OperationCallExpression) {
			OperationCallExpression opExp = (OperationCallExpression)exp;
			String opName = opExp.getOperation(); 
			if (opName.equalsIgnoreCase("union") 
					|| opName.equalsIgnoreCase("intersection") 
					|| opName.equalsIgnoreCase("symmetricdifference")
					|| opName.equalsIgnoreCase("difference")
					|| opName.equalsIgnoreCase("including")
					|| opName.equalsIgnoreCase("excluding")
					) {
				return true;
			} 		
		}
		return false;
	}
	
	public static RotationNode ROOTROTATION = new RotationNode();
	
	public void printTreeLeftToRight(StringBuffer sb, int indentation, TreePrefix treePrefix) {
		if (this == ROOTROTATION)
			return;
		if (row == null)
			row = nodeToRow(this.node, indentation);
		sb.append("\r\n"+row.printEvalRow(indentation, this.parent != null ? this.parent.getRow() : null, treePrefix));
		if (!lhs.isEmpty() && !rhs.isEmpty()) {
			System.out.println("Both sides nonempty");
		}
		
		lhs.stream()
		.filter(nodeL -> !(nodeL.getNode().expression instanceof LiteralExpression))
		.forEach(nodeL -> nodeL.printTreeLeftToRight(sb, indentation+2, TreePrefix.left));
								
		rhs.stream()
		.filter(nodeR -> nodeR.getNode() != null && !(nodeR.getNode().expression instanceof LiteralExpression))
		.forEach(nodeR -> nodeR.printTreeLeftToRight(sb, indentation+2, TreePrefix.right));		
	}
	

	
	private static EvaluationRow nodeToRow(EvaluationNode node, int indentation) {
		return new EvaluationRow(
				getExpr(node, indentation),
				//getInputLeaf(node),
				node.resultValue,
				node
				);
				
				
	}
	
	private static String getExpr(EvaluationNode evalNode, int indentation) {		
		return evalNode.expression.getLocalARL() ; //(indentation, false);
	}
	
//	private static EvalInput getInputLeaf(EvaluationNode evalNode) {
//		if (evalNode.children != null && evalNode.children.length > 0) {
//			EvalInput lowerInput = getInputLeaf(evalNode.children[0]);
//			if (evalNode.expression instanceof PropertyCallExpression) {
//				List<Object> results = evalNode.resultValue instanceof Collection ? new ArrayList<Object>((Collection<Object>)evalNode.resultValue) : List.of(evalNode.resultValue);
//				return new EvalInput(lowerInput.getSubject(), lowerInput.getPath()+"."+((PropertyCallExpression)evalNode.expression).property, results);
//			} else 
//				return lowerInput;
//		} else {
//			String input = evalNode.resultValue != null ? Objects.toString(evalNode.resultValue) : "";
//			return new EvalInput(input, "", null);
//		}
//	}
//	

}
 