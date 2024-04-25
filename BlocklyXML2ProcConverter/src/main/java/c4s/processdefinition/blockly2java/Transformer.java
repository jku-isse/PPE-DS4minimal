package c4s.processdefinition.blockly2java;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import at.jku.isse.passiveprocessengine.definition.serialization.DTOs.Constraint;
import at.jku.isse.passiveprocessengine.instance.StepLifecycle.Conditions;
import c4s.processdefinition.blockly2java.processors.BlockProcessors;
import c4s.processdefinition.blockly2java.processors.DataBlockProcessors;
import c4s.processdefinition.blockly2java.processors.DatamappingBlockProcessor;
import c4s.processdefinition.blockly2java.processors.QACheckBlockProcessor;
import c4s.processdefinition.blockly2java.processors.TransitionBlockProcessor;
import c4s.processdefinition.blockly2java.processors.TransitionBlockProcessor.ArtifactOrigin;
import c4s.processdefinition.blockly2java.processors.Utils.ArtSource;
import https.developers_google_com.blockly.xml.Xml;

public class Transformer {
	
	private static Logger log = LogManager.getLogger(Transformer.class);
	private ProcessingContext ctx = new ProcessingContext();
	
	public Transformer() {
		ctx.addBlocksProcessor(new BlockProcessors.ConfigBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.ConfigPropertyBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.VarSetBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.FunctionsBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.ParallelBlockProcessor(ctx));
		ctx.addBlocksProcessor(new BlockProcessors.StepBlockProcessor(ctx));
		ctx.addBlocksProcessor(new DataBlockProcessors.StreamBlockProcessor(ctx));
		ctx.addBlocksProcessor(new QACheckBlockProcessor(ctx));
		ctx.addBlocksProcessor(new TransitionBlockProcessor(ctx));
		ctx.addBlocksProcessor(new DatamappingBlockProcessor(ctx));
		ctx.addStatementProcessor(new BlockProcessors.ParaBranchProcessor(ctx));
		ctx.addStatementProcessor(new BlockProcessors.ConfigPropertiesProcessor(ctx));
	}
		
	
	public List<DTOs.Process> toProcessDefinition(Xml root) {						
				
		root.getVariables().getVariable().forEach(var -> ctx.getVarIndex().put(var.getId(), BlockProcessors.UNKNOWNTYPE));				
		root.getBlock().stream()
			.forEach(block -> ctx.processBlock(block));			
			// all highlevel subroutines/procedures/functions etc
			//TODO: distinguish between processes and functions used in quality assurance constraints, etc			
		// only return workflows with at least one task.
		
		
		return ctx.getFlows().values().stream()
				.filter(wfd -> wfd.getSteps().size() > 0)
				.peek(wfd -> determineQAConstraintInputLocation(wfd))
				.peek(wfd -> determineTransitionInputLocation(wfd))
				.peek(wfd -> determineFirstOccurranceOfArtifacts(wfd)) // determine first occurrence of artifact	
				.peek(wfd -> determineConditions(wfd))
				.peek(wfd -> determineQAs(wfd))
				.collect(Collectors.toList());	
	}
	
	private void determineQAConstraintInputLocation(DTOs.Process wfd) {
		ctx.getTask2constraint().entrySet().stream()
			.forEach(entry -> { entry.getValue().stream().forEach(spec -> {
						DTOs.Step td = wfd.getStepByCode(spec.taskId);
						if (td == null) return;
//						spec.var2type.keySet().forEach(var -> {
//							ArtSource artS = null;
//							if (td.getOutput().keySet().contains(var))
//								artS = ArtSource.stepOut;
//							else if (td.getInput().keySet().contains(var))
//								artS = ArtSource.stepIn;
//							else if (wfd.getInput().keySet().contains(var))
//								artS = ArtSource.procIn;
//							if (artS != null)
//								spec.var2source.put(var, artS);
//						});
					}); 				
				});
	}
	
	private void determineTransitionInputLocation(DTOs.Process wfd) {
		ctx.getTask2transition().entrySet().stream()
			.forEach(entry -> { entry.getValue().stream().forEach(spec -> {
						DTOs.Step td = wfd.getStepByCode(spec.taskId);
						if (td == null) return;
						Set<String> paramNames = new HashSet<>();
						paramNames.addAll(td.getInput().keySet());
						paramNames.addAll(td.getOutput().keySet());
						paramNames.forEach(var -> {
							ArtSource artS = null;
							if (td.getOutput().keySet().contains(var))
								artS = ArtSource.stepOut;
							else if (td.getInput().keySet().contains(var))
								artS = ArtSource.stepIn;
							else if (wfd.getInput().keySet().contains(var))
								artS = ArtSource.procIn;
							if (artS != null)
								spec.var2source.put(var, artS);
						});
					}); 				
				});
	}
	
