package net.thucydides.reports.dashboard.jira;

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

}
