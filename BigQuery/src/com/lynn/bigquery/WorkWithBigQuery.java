package com.lynn.bigquery;

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
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;
import com.google.api.services.bigquery.model.TableCell;
import com.google.api.services.bigquery.model.TableRow;

public class WorkWithBigQuery {

	private static final String PROJECT_NUMBER = "<replace_me>";
	private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";
	private static final String REDIRECT_URI = "urn:ietf:<replace_me>";
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	static GoogleClientSecrets clientSecrets = loadClientSecrets(CLIENTSECRETS_LOCATION);
	private static GoogleAuthorizationCodeFlow flow = null;
	private static final List<String> SCOPES = Arrays
			.asList(BigqueryScopes.BIGQUERY);

	private static String QUERY = "SELECT TOP(title, 10), count(*) FROM publicdata:samples.wikipedia WHERE wp_namespace=0;";

	public static void main(String[] args) throws IOException {
		Bigquery bigquery = authorize();
		executeQuery(bigquery, PROJECT_NUMBER, QUERY);
	}

	public static void executeQuery(Bigquery bigqueryService,
			String projectNumber, String query) throws IOException {

		QueryRequest queryInfo = new QueryRequest().setQuery(query);
		Bigquery.Jobs.Query queryRequest = bigqueryService.jobs().query(
				projectNumber, queryInfo);
		QueryResponse queryResponse = queryRequest.execute();

		if (queryResponse.getRows() != null) {
			for (TableRow row : queryResponse.getRows()) {
				for (TableCell field : row.getF()) {
					System.out.printf("%s-30s", field.getV());
				}
				System.out.println();
			}
		}
	}

	public static Bigquery authorize() throws IOException {

		String authorizeUrl = new GoogleAuthorizationCodeRequestUrl(
				clientSecrets, REDIRECT_URI, SCOPES).setState("").build();
		System.out
				.println("Paste this URL into a web browser to authorize BigQuery Access:\n"
						+ authorizeUrl);
		System.out.println("... and type the code you received here: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String authorizationCode = in.readLine();
		Credential credential = exchangeCode(authorizationCode);
		return new Bigquery(HTTP_TRANSPORT, JSON_FACTORY, credential);
	}

	private static GoogleClientSecrets loadClientSecrets(
			String clientSecretsLocation) {
		try {
			clientSecrets = GoogleClientSecrets.load(new JacksonFactory(),
					WorkWithBigQuery.class
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

}