	private void determineFirstOccurranceOfArtifacts(DTOs.Process wfd) {
		ctx.getTask2transition().entrySet().stream()
		.forEach(entry -> { entry.getValue().stream().forEach(spec -> {
					DTOs.Step td = wfd.getStepByCode(spec.taskId);
					if (td == null) return;
					spec.var2source.entrySet().forEach(entry2 -> {
						spec.varFirstSource.add(findSource(wfd, td, entry2.getKey(), entry2.getValue()));					
					});
				}); 				
			});
	}
	
	private void determineQAs(DTOs.Process wfd) {
		ctx.getTask2constraint().entrySet().stream()
		.forEach(entry -> { 
			DTOs.Step td = wfd.getStepByCode(entry.getKey());
			assert (td != null);
			entry.getValue().stream().forEach(constr -> {
				Constraint qac = new Constraint(constr.condition);
				qac.setCode(constr.checkId);
				qac.setDescription(constr.checkDesc);
				//qac.setArlRule(constr.condition);
				qac.setSpecOrderIndex(constr.order);
				td.getQaConstraints().add(qac);
			});
			
			
		});
	}
	
	private void determineConditions(DTOs.Process wfd) {
		ctx.getTask2transition().entrySet().stream()
		.forEach(entry -> { 
			DTOs.Step td = wfd.getStepByCode(entry.getKey());
			assert (td != null);
			for (Conditions cond : Conditions.values()) {
				entry.getValue().stream()
				.filter(spec -> spec.getStateId().equals(cond))						
				.forEach(spec -> { 
					Constraint constr = new Constraint(spec.getCondition());
					constr.setDescription(spec.getDescription());
					constr.setOverridable(spec.isOverridable());
					constr.setSpecOrderIndex(spec.getSpecOrder());
					constr.setCode(spec.getCheckId());
					td.getConditions().computeIfAbsent(cond, k -> new LinkedList<>()).add(constr); } );
				
				switch(cond) {
				case ACTIVATION: //fallthrough
				case CANCELATION:						
					break;
				case POSTCONDITION:
					if (entry.getValue().stream().filter(spec -> spec.getStateId().equals(cond)).findAny().isEmpty()) {
						// if there is no output, error as we won't know when this task is complete!
						if (td.getOutput().isEmpty()) log.warn("Neither completion/postcondition nor output available, process cannot be executed.");
						else {
							// if there is no condition, check if there is some output, ensure that each output is at least size 1, otherwise, custom rule required
							AtomicInteger order = new AtomicInteger(0);
							td.getOutput().keySet().stream()
									.map(param -> "self.out_"+param+"->size() > 0")
									.forEach(rule -> {
										Constraint constr = new Constraint(rule);
										constr.setDescription("Autogenerated default postcondition");
										constr.setOverridable(true);
										constr.setSpecOrderIndex(order.getAndIncrement());
										constr.setCode(cond.toString()+constr.getSpecOrderIndex());
										td.getConditions().computeIfAbsent(cond, k -> new LinkedList<>()).add(constr);
									});							 
						}
					}																			
					break;
				case PRECONDITION:
					if (entry.getValue().stream().filter(spec -> spec.getStateId().equals(cond)).findAny().isEmpty()) {
						// if there is no input, error as we won't know when this task should start!
						if (td.getInput().isEmpty()) log.warn("Neither enabling/precondition nor input available, process cannot be executed.");
						else {
							// if there is no condition, check if there is some input, ensure that each input is at least size 1, otherwise, custom rule required
							AtomicInteger order = new AtomicInteger(0);
							td.getInput().keySet().stream()
									.map(param -> "self.in_"+param+"->size() > 0")
									.forEach(rule -> {
										Constraint constr = new Constraint(rule);										
										constr.setDescription("Autogenerated default precondition");
										constr.setOverridable(true);
										constr.setSpecOrderIndex(order.getAndIncrement());
										constr.setCode(cond.toString()+constr.getSpecOrderIndex());
										td.getConditions().computeIfAbsent(cond, k -> new LinkedList<>()).add(constr);
									});								
						}
					}												
					break;
				}
			}			 				
			});
	}
	
