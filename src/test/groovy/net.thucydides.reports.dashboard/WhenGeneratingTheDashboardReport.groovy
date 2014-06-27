package net.thucydides.reports.dashboard

import com.github.goldin.spock.extensions.tempdir.TempDir
import net.thucydides.maven.plugins.ThucydidesDashboardMojo
import net.thucydides.reports.dashboard.pages.DashboardPage
import spock.lang.Specification

class WhenGeneratingTheDashboardReport extends Specification {

    @TempDir File outputDirectory

    def sourceDirectory = new File(Thread.currentThread().contextClassLoader.getResource("sampleresults").file)

    def multiProjectConfiguration = """
Title: My Dashboard
Dictionary Project:
    tags: "component:dictionary"
    subsections:
        Sprint 1: "iteration:sprint-1"
        Sprint 2: "iteration:sprint-2"
UI Project:
    tags: "component:ui"
    subsections:
        Sprint 1: "iteration:sprint-1"
        Sprint 2: "iteration:sprint-2"
        UI stuff:
            tags: "component:ui"
            subsections:
                Sprint 1: "iteration:sprint-1"
                Sprint 2: "iteration:sprint-2"

"""

    def DashboardPage dashboardPage


    def cleanup() {
        if (dashboardPage) {
            dashboardPage.close()
        }
    }

    def "should generate a dashboard report in the output directory"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
        then:
            new File(outputDirectory,"dashboard.html").exists()
    }

    def "should generate a dashboard report with a list of all the configured projects"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
            def dashboard = new File(outputDirectory,"dashboard.html")
            dashboardPage = new DashboardPage(dashboard)
            dashboardPage.open()
        then:
            dashboardPage.dashboardTitle == "My Dashboard"
        and:
            dashboardPage.projectHeadings == ["DICTIONARY PROJECT","UI PROJECT"]
        and:
            dashboardPage.projectSubheadings == ["Sprint 1", "Sprint 2", "Sprint 1", "Sprint 2", "UI stuff", "Sprint 1", "Sprint 2"]
        and:
            dashboardPage.graphHeadings == ["Overview","Dictionary Project","UI Project"]
        and:
            dashboardPage.graphDataPoints == ["56%","44%","80%","20%","25%","75%"]
    }

    def "should generate a dashboard report with links to detailed reports"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
            def dashboard = new File(outputDirectory,"dashboard.html")
            dashboardPage = new DashboardPage(dashboard)
            dashboardPage.open()
            dashboardPage.selectProject(1)
        then:
            dashboardPage.title == 'Thucydides Reports'
    }

    def "should generate a dashboard report when the Maven plugin is called"() {
        given: "a maven plugin"
            def mojo = new ThucydidesDashboardMojo()
            mojo.projectKey = "MY-PROJECT"
            mojo.sourceDirectory = sourceDirectory
            mojo.outputDirectory = outputDirectory
            mojo.format = "XML"
        when: "we generate the report"
            mojo.execute()
            def dashboard = new File(outputDirectory,"dashboard.html")
            dashboardPage = new DashboardPage(dashboard)
            dashboardPage.open()
            dashboardPage.selectProject(1)
        then: "the dashboard should exist"
            dashboard.exists()
        and: "the dashboard should be correctly formed"
            dashboardPage.title == 'Thucydides Reports'
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
