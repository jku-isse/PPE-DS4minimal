package at.jku.isse.designspace.git.updateservice;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import at.jku.isse.designspace.git.service.GitService;

import java.util.Map;

@RestController
@ConditionalOnExpression(value = "${git.enabled:false}")
public class GitWebhookEndpoint {

	@Autowired
	GitService gitService;

	@PostMapping( value="github/webhook", consumes="application/json")
	void processGithubWebhookEvent(@RequestBody Map<String, Object> json) {
		if (gitService.getUpdateConnection().isPresent()) {
			gitService.getUpdateConnection().get().createChanges(json);
		}
	}


}