	public static ArtifactOrigin findSource(DTOs.Process wfd, DTOs.Step td, String localRoleId, ArtSource currentSource) {
		if (currentSource.equals(ArtSource.procIn)) {
			// for each var id, backtrack to last use: if process, then fine/done
			return new ArtifactOrigin(localRoleId, wfd.getCode(), localRoleId, currentSource);
		} else if (currentSource.equals(ArtSource.stepIn)) {
			//, if input, then check DNI mapping
			Optional<DTOs.Mapping> optMap = wfd.getInDNof(td).getMapping().stream()
				.filter(mapd -> mapd.getToStep().equals(td.getCode()) && // match this task as the target task 
								mapd.getToParam().equals(localRoleId)) // match input var
				.findAny();
			if (optMap.isPresent()) {
					DTOs.Mapping mapd = optMap.get(); // TODO: there might be multiple mappings, e.g, in OR or XOR branch, not supported yet
					String prevStepId = mapd.getFromStep();
					String idInStep =  mapd.getFromParam();
					// check if step or process
					if (prevStepId.equals(wfd.getCode())) { 
						 return new ArtifactOrigin(localRoleId, wfd.getCode(), idInStep, ArtSource.procIn);
					} else { //must be  a step
						//stepId, in or output, and role
						ArtifactOrigin sourceT = findSourceOfArtifact(wfd, prevStepId, idInStep);
						if (sourceT != null) {		
							sourceT.localRoleId=localRoleId;
							return sourceT;											
						} else {
							 return new ArtifactOrigin(localRoleId, td.getCode(), localRoleId, ArtSource.stepIn);
						}
						// else: there is no way to prematurely detect this transition based on this artifact										
					}
				}
		} else if (currentSource.equals(ArtSource.stepOut)) {
			// if output then we need to parse the condition that produces the output, HUGE TODO!
			// thus not possible to prematurely detect this currently	
			return new ArtifactOrigin(localRoleId, td.getCode(), localRoleId, currentSource);
		}
		return new ArtifactOrigin(localRoleId, wfd.getCode(), localRoleId, currentSource);
	}
	
	private static ArtifactOrigin findSourceOfArtifact(DTOs.Process wfd, String taskId, String roleId) {
		// this is only called when the task is not the process
		DTOs.Step td = wfd.getStepByCode(taskId);
		if (td.getOutput().entrySet().stream().anyMatch(entry -> entry.getKey().equals(roleId)) ) {
			// recall: we cant process how output is derived from input yet
			return new ArtifactOrigin(null, taskId, roleId, ArtSource.stepOut);
		}			
		else if ( td.getInput().entrySet().stream().anyMatch(entry -> entry.getKey().equals(roleId)) ) {
			Optional<DTOs.Mapping> optmd = wfd.getInDNof(td).getMapping().stream()
			.filter(mapd -> mapd.getToStep().equals(td.getCode()) && // match this task as the target task 
							mapd.getToParam().equals(roleId)) // match input var
			.findAny();
			if (optmd.isEmpty()) {
				log.warn(String.format("Cannot determine source of %s %s as we cannot find how this input is obtained", taskId, roleId));
				// so we just return the input source
				return new ArtifactOrigin(null, taskId, roleId, ArtSource.stepIn);
			} else {
				DTOs.Mapping mapd = optmd.get();
				String prevStepId = mapd.getFromStep();
				String idInStep =  mapd.getFromParam();
				// check if step or process
				if (prevStepId.equals(wfd.getCode())) { 
					return new ArtifactOrigin(null, prevStepId, idInStep, ArtSource.procIn);
				} else { //must be  a step
					//stepId, in or output, and role
					ArtifactOrigin parent = findSourceOfArtifact(wfd, prevStepId, idInStep);
					if (parent == null) { // we just return this input
						return new ArtifactOrigin(null, taskId, roleId, ArtSource.stepIn);
					} 
					return parent;
				}
			}			
		} else {
			log.warn(String.format("Inconsistent datamapping in %s to nonexisting artifact in or output %s", taskId, roleId));
			return null;
		}
	}
	
	
	
	public ProcessingContext getContext() {
		return ctx;
	}
	
	
}
