package c4s.processdefinition.blockly2java;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import c4s.processdefinition.blockly2java.processors.BlockProcessors;
import c4s.processdefinition.blockly2java.processors.DataBlockProcessors;
import https.developers_google_com.blockly.xml.Xml;

public class SkeletonTransformer {
	
	private static Logger log = LogManager.getLogger(SkeletonTransformer.class);
	private ProcessingContext ctx = new ProcessingContext();
	
	public SkeletonTransformer() {
		ctx.addBlocksProcessor(new BlockProcessors.VarSetBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.FunctionsBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.ParallelBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.StepBlockProcessor(ctx));
		ctx.addBlocksProcessor(new DataBlockProcessors.StreamBlockProcessor(ctx));
		//ctx.addBlocksProcessor(new QACheckBlockProcessor(ctx));
		//ctx.addBlocksProcessor(new TransitionBlockProcessor(ctx));
		//ctx.addBlocksProcessor(new DatamappingBlockProcessor(ctx));
		ctx.addStatementProcessor(new BlockProcessors.ParaBranchProcessor(ctx));
	}
		
	// provides just the step and decision node sceleton with data input and output but not rules whatsoever, primary use: instantiate to test rules against
	public List<DTOs.Process> toProcessDefinition(Xml root) {						
				
		root.getVariables().getVariable().forEach(var -> ctx.getVarIndex().put(var.getId(), BlockProcessors.UNKNOWNTYPE));				
		root.getBlock().stream()
			.forEach(block -> ctx.processBlock(block));			
		// only return workflows with at least one task.
		return ctx.getFlows().values().stream()
				.filter(wfd -> wfd.getSteps().size() > 0)						
				.collect(Collectors.toList());	
	}

	
	public ProcessingContext getContext() {
		return ctx;
	}
	
	
}
