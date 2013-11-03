package net.thucydides.reports.dashboard.jira;

import ch.lambdaj.function.convert.Converter;
import com.google.common.collect.ImmutableList;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.plugins.jira.client.JerseyJiraClient;
import net.thucydides.plugins.jira.domain.IssueSummary;
import net.thucydides.reports.dashboard.FilterService;
import net.thucydides.plugins.jira.service.JIRAConfiguration;
import net.thucydides.plugins.jira.service.SystemPropertiesJIRAConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ch.lambdaj.Lambda.convert;

public class JiraFilterService implements FilterService {

    private static final List<TestTag> NO_TAGS = ImmutableList.of();
    private final String jiraUrl;
    private JerseyJiraClient jiraClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraFilterService.class);

    public JiraFilterService(EnvironmentVariables environmentVariables) {
        JIRAConfiguration jiraConfiguration = new SystemPropertiesJIRAConfiguration(environmentVariables);
        jiraClient = new JerseyJiraClient(jiraConfiguration.getJiraUrl(),
                                          jiraConfiguration.getJiraUser(),
                                          jiraConfiguration.getJiraPassword(),
                                          jiraConfiguration.getProject());
        jiraUrl = jiraConfiguration.getJiraUrl(); 
    }

    public boolean isJiraConfigured() {
        return jiraUrl != null;
    }

    @Override
    public List<TestTag> loadTagsByFilter(String filter) {
        System.out.println("Loading filter tags for: " + filter);
        if (jiraUrl != null) {
            try {
                List<IssueSummary> matchingIssues = jiraClient.findByJQL(filter);
                return convert(matchingIssues, toIssueKeyTags());
            } catch (Exception e) {
                System.out.println("Failed to get issues from JIRA: " + e.getMessage());
                LOGGER.error("Could not connect to JIRA to get issues", e);
            }
        }
        return NO_TAGS;
    }

    private Converter<IssueSummary, TestTag> toIssueKeyTags() {
        return new Converter<IssueSummary, TestTag>() {

            @Override
            public TestTag convert(IssueSummary from) {
                return TestTag.withName(from.getKey()).andType("issue");
            }
        };
    }
}
