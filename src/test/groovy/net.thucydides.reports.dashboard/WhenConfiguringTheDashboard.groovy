package net.thucydides.core.reports.dashboard

import net.thucydides.core.digest.Digest
import net.thucydides.core.model.TestTag
import net.thucydides.reports.dashboard.model.Section
import net.thucydides.reports.dashboard.DashboardConfigurationLoader
import spock.lang.Specification

class WhenConfiguringTheDashboard extends Specification {

    def dashboardConfigurationLoader = new DashboardConfigurationLoader()

    def "should read a dashboard configuration file from a 'dashboard.groovy' file on the class path"() {
        when:
            def dashboardConfiguration = dashboardConfigurationLoader.loadDefault();
        then:
            dashboardConfiguration != null
    }

    def "user should provide a list of top-level dashboard projects"() {
        when:
            def dashboardConfiguration = dashboardConfigurationLoader.loadDefault();
        then:
            dashboardConfiguration != null
    }

    def "should load section titles"() {
        given:
            def configSource = 'Section 1: "project:PROJ1"'
        when:
            def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
            dashboardConfiguration.sections
        and:
            dashboardConfiguration.sections.collect{ it.title } == ["Section 1"]
    }

    def "should load several sections"() {
        given:
        def configSource =
"""
Section 1: "project:PROJ1"
Section 2: "project:PROJ2"
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections
        and:
        dashboardConfiguration.sections.collect{ it.title } == ["Section 1","Section 2"]
    }


    def "each section should have a different directory name"() {
        given:
        def configSource =
            """
Section 1: "project:PROJ1"
Section 2: "project:PROJ2"
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections
        and:
        def section1Dir = Digest.ofTextValue("sections_section_1")
        def section2Dir = Digest.ofTextValue("sections_section_2")
        dashboardConfiguration.sections.collect{ it.directoryName } == [section1Dir, section2Dir]
    }

    def "should be able to break sections up into lists of sections to display in rows"() {
        given:
            def configSource =
            """
Section 1: "project:PROJ1"
Section 2: "project:PROJ2"
Section 3: "project:PROJ2"
Section 4: "project:PROJ2"
Section 5: "project:PROJ2"
"""
        when:
            def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
            def rows = dashboardConfiguration.getSectionRows(2)
        then:
            rows.toString() == "[[Section 1, Section 2], [Section 3, Section 4], [Section 5]]"
    }


    def "a section has a hashed directory name"() {
        given:
            def section = new Section("Some Section", [], []);
        when:
            def directoryName = section.directoryName
        then:
            directoryName == Digest.ofTextValue("sections_some_section");
    }

    def "a nested section has a unique hashed directory name"() {
        given:
        def section1 = new Section("Parent 1", [], []);
        def section2 = new Section("Parent 2", [], []);
        def section = new Section("Some Section", [], []);
        when:
        def directoryName = section.getDirectoryName([section1, section2])
        then:
        directoryName == Digest.ofTextValue("sections_parent_1__parent_2__some_section");
    }

    def "a section with parent sections has a unique hashed directory name"() {
        given:
        def section1 = new Section("Parent 1", [], []);
        def section2 = new Section("Parent 2", [], []);
        when:
        def section = new Section("Some Section", [], []).withParents([section1, section2])
        then:
        section.directoryName == Digest.ofTextValue("sections_parent_1__parent_2__some_section");
    }


    def "should load tags for each section"() {
        given:
        def configSource =
"""
Section 1: "project:PROJ1"
Section 2: ["project:PROJ2","project:PROJ3"]
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections[0].tags == [TestTag.withValue("project:PROJ1")]
        and:
        dashboardConfiguration.sections[1].tags == [TestTag.withValue("project:PROJ2"), TestTag.withValue("project:PROJ3")]
    }

    def "should load sections with tags in long form"() {
        given:
        def configSource =
"""
Section 1:
  tags: ["project:P1", "project:P2"]
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections[0].tags == [TestTag.withValue("project:P1"),TestTag.withValue("project:P2")]
    }

    def "tags section should be optional"() {
        given:
        def configSource =
            """
Section 1:
    subsection:
Section 2:
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections[0].tags.isEmpty()
        and:
        dashboardConfiguration.sections[1].tags.isEmpty()
    }

    def "tags should be optional"() {
        given:
        def configSource =
            """
Section 1:
  tags:
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections[0].tags.isEmpty()
    }

    def "sections can have subsections"() {
        given:
        def configSource =
            """
Section 1:
  tags: "project:P1"
  subsections:
    - Section 1.1:
        tags: "iteration:I1"
    - Section 1.2: "iteration:I2"
    - Section 1.3:
        tags: "iteration:I3"

Section 2: "project:P2"
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
            dashboardConfiguration.sections.size() == 2
        and:
            dashboardConfiguration.sections[0].subsections.collect{ it.title } == ["Section 1.1","Section 1.2","Section 1.3"]
        and:
            dashboardConfiguration.sections[1].subsections.isEmpty()
        and:
            dashboardConfiguration.sections[0].subsections.each { subsection ->
                assert subsection.parents.collect { it.title } == ["Section 1"]
            }

    }

    def "subsections can have tags"() {
        given:
        def configSource =
            """
Section 1:
  tags: "project:P1"
  subsections:
    - Section 1.1:
        tags: "iteration:I1"
    - Section 1.2: ["iteration:I2","iteration:I3"]
    - Section 1.3:
        tags: "iteration:I4"

Section 2: "project:P2"
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections.size() == 2
        and:
        dashboardConfiguration.sections[0].subsections[0].tags == [TestTag.withValue("iteration:I1")]
        and:
        dashboardConfiguration.sections[0].subsections[1].tags == [TestTag.withValue("iteration:I2"),TestTag.withValue("iteration:I3")]
    }

    def "subsections can have sub-subsections"() {
        given:
        def configSource =
            """
Section 1:
  tags: "project:P1"
  subsections:
    - Section 1.1:
        tags: "iteration:I1"
        subsections:
            - Section 1.1.1:
                tags: "component:ui"
            - Section 1.1.2:
                tags: "component:database"

    - Section 1.2: ["iteration:I2","iteration:I3"]
    - Section 1.3:
        tags: "iteration:I4"

Section 2: "project:P2"
"""
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections.size() == 2
        and:
        dashboardConfiguration.sections[0].subsections[0].subsections.collect { it.title } == ["Section 1.1.1", "Section 1.1.2"]
        and:
        dashboardConfiguration.sections[0].subsections[0].subsections[0].tags == [TestTag.withValue("component:ui")]
        and:
        dashboardConfiguration.sections[0].subsections[0].subsections.each { subsection ->
            assert subsection.parents.collect { it.title } == ["Section 1", "Section 1.1"]
        }

    }

    def "another example of sections and subsections"() {
        given:
        def configSource ="""
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
        when:
        def dashboardConfiguration = dashboardConfigurationLoader.loadFrom(streamed(configSource))
        then:
        dashboardConfiguration.sections.size() == 2
        and:
        dashboardConfiguration.sections[0].subsections.collect { it.title } == ["UI", "Dictionary"]
        and:
        dashboardConfiguration.sections[0].subsections.collect { it.title } == ["UI", "Dictionary"]
    }

    InputStream streamed(String source) { new ByteArrayInputStream(source.bytes) }
}
