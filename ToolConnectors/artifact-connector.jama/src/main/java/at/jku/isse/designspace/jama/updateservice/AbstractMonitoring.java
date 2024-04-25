package at.jku.isse.designspace.jama.updateservice;

import java.time.Duration;
import java.time.Instant;


public abstract class AbstractMonitoring implements Runnable{

	protected String id;	
	protected int intervalInMinutes = 60; //default value, once per hour
	protected Instant lastSuccessfulUpdate = Instant.now();


	public Duration getDurationSinceLastUpdate() {
		return Duration.between(Instant.now(), lastSuccessfulUpdate);		
	}
	
	public int getIntervalInMinutes() {
		return intervalInMinutes;
	}
	
	public void setInterval(int intervalInMinutes) {
		this.intervalInMinutes = intervalInMinutes;
	}
	
	public String getId() {
		return id;
	}
	
	
}
