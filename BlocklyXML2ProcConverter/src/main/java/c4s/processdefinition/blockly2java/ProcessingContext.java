package c4s.processdefinition.blockly2java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.isse.passiveprocessengine.definition.serialization.DTOs;
import c4s.processdefinition.blockly2java.processors.AbstractBlockProcessor;
import c4s.processdefinition.blockly2java.processors.AbstractStatementProcessor;
import c4s.processdefinition.blockly2java.processors.QACheckBlockProcessor;
import c4s.processdefinition.blockly2java.processors.TransitionBlockProcessor;
import c4s.processdefinition.blockly2java.processors.Utils;
import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Statement;

public class ProcessingContext {
	
	private static Logger log = LogManager.getLogger(ProcessingContext.class);
	private HashMap<String, DTOs.Process> flows = new HashMap<String, DTOs.Process>();	
	private Set<AbstractBlockProcessor> blockProcessors = new HashSet<>();
	private Set<AbstractStatementProcessor> stmtProcessors = new HashSet<>();
	
	private int counter = 0;		
	private DTOs.Process currentWFD = null;
	private String currentConfigName;
	private DTOs.Step currentWFT = null;
	private LinkedList<DTOs.DecisionNode> closingDND = new LinkedList<>();
	private LinkedList<DTOs.DecisionNode> dndStack = new LinkedList<>();
	private LinkedList<Long> sequenceLengthHierarchy = new LinkedList<>(); //within a parallel/sequence
	
	private int noopCount = 0;
	private LinkedList<DTOs.Step> taskHierarchy = new LinkedList<>();
	
	private HashMap<String, Utils.ArtifactType> varIndex = new HashMap<String, Utils.ArtifactType>(); // tracks which var is of which type			
	private HashMap<String, Map.Entry<String,String>> varId2outputRole = new HashMap<>();
	private HashMap<String, String> varId2Name = new HashMap<String, String>(); // tracks name of each var	
	
	private LinkedList<BlockType> checkPoints = new LinkedList<>();
	
	private HashMap<String, List<QACheckBlockProcessor.ConstraintSpec>> task2constraint = new HashMap<>();
	private HashMap<String, List<TransitionBlockProcessor.TransitionSpec>> task2transition = new HashMap<>();
	
	
	
	private static BlockType dummyBlock = null;
	public static BlockType getDummyStepBlock() {
		if (dummyBlock == null) {
			dummyBlock = new BlockType();
			dummyBlock.setType("noopstep");
		}
		return dummyBlock;
	}
	
	public LinkedList<BlockType> getCheckPoints() {
		return checkPoints;
	}

	public void setCurrentWFD(DTOs.Process currentWFD) {
		this.currentWFD = currentWFD;
	}
	
	public void setCurrentConfigName(String configName) {
		this.currentConfigName = configName;
	}
	
	public String getCurrentConfigName() {
		return currentConfigName;
	}
	
	public DTOs.Process getCurrentWFD() {
		return currentWFD;
	}
	public LinkedList<DTOs.DecisionNode> getDndStack() {
		return dndStack;
	}

	public HashMap<String, Utils.ArtifactType> getVarIndex() {
		return varIndex;
	}
	
	public String produceId() {
		return ""+counter++;
	}
	
	public void addBlocksProcessor(AbstractBlockProcessor processor) {
		blockProcessors.add(processor);
	}
	
	public void addStatementProcessor(AbstractStatementProcessor processor) {
		stmtProcessors.add(processor);
	}
	
	public void processBlock(BlockType block) {
		long count = blockProcessors.stream()
			.filter(processor -> processor.getSupportedTypes().contains(block.getType()))
			.map(processor -> { processor.processBlock(block); return block; })
			.count();
		if (count <= 0) {
			log.debug(String.format("No processor applied to block: %s %s ", block.getType(), block.getId()));
		}
	}
	
	public void processStatement(Statement stmt) {
		long count = stmtProcessors.stream()
			.filter(processor -> processor.willAccept(stmt))
			.map(processor -> { processor.processStatement(stmt); return stmt; })
			.count();
		if (count <= 0) {
			log.debug(String.format("No processor applied to statement: %s ", stmt.getName()));
		}
	}

	public HashMap<String, DTOs.Process> getFlows() {
		return flows;
	}

	public LinkedList<DTOs.DecisionNode> getClosingDND() {
		return closingDND;
	}

//	public void setCurrentClosingDND(DecisionNodeDefinition currentClosingDND) {
//		this.currentClosingDND = currentClosingDND;
//	}

	public LinkedList<DTOs.Step> getTaskHierarchy() {
		return taskHierarchy;
	}

	public LinkedList<Long> getSequenceLengthHierarchy() {
		return sequenceLengthHierarchy;
	}

	public int getAndIncrNoopCounter() {
		return noopCount++;
	}

	public HashMap<String, Map.Entry<String,String>> getVarId2outputRole() {
		return varId2outputRole;
	}
	
	public Optional<Map.Entry<String,String>> getOutputRoleFor(String varId) {
		return Optional.ofNullable(varId2outputRole.get(varId));
	}
	
	public HashMap<String, String> getVarId2Name() {
		return varId2Name;
	}

	public DTOs.Step getCurrentWFT() {
		return currentWFT;
	}

	public void setCurrentWFT(DTOs.Step currentWFT) {
		this.currentWFT = currentWFT;
	}

	public HashMap<String, List<QACheckBlockProcessor.ConstraintSpec>> getTask2constraint() {
		return task2constraint;
	}
	
	public HashMap<String, List<TransitionBlockProcessor.TransitionSpec>> getTask2transition() {
		return task2transition;
	}


}
