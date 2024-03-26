package com.eoral.commentedoutcoderemover.sonarqube;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SonarQubeUtils {

    public static Map<String, List<Issue>> groupIssuesByComponent(List<Issue> issues) {
        return issues.stream().collect(Collectors.groupingBy(Issue::getComponent));
    }

    public static List<Issue> filterOpenIssuesCausedByCommentedOutCode(List<Issue> issues) {
        return issues.stream()
                .filter(issue -> issue.getStatus().equals(Constants.ISSUE_STATUS_OPEN)
                        && issue.getRule().endsWith(Constants.COMMENTED_OUT_CODE_RULE_SUFFIX))
                .collect(Collectors.toList());
    }

    public static List<Issue> getAllIssues(String sonarQubeBaseUrl, String projectKey, String token) {
        List<Issue> allIssues = new ArrayList<>();
        int page = 0;
        int pageSize = 100;
        while (true) {
            page++;
            IssueSearchResponse issueSearchResponse = getIssues(sonarQubeBaseUrl, projectKey, page, pageSize, token);
            int issueCount = 0;
            if (issueSearchResponse.getIssues() != null) {
                allIssues.addAll(issueSearchResponse.getIssues());
                issueCount = issueSearchResponse.getIssues().size();
            }
            if (issueCount < pageSize) {
                break;
            }
        }
        return allIssues;
    }

    public static IssueSearchResponse getIssues(
            String sonarQubeBaseUrl, String projectKey, int page, int pageSize, String token) {
        try {
            String str = getIssuesAsString(sonarQubeBaseUrl, projectKey, page, pageSize, token);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(str, IssueSearchResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getIssuesAsString(
            String sonarQubeBaseUrl, String projectKey, int page, int pageSize, String token) throws IOException, InterruptedException {
        String url = sonarQubeBaseUrl + "/api/issues/search?componentKeys=" + projectKey + "&p=" + page + "&ps=" + pageSize;
        String basicAuthCredentials = generateBasicAuthCredentials(token, null);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Basic " + basicAuthCredentials)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String generateBasicAuthCredentials(String username, String password) {
        String input = "";
        if (username != null) {
            input += username;
        }
        input += ":";
        if (password != null) {
            input += password;
        }
        return Base64.getEncoder().encodeToString((input).getBytes());
    }
}
