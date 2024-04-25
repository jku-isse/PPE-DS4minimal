package at.jku.isse.designspace.jama.updateservice;

import java.time.Instant;

public interface CacheStatus {

	void setLastUpdated(int projectId, Instant timestamp);

	Instant getLastUpdated(int projectId);

	void persistStatus();

}