package net.thucydides.reports.dashboard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.reports.dashboard.model.Section;

import java.util.List;

/**
 * Dashboard configuration is stored as a simple YAML file.
 */
public class DashboardConfiguration {

    private final List<Section> sections;

    public DashboardConfiguration(List<Section> sections) {
        this.sections = ImmutableList.copyOf(sections);
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<List<Section>> getSectionRows(int rowSize) {
        return Lists.partition(sections, rowSize);
    }

}
