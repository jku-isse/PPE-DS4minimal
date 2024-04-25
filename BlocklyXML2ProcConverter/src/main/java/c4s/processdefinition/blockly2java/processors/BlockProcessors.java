package c4s.processdefinition.blockly2java.processors;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.configurability.ProcessConfigBaseElementFactory.PropertySchemaDTO;
import at.jku.isse.passiveprocessengine.definition.DecisionNodeDefinition;
import at.jku.isse.passiveprocessengine.definition.DecisionNodeDefinition.InFlowType;
import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import at.jku.isse.passiveprocessengine.definition.serialization.DTOs.DecisionNode;
import at.jku.isse.passiveprocessengine.definition.serialization.DTOs.Mapping;
import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Statement;

public class BlockProcessors {

	private static Logger log = LogManager.getLogger(BlockProcessors.class);

	public static Utils.ArtifactType UNKNOWNTYPE = new Utils.ArtifactType("Unknown");

	
	public static class ConfigBlockProcessor extends AbstractBlockProcessor {
		
		public final static String CONFIG = "config";
		
		public ConfigBlockProcessor(ProcessingContext ctx) {
			super(CONFIG, ctx);
		}
		
		@Override
		public void processBlock(BlockType block) {
			String configName = Utils.getFieldValue(block.getField(), "ConfigId").orElse("Config");
			DTOs.Process proc = ctx.getCurrentWFD();
			if (proc!= null) { // config comes at correct place
				proc.getConfigs().putIfAbsent(configName, new HashSet<PropertySchemaDTO>());
				ctx.setCurrentConfigName(configName);
				Optional<Statement> opStatement = Utils.getStatement(block.getStatementOrValue());
				opStatement.ifPresent(stmt -> {
					ctx.processStatement(stmt);
				});			
				ctx.setCurrentConfigName(null);
			} else {
				log.info("Ignoring config block outside process context");
			}
		}
	}
		
	public static class ConfigPropertiesProcessor extends AbstractStatementProcessor {

		public ConfigPropertiesProcessor(ProcessingContext ctx) {
			super(ctx);			
		}
		
		@Override
		public boolean willAccept(Statement stmt) {
			return stmt.getName().equals("Properties");
		}
		
		@Override
		public void processStatement(Statement stmt) {
			List<BlockType> blocks = Utils.flattenByNext(stmt.getBlock());
			blocks.stream().forEach(block -> {
				log.info("  |-> "+block.getType());
				ctx.processBlock(block);				
			});
		}		
	}
	
	public static class ConfigPropertyBlockProcessor extends AbstractBlockProcessor {
			
			public final static String CONFIGPROP = "configproperty";
			
			public ConfigPropertyBlockProcessor(ProcessingContext ctx) {
				super(CONFIGPROP, ctx);
			}
			
			@Override
			public void processBlock(BlockType block) {
				String propName = Utils.getFieldValue(block.getField(), "propertyName").orElse("Config");
				String propType = Utils.getFieldValue(block.getField(), "propertyType").orElse("BOOLEAN");
				String cardinality = Utils.getFieldValue(block.getField(), "cardinality").orElse("SINGLE");
				String isRepairable = Utils.getFieldValue(block.getField(), "isRepairable").orElse("TRUE");
				String configName = ctx.getCurrentConfigName();
				if (configName != null) { // config property used inside of config context
					PropertySchemaDTO prop = new PropertySchemaDTO(propName, propType, cardinality);
					prop.setRepairable(Boolean.parseBoolean(isRepairable));
					ctx.getCurrentWFD().getConfigs().get(configName).add(prop);					
				} else {
					log.info("Ignoring config property block outside configuration context");
				}
			}
	}	
		
	
	public static class VarSetBlockProcessor extends AbstractBlockProcessor {

		public final static String VARIABLES_SET = "variables_set";

		public VarSetBlockProcessor(ProcessingContext ctx) {
			super(VARIABLES_SET, ctx);
		}

