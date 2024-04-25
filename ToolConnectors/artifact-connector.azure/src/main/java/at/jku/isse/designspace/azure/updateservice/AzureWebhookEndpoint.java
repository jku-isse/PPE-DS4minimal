package at.jku.isse.designspace.azure.updateservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import at.jku.isse.designspace.azure.service.AzureService;

@RestController
@ConditionalOnExpression(value = "${azure.enabled:false}")
public class AzureWebhookEndpoint {

    @Autowired
    AzureService azureService;
	
	@PostMapping( value="azure/webhook", consumes="application/json")
	void processAzureWebhookEvent(@RequestBody byte[] json) {
		azureService.processWebhook(json);
	}

}
