package at.jku.isse.designspace.jama.updateservice;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.jku.isse.designspace.jama.connector.restclient.httpconnection.JamaClient;
import at.jku.isse.designspace.jama.replaying.JamaActivity;
import at.jku.isse.designspace.jama.service.JamaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChangeStreamPoller extends AbstractMonitoring{

	private JamaClient ji;
	private JamaService jamaService;
	
	int projectId;
	CacheStatus cs;
	
	public ChangeStreamPoller(JamaClient ji, JamaService jamaService, int projectId, CacheStatus cs, int intervalInMinutes) {
		this.projectId = projectId;
		this.ji = ji;
		this.jamaService = jamaService;
		
		super.id = projectId+"";
		super.intervalInMinutes = intervalInMinutes;
		super.lastSuccessfulUpdate = cs.getLastUpdated(projectId);
		this.cs = cs; // I dont like this double bookkeeping of last updated
		log.info(String.format("Jama Change Poller setup to poll project %s every %s minutes", projectId, intervalInMinutes));
	}

	@Override
	public void run() {
		try {
			Instant pollTime = Instant.now().minus(1l, ChronoUnit.MINUTES); // one minute overlap just to be on the save side.
			poll(super.lastSuccessfulUpdate);
			cs.setLastUpdated(projectId, pollTime); // I dont like this double bookkeeping of last updated
			cs.persistStatus();
			super.lastSuccessfulUpdate = pollTime;
		} catch (Exception e) {
			log.warn("Error retrieving/processing change stream", e);
		}
	}
	
	public void poll(Instant lastSuccessfulUpdate) throws Exception {				
			String query = "activities/?project="+projectId+"&date="+lastSuccessfulUpdate+"&objectType=ITEM&objectType=RELATIONSHIP&eventType=CREATE&eventType=UPDATE&eventType=DELETE&maxResults=50";
			log.debug(query);
			List<Map<String,Object>> jdos = ji.getAll(query);
			processChanges(jdos);					
	}
	
	protected void processChanges(List<Map<String,Object>> jdos) {
		jamaService.updateItems(jdos.stream()
				.map(entry -> JamaActivity.fromJson(entry))
				.collect(Collectors.toList()));		
	}

	public void setJama(JamaClient ji, JamaService jamaService) {
		this.ji = ji;
		this.jamaService = jamaService;
	}

}
