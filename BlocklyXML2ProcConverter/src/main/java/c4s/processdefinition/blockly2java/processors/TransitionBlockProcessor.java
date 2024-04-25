package c4s.processdefinition.blockly2java.processors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import at.jku.isse.passiveprocessengine.instance.StepLifecycle.Conditions;
import c4s.processdefinition.blockly2java.ProcessingContext;
import c4s.processdefinition.blockly2java.processors.Utils.ArtSource;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Value;
import lombok.Data;

public class TransitionBlockProcessor extends AbstractBlockProcessor {

	private static Logger log = LogManager.getLogger(TransitionBlockProcessor.class);
	
	public final static String TRANSITIONTYPE = "transition";
	
	public TransitionBlockProcessor(ProcessingContext ctx) {
		super(TRANSITIONTYPE, ctx);
	}

	@Override
	public void processBlock(BlockType block) {
		String internalId = block.getId();
		Optional<String> id = Utils.getFieldValue(block.getField(), "State");		
		// identify parent task/step:	
		DTOs.Step td = ctx.getCurrentWFT();
		if (td == null) {
			log.warn(String.format("Cannot process Transition %s without enclosing WorkflowTask", internalId));
			return;
		}
		// create event to add transition
		TransitionSpec spec = new TransitionSpec();
		spec.taskId = td.getCode();
		spec.stateId = Conditions.valueOf(id.get());
		
		
		// process transition trigger to extract artifact usage
		Optional<Value> transitionOpt = Utils.getFirstValue(block.getStatementOrValue());
		if (transitionOpt.isEmpty()) {
			log.warn(String.format("Cannot process Transition %s without condition value", internalId));
			return;
		} else {
			Optional<String> textField = Utils.getFieldValue(transitionOpt.get().getBlock().getField(), "TEXT");
			if (textField.isEmpty())
				textField = Utils.getFieldValue(transitionOpt.get().getBlock().getField(), "arlRule");
			String constr = textField.get();
			if (constr.length() < 5)
				log.warn(String.format("Cannot process Transition %s without proper condition value", internalId));
			else
				spec.condition = constr;
		}
	
//		List<BlockType> varGets = Utils.retrieveIterative(transitionOpt.get(), "variables_get");
//		varGets.stream().forEach(var -> {
//			Field field = Utils.getField(var.getField(), "VAR").get();
//			ArtifactType type = ctx.getVarIndex().get(field.getId());
//			String artRole = field.getValue();
//			spec.var2type.put(artRole, type);
//		});
		if (block.getComment() != null && block.getComment().getValue() != null)
			spec.setDescription(block.getComment().getValue());		
		// TODO: fetch isOverridable from XML
		String isOverridable = Utils.getFieldValue(block.getField(), "isOverridable").orElse("FALSE");		
		spec.setOverridable(Boolean.parseBoolean(isOverridable));		
		int specOrder = ctx.getTask2transition().containsKey(td.getCode()) ? ctx.getTask2transition().get(td.getCode()).size() : 0;
		spec.setSpecOrder(specOrder);
		spec.setCheckId(spec.stateId.toString()+specOrder);
		ctx.getTask2transition().computeIfAbsent(td.getCode(), k -> new LinkedList<TransitionSpec>()).add(spec);
	}

	@Data
	public static class TransitionSpec {		
		
		public String taskId;
		public Conditions stateId;	
		public String checkId;
		public HashMap<String, Utils.ArtifactType> var2type = new HashMap<String,Utils.ArtifactType>();
		public HashMap<String, ArtSource> var2source = new HashMap<String,ArtSource>();
		public List<ArtifactOrigin> varFirstSource = new LinkedList<ArtifactOrigin>();
		String condition; 
		String description;
		int specOrder;
		boolean isOverridable = false;
	}
	
	public static class ArtifactOrigin {
		public String localRoleId;
		public String originTaskId;			
		public String roleId;
		public ArtSource location;
		
		public ArtifactOrigin(String localRoleId, String originTaskId, String roleId, ArtSource location) {
			super();
			this.localRoleId = localRoleId;
			this.originTaskId = originTaskId;
			this.roleId = roleId;
			this.location = location;
		}

		public String getLocalRoleId() {
			return localRoleId;
		}

		public void setLocalRoleId(String localRoleId) {
			this.localRoleId = localRoleId;
		}

		public String getOriginTaskId() {
			return originTaskId;
		}

		public void setOriginTaskId(String originTaskId) {
			this.originTaskId = originTaskId;
		}

		public String getRoleId() {
			return roleId;
		}

		public void setRoleId(String roleId) {
			this.roleId = roleId;
		}

		public ArtSource getLocation() {
			return location;
		}

		public void setLocation(ArtSource location) {
			this.location = location;
		}
		
		
	}
	
	
}
