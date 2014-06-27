package net.thucydides.reports.dashboard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.reports.dashboard.model.Section;

import java.util.List;

/**
 * Dashboard configuration is stored as a simple YAML file.
 */
public class DashboardConfiguration {

    public final static String DEFAULT_TITLE = "Project Dashboard";

    private final List<Section> sections;
    private final String title;

    public DashboardConfiguration(List<Section> sections) {
        this.title = DEFAULT_TITLE;
        this.sections = ImmutableList.copyOf(sections);
    }
    public DashboardConfiguration(String title, List<Section> sections) {
        this.title = (title == null) ? DEFAULT_TITLE : title;
        this.sections = ImmutableList.copyOf(sections);
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<List<Section>> getSectionRows(int rowSize) {
        return Lists.partition(sections, rowSize);
    }

    public String getTitle() {
        return title;
    }
}