		@Override
		public void processBlock(BlockType block) {
			Utils.getField(block.getField(), "VAR").ifPresent(
					field -> { String varId = field.getId(); 
					Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent( 
									type -> { 									
										// we set the variable type (i.e., artifact type)
										ctx.getVarIndex().replace(varId, type);
										ctx.getVarId2Name().putIfAbsent(varId, field.getValue());
										// now  we also add this variable definition/setting to parent step only if no previous value set or of unknonw type
										// TODO: commented out for now, as this adds any variables defined anywhere to process input which is not intended
//										Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
//												td -> { 																
//													if (td.getExpectedInput().containsKey(field.getValue())) {												
//														td.getExpectedInput().replace(field.getValue(), UNKNOWNTYPE, type);
//													} else {
//														td.getExpectedInput().put(field.getValue(), type);
//													}
//												} );													
									} ) ); 
					} );				
		}						
	}



	public static class FunctionsBlockProcessor extends AbstractBlockProcessor {

		public final static String[] PROCEDURETYPES = new String[]{"procedures_defnoreturn", "procedures_defreturn"};

		public FunctionsBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(PROCEDURETYPES).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType block) {						
			String funcName = Utils.getFieldValue(block.getField(), "NAME").orElse("Anonym");
			log.info("Processing: "+funcName);						
			DTOs.Process wfd = new DTOs.Process();
			wfd.setCode(funcName);
			if (block.getComment() != null && block.getComment().getValue() != null)
				wfd.setDescription(block.getComment().getValue());
			ctx.getTaskHierarchy().push(wfd);
			ctx.setCurrentWFD(wfd);
			// TODO:			wfd.getExpectedInput().add();
			ctx.getFlows().put(funcName, wfd);
			DTOs.DecisionNode dnd = new DTOs.DecisionNode(); 
			dnd.setCode(ctx.produceId());
			dnd.setInflowType(DecisionNodeDefinition.InFlowType.SEQ);
			wfd.getDns().add(dnd);
			ctx.getDndStack().push(dnd);
			// prepare Closing DND			
			DTOs.DecisionNode closingDND = new DTOs.DecisionNode();	
			closingDND.setCode(ctx.produceId());
			wfd.getDns().add(closingDND);
			ctx.getClosingDND().push(closingDND);			


			Optional<Statement> opStatement = Utils.getStatement(block.getStatementOrValue());
			opStatement.ifPresent(stmt -> {
				ctx.processStatement(stmt);
			});		
			//clean up for next process/function
			ctx.setCurrentWFD(null);
			ctx.getClosingDND().clear();
			ctx.getDndStack().clear();
			ctx.getTaskHierarchy().clear();
			ctx.getCheckPoints().clear();
			// DOES NOT WORK!!! --> we need a assignment with ArtifactType anyway to define the variable type
			//			Object mutation = block.getMutation();
			//			if (mutation != null && mutation instanceof Element) { // we have input parameters, process input parameters 
			//				Element mutEl = (Element)mutation;
			//				System.out.println(mutEl.getChildNodes());
			//			}			
		}						
	}


	public static class ParallelBlockProcessor extends AbstractBlockProcessor {
		public final static String PARALLEL_TYPE = "parallelexecution";

		public ParallelBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(PARALLEL_TYPE).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType paraEx) {

			long size = ctx.getSequenceLengthHierarchy().pop(); //remove sequence length
			size--;
			ctx.getSequenceLengthHierarchy().push(size);// reduce by one and place back on stack

			if (size > 0) {//this is not the last item in the sequence, we thus need to create a new closing DND				
				DTOs.DecisionNode closingDND = new DTOs.DecisionNode(); 
				closingDND.setCode(ctx.produceId());
				getInflowType(paraEx).ifPresent(bType -> closingDND.setInflowType(bType));	
				ctx.getCurrentWFD().getDns().add(closingDND);
				ctx.getClosingDND().push(closingDND);				
			} else {				
				getInflowType(paraEx).ifPresent(bType -> ctx.getClosingDND().peek().setInflowType(bType));
				ctx.getClosingDND().push(ctx.getClosingDND().peek()); //we push the same element again, so that at the end we can remove one without checking
			}				
			//part of the parallel type, now lets look inside eachof them
			if (Utils.getStatements(paraEx.getStatementOrValue()).stream().count() > 0) {
				Utils.getStatements(paraEx.getStatementOrValue()).stream()
					.forEach(branch -> ctx.processStatement(branch));
			} else { // if there is nothing in the parablock yet, then put in a dummy
				produceDummyStep();
			}
			// push Closing DND onto stack 			
			ctx.getDndStack().push(ctx.getClosingDND().pop());
		}
		
		private void produceDummyStep() {
			// we simulate we are a block
			new StepBlockProcessor(this.ctx).processBlock(ProcessingContext.getDummyStepBlock());
		}

		public static Optional<InFlowType> getInflowType (BlockType paraEx) {
			return Utils.getFieldValue(paraEx.getField(), "InFlowType").map(strType -> InFlowType.valueOf(strType)); 
		}
	}

