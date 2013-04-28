package net.thucydides.core.reports.dashboard

import com.github.goldin.spock.extensions.tempdir.TempDir
import net.thucydides.core.reports.dashboard.pages.DashboardPage
import net.thucydides.reports.dashboard.HtmlDashboardReporter
import spock.lang.Specification

class WhenGeneratingTheDashboardReport extends Specification {

    @TempDir File outputDirectory

    def sourceDirectory = new File(Thread.currentThread().contextClassLoader.getResource("sampleresults").file)

    def multiProjectConfiguration = """
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

    def "should generate a dashboard report in the output directory"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration))
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
        then:
            new File(outputDirectory,"dashboard.html").exists()
    }

    def "should generate a dashboard report with a list of all the configured projects"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration))
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
            def dashboard = new File(outputDirectory,"dashboard.html")
            DashboardPage dashboardPage = new DashboardPage(dashboard)
            dashboardPage.open()
        then:
            dashboardPage.projectHeadings == ["Dictionary Project","UI Project"]
        and:
            dashboardPage.projectSubheadings == ["Sprint 1", "Sprint 2", "Sprint 1", "Sprint 2", "UI stuff", "Sprint 1", "Sprint 2"]
        and:
            dashboardPage.graphHeadings == ["Dictionary Project","UI Project"]
        and:
            dashboardPage.graphDataPoints == ["80%","20%","25%","75%"]
    }

    def "should generate a dashboard report with links to detailed reports"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT",outputDirectory, streamed(multiProjectConfiguration))
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory)
            def dashboard = new File(outputDirectory,"dashboard.html")
            DashboardPage dashboardPage = new DashboardPage(dashboard)
            dashboardPage.open()
            dashboardPage.selectProject(1)
        then:
            dashboardPage.title == 'Thucydides Reports'
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
