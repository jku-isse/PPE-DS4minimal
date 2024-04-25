package at.jku.isse.designspace.git.updateservice;

import at.jku.isse.designspace.git.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

//TODO: reactivate when stable JMS server is actually used

@Component
@ConditionalOnExpression(value = "${git.enabled:false}")
public class GitJMSWebhookListener {

	public static final String QUEUE_GITHUB = "githubQueue";
	private Queue<Map<String, Object>> queue;

	@Autowired
	GitService gitService;

	public GitJMSWebhookListener() {
		this.queue = new LinkedList<>();
	}

	/*
	@JmsListener(destination = QUEUE_GITHUB)
	public void receiveGithubEvent(Map<String, Object> data) {
		if (gitService.getUpdateConnection().isPresent()) {
			gitService.getUpdateConnection().get().createChanges(data);
		}
	}
	 */

}
