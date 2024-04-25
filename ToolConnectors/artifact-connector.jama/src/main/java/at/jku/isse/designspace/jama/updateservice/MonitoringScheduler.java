package at.jku.isse.designspace.jama.updateservice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitoringScheduler {
	
	private Map<AbstractMonitoring, ScheduledFuture<?>> monitoringTasks;
	private Map<String, AbstractMonitoring> id2Task;
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);

	
	public MonitoringScheduler() {
		monitoringTasks = new HashMap<AbstractMonitoring, ScheduledFuture<?>>();
		id2Task = new HashMap<String, AbstractMonitoring>();
	}
	
	public boolean hasTaskById(String id) {
		return id2Task.containsKey(id);
	}
	
	public boolean hasTask(AbstractMonitoring as) {
		return hasTaskById(as.getId());
	}
	
	public boolean registerAndStartTask(AbstractMonitoring as) {
		id2Task.put(as.getId(), as);
		long intervalInSeconds = as.getIntervalInMinutes()*60;
		long initialDelayInSeconds = 120;
		final ScheduledFuture<?> handle =
			       scheduler.scheduleWithFixedDelay(as, initialDelayInSeconds, intervalInSeconds, java.util.concurrent.TimeUnit.SECONDS); // we dont want to immediately fetch again if the fetching takes a lot of time						
		ScheduledFuture<?> prev = monitoringTasks.put(as, handle);
		if (prev != null) {
			prev.cancel(true);
		}
		return true;
	}
	
	public boolean runAllMonitoringTasksSequentiallyOnceNow() {		
		id2Task.values().stream().distinct()
			.filter(am -> Math.abs(am.getDurationSinceLastUpdate().getSeconds()) > 60) // only if last update was more than 2 minutes ago
			.forEach(am -> {
					am.run();
					} );
		return true;
	}
	
	public boolean stopAndRemoveTask(AbstractMonitoring as) {
		if(!id2Task.containsKey(as.getId()))
			return false;
		
		ScheduledFuture<?> task = monitoringTasks.get(as);
		if(task != null)
			task.cancel(true);
		
		return true;
	}
	
	public boolean stopAndRemoveTask(String id) {
		return stopAndRemoveTask(getTaskById(id));
	}
	

	
	public AbstractMonitoring getTaskById(String id) {
		return id2Task.get(id);
	}
	
	public void shutdown() {
		log.info("Shutting down Jama change polling");
		monitoringTasks.entrySet().stream().forEach(e -> {
			if(e.getValue() != null) {				
				e.getValue().cancel(true);
			}
		});
		scheduler.shutdownNow();
	}
	
	@Override
	public void finalize() {
		shutdown();
	}
}