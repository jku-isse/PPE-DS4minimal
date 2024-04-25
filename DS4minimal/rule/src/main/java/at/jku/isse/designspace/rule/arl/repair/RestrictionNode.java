package at.jku.isse.designspace.rule.arl.repair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.parser.ArlType.CollectionKind;
//import io.netty.util.ByteProcessor.IndexNotOfProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RestrictionNode {

	protected RestrictionNode nextNode = null; 
	protected RestrictionNode priorNode = null;

	public abstract boolean matches(RestrictionNode comparison);
	public abstract RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom);
	public abstract String toTreeString(int indentation);
	public abstract int getNumberOfRestrictions();
	public abstract Map<String, Integer> getRestComplexity();

	public RestrictionNode getNextNode() {
		return nextNode;
	}

	public RestrictionNode setNextNodeFluent(RestrictionNode nextNode) {
		this.nextNode = nextNode;
		if (nextNode != null) {
			nextNode.priorNode = this;
		}
		return this;
	}

	public void setNextNode(RestrictionNode nextNode) {
		this.nextNode = nextNode;
		if (nextNode != null) {
			nextNode.priorNode = this;
		}
	}

	public String printNodeTree(boolean doInvert,int indentation) {
		if (nextNode != null)
			return nextNode.printNodeTree(doInvert,indentation);
		else
			return "";
	}

	private RestrictionNode getPriorNode() {
		return priorNode;
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class PropertyNode extends RestrictionNode {
		final @NonNull String property;
		final ArlType type;		

		public String printNodeTree(boolean doInvert,int indentation)
		{
			String ret="";
			String postFix = "";
			if(priorNode instanceof ValueNode)
			{
				return " "+property;
			}
			if (isNextLogicalNodeAPropertyNode()) 
				postFix = " with ";
			else {
				if (isNextLogicalNodeAnOperatorOrComparator())	
					postFix = "";
				else if (getNextLogicalLeafNode(this) != null)
					postFix = " that has ";
			}
			String typeStr = ""; 
			if (type != null && type.nativeType != null && type.nativeType instanceof InstanceType) {				
				typeStr = ((InstanceType)type.nativeType).name();								
			}
			if (priorNode == null) { //first node in restriction			
				if (typeStr.length() > 0) // there is a complex type	
				{
					typeStr=typeStr.replaceFirst(" ","");
					postFix=postFix.replaceFirst(" ","");
					ret= "a "+typeStr+" "+postFix+"\n"+super.printNodeTree(doInvert,indentation+2);
					return "~".repeat(indentation)+ret;
					//return String.format("%"+indentation+"s%s"," ",ret);
				}
				else
				{
					ret= postFix+super.printNodeTree(doInvert,indentation);
					return ret;
				}
			}
			else
			{
				if (type != null && type.collection != null && type.collection.equals(CollectionKind.SINGLE)) {
					if(typeStr.length()>0)
						typeStr = " set to a "+typeStr;
					if (!isPriorElementAForAll()) {
						//typeStr=typeStr.replaceFirst(" ","");
						String sup=super.printNodeTree(doInvert,indentation+2);
						if(sup.equals(""))
						{
							ret= "its "+property+typeStr+postFix;
							ret=ret.replaceAll("  ", " ");
							return "~".repeat(indentation)+ret;
							//return String.format("%"+indentation+"s%s"," ",ret);
						}
						else
						{
							if(sup.startsWith("=") || sup.startsWith("equalsIgnoreCase") || sup.startsWith("starting with"))
								ret= "its "+property+" "+typeStr+postFix+sup;
							else
								ret= "its "+property+" "+typeStr+postFix+"\n"+sup;
							ret=ret.replaceAll("  ", " ");
							return "~".repeat(indentation)+ret;
							//return String.format("%"+indentation+"s%s"," ",ret);
						}
					} else {
						String sup=super.printNodeTree(doInvert,indentation+2);
						if(!sup.equals(""))
							ret= "their "+property+typeStr+postFix+"\n"+sup;
						else
							ret= "their "+property+typeStr+postFix;
						return "\n"+"~".repeat(indentation)+ret;
					}	
				}
				else
				{
					String sup=super.printNodeTree(doInvert,indentation+2);
					if ( isPriorElementSizeOrIsEmpty() ) 
					{
						if(sup.equals(""))
							ret=property+" of type "+typeStr+postFix;
						else if(sup.startsWith("=") || sup.startsWith("equalsIgnoreCase") || sup.startsWith("starting with"))
							ret=property+" of type "+typeStr+postFix+sup;
						else
							ret= property+" containing a "+typeStr+postFix+"\n"+sup;
						return "~".repeat(indentation)+ret;
						//return String.format("%"+indentation+"s%s"," ",ret);
					}
					else {
						if(isPriorElementAForAll())
						{
							ret= property+" being a "+typeStr+postFix+sup;
							return String.format(" %s",ret);
						}
						else if (isNextElementAForAll()) {
							ret= property+" containing only "+typeStr+"s which have"+sup;
						} else 
						{
							if(sup.startsWith("=") || sup.startsWith("equalsIgnoreCase") || sup.startsWith("starting with"))
								ret= property+" containing a "+typeStr+postFix+sup;
							else
								ret= property+" containing a "+typeStr+postFix+"\n"+sup;
							return "~".repeat(indentation)+ret;
							//return String.format("%"+indentation+"s%s"," ",ret);
						}
					}
				}
			}
			return null;

		}


		@Override
		public boolean matches(RestrictionNode comparison) { //we ignore type for now here			
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof PropertyNode)
					&& (((PropertyNode)comparison).property.equals(property) )
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		private boolean isPriorElementAForAll() {
			RestrictionNode priorNode = this.priorNode;
			while (priorNode != null 
					&& (priorNode instanceof BipartComparatorNode
							|| priorNode instanceof OnlyComparatorNode
							|| priorNode instanceof SubtreeCombinatorNode
							|| priorNode instanceof NotNode
							)) {
				priorNode = priorNode.getPriorNode();
			}			
			return (priorNode != null && priorNode instanceof OperationNode && (((OperationNode) priorNode).getOperation().equalsIgnoreCase("forall")));
		}

		private boolean isNextElementAForAll() {
			RestrictionNode nextNode = getNextLogicalLeafNode(this);
			return (nextNode != null && nextNode instanceof OperationNode && (((OperationNode) nextNode).getOperation().equalsIgnoreCase("forall")));
		}
		

		private boolean isPriorElementSizeOrIsEmpty() {
			return (priorNode != null && priorNode instanceof OperationNode 
					&& (((OperationNode) priorNode).getOperation().equalsIgnoreCase("size") 
							|| ((OperationNode) priorNode).getOperation().equalsIgnoreCase("isempty"))
					)
					||
					(priorNode != null && priorNode.priorNode != null && priorNode.priorNode instanceof OperationNode 
					&& (((OperationNode) priorNode.priorNode).getOperation().equalsIgnoreCase("size") 
							|| ((OperationNode) priorNode.priorNode).getOperation().equalsIgnoreCase("isempty")
							));
		}

		private boolean isNextLogicalNodeAPropertyNode() {			
			RestrictionNode nextNode = getNextLogicalLeafNode(this);			
			if (nextNode instanceof PropertyNode)
				return true;
			else
				return false;
		}

		private boolean isNextLogicalNodeAnOperatorOrComparator() {
			RestrictionNode nextNode = getNextLogicalLeafNode(this);
			if (nextNode instanceof SubtreeCombinatorNode || 
					nextNode instanceof OnlyComparatorNode || 
					nextNode instanceof BipartComparatorNode)				
				return true;
			else if (nextNode instanceof OperationNode 
					&& !(((OperationNode) nextNode).getOperation().equalsIgnoreCase("size"))
					&& !(((OperationNode) nextNode).getOperation().equalsIgnoreCase("any")))
				return true;
			else
				return false;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this)) {
				if (nextNode != null) {
					return nextNode.getNextLogicalLeafNode(this);
				} else	{
					return priorNode != null ? priorNode.getNextLogicalLeafNode(this) : null; 					
				}
			} else if (comingFrom.equals(nextNode)) {
				return null;				
			} else // coming from prior node
				return this;						
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			return String.format("%"+ indentation +"s Property [%s] %s", " ", property, next );					
		}		
		// Added Code
		@Override
		public int getNumberOfRestrictions() {
			if (this.nextNode != null)
				return 1 + this.nextNode.getNumberOfRestrictions();
			else
				return 1;
		}
		@Override
		public Map<String, Integer> getRestComplexity() {
			if(this.nextNode!=null)
				return this.nextNode.getRestComplexity();
			else return new HashMap<>();
		}
		// End Here

	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class ValueNode extends RestrictionNode {
		final String value;

		public String printNodeTree(boolean doInvert,int indentation)
		{
			return value+super.printNodeTree(doInvert,indentation+2);
		}
		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof ValueNode) 
					&& ((ValueNode)comparison).value.equals(value) 
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			return null;
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			return String.format("%"+ indentation +"s Value [%s] %s", " ", value, next );					
		}	
		@Override
		public int getNumberOfRestrictions() {
			if (this.nextNode != null)
				return 1 + this.nextNode.getNumberOfRestrictions();
			else
				return 1;
		}
		@Override
		public Map<String, Integer> getRestComplexity() {
			if (this.nextNode != null)
				return this.nextNode.getRestComplexity();
			else
				return new HashMap<>();
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class OnlyComparatorNode extends RestrictionNode {		
		final @NonNull Operator operator;

		public String printNodeTree(boolean doInvert,int indentation)
		{
			String op = operator.toString();
			switch(operator) {
			case ADD:
				//this.addRestComplexity("ADD");
				op = doInvert ? "remove" : "add"; 
				break;
			case MOD_EQ:
				//this.addRestComplexity("MOD_EQ");
				op = doInvert ? "<>" : "=";				
				break;
			case MOD_GT:
				//this.addRestComplexity("MOD_GT");
				op = doInvert ? "=<" : ">";
				break;
			case MOD_LT:
				//this.addRestComplexity("MOD_LT");
				op = doInvert ? "=>" : "<";				
				break;
			case MOD_NEQ:
				//this.addRestComplexity("MOD_NEQ");
				op = doInvert ? "=" : "<>";				
				break;
			case REMOVE:
				//this.addRestComplexity("REMOVE");
				op = doInvert ? "add" : "remove";
				break;		
			}
			return op+" "+super.printNodeTree(false,indentation+2);
		}



		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			return String.format("%"+ indentation +"s [%s] %s", " ", operator, next );					
		}	

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof OnlyComparatorNode) 
					&& ((OnlyComparatorNode)comparison).operator.equals(operator) 
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(nextNode)) {
				return null;				
			} else 
				return this;
		}
		@Override
		public int getNumberOfRestrictions() {
			if (this.nextNode != null)
				return 1 + this.nextNode.getNumberOfRestrictions();
			else
				return 1;
		}
		@Override
		public Map<String, Integer> getRestComplexity() {
			String op = operator.toString();
			Map<String, Integer> mp=new HashMap<>();
			if (this.nextNode != null)
			{
				mp=this.nextNode.getRestComplexity();
				if(mp.putIfAbsent(op, 1)!=null)
				{
					mp.put(op, mp.get(op)+1);
				}
			}
			else
			{
				mp.put(op, 1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class SubtreeCombinatorNode extends RestrictionNode {
		final @NonNull RestrictionNode lhs;
		final @NonNull RestrictionNode rhs;

		public SubtreeCombinatorNode(RestrictionNode lhs, RestrictionNode rhs) {
			if (lhs == null || rhs == null) throw new IllegalArgumentException("Constructor arguments must not be null");
			this.lhs = lhs;
			this.rhs = rhs;
			lhs.priorNode = this;
			rhs.priorNode = this;
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			String lhsStr = lhs.toTreeString(indentation+1);
			String rhsStr = rhs.toTreeString(indentation+1);
			return String.format("%"+ indentation +"s [SubtreeCombinator \n%s \n%s \n%"+ indentation +"s ] %s", " ", lhsStr, rhsStr, " ", next );					
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			String lhsStr=lhs.printNodeTree(doInvert,indentation);
			String rhsStr=rhs.printNodeTree(doInvert,indentation+1);
			String supStr=super.printNodeTree(doInvert,indentation+1);
			return lhsStr+"\n"+rhsStr+supStr;
		}
		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof SubtreeCombinatorNode)
					&& lhs.matches(((SubtreeCombinatorNode)comparison).lhs)
					&& rhs.matches(((SubtreeCombinatorNode)comparison).rhs)
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return lhs.getNextLogicalLeafNode(this);				
			} else if (comingFrom.equals(lhs)) { 
				return rhs.getNextLogicalLeafNode(this);
			} else if (comingFrom.equals(rhs)) {
				if (nextNode != null)
					return nextNode.getNextLogicalLeafNode(this);
				else 
					return priorNode.getNextLogicalLeafNode(this);
			} else //if (comingFrom.equals(nextNode)) {
				return priorNode.getNextLogicalLeafNode(this);				
			//} 

		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.lhs != null)
				count += this.lhs.getNumberOfRestrictions();
			if (this.rhs != null)
				count += this.rhs.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> l_mp=new HashMap<>();
			Map<String, Integer> r_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.lhs != null)
			{
				l_mp = this.lhs.getRestComplexity();
				mp=this.mergeMaps(mp, l_mp);
			}
			if (this.rhs != null)
			{
				r_mp = this.rhs.getRestComplexity();
				mp=this.mergeMaps(mp, r_mp);
			}
			if(mp.putIfAbsent("SubtreeCombinator", 1)!=null)
			{
				mp.put("SubtreeCombinator", mp.get("SubtreeCombinator")+1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class BipartComparatorNode extends SubtreeCombinatorNode {		
		final @NonNull Operator operator;

		public BipartComparatorNode(Operator operator, RestrictionNode lhs, RestrictionNode rhs) {
			super(lhs, rhs);
			this.operator = operator;
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			String lhsStr = lhs.toTreeString(indentation+2);
			String rhsStr = rhs.toTreeString(indentation+2);
			return String.format("%"+ indentation +"s [BipartComparator [%s] \n%s \n%s \n%"+ indentation +"s ] %s", " ", operator, lhsStr, rhsStr, " ", next );					
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			//this.addRestComplexity("BipartComparator");
			String op = operator.toString();
			switch(operator) {
			case ADD:
				op = doInvert ? "remove" : "add"; 
				break;
			case MOD_EQ:
				op = doInvert ? "<>" : "=";				
				break;
			case MOD_GT:
				op = doInvert ? "<=" : ">";
				break;
			case MOD_LT:
				op = doInvert ? ">=" : "<";				
				break;
			case MOD_NEQ:
				op = doInvert ? "=" : "<>";				
				break;
			case REMOVE:
				op = doInvert ? "add" : "remove";
				break;		
			}		
			String nn = nextNode != null ? nextNode.printNodeTree(doInvert,indentation+2) : "";
			String out = null;
			if ( rhs instanceof ValueNode || lhs instanceof ValueNode) 
				out = compileSpecial(op,indentation);
			if (out == null)
			{
				if(rhs instanceof PropertyNode && lhs instanceof PropertyNode)
				{
					String rSide=rhs.printNodeTree(false,indentation);
					rSide=rSide.replaceFirst("~".repeat(indentation), "");
					out = lhs.printNodeTree(false,indentation)+" "+op+" "+rSide;
				}
				else
				{
					String rSide=rhs.printNodeTree(false,indentation);
					if(rSide.matches("-?\\d+"))
					{
						out = lhs.printNodeTree(false,indentation)+
								"\n"+"~".repeat(indentation+1)+op+" "+rSide;
						//String.format("\n%"+(indentation+1)+"s%s %s", " ",op,rSide);
					}
					else	
						out = lhs.printNodeTree(false,indentation)+" "+op+" "+rSide;
				}
			}
			if(nn.equals(""))
			{
				return out;
			}
			else
				return out+nn;			
		}



		private String compileSpecial(String op,int indentation) {
			String ret="";
			String oSide="";
			if (rhs instanceof ValueNode) {
				Long rhsL = parseValueNode((ValueNode)rhs);
				if (rhsL != null && (rhsL == 0 || rhsL == 1)) {
					String wordOp = "";
					switch(op) {
					case ">":  wordOp = (rhsL == 0) ? "at least one" : "more than one";
					//ret=String.format("%"+indentation+"s%s ", " ",wordOp);
					ret="~".repeat(indentation)+wordOp+" ";
					oSide=lhs.printNodeTree(false,indentation);
					oSide=oSide.replaceFirst("~".repeat(indentation), "");
					//oSide=oSide.replaceFirst(String.format("%"+indentation+"s", " "),"");
					return ret+oSide;
					case ">=": wordOp = (rhsL == 0) ? "zero or more" : "at least one";
					return wordOp+"\n"+lhs.printNodeTree(false,indentation);
					case "<":  wordOp = (rhsL == 0) ? "less then zero" : "less than one";
					return wordOp+"\n"+lhs.printNodeTree(false,indentation);
					case "<=": wordOp = (rhsL == 0) ? "zero or less" : "less than one";
					return wordOp+"\n"+lhs.printNodeTree(false,indentation);
					case "=": wordOp = (rhsL == 0) ? "exactly none" : "= 1";
					oSide=lhs.printNodeTree(false,indentation);
					if(lhs instanceof OperationNode)
					{
						if(wordOp.equals("= 1")) wordOp="exactly one";
						ret="~".repeat(indentation)+wordOp+" ";
						oSide=lhs.printNodeTree(false,indentation);
						oSide=oSide.replaceFirst("~".repeat(indentation), "");
						return ret+oSide; 
					}
					else
						return oSide+" "+wordOp;
					//Operation Node .. let's see for experiment what the type of node is.
					/*ret="~".repeat(indentation)+wordOp+" ";
					oSide=lhs.printNodeTree(false,indentation);
					oSide=oSide.replaceFirst("~".repeat(indentation), "");
					return ret+oSide;*/
					default: return null;
					}
				} else return null;
			} else {
				Long lhsL = parseValueNode((ValueNode)lhs);
				if (lhsL != null && (lhsL == 0 || lhsL == 1) ) {
					String wordOp = "";
					switch(op) { // as now the comparison value is on the left hand side 
					//we have to invert the meaning of < with > and <= with >=
					case ">":  wordOp = (lhsL == 0) ? "less then zero" : "less than one";
					return wordOp+"\n"+rhs.printNodeTree(false,indentation);
					case ">=": wordOp = (lhsL == 0) ? "zero or less" : "less than one";
					return wordOp+"\n"+rhs.printNodeTree(false,indentation);
					case "<":  wordOp = (lhsL == 0) ? "at least one" : "more than one"; 
					//ret=String.format("%"+indentation+"s%s ", " ",wordOp);
					ret="~".repeat(indentation)+wordOp+" ";
					oSide=lhs.printNodeTree(false,indentation);
					oSide=oSide.replaceFirst("~".repeat(indentation), "");
					//oSide=oSide.replaceFirst(String.format("%"+indentation+"s", " "),"");
					return ret+oSide;
					case "<=": wordOp = (lhsL == 0) ? "zero or more" : "at least one";
					return wordOp+"\n"+rhs.printNodeTree(false,indentation);
					case "=": wordOp = (lhsL == 0) ? "exactly none" : "exactly one";
					ret="~".repeat(indentation)+wordOp+" ";
					oSide=lhs.printNodeTree(false,indentation);
					oSide=oSide.replaceFirst("~".repeat(indentation), "");
					return ret+oSide;
					default: return null;
					}
				} else return null;
			}
		}

		private Long parseValueNode(ValueNode vn) {
			try {
				return Long.parseLong(vn.value);
			} catch(Exception e) {
				return null;
			}
		}

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof BipartComparatorNode) 
					&& ((BipartComparatorNode)comparison).operator.equals(operator) 
					&& super.matches(comparison);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return lhs.getNextLogicalLeafNode(this);				
			} else if (comingFrom.equals(lhs)) { 
				return this;
			} else 
				return null;										
		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.lhs != null)
				count += this.lhs.getNumberOfRestrictions();
			if (this.rhs != null)
				count += this.rhs.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> l_mp=new HashMap<>();
			Map<String, Integer> r_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.lhs != null)
			{
				l_mp = this.lhs.getRestComplexity();
				mp=this.mergeMaps(mp, l_mp);
			}
			if (this.rhs != null)
			{
				r_mp = this.rhs.getRestComplexity();
				mp=this.mergeMaps(mp, r_mp);
			}
			if(mp.putIfAbsent("BipartComparator", 1)!=null)
			{
				mp.put("BipartComparator", mp.get("BipartComparator")+1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class NotNode extends RestrictionNode {

		final RestrictionNode rn;	

		public NotNode(RestrictionNode rn) {
			this.rn = rn;
			rn.priorNode = this;
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";	
			String subStr = rn.toTreeString(indentation+2);
			return String.format("%"+ indentation +"s [Not \n%s \n%"+ indentation +"s ] %s", " ", subStr, " ", next );						
		}

		public String printNodeTree(boolean doInvert,int indentation)
		{
			//this.addRestComplexity("NotNode");
			String nn = nextNode != null ? nextNode.printNodeTree(doInvert,indentation) : "";
			return rn.printNodeTree(!doInvert,indentation)+ nn;
		}

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof NotNode)					
					&& rn.matches(((NotNode)comparison).rn)
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return rn.getNextLogicalLeafNode(this);				
			} else 
				return null;										
		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.rn != null)
				count += this.rn.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> rn_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.rn != null)
			{
				rn_mp = this.rn.getRestComplexity();
				mp=this.mergeMaps(mp, rn_mp);
			}
			if(mp.putIfAbsent("NotNode", 1)!=null)
			{
				mp.put("NotNode", mp.get("NotNode")+1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class AndNode extends SubtreeCombinatorNode {

		public AndNode(RestrictionNode lhs, RestrictionNode rhs) {
			super(lhs, rhs);			
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			String lhsStr = lhs.toTreeString(indentation+2);
			String rhsStr = rhs.toTreeString(indentation+2);
			return String.format("%"+ indentation +"s [And \n%s \n%s \n%"+ indentation +"s ] %s", " ", lhsStr, rhsStr, " ", next );					
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			//this.addRestComplexity("ANDNode");
			String nn = nextNode != null ? nextNode.printNodeTree(doInvert,indentation) : "";
			String lhsStr = lhs.printNodeTree(doInvert,indentation+1);
			String rhsStr = rhs.printNodeTree(doInvert,indentation+1);
			rhsStr = (rhsStr != null && rhsStr.length() > 0) ? rhsStr : "";
			if(!nn.equals(""))
			{
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"both "+lhsStr+" and " +rhsStr+nn;
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"and\n"+rhsStr+nn;
				//return String.format(lhsStr+
				//	"\n%"+(indentation)+"sor\n"+rhsStr+nn," ");
			}
			else
			{
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"both "+lhsStr+" and " +rhsStr;
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"and\n"+rhsStr;
				//return String.format(lhsStr+
				//	"\n%"+(indentation)+"sor\n"+rhsStr," ");
			}
		}

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof AndNode)
					&& lhs.matches(((AndNode)comparison).lhs)
					&& rhs.matches(((AndNode)comparison).rhs)
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return lhs.getNextLogicalLeafNode(this);				
			} else if (comingFrom.equals(lhs)) { 
				return this;
			} else 
				return null;			
		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.lhs != null)
				count += this.lhs.getNumberOfRestrictions();
			if (this.rhs != null)
				count += this.rhs.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> l_mp=new HashMap<>();
			Map<String, Integer> r_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.lhs != null)
			{
				l_mp = this.lhs.getRestComplexity();
				mp=this.mergeMaps(mp, l_mp);
			}
			if (this.rhs != null)
			{
				r_mp = this.rhs.getRestComplexity();
				mp=this.mergeMaps(mp, r_mp);
			}
			if(mp.putIfAbsent("ANDNode", 1)!=null)
			{
				mp.put("ANDNode", mp.get("ANDNode")+1);
			}
			return mp;
		}
		
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class OrNode extends SubtreeCombinatorNode {

		public OrNode(RestrictionNode lhs, RestrictionNode rhs) {
			super(lhs, rhs);
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			String lhsStr = lhs.toTreeString(indentation+4);
			String rhsStr = rhs.toTreeString(indentation+4);
			return String.format("%"+ indentation +"s [Or \n%s \n%s \n%"+ indentation +"s ] %s", " ", lhsStr, rhsStr, " ", next );			
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			//this.addRestComplexity("ORNode");
			String nn = nextNode != null ? nextNode.printNodeTree(doInvert,indentation) : "";
			//if(lhs instanceof AndNode)
			String lhsStr = lhs.printNodeTree(doInvert,indentation+1);
			String rhsStr = rhs.printNodeTree(doInvert,indentation+1);
			rhsStr = (rhsStr != null && rhsStr.length() > 0) ? rhsStr : "";
			if(!nn.equals(""))
			{
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"either "+lhsStr+" or " +rhsStr+nn;
				}
				else if(rhs instanceof ValueNode)
				{
					return lhsStr+"\n"+"~".repeat(indentation)+"or\n"+"~".repeat(indentation+1)+rhsStr+nn;
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"or\n"+rhsStr+nn;
				//return String.format(lhsStr+
				//	"\n%"+(indentation)+"sor\n"+rhsStr+nn," ");
			}
			else
			{
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"either "+lhsStr+" or " +rhsStr;
				}
				else if(rhs instanceof ValueNode)
				{
					return lhsStr+"\n"+"~".repeat(indentation)+"or\n"+"~".repeat(indentation+1)+rhsStr;
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"or\n"+rhsStr;
				//return String.format(lhsStr+
				//	"\n%"+(indentation)+"sor\n"+rhsStr," ");
			}
		}

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof OrNode)
					&& lhs.matches(((OrNode)comparison).lhs)
					&& rhs.matches(((OrNode)comparison).rhs)
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return lhs.getNextLogicalLeafNode(this);				
			} else if (comingFrom.equals(lhs)) { 
				return this;
			} else 
				return null;			
		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.lhs != null)
				count += this.lhs.getNumberOfRestrictions();
			if (this.rhs != null)
				count += this.rhs.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> l_mp=new HashMap<>();
			Map<String, Integer> r_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.lhs != null)
			{
				l_mp = this.lhs.getRestComplexity();
				mp=this.mergeMaps(mp, l_mp);
			}
			if (this.rhs != null)
			{
				r_mp = this.rhs.getRestComplexity();
				mp=this.mergeMaps(mp, r_mp);
			}
			if(mp.putIfAbsent("ORNode", 1)!=null)
			{
				mp.put("ORNode", mp.get("ORNode")+1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class XOrNode extends SubtreeCombinatorNode {

		public XOrNode(RestrictionNode lhs, RestrictionNode rhs) {
			super(lhs, rhs);
		}

		public String toTreeString(int indentation) {
			String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
			String lhsStr = lhs.toTreeString(indentation+2);
			String rhsStr = rhs.toTreeString(indentation+2);
			return String.format("%"+ indentation +"s [Xor \n%s \n%s \n%"+ indentation +"s ] %s", " ", lhsStr, rhsStr, " ", next );		
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			//this.addRestComplexity("XORNode");
			String nn = nextNode != null ? nextNode.printNodeTree(doInvert,indentation) : "";
			String lhsStr = lhs.printNodeTree(doInvert,indentation);
			String rhsStr = rhs.printNodeTree(doInvert,indentation);
			rhsStr = (rhsStr != null && rhsStr.length() > 0) ? rhsStr : "";
			if(!nn.equals(""))
			{
				return "\n"+"~".repeat(indentation)+"either\n"+lhsStr+"\n"+"~".repeat(indentation)+"or\n" +rhsStr+"\n"+"~".repeat(indentation)+"but not both at same time"+"\n"+nn;
				/*
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"either "+lhsStr+" or " +rhsStr+" but not both at a time"+"\n"+nn;
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"xor\n"+rhsStr+nn;*/
			}
			else
			{
				return "\n"+"~".repeat(indentation)+"either\n"+lhsStr+"\n"+"~".repeat(indentation)+"or\n" +rhsStr+"\n"+"~".repeat(indentation)+"but not both at same time";
				/*
				if(lhs instanceof ValueNode && rhs instanceof ValueNode)
				{
					return "\n"+"~".repeat(indentation)+"either "+lhsStr+" or " +rhsStr+" but not both at a time";
				}
				else
					return lhsStr+"\n"+"~".repeat(indentation)+"xor\n"+rhsStr;*/
			}
		}

		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof XOrNode)
					&& lhs.matches(((XOrNode)comparison).lhs)
					&& rhs.matches(((XOrNode)comparison).rhs)
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this) || comingFrom.equals(priorNode)) {				
				return lhs.getNextLogicalLeafNode(this);				
			} else if (comingFrom.equals(lhs)) { 
				return this;
			} else 
				return null;			
		}
		@Override
		public int getNumberOfRestrictions() {
			int count = 1;
			if (this.nextNode != null)
				count += this.nextNode.getNumberOfRestrictions();
			if (this.lhs != null)
				count += this.lhs.getNumberOfRestrictions();
			if (this.rhs != null)
				count += this.rhs.getNumberOfRestrictions();
			return count;
		}

		@Override
		public Map<String, Integer> getRestComplexity() {
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> l_mp=new HashMap<>();
			Map<String, Integer> r_mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if (this.lhs != null)
			{
				l_mp = this.lhs.getRestComplexity();
				mp=this.mergeMaps(mp, l_mp);
			}
			if (this.rhs != null)
			{
				r_mp = this.rhs.getRestComplexity();
				mp=this.mergeMaps(mp, r_mp);
			}
			if(mp.putIfAbsent("XORNode", 1)!=null)
			{
				mp.put("XORNode", mp.get("XORNode")+1);
			}
			return mp;
		}
	}

	@EqualsAndHashCode(callSuper=true)
	@Data
	public static class OperationNode extends RestrictionNode {	

		List<RestrictionNode> parameterizedRest=new ArrayList<>();
		final @NonNull String operation;

		public OperationNode(String op,List<RestrictionNode> rn) {
			this.parameterizedRest = rn;
			this.operation = op;
			this.parameterizedRest.forEach(rest->rest.priorNode=this);
		}
		public OperationNode(String op) {
			this.parameterizedRest = null;
			this.operation=op;
		}

		public String toTreeString(int indentation) {
			String op=operation;
			String c0="";
			String c1="";
			String nn="";
			switch(operation.toLowerCase())
			{
			case "union":
				c0 = this.parameterizedRest.get(0) !=null ? "\n"+this.parameterizedRest.get(0).toTreeString(indentation) : "";
				c1 = this.parameterizedRest.get(1) !=null ? "\n"+this.parameterizedRest.get(1).toTreeString(indentation) : "";
				nn = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
				return String.format("%"+ indentation +"s Operation [%s] %s %s %s", " ", operation, c0,c1,nn );
			case "intersection":
				c0 = this.parameterizedRest.get(0) !=null ? "\n"+this.parameterizedRest.get(0).toTreeString(indentation) : "";
				c1 = this.parameterizedRest.get(1) !=null ? "\n"+this.parameterizedRest.get(1).toTreeString(indentation) : "";
				nn = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";
				return String.format("%"+ indentation +"s Operation [%s] %s %s %s", " ", operation, c0,c1,nn );
			case "forall":
				c0 = this.parameterizedRest.get(0) !=null ? "\n"+this.parameterizedRest.get(0).toTreeString(indentation) : "";
				c1 = this.parameterizedRest.size()>1 ? "\n"+this.parameterizedRest.get(1).toTreeString(indentation) : "";
				if(c1.equals(""))
				{
					return String.format("%"+ indentation +"s Operation [%s] %s", " ", operation, c0 );
				}
				else
					return String.format("%"+ indentation +"s Operation [%s] %s %s", " ", operation, c0,c1 );
			default:
				String next = nextNode !=null ? "\n"+nextNode.toTreeString(indentation) : "";			
				return String.format("%"+ indentation +"s Operation [%s] %s", " ", operation, next );	
			}			
		}	

		public String printNodeTree(boolean doInvert,int indentation)
		{
			String op = operation;
			String ret="";
			switch (operation.toLowerCase()) {
			case "aslist":
				return ""+super.printNodeTree(false,indentation);
			case "first":
				return ""+super.printNodeTree(false,indentation);
			case "any":
				op = "any of the"; //this is handled as part of a property node
				ret="~".repeat(indentation)+op+"\n";
				//ret=String.format("%"+indentation+"s%s\n", " ",op);
				return ret+super.printNodeTree(false,indentation+2);
			case "equalsignorecase":
				op = doInvert ? "not equalsIgnoreCase" : "equalsIgnoreCase";
				return op+" "+super.printNodeTree(false,indentation);	
			case "startswith":
				op = doInvert ? "not starting with" : "starting with";
				return "~".repeat(indentation)+op+" "+super.printNodeTree(false,indentation);
			case "isdefined":
				op = doInvert ? "which is not defined" : "which is defined";
				ret="~".repeat(indentation)+op+"\n";
				return ret+super.printNodeTree(false,indentation+2);
			case "isempty":
				op = doInvert ? "that at least one element in its" : "that has no element in its";
				//ret=String.format("%"+indentation+"s%s\n", " ",op);
				ret="~".repeat(indentation)+op+"\n";
				return ret+super.printNodeTree(false,indentation+2);
			case "size":	
				op = "the number of elements with";
				if (priorNode != null && priorNode instanceof BipartComparatorNode)
				{
					if(nextNode instanceof OperationNode && ((OperationNode)nextNode).getOperation().equals("intersection"))
					{
						op = "element of";
						ret="~".repeat(indentation)+op+"\n";
						return ret+super.printNodeTree(false,indentation+2);
					}
					else
					{
					op = "element with";
					ret="~".repeat(indentation)+op+"\n";
					return ret+super.printNodeTree(false,indentation+2);
					}
				}
				else
				{
					ret="\n"+"~".repeat(indentation)+op+"\n";
					return ret+super.printNodeTree(false,indentation+2);
				}
				//return String.format("\n%"+indentation+"s %s %"+indentation+"s%s", " ",op," ",super.printNodeTree(doInvert,indentation+2));
			case "forall":
				op = "for all"; //this is handled as part of a property node
				String child0="";
				if(this.parameterizedRest.get(0) instanceof SubtreeCombinatorNode)
					child0=this.parameterizedRest.get(0).printNodeTree(doInvert,indentation+1);
				else
					child0=this.parameterizedRest.get(0).printNodeTree(doInvert,indentation+2);
				String child1=this.parameterizedRest.size()>1 ? "\n"+this.parameterizedRest.get(1).printNodeTree(doInvert,indentation+2) : "";
				if(!child1.equals(""))
				{
					child1=child1.strip();
					ret="~".repeat(indentation)+
							op+
							child0+"\n"+
							"~".repeat(indentation+1)+"should have \n"+ "~".repeat(indentation+1)+child1;
					return ret;
					/*child1=child1.strip();
					return 	String.format("\n%"+(indentation+2)+"s %s", " ",op)
							+String.format(" %s",child0)
							+String.format("\n%"+(indentation+3)+"s should have %s", " ",child1);*/
				}
				else
				{
					ret="~".repeat(indentation)+
							op+" "+
							child0;
					return ret;
					/*return String.format("\n%"+(indentation+2)+"s %s", " ",op)
							+String.format(" %s",child0);*/
				}	
			case "includedin":
				if (nextNode instanceof ValueNode) {
					op = doInvert ? "none of" : "which is";
					ret="~".repeat(indentation)+op+" "+super.printNodeTree(doInvert,indentation+2);
					return ret;
				} else {
					op = doInvert ? "which do not include" : "which includes";	
					ret="~".repeat(indentation)+op+"\n"+super.printNodeTree(doInvert,indentation+2);
					return ret;
				}

			case "contains":
				op = doInvert ? "which do not contain" : "which contains";
				if(nextNode instanceof ValueNode)
					ret="~".repeat(indentation)+op+" "+super.printNodeTree(doInvert,indentation+2);
				else
					ret="~".repeat(indentation)+op+"\n"+super.printNodeTree(doInvert,indentation+2);
				return ret;
			case "union":
				String u_rest="";
				String ch1=this.parameterizedRest.get(1).printNodeTree(doInvert,indentation+2);
				String ch0=this.parameterizedRest.get(0).printNodeTree(doInvert,indentation+2);
				String nNode=this.nextNode!=null ? this.nextNode.printNodeTree(doInvert, indentation+2):"";
				ret="~".repeat(indentation)+"union between\n"+ch0+"\n"+"~".repeat(indentation)+"and\n"+ch1;
				return ret;
			case "substring": //Pending
				//this.addRestComplexity("substring");
				op = doInvert ? "lacking the "+op : "having the"+op;
				break;
			case "indexof":	 //Pending
				op = doInvert ? "not having position of" : "having position of";
				break;

			case "intersection":
				String c1=this.parameterizedRest.get(1).printNodeTree(doInvert,indentation);
				String c0=this.parameterizedRest.get(0).printNodeTree(doInvert,indentation);
				String nn=this.nextNode!=null ? this.nextNode.printNodeTree(doInvert, indentation+2):"";
				if(this.parameterizedRest.get(0) instanceof ValueNode && this.parameterizedRest.get(1) instanceof ValueNode)
				{
					ret=c1+"\n"+"~".repeat(indentation)+"should contain\n"+"~".repeat(indentation)+c0+nn;
				}
				else if(this.parameterizedRest.get(0) instanceof ValueNode)
				{
					ret=c1+"\n"+"~".repeat(indentation)+"should contain\n"+"~".repeat(indentation)+c0+nn;
				}
				else if(this.parameterizedRest.get(1) instanceof ValueNode && this.parameterizedRest.get(1).nextNode!=null)
				{
					ret=c0+"\n"+"~".repeat(indentation)+"and the same item must be present in\n"+"~".repeat(indentation)+c1+nn;
				}
				else if(this.parameterizedRest.get(1) instanceof ValueNode && this.parameterizedRest.get(1).nextNode==null)
				{
					ret=c0+"\n"+"~".repeat(indentation)+"must be\n"+"~".repeat(indentation)+c1+nn;
				}
				else
				{
					ret=c0+"\n"+"~".repeat(indentation)+"should have intersection with\n"+c1+nn;
				}
				return ret;
			default:
				return op+super.printNodeTree(false,indentation);	
			}
			return null;
		}



		@Override
		public boolean matches(RestrictionNode comparison) { 
			if (comparison == null ) { 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " is not expected");
				return false;
			}
			boolean result =  (comparison instanceof OperationNode) 
					&& ((OperationNode)comparison).operation.equals(operation) 
					&& (this.nextNode != null ? this.nextNode.matches(comparison.getNextNode()) : comparison.getNextNode() == null);
			if (!result) 
				log.debug("RestrictionNodeMismatch: "+this.toString()+ " does not match expected: "+comparison.toString());
			return result;
		}

		@Override
		public RestrictionNode getNextLogicalLeafNode(RestrictionNode comingFrom) {
			if (comingFrom.equals(this))  {				
				if (nextNode != null)
					return nextNode.getNextLogicalLeafNode(this);
				else 
					return null;				
			} else if (comingFrom.equals(priorNode)) { 
				return this;
			} else //if (comingFrom.equals(nextNode)) {
				return null;				
			//} 			
		}
		@Override
		public int getNumberOfRestrictions() {
			if (this.nextNode != null)
				return 1 + this.nextNode.getNumberOfRestrictions();
			else
				return 1;
		}
		@Override
		public Map<String, Integer> getRestComplexity() {
			String op = operation;
			Map<String, Integer> mp=new HashMap<>();
			Map<String, Integer> nn_mp=new HashMap<>();
			if (this.nextNode != null)
			{
				nn_mp=this.nextNode.getRestComplexity();
				mp=this.mergeMaps(mp, nn_mp);
			}
			if(mp.putIfAbsent(op, 1)!=null)
			{
				mp.put(op, mp.get(op)+1);
			}
			return mp;
		}
	}
	public Map<String, Integer> mergeMaps(Map<String, Integer> main_m,Map<String, Integer> m)
	{
		for (Map.Entry<String, Integer> entry : m.entrySet()) 
		{
			if(main_m.putIfAbsent(entry.getKey(), entry.getValue())!=null)
			{
				main_m.put(entry.getKey(), main_m.get(entry.getKey())+1);
			}
        }
		return main_m;
	}
	/*public void addRestComplexity(String op)
	{
		
	}*/
	/*public void resetRestComplexity() {
		this.restComplexity.clear();
	}*/
	/*
	*/


}

