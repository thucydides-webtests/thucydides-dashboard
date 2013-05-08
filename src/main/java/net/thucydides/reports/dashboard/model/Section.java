package net.thucydides.reports.dashboard.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.thucydides.core.model.ReportNamer;
import net.thucydides.core.model.ReportType;
import net.thucydides.core.model.TestTag;

import java.util.List;

import static ch.lambdaj.Lambda.joinFrom;

public class Section {

    private final String title;
    private final List<TestTag> tags;
    private final Optional<String> filter;
    private final ChartType chartType;
    private final List<Section> subsections;
    private final List<Section> parents;

    private static final Optional<String> NO_FILTER = Optional.absent();

    public Section withSubsections(List<Section> subsections) {
        return new Section(title, tags, filter, chartType, subsections, parents);
    }

    public Section withParents(List<Section> parents) {
        return new Section(title, tags, filter, chartType, subsections, parents);
    }

    public Section(String title, List<TestTag> tags, Optional<String> filter,  ChartType chartType) {
        this.title = title;
        this.filter = filter;
        this.chartType = chartType;
        this.tags = ImmutableList.copyOf(tags);
        this.subsections = ImmutableList.of();
        this.parents = ImmutableList.of();
    }

    public Section(String title, List<TestTag> tags,  List<Section> subsections) {
        this(title, tags, NO_FILTER, ChartType.TESTS, subsections);
    }

    public Section(String title, List<TestTag> tags, Optional<String> filter,  ChartType chartType, List<Section> subsections) {
        this.title = title;
        this.tags = ImmutableList.copyOf(tags);
        this.filter = filter;
        this.chartType = chartType;
        this.subsections = ImmutableList.copyOf(subsections);
        this.parents = ImmutableList.of();
    }

    public Section(String title, List<TestTag> tags, Optional<String> filter,  ChartType chartType, List<Section> subsections, List<Section> parents) {
        this.title = title;
        this.filter = filter;
        this.chartType = chartType;
        this.tags = ImmutableList.copyOf(tags);
        this.subsections = ImmutableList.copyOf(subsections);
        this.parents = ImmutableList.copyOf(parents);
    }

    public Section withTags(List<TestTag> tags) {
        return new Section(title, tags, filter, chartType, subsections, parents);
    }

    public String getTitle() {
        return title;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public List<TestTag> getTags() {
        return ImmutableList.copyOf(tags);
    }

    public boolean hasFilter() {
        return filter.isPresent();
    }

    public String getFilter() {
        return filter.get();
    }

    public List<Section> getSubsections() {
        return ImmutableList.copyOf(subsections);
    }

    public List<Section> getParents() {
        return ImmutableList.copyOf(parents);
    }

    public String getDirectoryName() {
        return getDirectoryName(getParents());
    }

    public String getDirectoryName(List<Section> parents) {
        String sectionTitles = parents.isEmpty() ? getTitle() : joinFrom(parents).getTitle() + ", " + getTitle();
        ReportNamer reportNamer = ReportNamer.forReportType(ReportType.ROOT);
        return reportNamer.getNormalizedTestNameFor("sections:" + sectionTitles);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
