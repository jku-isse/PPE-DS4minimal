package c4s.processdefinition.blockly2java.processors;

import java.util.LinkedList;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Value;

public class QACheckBlockProcessor extends AbstractBlockProcessor {

	private static Logger log = LogManager.getLogger(QACheckBlockProcessor.class);
	
	public final static String QACHECKTYPE = "qacheck";
	
	public QACheckBlockProcessor(ProcessingContext ctx) {
		super(QACHECKTYPE, ctx);
	}

	@Override
	public void processBlock(BlockType block) {
		String internalId = block.getId();
		Optional<String> id = Utils.getFieldValue(block.getField(), "qacheckId");
		Optional<String> desc = Utils.getFieldValue(block.getField(), "description");
		// identify parent task/step:	
		DTOs.Step td = ctx.getCurrentWFT();
		if (td == null) {
			log.warn(String.format("Cannot process QACheck %s without enclosing WorkflowTask", internalId));
			return;
		}
		// create event to add constraint
		ConstraintSpec spec = new ConstraintSpec();
		spec.taskId = td.getCode();
		spec.checkId = id.orElse("ConstraintId");
		spec.checkDesc = desc.orElse("Missing description");
		// process constraint trigger to extract artifact usage
		Optional<Value> constraintOpt = Utils.getFirstValue(block.getStatementOrValue());
		if (constraintOpt.isEmpty()) {
			log.warn(String.format("Cannot process QACheck %s without constraint value", internalId));
			return;
		}
//		List<BlockType> varGets = Utils.retrieveIterative(constraintOpt.get(), "variables_get");
//		varGets.stream().forEach(var -> {
//			Field field = Utils.getField(var.getField(), "VAR").get();
//			ArtifactType type = ctx.getVarIndex().get(field.getId());
//			String artRole = field.getValue();
//			spec.var2type.put(artRole, type);
//		});
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
				log.warn(String.format("Cannot process QAConstraint %s without proper condition value", internalId));
			else
				spec.condition = constr;
		}
		int specOrder = ctx.getTask2constraint().containsKey(td.getCode()) ? ctx.getTask2constraint().get(td.getCode()).size() : 0;
		spec.order = specOrder;
		String isOverridable = Utils.getFieldValue(block.getField(), "isOverridable").orElse("FALSE");		
		spec.isOverridable = Boolean.parseBoolean(isOverridable);
		ctx.getTask2constraint().computeIfAbsent(td.getCode(), k -> new LinkedList<ConstraintSpec>()).add(spec);
	}

	
	public static class ConstraintSpec {		
		
		public String taskId;
		public String checkId;
		public String checkDesc;	
		public String condition;
	//	public HashMap<String, ArtifactType> var2type = new HashMap<String,ArtifactType>();
	//	public HashMap<String, ArtSource> var2source = new HashMap<String,ArtSource>();
		public int order;
		public boolean isOverridable;
	}
	
	
	
}
