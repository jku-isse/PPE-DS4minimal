package c4s.processdefinition.blockly2java.processors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import https.developers_google_com.blockly.xml.BlockType;
import https.developers_google_com.blockly.xml.Field;
import https.developers_google_com.blockly.xml.Statement;
import https.developers_google_com.blockly.xml.Value;

public class Utils {

	private static Logger log = LogManager.getLogger(Utils.class);
	
	public static class ArtifactType{
		String type;
		public ArtifactType(String type) {
			this.type = type;
		}
		
		public String getArtifactType() {
			return type;
		}
	}
	
	public enum ArtSource {stepOut, stepIn, procIn, procOut};
	
	public static Optional<String> getFieldValue(List<Field> fields, String fieldName) {
		return fields.stream()
				.filter(field -> field.getName().equals(fieldName))
				.findAny()
				.map(field -> field.getValue());
	}
	
	public static Optional<Field> getField(List<Field> fields, String fieldName) {
		return fields.stream()
				.filter(field -> field.getName().equals(fieldName))
				.findAny();				
	}
	

	public static LinkedList<BlockType> flattenByNext(BlockType block) {
			if (block.getNext() != null) {
				LinkedList<BlockType> prevList = flattenByNext(block.getNext().getBlock());
				prevList.push(block);
				return prevList;
			} else {
				LinkedList<BlockType> list = new LinkedList<BlockType>();
				list.push(block);
				return list;
			}							
	}
	
	public static List<Statement> getStatements(List<Object> list) {
		return list.stream()
		  .filter(Statement.class::isInstance)
		    .map (Statement.class::cast)		    
		    .collect(Collectors.toList());		
	}
	
	public static Optional<Statement> getStatement(List<Object> list) {
		return list.stream()
		  .filter(Statement.class::isInstance)
		    .map (Statement.class::cast)		    
		    .findAny();		
	}
	
	public static Optional<Value> getValueByName(List<Object> list, String name) {
		return list.stream()
				  .filter(Value.class::isInstance)
				    .map (Value.class::cast)		    
				    .filter(value -> value.getName().equals(name))
				    .findFirst();		
	}
	
	public static Optional<Value> getFirstValue(List<Object> list) {
		return list.stream()
				  .filter(Value.class::isInstance)
				    .map (Value.class::cast)		    
				    .findFirst();		
	}
	
	public static List<Value> getValues(List<Object> list) {
		return list.stream()
		  .filter(Value.class::isInstance)
		    .map (Value.class::cast)		    
		    .collect(Collectors.toList());		
	}
	
	public static List<BlockType> retrieveIterative(Value entryPoint, String blockType) {
		BlockType block = entryPoint.getBlock();
		if (block != null) {
			List<BlockType> list = getValues(block.getStatementOrValue()).stream()
				.flatMap(value -> retrieveIterative(value, blockType).stream())
				.filter(childBlock -> childBlock.getType().equals(blockType))
				.collect(Collectors.toList());
			if (block.getType().equals(blockType))
				list.add(0, block);
			return list;
		} else {
			return Collections.emptyList();
		}
	}
}
