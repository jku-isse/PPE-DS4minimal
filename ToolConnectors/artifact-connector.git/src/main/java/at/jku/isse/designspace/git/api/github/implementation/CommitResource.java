package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitResource extends MapResource implements IGitCommit {

    private final String repositoryName;
    private final String sha;

    private List<IGitComment> comments;
    private String[] branches;

    public CommitResource(IGithubRestClient source, String repositoryName, String[] branches, String sha) {
        super(source);
        assert repositoryName != null && sha != null && branches != null;
        this.repositoryName = repositoryName;
        this.sha = sha;
        this.branches = branches;
    }

    public CommitResource(IGithubRestClient source, String repositoryName, String[] branches, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        assert repositoryName != null && branches != null;
        this.repositoryName = repositoryName;
        this.sha = getSha();
        this.branches = branches;
        if (this.sha == null) throw new InsufficientDataException();
    }

    @Override
    public String getSha() {
        return accessStringProperty("sha");
    }

    @Override
    public String getRepository() {
        return this.repositoryName;
    }

    @Override
    public String getHTMLUrl() {
        return accessStringProperty("html_url");
    }

    @Override
    public String getCommitMessage() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object commitInner = resource.get("commit");
            if (commitInner != null) {
                Map<String, Object> content = (Map<String, Object>) commitInner;
                Object description = content.get("message");
                return (String) description;
            }
        }
        return null;
    }

    @Override
    public int getTotalDeletions() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object stats = resource.get("stats");
            if (stats != null) {
                Map<String, Object> content = (Map<String, Object>) stats;
                Object deletions = content.get("deletions");
                return (int) deletions;
            }
        }
        return -1;
    }

    @Override
    public int getTotalAdditions() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object stats = resource.get("stats");
            if (stats != null) {
                Map<String, Object> content = (Map<String, Object>) stats;
                Object additions = content.get("additions");
                return (int) additions;
            }
        }
        return -1;
    }

    @Override
    public String[] getBranches() {
        return this.branches;
    }

    @Override
    public List<IGitFile> getFiles() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object files_ = resource.get("files");
            if (files_ != null) {
                ArrayList<Map<String, Object>> files = (ArrayList<Map<String, Object>>) files_;
                ArrayList<IGitFile> fileList = new ArrayList<>();
                for (Map<String, Object> file : files) {
                    fileList.add(new FileResource(this.getSource(), this.repositoryName, file));
                }
                return fileList;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public IGitUser getAuthor() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("author");
            if (incompleteUser_ != null) {
                Map<String, Object> incompleteUser = (Map<String, Object>) incompleteUser_;
                try {
                    return new UserResource(this.getSource(), incompleteUser);
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public IGitUser getCommitter() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("committer");
            if (incompleteUser_ != null) {
                Map<String, Object> incompleteUser = (Map<String, Object>) incompleteUser_;
                try {
                    return new UserResource(this.getSource(), incompleteUser);
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public List<IGitComment> getComments() {
        if (this.comments == null) {
            CommitCommentsResource commitComments = new CommitCommentsResource(this.getSource(), this.repositoryName, this.sha);
            this.comments = commitComments.getResources();
        }
        return this.comments;
    }

    @Override
    public int[] getLinkedIssueKeys() {
        ArrayList<Integer> keys = new ArrayList<>();

        String description = getCommitMessage();
        if (description != null) {
            keys.addAll(getKeysFromText(description));
        }

        List<IGitComment> comments = this.getComments();
        if (comments != null) {
            for (IGitComment comment : comments) {
                String body = comment.getBody();
                if (body != null) {
                    keys.addAll(getKeysFromText(body));
                }
            }
        }

        return keys.stream().distinct().mapToInt(i->i).toArray();
    }

    @Override
    public String getURI() {
        return accessStringProperty("url");
    }

    private ArrayList<Integer> getKeysFromText(String text) {
        assert text != null;
        String[] splits = text.split("#");
        ArrayList<Integer> keys = new ArrayList<>();
        for (int i=1; i<splits.length; i++) {
            StringBuilder number = new StringBuilder();
            for (int j=0; j<splits[i].length(); j++) {
                char cur = splits[i].charAt(j);
                if (cur >= '0' && cur <= '9') {
                    number.append(cur);
                } else {
                    break;
                }
            }
            if (number.length() > 0) {
                keys.add(Integer.parseInt(number.toString()));
            }
        }
        return keys;
    }


    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return source.getCommit(this.repositoryName, this.sha);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \n");

        sb.append("sha = ").append(getSha()).append(",\n");
        sb.append("commitMessage = ").append(getCommitMessage()).append(",\n");
        sb.append("html_url = ").append(getHTMLUrl()).append(",\n");
        sb.append("author = ").append(getAuthor().getUserId()).append(",\n");
        sb.append("repository = ").append(getRepository()).append(",\n");
        sb.append("totalAdditions = ").append(getTotalAdditions()).append(",\n");
        sb.append("totalDeletions = ").append(getTotalDeletions()).append(",\n");

        List<IGitComment> comments = getComments();
        sb.append("comments = [ \n");
        for (IGitComment comment : comments) {
            sb.append("{ \n");
            sb.append("author = ").append(comment.getAuthor().getUserId()).append(",\n");
            sb.append("body = ").append(comment.getBody()).append(",\n");
            sb.append("} \n");
        }
        sb.append("], \n");

        List<IGitFile> files = getFiles();
        sb.append("files = [ \n");
        for (IGitFile file : files) {
            sb.append("name = ").append(file.getName()).append(",\n");
            sb.append("path = ").append(file.getFullPath()).append(",\n");
        }
        sb.append("] \n");

        sb.append("\n}");

        return sb.toString();
    }

}
