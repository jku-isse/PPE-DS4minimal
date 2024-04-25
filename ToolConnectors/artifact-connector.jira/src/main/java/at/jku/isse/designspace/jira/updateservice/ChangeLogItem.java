package at.jku.isse.designspace.jira.updateservice;

import java.sql.Timestamp;
import java.util.HashSet;

import at.jku.isse.designspace.artifactconnector.core.exceptions.PropertyNotFoundException;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.jira.service.IJiraService;

public abstract class ChangeLogItem implements Comparable<ChangeLogItem>, IChangeLogItem {

	public ChangeLogItem() {

	}

	protected String id;
	protected String field = "";
	protected String correspondingArtifactIdInSource;
	protected String correspondingArtifactId;
	protected String timeCreated;
	protected String artifactId;

	public abstract Instance applyChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException;

	public abstract Instance undoChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException;

	public abstract HashSet<String> getInvolvedArtifactIds();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	public String getCorrespondingArtifactId() {
		return correspondingArtifactId;
	}

	public void setCorrespondingArtifactId(String correspondingArtifactId) {
		this.correspondingArtifactId = correspondingArtifactId;
	}

	public String getCorrespondingArtifactIdInSource() {
		return correspondingArtifactIdInSource;
	}

	public void setCorrespondingArtifactIdInSource(String correspondingArtifactIdInSource) {
		this.correspondingArtifactIdInSource = correspondingArtifactIdInSource;
	}
	
	public Timestamp getTimestamp() {
		if(timeCreated==null) return null;
		return Timestamp.valueOf(timeCreated);
	}	
	
	@Override
	public int compareTo(ChangeLogItem item) {
		return 0;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}
