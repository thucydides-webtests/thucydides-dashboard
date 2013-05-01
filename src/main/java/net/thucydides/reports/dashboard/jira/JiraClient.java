package net.thucydides.reports.dashboard.jira;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class JiraClient {

    private final String url;
    private final String username;
    private final String password;
    private final int maxResults;

    public JiraClient(String url, String username, String password, int maxResults) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxResults = maxResults;
    }

    public Iterable<BasicIssue> findIssuesByFilter(String filter) throws URISyntaxException {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(new URI(url), username, password);
        SearchResult searchResults =  restClient.getSearchClient().searchJql(filter).claim();

        System.out.println("Found " + searchResults.getTotal() + " issues");
        return searchResults.getIssues();
    }

}
