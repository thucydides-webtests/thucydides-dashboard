package net.thucydides.reports.dashboard

import com.atlassian.jira.rest.client.domain.BasicIssue
import net.thucydides.core.model.TestTag
import net.thucydides.core.util.MockEnvironmentVariables
import net.thucydides.reports.dashboard.jira.JiraClient
import net.thucydides.reports.dashboard.jira.JiraFilterService
import spock.lang.Specification

import java.rmi.RemoteException

class WhenLoadingTagsFromJIRAUsingJQL extends Specification {

    def jiraClient = Mock(JiraClient)
    def issue1 = Mock(BasicIssue)
    def issue2 = Mock(BasicIssue)

    def environmentVariables = new MockEnvironmentVariables()

    def setup() {
        issue1.getKey() >> "PROJ-1"
        issue2.getKey() >> "PROJ-2"
    }

    def "should not use JIRA configuration if no JIRA url is defined"() {
        given:
            environmentVariables.clearProperty("jira.url")
        when:
            def filterService = new JiraFilterService(environmentVariables)
        then:
            !filterService.isJiraConfigured()
    }

    def "should use JIRA configuration if a JIRA url is defined"() {
        given:
            environmentVariables.setProperty("jira.url","http://my.jira.instance")
            environmentVariables.setProperty("jira.project","MY-PROJECT")
            environmentVariables.setProperty("jira.username","scott")
            environmentVariables.setProperty("jira.password","tiger")
        when:
            def filterService = new JiraFilterService(environmentVariables)
        then:
            filterService.isJiraConfigured()
    }

    def "should load sections with JQL filters rather than tags"() {
        given:
            jiraClient.findIssuesByJQL(_) >> [issue1, issue2]
            environmentVariables.setProperty("jira.url","http://my.jira.instance")
            def configurationLoader = new DashboardConfigurationLoader(environmentVariables)
            def filterService = new JiraFilterService(environmentVariables)
            filterService.jiraClient = jiraClient
            configurationLoader.filterService = filterService
        and:
            def configSource = """
Section 1:
  filter: "project = P1"
"""
        when:
            def dashboardConfiguration = configurationLoader.loadFrom(streamed(configSource))
        then:
            dashboardConfiguration.sections[0].hasFilter()
        and:
            dashboardConfiguration.sections[0].filter == "project = P1"
        and:
            dashboardConfiguration.sections[0].tags == [TestTag.withValue("issue:PROJ-1"),
                                                        TestTag.withValue("issue:PROJ-2")]
    }

    def "should load sections with JQL filters rather than tags for nested sections"() {
        given:
            jiraClient.findIssuesByJQL(_) >> [issue1, issue2]
            environmentVariables.setProperty("jira.url","http://my.jira.instance")
            def configurationLoader = new DashboardConfigurationLoader(environmentVariables)
            def filterService = new JiraFilterService(environmentVariables)
            filterService.jiraClient = jiraClient
            configurationLoader.filterService = filterService
        and:
            def configSource = """
Sprint 1:
  filter: "project = P1"
  subsections:
    - UI:
       filter: "component = C1"
    - Dictionary:
       filter: "component = C2"
"""
        when:
            def dashboardConfiguration = configurationLoader.loadFrom(streamed(configSource))
        then:
            dashboardConfiguration.sections[0].hasFilter()
        and:
            dashboardConfiguration.sections[0].filter == "project = P1"
        and:
            dashboardConfiguration.sections[0].tags == [TestTag.withValue("issue:PROJ-1"),
                                                        TestTag.withValue("issue:PROJ-2")]
        and:
            dashboardConfiguration.sections[0].getSubsections()[0].tags == [TestTag.withValue("issue:PROJ-1"),
                                                                            TestTag.withValue("issue:PROJ-2")]
            dashboardConfiguration.sections[0].getSubsections()[1].tags == [TestTag.withValue("issue:PROJ-1"),
                                                                            TestTag.withValue("issue:PROJ-2")]

    }

    def "should return add no tags if JIRA is not available"() {
        given:
            jiraClient.findIssuesByJQL(_) >> { throw new RemoteException("Couldn't find JIRA")}
            environmentVariables.setProperty("jira.url","http://my.jira.instance")
            def configurationLoader = new DashboardConfigurationLoader(environmentVariables)
            def filterService = new JiraFilterService(environmentVariables)
            filterService.jiraClient = jiraClient
            configurationLoader.filterService = filterService
        and:
            def configSource = """
Section 1:
  filter: "project = P1"
"""
        when:
            def dashboardConfiguration = configurationLoader.loadFrom(streamed(configSource))
        then:
            dashboardConfiguration.sections[0].hasFilter()
        and:
            dashboardConfiguration.sections[0].filter == "project = P1"
        and:
            dashboardConfiguration.sections[0].tags == []
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
