package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitFile;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;


import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

public class FileResource extends MapResource implements IGitFile {

    private String decodedContent = null;
    private String repositoryName;
    private String path;

    public FileResource(IGithubRestClient source, String repositoryName, String path) {
        super(source);
        assert repositoryName != null && path != null;
        this.repositoryName = repositoryName;
        this.path = path;
    }

    public FileResource(IGithubRestClient source, String repositoryName, Map<String, Object> data) {
        super(source, data);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    @Override
    public String getName() {
        return repositoryName + "/" + accessStringProperty("filename");
    }

    @Override
    public String getRepository() {
        return repositoryName;
    }

    @Override
    public String getLocalPath() {
        return accessStringProperty("filename");
    }

    @Override
    public String getContent() {
        if (decodedContent == null) {
            String contents_url = accessStringProperty("contents_url");
            if (contents_url != null) {
                Map<String, Object> resource = this.getSource().getMapResponse(contents_url);
                if (resource != null && resource.containsKey("content")) {
                    StringBuilder resultBuilder = new StringBuilder();
                    String content = (String) resource.get("content");
                    String[] lines = content.split("\n");

                    for (String line : lines) {
                        byte[] l = Base64.getDecoder().decode(line);
                        try {
                            String cur = new String(l, "UTF-8");
                            resultBuilder.append(cur);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    return resultBuilder.toString();
                }
            }
            return null;
        }
        return decodedContent;
    }

    @Override
    public String getContent(String branch) {
        Map<String, Object> contentMap = this.getSource().getFile(repositoryName, getLocalPath(), branch);

        StringBuilder resultBuilder = new StringBuilder();
        if (contentMap.containsKey("content")) {
            String content = (String) contentMap.get("content");
            String[] lines = content.split("\n");

            for (String line : lines) {
                byte[] l = Base64.getDecoder().decode(line);
                try {
                    String cur = new String(l, "UTF-8");
                    resultBuilder.append(cur);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return resultBuilder.toString();
    }

    @Override
    public String getFullPath() {
        return accessStringProperty("raw_url");
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        if (path != null && repositoryName != null) {
            this.getSource().getFile(this.repositoryName, this.path);
        }
        return null;
    }

}
