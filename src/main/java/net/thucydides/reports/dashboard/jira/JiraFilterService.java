package net.thucydides.reports.dashboard.jira;

import ch.lambdaj.function.convert.Converter;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.google.common.collect.ImmutableList;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.reports.dashboard.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ch.lambdaj.Lambda.convert;

public class JiraFilterService implements FilterService {

    private static final List<TestTag> NO_TAGS = ImmutableList.of();
    private final String jiraUrl;
    private JiraClient jiraClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraFilterService.class);

    public JiraFilterService(EnvironmentVariables environmentVariables) {
        this.jiraUrl = environmentVariables.getProperty(ThucydidesSystemProperty.JIRA_URL);
        String username = environmentVariables.getProperty(ThucydidesSystemProperty.JIRA_USERNAME);
        String password = environmentVariables.getProperty(ThucydidesSystemProperty.JIRA_PASSWORD);

        jiraClient = new JiraClient(jiraUrl, username, password);
    }

    public boolean isJiraConfigured() {
        return jiraUrl != null;
    }

    @Override
    public List<TestTag> loadTagsByFilter(String filter) {
        System.out.println("Loading filter tags for: " + filter);
        if (jiraUrl != null) {
            try {
                Iterable<BasicIssue> matchingIssues = jiraClient.findIssuesByJQL(filter);
                return convert(ImmutableList.copyOf(matchingIssues), toIssueKeyTags());
            } catch (Exception e) {
                System.out.println("Failed to get issues from JIRA: " + e.getMessage());
                LOGGER.error("Could not connect to JIRA to get issues", e);
            }
        }
        return NO_TAGS;
    }

    private Converter<BasicIssue, TestTag> toIssueKeyTags() {
        return new Converter<BasicIssue, TestTag>() {

            @Override
            public TestTag convert(BasicIssue from) {
                return TestTag.withName(from.getKey()).andType("issue");
            }
        };
    }
}
