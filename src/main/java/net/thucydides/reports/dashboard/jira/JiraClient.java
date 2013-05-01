package net.thucydides.reports.dashboard.jira;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class JiraClient {

    private final String url;
    private final String username;
    private final String password;

    private final int BATCH_SIZE = 100;

    public JiraClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Iterable<BasicIssue> findIssuesByJQL(String query) throws URISyntaxException {
        System.out.println("Searching with query " + query);
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(new URI(url), username, password);
        List<BasicIssue> issues = fetchAllIssues(restClient.getSearchClient(), query);
        System.out.println("Issues: ");
        for(BasicIssue issue : issues) { System.out.print(issue.getKey() + " ");}
        return issues;
    }


    private List<BasicIssue> fetchAllIssues(SearchRestClient searchClient, String query) {

        List<BasicIssue> matchingIssues = Lists.newArrayList();

        int loadedIssues = 0;
        boolean allLoaded = false;
        int startAt = 0;

        while(!allLoaded) {
            SearchResult batchResults = searchClient.searchJql(query, BATCH_SIZE, startAt).claim();
            List<BasicIssue> issuesInThisBatch = Lists.newArrayList(batchResults.getIssues());
            matchingIssues.addAll(issuesInThisBatch);

            startAt += BATCH_SIZE;
            loadedIssues += issuesInThisBatch.size();
            allLoaded = batchResults.getTotal() <= loadedIssues;
        }
        return matchingIssues;
    }

}
