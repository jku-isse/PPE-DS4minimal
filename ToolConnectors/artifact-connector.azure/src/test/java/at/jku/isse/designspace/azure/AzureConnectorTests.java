package at.jku.isse.designspace.azure;

import org.junit.jupiter.api.Test;

import at.jku.isse.designspace.azure.api.AzureApi;



public class AzureConnectorTests {

	
	@Test
	public void testFetchReview() throws Exception {	
		AzureApi api = new AzureApi();
		byte[] rawJson = api.getWorkItem("UserStudy1Prep", 878);
		String json = new String(rawJson);
		System.out.println(json);
	}
}
