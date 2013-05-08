package net.thucydides.reports.dashboard;

import net.thucydides.core.guice.Injectors;
import net.thucydides.core.issues.IssueTracking;
import net.thucydides.core.reports.TestOutcomeLoader;
import net.thucydides.core.reports.TestOutcomes;
import net.thucydides.core.reports.html.HtmlAggregateStoryReporter;
import net.thucydides.core.reports.html.HtmlReporter;
import net.thucydides.core.requirements.RequirementsProviderService;
import net.thucydides.core.requirements.reports.RequirementsOutcomes;
import net.thucydides.core.requirements.reports.RequirmentsOutcomeFactory;
import net.thucydides.reports.dashboard.model.Section;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlDashboardReporter extends HtmlReporter {

    private final String projectName;
    private final DashboardConfiguration configuration;
    private final RequirmentsOutcomeFactory requirementsFactory;
    private final IssueTracking issueTracking;

    private final String DASHBOARD_REPORT = "freemarker/dashboard.ftl";
    private final String DASHBOARD_REPORT_NAME = "dashboard.html";

    public HtmlDashboardReporter(String projectName, File outputDirectory, InputStream configurationSource) {
        this(projectName, outputDirectory, new DashboardConfigurationLoader().loadFrom(configurationSource));
    }

    protected HtmlDashboardReporter(String projectName, File outputDirectory, DashboardConfiguration configuration) {
        this.projectName = projectName;
        setOutputDirectory(outputDirectory);
        this.configuration = configuration;
        this.issueTracking = Injectors.getInjector().getInstance(IssueTracking.class);
        RequirementsProviderService requirementsProviderService = Injectors.getInjector().getInstance(RequirementsProviderService.class);
        this.requirementsFactory = new RequirmentsOutcomeFactory(requirementsProviderService.getRequirementsProviders(),  issueTracking);
    }

    public String getProjectName() {
        return projectName;
    }

    public void generateReportsForTestResultsFrom(File sourceDirectory) throws IOException {
        TestOutcomes allTestOutcomes = loadTestOutcomesFrom(sourceDirectory);

        generateReportsForSections(allTestOutcomes, configuration.getSections());
        generateDashboardReportFor(allTestOutcomes);
   }

    private void generateDashboardReportFor(TestOutcomes outcomes) throws IOException {

        RequirementsOutcomes requirementsOutcomes = requirementsFactory.buildRequirementsOutcomesFrom(outcomes);

        Map<String,Object> context = new HashMap<String,Object>();
        context.put("dashboard", configuration);
        context.put("allTestOutcomes", outcomes);
        context.put("requirementsFactory", requirementsFactory);

        requirementsOutcomes.getFailingRequirementsCount();

        String htmlContents = mergeTemplate(DASHBOARD_REPORT).usingContext(context);
        copyResourcesToOutputDirectory();
        writeReportToOutputDirectory(DASHBOARD_REPORT_NAME, htmlContents);

    }

    private void generateReportsForSections(TestOutcomes testOutcomes, List<Section> sections) throws IOException {
        for(Section section : sections) {
            TestOutcomes sectionOutcomes = testOutcomes.withTags(section.getTags()).withLabel(section.getTitle());
            generateSectionReport(section, sectionOutcomes);
            generateReportsForSections(sectionOutcomes, section.getSubsections());
        }
    }

    private void generateSectionReport(Section section, TestOutcomes sectionOutcomes) throws IOException {
        HtmlAggregateStoryReporter sectionReporter = new HtmlAggregateStoryReporter(getProjectName(),"../");
        File sectionDirectory = createSectionDirectory(section);
        sectionReporter.setOutputDirectory(sectionDirectory);
        sectionReporter.generateReportsForTestResultsIn(sectionOutcomes);
    }

    private File createSectionDirectory(Section section) {
        File sectionDirectory = new File(getOutputDirectory(), section.getDirectoryName());
        sectionDirectory.mkdirs();
        return sectionDirectory;
    }

    private TestOutcomes loadTestOutcomesFrom(File sourceDirectory) throws IOException {
        return TestOutcomeLoader.testOutcomesIn(sourceDirectory).withHistory();
    }
}
