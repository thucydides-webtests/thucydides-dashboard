package net.thucydides.reports.dashboard;

import ch.lambdaj.function.convert.Converter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.core.model.TestTag;
import net.thucydides.reports.dashboard.model.Section;
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

    private static final String DEFAULT_DASHBOARD_CONFIGURATION_FILE = "dashboard.yml";
    private static final String SUBSECTIONS = "subsections";

    /**
     * Load the default dashboard configuration, stored on a file called 'dashboard.yaml' at the root of the classpath.
     */
    public DashboardConfiguration loadDefault() {
        return loadFrom(defaultConfigurationFile());
    }

    private static InputStream defaultConfigurationFile() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_DASHBOARD_CONFIGURATION_FILE);
    }

    private final static List<Section> NO_PARENTS = Lists.newArrayList();

    public DashboardConfiguration loadFrom(InputStream inputStream) {
        Map<String, Object> fields = (Map<String, Object>) new Yaml().load(inputStream);
        List<Section> sections = readSectionsFrom(fields, NO_PARENTS );
        return new DashboardConfiguration(sections);
    }

    private List<Section> readSectionsFrom(Map<String, Object> fields, List<Section> parents) {
        Set<String> sectionTitles = fields.keySet();
        List<Section> sections = Lists.newArrayList();
        for (String sectionTitle : sectionTitles) {
            Object sectionValues = fields.get(sectionTitle);
            List<TestTag> tags = readTagsFrom(sectionValues);
            Section section = new Section(sectionTitle, tags);
            List<Section> subsections = readSubsectionsFrom(sectionValues, getParents(parents, section));
            sections.add(section.withSubsections(subsections).withParents(parents));
        }
        return sections;
    }

    private List<Section> getParents(List<Section> parents, Section section) {
        List<Section> subsectionParents = new ArrayList(parents);
        subsectionParents.add(section);
        return ImmutableList.copyOf(subsectionParents);
    }

    private List<TestTag> readTagsFrom(Object sectionValues) {
        if (containsTags(sectionValues)) {
            return convertToTags(sectionValues);
        } else if (isMultipleFieldValues(sectionValues)) {
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
                return readSectionsFrom((Map) sectionValueMap.get(SUBSECTIONS), parents);
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

    private boolean isMultipleFieldValues(Object fieldValue) {
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
            return ImmutableList.of();
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
