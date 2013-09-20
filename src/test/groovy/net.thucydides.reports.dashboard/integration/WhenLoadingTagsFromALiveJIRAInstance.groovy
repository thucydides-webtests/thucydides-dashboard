package net.thucydides.reports.dashboard.integration

import net.thucydides.core.util.MockEnvironmentVariables
import net.thucydides.reports.dashboard.DashboardConfigurationLoader
import spock.lang.Specification

class WhenLoadingTagsFromALiveJIRAInstance extends Specification {

    def environmentVariables = new MockEnvironmentVariables()

    def "should load sections with JQL filters rather than tags"() {
        given:
            environmentVariables.setProperty("jira.url","https://wakaleo.atlassian.net")
            environmentVariables.setProperty("jira.project","DEMO")
            environmentVariables.setProperty("jira.username","bruce")
            environmentVariables.setProperty("jira.password","batm0bile")
            def configurationLoader = new DashboardConfigurationLoader(environmentVariables)
        and:
            def configSource = """
Section 1:
  filter: "project=DEMO"
"""
        when:
            def dashboardConfiguration = configurationLoader.loadFrom(streamed(configSource))
        then:
            !dashboardConfiguration.sections[0].tags.isEmpty()
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
