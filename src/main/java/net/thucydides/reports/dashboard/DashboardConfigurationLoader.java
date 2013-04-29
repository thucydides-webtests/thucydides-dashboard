package net.thucydides.reports.dashboard;

import ch.lambdaj.function.convert.Converter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.plugins.jira.service.JIRAConfiguration;
import net.thucydides.plugins.jira.service.JIRAConnection;
import net.thucydides.plugins.jira.service.SystemPropertiesJIRAConfiguration;
import net.thucydides.reports.dashboard.model.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import thucydides.plugins.jira.soap.RemoteIssue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.flatten;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Loads a dashboard configuration from a YAML file.
 */
public class DashboardConfigurationLoader {

    private static final String DEFAULT_DASHBOARD_CONFIGURATION_FILE = "dashboard.yml";
    private static final String SUBSECTIONS = "subsections";
    private static final Optional<JIRAConfiguration> JIRA_NOT_CONFIGURED = Optional.absent();
    private static final List<TestTag> NO_TAGS = ImmutableList.of();
    private static final int MAX_RESULTS = 1000;

    private final EnvironmentVariables environmentVariables;
    private Optional<JIRAConfiguration> jiraConfiguration;
    private JIRAConnection jiraConnection;

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardConfigurationLoader.class);

    public DashboardConfigurationLoader() {
        this(Injectors.getInjector().getInstance(EnvironmentVariables.class));
    }

    public DashboardConfigurationLoader(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public Optional<JIRAConfiguration> getJiraConfiguration() {
        if (jiraConfiguration == null) {
            if (jiraDefinedIn(environmentVariables)) {
                JIRAConfiguration configuration = new SystemPropertiesJIRAConfiguration(environmentVariables); 
                jiraConfiguration = Optional.of(configuration);
            } else {
                jiraConfiguration = JIRA_NOT_CONFIGURED;
            }
        }
        return jiraConfiguration;
    }

    public JIRAConnection getJIRAConnection() {
        if (jiraConnection == null) {
            jiraConnection = new JIRAConnection(jiraConfiguration.get());
        }
        return jiraConnection;
    }

    private boolean jiraDefinedIn(EnvironmentVariables variables) {
        return isNotEmpty(variables.getProperty(ThucydidesSystemProperty.JIRA_URL));
    }

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
        List<Section> sectionsWithFilteredTags = updateTagsUsingFiltersIn(sections);
        return new DashboardConfiguration(sectionsWithFilteredTags);
    }

    private List<Section> updateTagsUsingFiltersIn(List<Section> sections) {
        return convert(sections, toSectionsWithTagsFromFilters());
    }

    private Converter<Section, Section> toSectionsWithTagsFromFilters() {
        return new Converter<Section, Section>() {

            @Override
            public Section convert(Section from) {
                if (from.hasFilter()) {
                    List<TestTag> filterTags = loadFilterTagsFor(from.getFilter());
                    List<Section> subsectionsWithFilteredTags = updateTagsUsingFiltersIn(from.getSubsections());
                    return from.withTags(filterTags)
                               .withSubsections(subsectionsWithFilteredTags);
                } else {
                    return from;
                }
            }
        };
    }

    private List<TestTag> loadFilterTagsFor(String filter) {
        if (getJiraConfiguration().isPresent()) {
            try {
                String token = getJIRAConnection().getAuthenticationToken();
                RemoteIssue[] matchingIssues = getJIRAConnection().getJiraSoapService().getIssuesFromJqlSearch(token, filter, MAX_RESULTS);
                return convert(ImmutableList.copyOf(matchingIssues), toIssueKeyTags());
            } catch (Exception e) {
                LOGGER.warn("Failed to load tags from JIRA: " + e.getMessage());
            }
        }
        return NO_TAGS;
    }

    private Converter<RemoteIssue, TestTag> toIssueKeyTags() {
        return new Converter<RemoteIssue, TestTag>() {

            @Override
            public TestTag convert(RemoteIssue from) {
                return TestTag.withName(from.getKey()).andType("issue");
            }
        };
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
        List<Section> subsectionParents = new ArrayList(parents);
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
