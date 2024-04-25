package at.jku.isse.designspace.jira.updateservice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import at.jku.isse.designspace.jira.service.IJiraService;

@RestController
@ConditionalOnExpression(value = "${jira.enabled:false}")
public class JiraWebhookEndpoint {

	@Autowired
	IJiraService jiraService;

	@PostMapping( value="jira/webhook", consumes="application/json")
	void processJiraWebhookEvent(@RequestBody Map<String, Object> json) {
		if (jiraService.getReactiveConnection().isPresent()) {
			jiraService.getReactiveConnection().get().parseChanges(json);
		}
	}

}
