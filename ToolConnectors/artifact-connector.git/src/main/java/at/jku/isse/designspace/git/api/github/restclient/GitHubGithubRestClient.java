package at.jku.isse.designspace.git.api.github.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class GitHubGithubRestClient implements IGithubRestClient {

    private static final String GITHUB_API_LINK = "https://api.github.com";
    private static final int MAX_ELEMENTS_PER_PAGE = 30;

    private final CloseableHttpClient httpClient;
    private final Header header;
    private final String userName;
    private final ObjectMapper mapper;

    public GitHubGithubRestClient(String username, String accessToken) {
        this.httpClient = HttpClients.custom().build();
        this.header = new BasicHeader("Authorization", "token " + accessToken);
        this.userName = username;
        this.mapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> getIssue(String repositoryName, int key) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/issues/" + key)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getIssues(String repositoryName) {
        try {
            ArrayList<Map<String, Object>> issuesMap = new ArrayList<>();
            ArrayList<Map<String, Object>> currentResult;
            int currentPage = 1;
            //getting all open issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/issues?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage)
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    issuesMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            currentPage = 1;
            //getting all closed issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/issues?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage + "&state=closed")
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    issuesMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            return issuesMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getIssueTimeline(String repositoryName, int key) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/issues/" + key + "/timeline")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getPullRequest(String repositoryName, int key) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/pulls/" + key)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getPullRequests(String repositoryName) {
        try {
            ArrayList<Map<String, Object>> pullsMap = new ArrayList<>();
            ArrayList<Map<String, Object>> currentResult;
            int currentPage = 1;
            //getting all open issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/pulls?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage)
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    pullsMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            currentPage = 1;
            //getting all closed issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/pulls?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage + "&state=closed")
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    pullsMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            return pullsMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getCommit(String repositoryName, String sha) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/commits/" + sha)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getCommits(String repositoryName, int pullRequestKey) {
        try {
            ArrayList<Map<String, Object>> commitsMap = new ArrayList<>();
            ArrayList<Map<String, Object>> currentResult;
            int currentPage = 1;
            //getting all open issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/pulls/" + pullRequestKey + "/commits?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage)
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    commitsMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            return commitsMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getCommits(String repositoryName) {
        try {
            ArrayList<Map<String, Object>> commitsMap = new ArrayList<>();
            ArrayList<Map<String, Object>> currentResult;
            int currentPage = 1;
            //getting all open issues for the repo
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/commits?per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage)
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatListOfMapResponse(responseString);
                    commitsMap.addAll(currentResult);
                }
                currentPage++;
            } while (currentResult.size() == MAX_ELEMENTS_PER_PAGE);

            return commitsMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getCommitsSince(String repositoryName, ZonedDateTime zonedDateTime) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/commits?since=" + zonedDateTime.toString())
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getBranch(String repositoryName, String branchName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/branches/" + branchName)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>>  getBranches(String repositoryName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/branches")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getProject(int projectId) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/projects/" + projectId)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getProject(String projectName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/projects/" + this.userName + "/" + projectName)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getProjectColumn(int columnId) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/projects/columns/" + columnId)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getRepository(String repositoryName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public ArrayList<Map<String, Object>> getWebhooks(String repositoryName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/hooks")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getRepositories() {

        try {
            ArrayList<Map<String, Object>> searchResult = new ArrayList<>();
            ArrayList<Map<String, Object>> result;
            Map<String, Object> currentResult;
            int currentPage = 1;
            do {
                HttpUriRequest request = RequestBuilder.get()
                        .setUri(GITHUB_API_LINK + "/search/repositories?q=user:" + this.userName + "&per_page=" + MAX_ELEMENTS_PER_PAGE + "&page=" + currentPage)
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    currentResult = formatMapResponse(responseString);
                    result = (ArrayList) currentResult.get("items");
                    searchResult.addAll(result);
                }

                currentPage++;
            } while (result.size() == MAX_ELEMENTS_PER_PAGE);

            return searchResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getIssueComments(String repositoryName, int issueKey) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/issues/" + issueKey + "/comments")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getCommitComments(String repositoryName, String sha) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/commits/" + sha + "/comments")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Map<String, Object>> getTags(String repositoryName) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/tags")
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getUser(String userId) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/users/" + userId)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getCompareResponse(String repositoryName, String branch, String sha) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/compare/" + branch + "..." + sha)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String deleteWebhook(String repositoryName, String webhookName) {
        assert repositoryName != null && webhookName != null;
        Optional<Integer> webHookId = findWebHookId(repositoryName, webhookName);
        if (webHookId.isPresent()) {
            try {
                HttpUriRequest request = RequestBuilder.delete()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/hooks/" + webHookId.get())
                        .setHeader(header)
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = "hook was deleted";
                    if (response.getEntity() != null) {
                        responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    }
                    EntityUtils.consume(response.getEntity());
                    return responseString;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }

    @Override
    public String createWebHook(String repositoryName, String forwardAddress) {
        try {
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/hooks")
                    .setHeader(header)
                    .setEntity(new StringEntity("{\"events\":[\"commit_comment\", \"discussion_comment\", \"create\", \"delete\", \"issues\", \"issue_comment\", \"project_card\", \"project_column\", \"project\", \"push\", \"pull_request\"], \"config\":{\"url\": \"" + forwardAddress + "\", \"content_type\":\"json\"}}", ContentType.APPLICATION_JSON))
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return responseString;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @Override
    public String setWebhookActive(String repositoryName, String forwardAddress, boolean active) {
        assert repositoryName != null;
        Optional<Integer> webHookId = findWebHookId(repositoryName, forwardAddress);
        if (webHookId.isPresent()) {
            try {
                HttpUriRequest request = RequestBuilder.patch()
                        .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/hooks/" + webHookId.get())
                        .setHeader(header)
                        .setEntity(new StringEntity("{\"active\":" + active + "}", ContentType.APPLICATION_JSON))
                        .build();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    EntityUtils.consume(response.getEntity());
                    return responseString;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "error";
    }

    @Override
	public ArrayList<Map<String, Object>> getListResponse(String url) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(url)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatListOfMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getFile(String repositoryName, String localPath) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/" + localPath)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getFile(String repositoryName, String filePath, String branch) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(GITHUB_API_LINK + "/repos/" + this.userName + "/" + repositoryName + "/contents/" + filePath + "?ref=" + branch)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
	public Map<String, Object> getMapResponse(String url) {
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(url)
                    .setHeader(header)
                    .build();
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                EntityUtils.consume(response.getEntity());
                return formatMapResponse(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Optional<Integer> findWebHookId(String repositoryName, String forwardAddress) {
        ArrayList<Map<String, Object>> webhooks = this.getWebhooks(repositoryName);
        if (webhooks != null) {
            for (Map<String, Object> hook : webhooks) {
                if (hook.containsKey("config")) {
                    Map<String, Object> config = (Map<String, Object>) hook.get("config");
                    if (forwardAddress.equals(config.get("url"))) {
                        return Optional.of((int) hook.get("id"));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, Object> formatMapResponse(String response) {
        return jsonToMap(response);
    }

    private ArrayList<Map<String, Object>> formatListOfMapResponse(String response) {
        ArrayList<Map<String, Object>> result = jsonToListOfMaps(response);
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }

    private Map<String, Object> jsonToMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private ArrayList<Map<String, Object>> jsonToListOfMaps(String json) {
        try {
            return mapper.readValue(json, new TypeReference<ArrayList<Map<String, Object>>>() {});
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

}
