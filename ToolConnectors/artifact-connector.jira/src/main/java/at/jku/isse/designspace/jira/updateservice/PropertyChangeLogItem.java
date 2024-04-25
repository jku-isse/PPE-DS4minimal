package at.jku.isse.designspace.jira.updateservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.exceptions.PropertyNotFoundException;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jira.model.JiraSchemaConverter;
import at.jku.isse.designspace.jira.service.IJiraService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyChangeLogItem extends ChangeLogItem {

	private Map<String, Object> from;

	private Map<String, Object> to;

	public Map<String, Object> getFrom() {
		return from;
	}

	public void setFrom(Map<String, Object> from) {
		this.from = from;
	}

	public Map<String, Object> getTo() {
		return to;
	}

	public void setTo(Map<String, Object> to) {
		this.to = to;
	}

	@Override
	public Instance applyChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException {
		if(to == null || from == null) return instance;

		if(to.isEmpty()) {
			if(!from.isEmpty()) {
				instance.getProperty(from.keySet().iterator().next()).set(null);
			}
		} else {
			updateProperty(instance, getFrom(), getTo());
		}
		return instance;
	}

	private void updateProperty(Instance instance, Map<String, Object> fromMap, Map<String, Object> toMap) throws PropertyNotFoundException {
		String fieldId = getTo().keySet().iterator().next();
		InstanceType instanceType = instance.getInstanceType();
		Optional<String> propertyName = JiraSchemaConverter.resolveFieldIdToProperty(fieldId, instanceType);

		if (propertyName.isPresent() && instance.hasProperty(propertyName.get())) {
			Property property = instance.getProperty(propertyName.get());

			if (!(toMap.get(fieldId) instanceof HashMap)) {
				InstanceType type = property.propertyType().getInstanceType();
				try {
					if (property.propertyType().cardinality() == Cardinality.SET) {
						Object toSet = toMap.get(fieldId);
						Object fromSet = fromMap.get(fieldId);
						ArrayList toList = new ArrayList();
						ArrayList fromList = new ArrayList();

						if (toSet != null) {
							toList = (ArrayList) toSet;
						}

						if (fromSet != null) {
							fromList = (ArrayList) fromSet;
						}

						SetProperty setProperty = (SetProperty) property;

						if (type.equals(Workspace.INTEGER)) {
							for (Object elem : fromList) {
								if (!toList.contains(elem)) {
									setProperty.remove(elem);
								}
							}
						} else {
							for (Object elem : fromList) {
								if (!toList.contains(elem)) {
									setProperty.remove(elem.toString());
								}
							}
						}

						if (type.equals(Workspace.INTEGER)) {
							for (Object elem : toList) {
								if (!fromList.contains(elem)) {
									setProperty.add(elem);
								}
							}
						} else {
							for (Object elem : toList) {
								if (!fromList.contains(elem)) {
									setProperty.add(elem.toString());
								}
							}
						}

					} else {
						property.set(toMap.get(fieldId));
					}
				} catch (IllegalArgumentException illegalArgumentException) {
					//sometimes number types are stored as Strings in JSON
					//that causes an error because the instanceType of the property
					//expects an integer
					try {
						if (type.equals(Workspace.INTEGER)) {
							property.set(Integer.parseInt((String) toMap.get(fieldId)));
						}
					} catch (Exception e) {
						log.debug("JIRA-SERVICE: Could not assign " + toMap.get(fieldId) + " to property " + property.name + "!");
					}
				} catch (ClassCastException classCastException) {
					log.debug("JIRA-SERVICE: A PropertyChangeLogItem contained a change of a field that is mar");
				}
			} else {
				log.debug("JIRA-SERVICE: HistoryCreator: ChangeLogItems should not contain HashMaps");
			}
		} else {
			throw new PropertyNotFoundException();
		}
	}

	@Override
	public Instance undoChange(Instance instance, IJiraService jiraService) throws PropertyNotFoundException {
		if(to == null|| from ==null) return instance;
		if(from.isEmpty()) {
			if(!to.isEmpty()) {
				instance.getProperties().remove(to.keySet().iterator().next());
			}
		} else {
			updateProperty(instance, getTo(), getFrom());
		}
		return instance;
	}

	@Override
	public HashSet<String> getInvolvedArtifactIds() {
		return null;
	} 

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("id : " + getId() + "\n");
		sb.append("artifactId : " + getCorrespondingArtifactId() + "\n");
		sb.append("From : " + getFrom() + "\n");
		sb.append("To : " + getTo() + "\n");
		sb.append("Time : " + getTimeCreated() + "\n");

		return sb.toString();
	}

}
