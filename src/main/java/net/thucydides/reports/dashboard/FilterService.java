package net.thucydides.reports.dashboard;

import net.thucydides.core.model.TestTag;

import java.util.List;

public interface FilterService {
    List<TestTag> loadTagsByFilter(String filter);
}
