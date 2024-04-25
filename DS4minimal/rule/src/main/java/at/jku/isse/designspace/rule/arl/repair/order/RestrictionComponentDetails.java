package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.HashMap;
import java.util.Map;

public class RestrictionComponentDetails {
	String restText;
	Map<String, Integer> restComponents;
	
	public RestrictionComponentDetails(String text,Map<String, Integer> mp ) {
		this.restText=text;
		this.restComponents=mp;
	}
	public RestrictionComponentDetails() {
		this.restText="";
		this.restComponents=new HashMap<>();
	}
	
	public Map<String, Integer> getRestComponents() {
		return restComponents;
	}

	public void setRestComponents(Map<String, Integer> restComponents) {
		this.restComponents = restComponents;
	}

	public String getRestText() {
		return restText;
	}

	public void setRestText(String restText) {
		this.restText = restText;
	}
}
