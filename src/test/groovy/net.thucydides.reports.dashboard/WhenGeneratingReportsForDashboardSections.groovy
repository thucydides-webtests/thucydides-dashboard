package net.thucydides.core.reports.dashboard

import com.github.goldin.spock.extensions.tempdir.TempDir
import net.thucydides.core.digest.Digest
import net.thucydides.reports.dashboard.HtmlDashboardReporter
import spock.lang.Specification

class WhenGeneratingReportsForDashboardSections extends Specification {

    @TempDir File outputDirectory

    def simpleConfiguration = """
Sprint 1: "iteration:sprint-1"
Sprint 2: "iteration:sprint-2"
"""

    def sourceDirectory = new File(Thread.currentThread().contextClassLoader.getResource("sampleresults").file)

    def "should produce reports containing only results for each section"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT", outputDirectory, streamed(simpleConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory);
        then:
            File report1 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1"))
            File report2 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2"))

            assert new File(report1, "index.html").exists()
            assert new File(report2, "index.html").exists()
    }

    def nestedConfiguration = """
Sprint 1:
  tags: "iteration:sprint-1"
  subsections:
    - UI: "component:ui"
    - Dictionary: "component:dictionary"
Sprint 2:
  tags: "iteration:sprint-2"
  subsections:
    - UI: "component:ui"
    - Dictionary: "component:dictionary"
"""
    def "should produce reports for top-level and nested sections"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT", outputDirectory, streamed(nestedConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory);
        then:
            File report1 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1"))
            File report2 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2"))
            File report3 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1__ui"))
            File report4 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1__dictionary"))
            File report5 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2__ui"))
            File report6 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2__dictionary"))

            new File(report1, "index.html").exists()
            new File(report2, "index.html").exists()
            new File(report3, "index.html").exists()
            new File(report4, "index.html").exists()
            new File(report5, "index.html").exists()
            new File(report6, "index.html").exists()
    }

    def anotherConfig = """
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
"""
    def "should produce reports for different tag configurations"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT", outputDirectory, streamed(anotherConfig),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory);
        then:
            File report1 = new File(outputDirectory, Digest.ofTextValue("sections_dictionary_project"))
            File report2 = new File(outputDirectory, Digest.ofTextValue("sections_dictionary_project__sprint_1"))
            File report3 = new File(outputDirectory, Digest.ofTextValue("sections_dictionary_project__sprint_2"))
            File report4 = new File(outputDirectory, Digest.ofTextValue("sections_ui_project"))
            File report5 = new File(outputDirectory, Digest.ofTextValue("sections_ui_project__sprint_1"))
            File report6 = new File(outputDirectory, Digest.ofTextValue("sections_ui_project__sprint_2"))

            new File(report1, "index.html").exists()
            new File(report2, "index.html").exists()
            new File(report3, "index.html").exists()
            new File(report4, "index.html").exists()
            new File(report5, "index.html").exists()
            new File(report6, "index.html").exists()
    }


    def "section reports should have relative links to test results"() {
        given:
            def reporter = new HtmlDashboardReporter("SOMEPROJECT", outputDirectory, streamed(nestedConfiguration),"xml")
        when:
            reporter.generateReportsForTestResultsFrom(sourceDirectory);
        then:
            File report1 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1"))
            File report2 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2"))
            File report3 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1__ui"))
            File report4 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_1__dictionary"))
            File report5 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2__ui"))
            File report6 = new File(outputDirectory, Digest.ofTextValue("sections_sprint_2__dictionary"))
            def reports = [report1, report2, report3, report4, report5, report6]
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
