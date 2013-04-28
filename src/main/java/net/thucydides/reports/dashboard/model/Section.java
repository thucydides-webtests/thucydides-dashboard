package net.thucydides.reports.dashboard.model;

import com.google.common.collect.ImmutableList;
import net.thucydides.core.model.ReportNamer;
import net.thucydides.core.model.ReportType;
import net.thucydides.core.model.TestTag;

import java.util.List;

import static ch.lambdaj.Lambda.joinFrom;

public class Section {

    private final String title;
    private final List<TestTag> tags;
    private final List<Section> subsections;
    private final List<Section> parents;

    public Section withSubsections(List<Section> subsections) {
        return new Section(title, tags, subsections, parents);
    }

    public Section withParents(List<Section> parents) {
        return new Section(title, tags, subsections, parents);
    }

    public Section(String title, List<TestTag> tags) {
        this.title = title;
        this.tags = ImmutableList.copyOf(tags);
        this.subsections = ImmutableList.of();
        this.parents = ImmutableList.of();
    }

    public Section(String title, List<TestTag> tags, List<Section> subsections) {
        this.title = title;
        this.tags = ImmutableList.copyOf(tags);
        this.subsections = ImmutableList.copyOf(subsections);
        this.parents = ImmutableList.of();
    }

    public Section(String title, List<TestTag> tags, List<Section> subsections, List<Section> parents) {
        this.title = title;
        this.tags = ImmutableList.copyOf(tags);
        this.subsections = ImmutableList.copyOf(subsections);
        this.parents = ImmutableList.copyOf(parents);
    }

    public String getTitle() {
        return title;
    }

    public List<TestTag> getTags() {
        return tags;
    }

    public List<Section> getSubsections() {
        return subsections;
    }

    public List<Section> getParents() {
        return parents;
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
