package com.eoral.commentedoutcoderemover;

import com.eoral.commentedoutcoderemover.sonarqube.Issue;
import com.eoral.commentedoutcoderemover.sonarqube.SonarQubeUtils;
import com.eoral.commentedoutcoderemover.sonarqube.TextRange;
import com.eoral.deletecharsfromfilebyposition.BehaviorAfterDeletionRulesExecutedForEachLine;
import com.eoral.deletecharsfromfilebyposition.DeletionRule;
import com.eoral.deletecharsfromfilebyposition.DeletionRuleExecution;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        App app = new App();
        Properties properties = app.loadProperties();
        String sonarQubeBaseUrl = properties.getProperty("sonarQubeBaseUrl");
        String projectKey = properties.getProperty("projectKey");
        String projectDirectory = properties.getProperty("projectDirectory");
        String sonarQubeToken = properties.getProperty("sonarQubeToken");
        Charset charsetOfCodeFiles = Charset.forName(properties.getProperty("charsetOfCodeFiles"));
        BehaviorAfterDeletionRulesExecutedForEachLine behaviorAfterDeletionRulesExecutedForEachLine = new BehaviorAfterDeletionRulesExecutedForEachLine();
        behaviorAfterDeletionRulesExecutedForEachLine.setDeleteLineIfBlank(true);
        List<Issue> allIssues = SonarQubeUtils.getAllIssues(sonarQubeBaseUrl, projectKey, sonarQubeToken);
        List<Issue> issues = SonarQubeUtils.filterOpenIssuesCausedByCommentedOutCode(allIssues);
        Map<String, List<Issue>> issuesGroupedByComponent = SonarQubeUtils.groupIssuesByComponent(issues);
        for (String component : issuesGroupedByComponent.keySet()) {
            List<Issue> issuesOfComponent = issuesGroupedByComponent.get(component);
            List<DeletionRule> deletionRulesOfComponent = new ArrayList<>();
            for (Issue issue : issuesOfComponent) {
                TextRange textRange = issue.getTextRange();
                deletionRulesOfComponent.addAll(app.convertToDeletionRules(textRange));
            }
            String relativeFilePathOfComponent = app.extractRelativeFilePathFromComponent(component);
            Path pathOfComponent = Path.of(projectDirectory, relativeFilePathOfComponent);
            new DeletionRuleExecution().deleteCharsFromFile(
                    pathOfComponent, charsetOfCodeFiles, deletionRulesOfComponent, behaviorAfterDeletionRulesExecutedForEachLine);
        }
    }

    public Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream("/app.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    public List<DeletionRule> convertToDeletionRules(TextRange textRange) {
        List<DeletionRule> deletionRules = new ArrayList<>();
        int startLine = textRange.getStartLine();
        int startOffset = textRange.getStartOffset();
        int endLine = textRange.getEndLine();
        int endOffset = textRange.getEndOffset();
        if (startLine == endLine) {
            deletionRules.add(new DeletionRule(startLine, startOffset + 1, endOffset));
        } else {
            deletionRules.add(new DeletionRule(startLine, startOffset + 1, null));
            for (int line = startLine + 1; line < endLine; line++) {
                deletionRules.add(new DeletionRule(line, null, null));
            }
            deletionRules.add(new DeletionRule(endLine, 1, endOffset));
        }
        return deletionRules;
    }

    public String extractRelativeFilePathFromComponent(String component) {
        int indexOfFirstColon = component.indexOf(":");
        return component.substring(indexOfFirstColon + 1);
    }
}
