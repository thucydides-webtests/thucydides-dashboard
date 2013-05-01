package net.thucydides.reports.dashboard;

import ch.lambdaj.function.convert.Converter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.plugins.jira.service.JIRAConfiguration;
import net.thucydides.reports.dashboard.jira.JiraFilterService;
import net.thucydides.reports.dashboard.model.Section;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.flatten;

/**
 * Loads a dashboard configuration from a YAML file.
 */
public class DashboardConfigurationLoader {

    private static final String SUBSECTIONS = "subsections";
    private static final Optional<JIRAConfiguration> JIRA_NOT_CONFIGURED = Optional.absent();
    private static final List<TestTag> NO_TAGS = ImmutableList.of();

    private FilterService filterService;

    public DashboardConfigurationLoader() {
        this(Injectors.getInjector().getInstance(EnvironmentVariables.class));
    }

    public DashboardConfigurationLoader(EnvironmentVariables environmentVariables) {
        filterService = new JiraFilterService(environmentVariables);
    }

    private final static List<Section> NO_PARENTS = Lists.newArrayList();

    public DashboardConfiguration loadFrom(InputStream inputStream) {
        Map<String, Object> fields = (Map<String, Object>) new Yaml().load(inputStream);
        List<Section> sections = readSectionsFrom(fields, NO_PARENTS );
        List<Section> sectionsWithFilteredTags = updateTagsUsingFiltersIn(sections);
        return new DashboardConfiguration(sectionsWithFilteredTags);
    }

    private List<Section> updateTagsUsingFiltersIn(List<Section> sections) {
        return convert(sections, toSectionsWithTagsFromFilters(""));
    }

    private List<Section> updateTagsUsingFiltersIn(List<Section> sections, String parentFilter) {
        return convert(sections, toSectionsWithTagsFromFilters(parentFilter));
    }

    private Converter<Section, Section> toSectionsWithTagsFromFilters(final String parentFilter) {
        return new Converter<Section, Section>() {

            @Override
            public Section convert(Section from) {
                if (from.hasFilter()) {
                    String filter = joinFilters(parentFilter, from.getFilter());
                    List<TestTag> filterTags = filterService.loadTagsByFilter(filter);
                    List<Section> subsectionsWithFilteredTags = updateTagsUsingFiltersIn(from.getSubsections(), filter);
                    return from.withTags(filterTags).withSubsections(subsectionsWithFilteredTags);
                } else {
                    return from;
                }
            }
        };
    }

    private String joinFilters(String parentFilter, String filter) {
        if (StringUtils.isNotEmpty(parentFilter)) {
            return String.format("(%s) and (%s)", parentFilter, filter);
        } else {
            return filter;
        }
    }

    private List<Section> readSectionsFrom(Map<String, Object> fields, List<Section> parents) {
        Set<String> sectionTitles = fields.keySet();
        List<Section> sections = Lists.newArrayList();
        for (String sectionTitle : sectionTitles) {
            Object sectionValues = fields.get(sectionTitle);
            List<TestTag> tags = readTagsFrom(sectionValues);
            Optional<String> filter = readFilterFrom(sectionValues);
            Section section = new Section(sectionTitle, tags, filter);
            List<Section> subsections = readSubsectionsFrom(sectionValues, getParents(parents, section));
            sections.add(section.withSubsections(subsections).withParents(parents));
        }
        return ImmutableList.copyOf(sections);
    }

    private Optional<String> readFilterFrom(Object sectionValues) {
        if (isMappedFieldValues(sectionValues)) {
            return Optional.fromNullable((String)((Map)sectionValues).get("filter"));
        } else {
            return Optional.absent();
        }
    }

    private List<Section> getParents(List<Section> parents, Section section) {
        List<Section> subsectionParents = new ArrayList<Section>(parents);
        subsectionParents.add(section);
        return ImmutableList.copyOf(subsectionParents);
    }

    private List<TestTag> readTagsFrom(Object sectionValues) {
        if (containsTags(sectionValues)) {
            return convertToTags(sectionValues);
        } else if (isMappedFieldValues(sectionValues)) {
            return extractTagFieldFrom((Map)sectionValues);
        } else {
            return ImmutableList.of();
        }
    }

    private List<Section> readSubsectionsFrom(Object sectionValues, List<Section> parents) {
        if (containsSubsections(sectionValues)) {
            Map<String,Object> sectionValueMap = (Map<String,Object>) sectionValues;
            Object subsectionValues = sectionValueMap.get(SUBSECTIONS);
            if (containsListOfSubsections(subsectionValues)){
                List<Map<String,Object>> subsectionData = (List<Map<String,Object>>) sectionValueMap.get(SUBSECTIONS);
                return flatten(convert(subsectionData, toSubsections(parents)));
            } else {
                return readSectionsFrom((Map<String, Object>) sectionValueMap.get(SUBSECTIONS), parents);
            }
        } else {
            return ImmutableList.of();
        }
    }

    private boolean containsListOfSubsections(Object subsectionValues) {
        return List.class.isAssignableFrom(subsectionValues.getClass());
    }

    private Converter<Map<String, Object>, List<Section>> toSubsections(final List<Section> parents) {
        return new Converter<Map<String,Object>, List<Section>>() {
            @Override
            public List<Section> convert(Map<String, Object> fields) {
                return readSectionsFrom(fields, parents);
            }
        };
    }

    private boolean containsSubsections(Object sectionValues) {
        return ((sectionValues != null)
                 && (Map.class.isAssignableFrom(sectionValues.getClass()))
                 && ((Map) sectionValues).containsKey(SUBSECTIONS));
    }

    private List<TestTag> extractTagFieldFrom(Map fieldValues) {
        if (fieldValues.containsKey("tags")) {
            return convertToTags(fieldValues.get("tags"));
        } else {
            return ImmutableList.of();
        }
    }

    private boolean isMappedFieldValues(Object fieldValue) {
        return (fieldValue != null) && Map.class.isAssignableFrom(fieldValue.getClass());
    }

    private boolean containsTags(Object fieldValue) {
        return (isASingleTag(fieldValue) ||  isAListOfTags(fieldValue));
    }

    private List<TestTag> convertToTags(Object fieldValue) {
        if (isAListOfTags(fieldValue)) {
            return convert(fieldValue, fromStringToTag());
        } else if (isASingleTag(fieldValue)) {
            return ImmutableList.of(TestTag.withValue(fieldValue.toString()));
        } else {
            return NO_TAGS;
        }
    }

    private boolean isASingleTag(Object fieldValue) {
        return fieldValue != null && fieldValue instanceof String;
    }

    private Converter<String, TestTag> fromStringToTag() {
        return new Converter<String, TestTag>() {
            @Override
            public TestTag convert(String from) {
                return TestTag.withValue(from);
            }
        };
    }

    private boolean isAListOfTags(Object fieldValue) {
        return fieldValue != null && containsListOfSubsections(fieldValue);
    }
}
