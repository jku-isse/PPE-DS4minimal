package c4s.processdefinition.blockly2java.processors;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;

public class DatamappingBlockProcessor extends AbstractBlockProcessor {

private static Logger log = LogManager.getLogger(TransitionBlockProcessor.class);
	
	public final static String DATAMAPPINGTYPE = "datamapping";
	
	public DatamappingBlockProcessor(ProcessingContext ctx) {
		super(DATAMAPPINGTYPE, ctx);
	}

	@Override
	public void processBlock(BlockType block) {
		String internalId = block.getId();
		Optional<String> id = Utils.getFieldValue(block.getField(), "mappingId");		
		Optional<String> spec = Utils.getFieldValue(block.getField(), "mappingSpec");
		DTOs.Step td = ctx.getCurrentWFT();
		if (td == null) {
			log.warn(String.format("Cannot process Datamapping %s without enclosing WorkflowTask", internalId));
			return;
		}
		td.getIoMapping().put(id.get(), spec.get());
	}
	
	
}
