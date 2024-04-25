package at.jku.isse.designspace.jama.updateservice;

import java.time.Instant;
import java.util.Map;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;

public class InstanceBasedCacheStatus implements CacheStatus {

	Instance instance;
	volatile Instant startupTime = Instant.now();
	
	public InstanceBasedCacheStatus(Instance instance) {
		this.instance = instance;				
	}
	
	
	@Override
	public void setLastUpdated(int projectId, Instant timestamp) {		
		instance.getPropertyAsMap("lastupdated").put(projectId+"", timestamp.toString());		
	}

	@Override
	public Instant getLastUpdated(int projectId) {
		Map<String, String> proj2update = instance.getPropertyAsMap("lastupdated").get();
		if (!proj2update.containsKey(projectId+""))
			return startupTime;
		else
			return Instant.parse(proj2update.get(projectId+""));
	}

	@Override
	public void persistStatus() {
		//noop, done with a conclude transaction anyway
	}

	
	public static Instance getInstance(Workspace ws) {
		Instance inst = ws.debugInstanceFindByName("jamaCacheStatus");
		if (inst == null) {
			InstanceType type = getCacheStatusInstanceType(ws);
			inst = ws.createInstance(type, "jamaCacheStatus");	
			//inst.getPropertyAsMap("lastupdated").set(new HashMap<String, String>());
		}
		return inst;
	}
	
	public static InstanceType getCacheStatusInstanceType(Workspace ws) {
		InstanceType type = ws.debugInstanceTypeFindByName(getTypeName());
		if (type != null)
			return type;
		type = ws.createInstanceType(getTypeName(), ws.TYPES_FOLDER);
		type.createPropertyType("lastupdated",  Cardinality.MAP, Workspace.STRING);	
		return type;
	}
	
	public static String getTypeName() {
		return InstanceBasedCacheStatus.class.getPackageName()+"."+InstanceBasedCacheStatus.class.getSimpleName();
	}
	
}
