package at.jku.isse.designspace.jira.updateservice;

import java.util.HashSet;
import java.util.Optional;

import org.apache.commons.text.CaseUtils;

import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.artifactconnector.core.exceptions.PropertyNotFoundException;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.model.JiraSchemaConverter;
import at.jku.isse.designspace.jira.service.IJiraService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelationChangeLogItem extends ChangeLogItem {

	private static final HashSet<String> singularLinks = new HashSet<>() {{
		add("status");
		add("priority");
		add("issuetype");
		add("creator");
		add("assignee");
	}};
	
	protected boolean artifactIsSource;
	
	protected String fromId;
	protected String fromKey;
	protected String toKey;
	protected String toId;
	protected String destinationRole;
	protected String sourceRole;

	public boolean isArtifactIsSource() {
		return artifactIsSource;
	}

	public void setArtifactIsSource(boolean artifactIsSource) {
		this.artifactIsSource = artifactIsSource;
	}
	
	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getFromKey() {
		return fromKey;
	}

	public void setFromKey(String fromKey) {
		this.fromKey = fromKey;
	}

	public String getToKey() {
		return toKey;
	}

	public void setToKey(String toKey) {
		this.toKey = toKey;
	}

	public String getDestinationRole() {
		return destinationRole;
	}

	public void setDestinationRole(String destinationRole) {
		this.destinationRole = destinationRole;
	}

	public String getSourceRole() {
		return sourceRole;
	}

	public void setSourceRole(String sourceRole) {
		this.sourceRole = sourceRole;
	}
	
	@Override
	public Instance applyChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException {
		updateRelations(instance, jiraService, toId, toKey, fromId, fromKey);
		return instance;
	}

	@Override
	public Instance undoChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException {
		updateRelations(instance, jiraService, fromId, fromKey, toId, toKey);
		return instance;
	}

	//
	@Override
	public HashSet<String> getInvolvedArtifactIds() {
		//in case an issue is no longer a member of the database
		//there is no key stored anymore, the only thing left
		//then is the previous id, which serves as fromKey
		//or toKey in that special case
		HashSet<String> ids = new HashSet<String>();
		if(fromId!=null) ids.add(fromId);
		if(toId!=null) ids.add(toId);
		return ids;
	}

	private void updateRelations(Instance instance, IJiraService jiraService, String toId, String toKey, String fromId, String fromKey) throws PropertyNotFoundException {
		String field = getField() != null ? getField() : "link";
		Optional<InstanceType> instanceType;

		if (field.equals("link")) {
			instanceType = Optional.of(jiraService.getCurSchema());
		} else {
			instanceType = JiraSchemaConverter.resolveLabelToInstanceType(getField(), jiraService.getCurSchema());
		}

		if (getField().contains("subtask")) {
			Optional<Instance> changedInstance_ = jiraService.findInstance(getArtifactId());
			if (changedInstance_.isPresent() && isArtifactIsSource()) {
				Instance changedInstance = changedInstance_.get();
				//parent perspective
				if (changedInstance.hasProperty("subtasks")) {
					SetProperty<Instance> subtasks = changedInstance.getPropertyAsSet("subtasks");
					Optional<Instance> subtask;
					if (toId != null) {
						subtask = jiraService.findInstance(toId);
						if (subtask.isEmpty()) {
							try {
								jiraService.getArtifact(toId, IJiraService.JiraIdentifier.JiraIssueId, false);
							} catch (IdentiferFormatException e) {
								log.debug("JIRA_SERVICE: Could not fetch and map the Jira Issue with the key " + toKey + " !");
							}
						}
						subtasks.add(subtask.get());
						if (subtask.get().hasProperty("parent")) {
							subtask.get().getProperty("parent").set(changedInstance);
						}
					} else if (fromId != null) {
						subtask = jiraService.findInstance(fromId);
						if (subtask != null && subtask.get().hasProperty("parent")) {
							subtasks.remove(subtask);
							subtask.get().getProperty("parent").set(null);
						}
					}
				}
			}
		} else {
			if (field.equals("link") && instanceType.isPresent() && instanceType.get() == jiraService.getCurSchema()) {

				Optional<Instance> otherInstance_ = Optional.empty();
				Instance otherInstance = null;
				if (toId != null) {
					otherInstance_ = jiraService.findInstance(toId);
					if (otherInstance_.isEmpty()) {
						try {
							Optional<Instance> fetchedInstance = jiraService.getArtifact(toId, IJiraService.JiraIdentifier.JiraIssueId, false);
							if (fetchedInstance.isPresent()) {
								otherInstance = fetchedInstance.get();
							}
						} catch (IdentiferFormatException e) {
							log.debug("JIRA_SERVICE: Could not fetch and map the Jira Issue with the key " + toKey + " !");
						}
					} else {
						otherInstance = otherInstance_.get();
					}
				}

				SetProperty<Instance> incomingRelations = instance.getPropertyAsSet(JiraBaseElementType.LINKS_INCOMING);
				SetProperty<Instance> outgoingRelations = instance.getPropertyAsSet(JiraBaseElementType.LINKS_OUTGOING);

				if (fromKey == null) {
					//in this case the changeLogItem indicates that a link was established,
					//that is why we will create a link instance.
					if (otherInstance == null) {
						return;
					}

					if (!isArtifactIsSource()) {
						if (getDestinationRole() != null) {
							String destinationRoleString = CaseUtils.toCamelCase(getDestinationRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
							if (instance.hasProperty(destinationRoleString)) {
								SetProperty<Instance> relations = instance.getPropertyAsSet(destinationRoleString);
								relations.add(otherInstance);
								incomingRelations.add(otherInstance);
							}
						}
					} else {
						if (getSourceRole() != null) {
							String sourceRoleString = CaseUtils.toCamelCase(getSourceRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
							if (instance.hasProperty(sourceRoleString)) {
								SetProperty<Instance> relations = instance.getPropertyAsSet(sourceRoleString);
								relations.add(otherInstance);
								outgoingRelations.add(otherInstance);
							}
						}
					}

				} else {
					//in case a link already existed and is now changed, we certainly have to remove the old link
					if (!isArtifactIsSource()) {
						if (getDestinationRole() != null) {
							String destinationRole = CaseUtils.toCamelCase(getDestinationRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
							if (instance.hasProperty(destinationRole)) {
								SetProperty<Instance> artifacts = instance.getPropertyAsSet(destinationRole);
								Optional<Instance> removedInstance = removeArtifactFromList(artifacts, fromKey);
								if (removedInstance.isPresent()) {
									incomingRelations.remove(removedInstance.get());
								}
							}
						}
					} else {
						if (getSourceRole() != null) {
							String sourceRole = CaseUtils.toCamelCase(getSourceRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
							if (instance.hasProperty(sourceRole)) {
								SetProperty<Instance> artifacts = instance.getPropertyAsSet(sourceRole);
								Optional<Instance> removedInstance = removeArtifactFromList(artifacts, fromKey);
								if (removedInstance.isPresent()) {
									outgoingRelations.remove(removedInstance.get());
								}
							}
						}
					}


					//sometimes the changeLogItem may not only delete an old link, but also immediately establish a new one
					if (getToKey() != null) {
						if (otherInstance == null) {
							return;
						}

						if (!isArtifactIsSource()) {
							if (getDestinationRole() != null) {
								String destinationRole = CaseUtils.toCamelCase(getDestinationRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
								if (instance.hasProperty(destinationRole)) {
									SetProperty<Instance> relations = instance.getPropertyAsSet(destinationRole);
									relations.add(otherInstance);
									incomingRelations.add(otherInstance);
								}
							}
						} else {
							if (getSourceRole() != null && instance.hasProperty(getSourceRole())) {
								String sourceRole = CaseUtils.toCamelCase(getSourceRole(), false, ' ', '_', '.', '-', '/', '\\').trim();
								if (instance.hasProperty(sourceRole)) {
									SetProperty<Instance> relations = instance.getPropertyAsSet(sourceRole);
									relations.add(otherInstance);
									outgoingRelations.add(otherInstance);
								}
							}
						}
					}

				}
			} else {

				Optional<String> propertyName = JiraSchemaConverter.resolveFieldIdToProperty(getField(), jiraService.getCurSchema());
				if (propertyName.isPresent() && instance.hasProperty(propertyName.get())) {
					if (instance.getProperty(propertyName.get()).propertyType().cardinality() == Cardinality.SET) {
						SetProperty<Instance> setProperty = instance.getPropertyAsSet(propertyName.get());
						if (fromId != null) {
							//we have to delete a subtype from a list of subtypes
							Optional<Instance> fetchedInstance = jiraService.findInstance(fromId);
							if (fetchedInstance.isPresent()) {
								setProperty.remove(fetchedInstance.get());
							}
						} else {
							//we have to add a subtype to a list of subtypes
							Optional<Instance> fetchedInstance = jiraService.findInstance(toId);
							if (fetchedInstance.isEmpty()) {
								fetchedInstance = Optional.of(jiraService.getArtifactPusher().createPlaceholderArtifact(toId, toKey, setProperty.propertyType().referencedInstanceType()));
							}
							setProperty.add(fetchedInstance.get());
						}

					} else {
						if (toId != null) {
							//first we check if the instance already exists
							Optional<Instance> valueToAssign = jiraService.findInstance(toId);
							if (valueToAssign.isEmpty()) {
								//in some cases the fieldId is provided by change item
								Optional<InstanceType> type = JiraSchemaConverter.resolveFieldIdToInstanceType(getField(), jiraService.getCurSchema());

								if (type.isEmpty()) {
									//in some other cases the label is provided directly
									type = JiraSchemaConverter.resolveLabelToInstanceType(getField(), jiraService.getCurSchema());
								}

								if (type.isPresent()) {
									if (type.get() == Workspace.STRING) {
										instance.getProperty(propertyName.get()).set(getToKey());
									} else {
										try {
											Optional<Instance> fetchedInstance = jiraService.getArtifact(toKey, IJiraService.JiraIdentifier.JiraIssueKey, false);
											if (fetchedInstance.isPresent()) {
												instance.getProperty(propertyName.get()).set(fetchedInstance.get());
											}
										} catch (IdentiferFormatException e) {
											log.debug("JIRA_SERVICE: Could not fetch and map the Jira Issue with the key " + toKey + " !");
										}
									}
								} else {
									log.debug("JIRA-SERVICE : InstanceType for field " + getField() + " was not found!");
									return;
								}
							} else {
								instance.getProperty(propertyName.get()).set(valueToAssign.get());
							}
						} else {
							instance.getProperty(getField()).set(null);
						}
					}
				} else {
					throw new PropertyNotFoundException();
				}
			}
		}
	}

	private Optional<Instance> removeArtifactFromList(SetProperty<Instance> artifacts, String artifactKey) {
		for (Instance artifact : artifacts) {
			if (artifact.hasProperty(BaseElementType.KEY)) {
				if (artifact.getProperty(BaseElementType.KEY).get().equals(artifactKey)) {
					artifacts.remove(artifact);
					return Optional.of(artifact);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("id : " + getId() + "\n");
		sb.append("artifactId : " + getCorrespondingArtifactId() + "\n");
		sb.append("FromKey : " + getFromKey() + "\n");
		sb.append("ToKey : " + getToKey() + "\n");
		sb.append("Time : " + getTimeCreated() + "\n");

		return sb.toString();
	}
	
}
