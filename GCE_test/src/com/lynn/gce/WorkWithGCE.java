package com.lynn.gce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;

public class WorkWithGCE {

	private static final String PROJECTID = "<replace_me>";
	private static final String ZONE = "<replace_me>";
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final List<String> SCOPES = Arrays
			.asList(ComputeScopes.COMPUTE_READONLY);
	private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";
	static GoogleClientSecrets clientSecrets = loadClientSecrets(CLIENTSECRETS_LOCATION);
	private static final String REDIRECT_URI = "urn:ietf:<replace_me>";
	private static GoogleAuthorizationCodeFlow flow = null;

	public static void main(String[] args) throws IOException {

		Compute comp = authorize();
		printInstances(comp, PROJECTID, ZONE);
	}

	public static Compute authorize() throws IOException {

		String authorizeUrl = new GoogleAuthorizationCodeRequestUrl(
				clientSecrets, REDIRECT_URI, SCOPES).setState("").build();
		System.out
				.println("Paste this URL into a web browser to authorize GCE Access:\n"
						+ authorizeUrl);
		System.out.println("... and type the code you received here: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String authorizationCode = in.readLine();
		Credential credential = exchangeCode(authorizationCode);
		return new Compute(HTTP_TRANSPORT, JSON_FACTORY, credential);
	}

	private static GoogleClientSecrets loadClientSecrets(
			String clientSecretsLocation) {
		try {
			clientSecrets = GoogleClientSecrets.load(new JacksonFactory(),
					WorkWithGCE.class
							.getResourceAsStream(clientSecretsLocation));
		} catch (Exception e) {
			System.out.println("Could not load client_secrets.json");
			e.printStackTrace();
		}
		return clientSecrets;
	}

	static Credential exchangeCode(String authorizationCode) throws IOException {
		GoogleAuthorizationCodeFlow flow = getFlow();
		GoogleTokenResponse response = flow.newTokenRequest(authorizationCode)
				.setRedirectUri(REDIRECT_URI).execute();
		return flow.createAndStoreCredential(response, null);
	}

	static GoogleAuthorizationCodeFlow getFlow() {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					jsonFactory, clientSecrets, SCOPES)
					.setAccessType("offline").setApprovalPrompt("force")
					.build();
		}
		return flow;
	}

	public static void printInstances(Compute compute, String projectId,
			String zone) throws IOException {
		System.out
				.println("================== Write out CGE Instances ==================");
		Compute.Instances.List inst = compute.instances().list(projectId, zone);
		InstanceList il = inst.execute();
		if (il.getItems() == null) {
			System.out
					.println("You don't have any GCE instances. Go to the Google APIs Console and spin up "
							+ "one or more instancs via: code.google.com/apis/console");
		} else {
			for (Instance instance : il.getItems()) {
				System.out.println(instance.toPrettyString());
			}
		}
	}
}
