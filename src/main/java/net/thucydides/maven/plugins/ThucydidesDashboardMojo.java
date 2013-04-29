package net.thucydides.maven.plugins;

import net.thucydides.core.reports.html.HtmlAggregateStoryReporter;
import net.thucydides.reports.dashboard.HtmlDashboardReporter;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Generate the Thucydides multi-project dashboard
 * 
 * @goal dashboard
 * @phase verify
 */
public class ThucydidesDashboardMojo extends ThucydidesMojo {

    private HtmlDashboardReporter reporter;

    public void execute() throws MojoExecutionException {
        prepareExecution();

        try {
            generateDashboard();
        } catch (IOException e) {
            throw new MojoExecutionException("Error generating thucydides dashboard", e);
        }
    }

    protected HtmlDashboardReporter getReporter() {
        if (reporter == null) {
            reporter = new HtmlDashboardReporter(projectKey, outputDirectory);
        }
        return reporter;

    }

    private void generateDashboard() throws IOException {
        getReporter().setOutputDirectory(outputDirectory);
        getReporter().generateReportsForTestResultsFrom(sourceDirectory);
    }
}
