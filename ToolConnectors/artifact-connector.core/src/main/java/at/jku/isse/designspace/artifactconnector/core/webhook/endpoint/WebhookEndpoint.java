package at.jku.isse.designspace.artifactconnector.core.webhook.endpoint;

//@RestController
public class WebhookEndpoint {

	public static final String QUEUE_GITHUB = "github";

	public static final String QUEUE_JIRA = "jira";

	public static final String QUEUE_AZURE = "azure";

//	@Autowired
//	JmsTemplate jmsTemplate;
//	
//	@PostMapping( value="jira/webhook", consumes="application/json")
//	void processJiraWebhookEvent(@RequestBody Map<String, Object> json) { //Map<String, Object>
//		//for now just print to console
//		//TODO: only forward that matches webhook events from jira, but nothing else, as we don;t want to polute the queue
//		jmsTemplate.convertAndSend(QUEUE_JIRA, json);
//	}
//	
//	
//	@PostMapping( value="github/webhook", consumes="application/json")
//	void processGithubWebhookEvent(@RequestBody Map<String, Object> json) { //Map<String, Object>
//		//for now just print to console
//		//TODO: only forward that matches webhook events from jira, but nothing else, as we don;t want to polute the queue
//		jmsTemplate.convertAndSend(QUEUE_GITHUB, json);
//	}
//
//	@PostMapping( value="azure/webhook", consumes="application/json")
//	void processAzureWebhookEvent(@RequestBody byte[] json) {
//		//for now just print to console
//		System.out.println("FROM Azure WEB: "+json);
//		jmsTemplate.convertAndSend(QUEUE_AZURE, json);
//	}

}