//	public static class CheckpointBlockProcessor extends AbstractBlockProcessor {
//		public final static String CHECKPOINT_TYPE = "checkpoint";
//
//		public CheckpointBlockProcessor(ProcessingContext ctx) {
//			super(Arrays.asList(CHECKPOINT_TYPE).parallelStream().collect(Collectors.toSet()), ctx);
//		}
//
//		@Override
//		public void processBlock(BlockType cp) {
//			ctx.getCheckPoints().push(cp);
//		}
//	}

	public static class ParaBranchProcessor extends AbstractStatementProcessor {

		List<BlockType> preStepBlocks = new LinkedList<BlockType>();
		List<BlockType> postStepBlocks = new LinkedList<BlockType>();
		
		public ParaBranchProcessor(ProcessingContext ctx) {
			super(ctx);			
		}

		@Override
		public boolean willAccept(Statement stmt) {
			return stmt.getName().startsWith("DO") || stmt.getName().equals("STACK");
		}

		@Override
		public void processStatement(Statement stmt) {
			preStepBlocks.clear();
			postStepBlocks.clear();
			
			List<BlockType> blocks = Utils.flattenByNext(stmt.getBlock());
			DTOs.DecisionNode openingDND = ctx.getDndStack().peek();
			//List<StepDefinition> newTDs = new LinkedList<>();
			long expTasks = blocks.stream()
					.filter(block -> isExecutionBlock(block)) 
					.count();
			if (expTasks <= 0) {
				ctx.getSequenceLengthHierarchy().push(expTasks);
				// we have an empty parallel branch, so the outer might expect a step/task to be produced here
				// additionally, we might be the last one from the outer, 
				// for simplicity, we just generate a dummy/noop step
				log.info("  |-> empty para, adding dummy ");
				ctx.processBlock(ProcessingContext.getDummyStepBlock());
			} else {
				// split into three parts: preExe, exe, and postExe
				reduceToPreExeBlocks(blocks);
				preStepBlocks.stream().forEach(block -> {
					log.info("  |-> "+block.getType());
					ctx.processBlock(block);				
				});
				produceProcInput();
				
				// check if the first block of step, noop step and parallelexe is a parallel execution, as we then need to insert a dummy step
				boolean needToInsertLeadingDummy = isNeedToInsertLeadingDummy(blocks);
				// check if the last block is a parallel, then also we need to insert a dummy
				boolean needToInsertTrailingDummy = isNeedToInsertTrailingDummy(blocks);
				if (needToInsertLeadingDummy) expTasks++;
				if (needToInsertTrailingDummy) expTasks++;
				ctx.getSequenceLengthHierarchy().push(expTasks);
				if (needToInsertLeadingDummy) {
					ctx.processBlock(ProcessingContext.getDummyStepBlock());
				}
				// regardless of noop step added with dnd or not, we now process the rest
				blocks.stream().forEach(block -> {
					log.info("  |-> "+block.getType());
					ctx.processBlock(block);				
				});
				if (needToInsertTrailingDummy) {
					ctx.processBlock(ProcessingContext.getDummyStepBlock());
				// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
				}
				// now remove all DND from stack as we have processed all steps within this parallel branch/sequence
				while (ctx.getDndStack().peek() != openingDND) {
					ctx.getDndStack().pop(); //pops all intermediary DNDs between opening and closing DND
				}
				reduceToPostExeBlocks(blocks);
				produceProcOutput();
			}
			ctx.getSequenceLengthHierarchy().pop(); // we are done with the sequence, so we remove the entry
			
		}
	
		private void reduceToPreExeBlocks(List<BlockType> blocks) {
			if (blocks.isEmpty()) return;
			while (!isExecutionBlock(blocks.get(0))) {
				preStepBlocks.add(blocks.remove(0));
			}
		}
		
		private void reduceToPostExeBlocks(List<BlockType> blocks) {
			if (blocks.isEmpty()) return;
			Collections.reverse(blocks);
			while (!isExecutionBlock(blocks.get(0))) {
				postStepBlocks.add(blocks.remove(0));
			}
			Collections.reverse(blocks);//back to original order
			Collections.reverse(postStepBlocks);
		}
		
		private void produceProcInput() {
			preStepBlocks.stream()
			.filter(block -> block.getType().equals("variables_set"))
			.forEach(varBlock -> { 
				Map.Entry<String,String> idVal = DataBlockProcessors.getVarIdAndNameFromBlock(varBlock);
				Utils.ArtifactType type= ctx.getVarIndex().get(idVal.getKey());
				Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
						td -> { 																
							if (td.getInput().containsKey(idVal.getValue())) {												
								td.getInput().replace(idVal.getValue(), UNKNOWNTYPE.getArtifactType(), type.getArtifactType());
							} else {
								td.getInput().put(idVal.getValue(), type.getArtifactType());
							}
							ctx.getVarId2outputRole().put(idVal.getKey(), new AbstractMap.SimpleEntry<String,String>(td.getCode(),idVal.getValue()));
						} );	
			});
		}
		
		
		private void produceProcOutput() {
			postStepBlocks.stream()
			.filter(block -> block.getType().equals("artuse"))
			.forEach(artBlock ->  Utils.getFirstValue(artBlock.getStatementOrValue()).ifPresent(
					value -> { 
						Map.Entry<String,String> idVal = DataBlockProcessors.getVarIdAndNameFromBlock(value.getBlock());
						Utils.ArtifactType type= ctx.getVarIndex().get(idVal.getKey());
						Optional.ofNullable(ctx.getTaskHierarchy().peek()).ifPresent(
								td -> { 																
									if (td.getOutput().containsKey(idVal.getValue())) {												
										td.getOutput().replace(idVal.getValue(), UNKNOWNTYPE.getArtifactType(), type.getArtifactType());
									} else {
										td.getOutput().put(idVal.getValue(), type.getArtifactType());
									}
									
									ctx.getOutputRoleFor(idVal.getKey())
									.ifPresent( 
										from -> {
											DecisionNode prevDnd = ctx.getClosingDND().get(0); //FIXME: check if that is always correct!
											prevDnd.getMapping().add(new Mapping(from.getKey(), from.getValue(), td.getCode(), from.getValue()));
										});
									
								} );	
					}));
		}
		
		private boolean isNeedToInsertLeadingDummy(List<BlockType> blocks) {
			return blocks.stream()
					.filter(block -> isExecutionBlock(block))
					.findFirst()
					.map( b -> { if (b.getType().equals("parallelexecution")) {
								// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
								return b;
							} else return null;
							}).isPresent();
		}
		
		private boolean isNeedToInsertTrailingDummy(List<BlockType> blocks) {
			return blocks.stream()
					.filter(block -> isExecutionBlock(block))
					.reduce(($, current) -> current) // retains the last, only efficien for short lists like here
					.map( b -> { if (b.getType().equals("parallelexecution")) {
								// need to add noop step incl dnd, situation is exactly as if we had a noop step between the parallelexecution statements
								return b;
							} else return null;
							}).isPresent();
		}
		
	}

	public static class StepBlockProcessor extends AbstractBlockProcessor {
		public final static String[] STEPTYPES = new String[]{"step", "noopstep"};

		public StepBlockProcessor(ProcessingContext ctx) {
			super(Arrays.asList(STEPTYPES).parallelStream().collect(Collectors.toSet()), ctx);
		}

		@Override
		public void processBlock(BlockType step) {
			if (ctx.getCurrentWFD() == null) {
				log.warn("Skipping Step Block definition without enclosing process "+step.getId());
				return;
			}
			DTOs.Step td = null;
			DTOs.DecisionNode inDND = ctx.getDndStack().peek(); //previous DND
			// Now find subsequent DND:
			DTOs.DecisionNode outDND;
			long size = ctx.getSequenceLengthHierarchy().pop(); //remove sequence length
			size--;
			ctx.getSequenceLengthHierarchy().push(size);// reduce by one and place back on stack
			if (size > 0) {
				DTOs.DecisionNode dndNext = new DTOs.DecisionNode();
				dndNext.setCode(ctx.produceId());
				ctx.getCurrentWFD().getDns().add(dndNext);
				ctx.getDndStack().push(dndNext);
				outDND = dndNext;
			} else { // for last one, use closing DND
				DTOs.DecisionNode dndClosing = ctx.getClosingDND().peek();
				outDND = dndClosing;		
			}
			
			// now put together StepDefinition object
			int stepOrderIndex = ctx.getCurrentWFD().getSteps().size()+1;
			if (step.getType().equals("step")) {
				String name = step.getField().get(0).getValue();
				td = new DTOs.Step(); //name, ctx.getCurrentWFD(), hasPreCondition, hasPostCondition, inDND, outDND);		
				td.setSpecOrderIndex(stepOrderIndex);
				td.setCode(name);
				td.setInDNDid(inDND.getCode());
				td.setOutDNDid(outDND.getCode());
				if (step.getComment() != null && step.getComment().getValue() != null)
					td.setDescription(step.getComment().getValue());
				// pre/post etc conditions are collected separately and later added
				ctx.setCurrentWFT(td);
				List<Statement> stepContent = Utils.getStatements(step.getStatementOrValue());
				processStepContent(stepContent, td, inDND);
			}
			// we disallow just plain noopsteps as we need pre and postconditions for them to know when something should not be done, or how to know when they have beend chosen not to be done
			//but we use noop internally for maintaining a wellformed process structure, hence, we need to include such a noop step and also support it in the process enginge
			else if (step.getType().equals("noopstep")) {
				String name = "NoOpStep"+ctx.getAndIncrNoopCounter();
				td = new DTOs.Step(); //(name, ctx.getCurrentWFD(), hasPreCondition, hasPostCondition, inDND, outDND);		
				td.setSpecOrderIndex(stepOrderIndex);
				td.setCode(name);
				td.setInDNDid(inDND.getCode());
				td.setOutDNDid(outDND.getCode());
				ctx.setCurrentWFT(td);
			} //todo if another parallel or function
			ctx.getCurrentWFD().getSteps().add(td);
			
			ctx.setCurrentWFT(null);
		}

//		private Set<String> peekStepTransitionContent(List<Statement> content) {
//			return content.stream().filter(stmt -> stmt.getName().equals("Transitions"))
//			.flatMap(stmt -> Utils.flattenByNext(stmt.getBlock()).stream())
//			.filter(block -> block.getType().equals("transition"))
//			.map(trans -> Utils.getFieldValue(trans.getField(), "State") )
//			.filter(optStr -> optStr.isPresent())
//			.map(optStr -> optStr.get())
//			.collect(Collectors.toSet());
//		}
		
		private void processStepContent(List<Statement> content, DTOs.Step td, DTOs.DecisionNode prevDnd) {
			// look for Input, Transitions, and output
			content.stream().filter(stmt -> stmt.getName().equals("Input"))
			.flatMap(inputStmt -> Utils.flattenByNext(inputStmt.getBlock()).stream())
			.filter(block -> block.getType().equals("artuse"))
			.forEach(block -> Utils.getFieldValue(block.getField(), "roletext").ifPresent(
						ignored ->	Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx)
							.ifPresent(artType -> { 
										// if this is a var created somewhere else, let map there - we require it to be mapped somewhere
										DataBlockProcessors.getVariableIdFromBlock(value.getBlock())
											.ifPresent(varId -> ctx.getOutputRoleFor(varId)
												.ifPresent( 
													from -> {
														prevDnd.getMapping().add(new Mapping(from.getKey(), from.getValue(), td.getCode(), from.getValue()));
														td.getInput().put(from.getValue(), artType.getArtifactType()); // add to step definition
													}));
									}) ) )						 
					);
						
			content.stream().filter(stmt -> stmt.getName().equals("Transitions"))
				.flatMap(stmt -> Utils.flattenByNext(stmt.getBlock()).stream())
				.filter(block -> block.getType().equals("transition"))
				.forEach(trans -> ctx.processBlock(trans));
			
			content.stream().filter(stmt -> stmt.getName().equals("Datamappings"))
			.flatMap(stmt -> Utils.flattenByNext(stmt.getBlock()).stream())
			.filter(block -> block.getType().equals("datamapping"))
			.forEach(dm -> ctx.processBlock(dm));

			content.stream().filter(stmt -> stmt.getName().equals("QA"))
			.flatMap(stmt -> Utils.flattenByNext(stmt.getBlock()).stream())
			.filter(block -> block.getType().equals("qacheck"))
			.forEach(dm -> ctx.processBlock(dm));
			
			content.stream().filter(stmt -> stmt.getName().equals("Output"))
			.flatMap(inputStmt -> Utils.flattenByNext(inputStmt.getBlock()).stream())
			.map(block -> { ctx.processBlock(block); return block; })
			.filter(block -> block.getType().equals("artuse"))
			.forEach(block -> Utils.getFieldValue(block.getField(), "roletext").ifPresent(
					ignored ->	Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
							value -> DataBlockProcessors.getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent(
									artType ->  
									// only if that role is a variable, which gets exposed, can we add that		
									DataBlockProcessors.getVariableIdFromBlock(value.getBlock()).ifPresent(varId -> {
											String name = ctx.getVarId2Name().get(varId);
											assert(name != null);
											ctx.getVarId2outputRole().put(varId, new AbstractMap.SimpleEntry<String,String>(td.getCode(),name));
											td.getOutput().put(name, artType.getArtifactType());
										}) ) ) )						 
					);
		}	
	}
	
	public static boolean isExecutionBlock(BlockType block) {
		return block.getType().equals("step") || block.getType().equals("noopstep") || block.getType().equals("parallelexecution"); //todo support subprocess steps
	}

}
