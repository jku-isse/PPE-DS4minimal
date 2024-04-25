package at.jku.isse.designspace.azure.updateservice;

//TODO: reactivate this, once JMS is deployed, until then, just directly process changes from webhook

//@Component
//@ConditionalOnExpression(value = "${azure.enabled:false}")
//public class AzureJmsWebhookListener {
//    public static final String QUEUE_AZURE = "azure";
//
//    @Autowired
//    AzureService azureService;
//
//    @JmsListener(destination = QUEUE_AZURE, containerFactory = "myFactory")
//    public void receiveAzureEvent( byte[] json) {
//        azureService.processWebhook(json);
//    }
//}
