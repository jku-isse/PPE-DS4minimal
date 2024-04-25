package at.jku.isse.designspace.git.updateservice;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.ListProperty;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.changemanagement.GitChange;
import at.jku.isse.designspace.git.service.GitService;
import at.jku.isse.designspace.git.service.IGitService;
import lombok.extern.slf4j.Slf4j;
import at.jku.isse.designspace.git.model.GitBaseElementType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class GitChangePatcher implements IGitChangePatcher {

    private IGitService gitService;

    public GitChangePatcher(IGitService gitService) {
        this.gitService = gitService;
    }

    @Override
    public boolean applyIssueChange(GitChange<IGitIssue> issueChange) {
        boolean newInstance = this.gitService.isIssueFetched(issueChange.getRepoName(), issueChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(issueChange.getRepoName(), issueChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            if (subjectToChange != null && subjectToChange.hasProperty(issueChange.getFieldName())) {
                IGitIssue issue = issueChange.getNewValue();
                if (issue != null) {
                    log.debug("Git-Service: Update on issue " + issue.getKey() + " | property " + issueChange.getFieldName());
                    Instance subtype = this.gitService.transferIssue(issue);
                    if (issueChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                        subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                        this.gitService.transferIssue(issue);
                        return true;
                    }
                    return processSubtypeModification(subjectToChange, issueChange, subtype);
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyPullRequestChange(GitChange<IGitPullRequest> pullRequestChange) {
        boolean newInstance = this.gitService.isIssueFetched(pullRequestChange.getRepoName(), pullRequestChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(pullRequestChange.getRepoName(), pullRequestChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            if (subjectToChange != null && subjectToChange.hasProperty(pullRequestChange.getFieldName())) {
                IGitIssue pullRequest = pullRequestChange.getNewValue();
                log.debug("Git-Service: Update on Pull Request " + pullRequest.getKey() + " | property " + pullRequestChange.getFieldName());
                if (pullRequest != null) {
                    Instance subtype = this.gitService.transferIssue(pullRequest);
                    if (pullRequestChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                        subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                        this.gitService.transferIssue(pullRequest);
                        return true;
                    }
                    return processSubtypeModification(subjectToChange, pullRequestChange, subtype);
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyCommitChange(GitChange<IGitCommit> commitChange) {
        if (commitChange.getChangeType() == GitChange.ChangeType.PULL_REQUEST) {
            boolean newInstance = !this.gitService.isIssueFetched(commitChange.getRepoName(), commitChange.getKey());
            Optional<Instance> issue_ = this.gitService.getPullRequest(commitChange.getRepoName(), commitChange.getKey());
            if (issue_.isPresent() && !newInstance) {
                Instance subjectToChange = issue_.get();
                if (subjectToChange != null && subjectToChange.hasProperty(commitChange.getFieldName())) {
                    IGitCommit commit = commitChange.getNewValue();
                    log.debug("Git-Service: Update on Commit " + commit.getSha() + " | property " + commitChange.getFieldName());
                    if (commit != null) {
                        Instance subtype = this.gitService.transferCommit(commit);
                        if (commitChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                            subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                            this.gitService.transferCommit(commit);
                            return true;
                        }
                        return processSubtypeModification(subjectToChange, commitChange, subtype);
                    }
                }
            }
        } else if (commitChange.getChangeType() == GitChange.ChangeType.REPOSITORY) {
            Optional<Instance> repository_ = this.gitService.getRepo(commitChange.getRepoName(), false);
            if (repository_.isPresent()) {
                Instance subjectToChange = repository_.get();
                if (subjectToChange != null && subjectToChange.hasProperty(commitChange.getFieldName())) {
                    IGitCommit commit = commitChange.getNewValue();
                    if (commit != null) {
                        log.debug("Git-Service: Update on Commit " + commit.getSha() + " | property " + commitChange.getFieldName());
                        Instance subtype = this.gitService.transferCommit(commit);
                        return processSubtypeModification(subjectToChange, commitChange, subtype);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyCommentChange(GitChange<IGitComment> commentChange) {
        Instance subjectToChange = null;
        if (commentChange.getChangeType() == GitChange.ChangeType.ISSUE) {
            Optional<Instance> issue_ = this.gitService.getIssue(commentChange.getRepoName(), commentChange.getKey());
            if (issue_.isPresent()) {
                subjectToChange = issue_.get();
            }
        } else if (commentChange.getChangeType() == GitChange.ChangeType.COMMIT) {
            Optional<Instance> commit_ = this.gitService.getCommit(commentChange.getRepoName(), commentChange.getId());
            if (commit_.isPresent()) {
                subjectToChange = commit_.get();
            }
        }

        if (subjectToChange != null && subjectToChange.hasProperty(commentChange.getFieldName())) {
            IGitComment comment = commentChange.getNewValue();
            if (comment != null) {
                boolean newInstance = !this.gitService.searchForInstance(comment.getURI()).isPresent();
                Instance subtype = this.gitService.transferComment(comment);
                String oldText = (String) subtype.getProperty(GitBaseElementType.BODY).get();
                String newText = commentChange.getNewValue().getBody();
                log.debug("Git-Service: Update on Comment " + comment.getKey() + " | property " + commentChange.getFieldName());

                if (commentChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                    subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                    updateIssueLinks(subjectToChange, commentChange.getRepoName(), oldText, newText);
                    this.gitService.transferComment(comment);
                    return true;
                } else if (commentChange.getModificationType() == GitChange.ModificationType.LIST_REMOVE) {
                    updateIssueLinks(subjectToChange, commentChange.getRepoName(), oldText, "");
                } else if (commentChange.getModificationType() == GitChange.ModificationType.LIST_ADD && newInstance) {
                    updateIssueLinks(subjectToChange, commentChange.getRepoName(), "", newText);
                }

                return processSubtypeModification(subjectToChange, commentChange, subtype);
            }
        }

        return false;
    }

    @Override
    public boolean applyUserChange(GitChange<IGitUser> userChange) {
        boolean newInstance = !this.gitService.isIssueFetched(userChange.getRepoName(), userChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(userChange.getRepoName(), userChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            if (subjectToChange != null && subjectToChange.hasProperty(userChange.getFieldName())) {
                IGitUser user = userChange.getNewValue();
                if (user != null) {
                    log.debug("Git-Service: Update on User " + user.getName() + " | property " + userChange.getFieldName());
                    Instance subtype = this.gitService.transferUser(user);
                    if (userChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                        subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                        this.gitService.transferUser(user);
                        return true;
                    }
                    return processSubtypeModification(subjectToChange, userChange, subtype);
                } else {
                    return processSubtypeModification(subjectToChange, userChange, null);
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyBranchChange(GitChange<IGitBranch> branchChange) {
        boolean newInstance = !this.gitService.isIssueFetched(branchChange.getRepoName(), branchChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(branchChange.getRepoName(), branchChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            if (subjectToChange != null && subjectToChange.hasProperty(branchChange.getFieldName())) {
                IGitBranch branch = branchChange.getNewValue();
                if (branch != null) {
                    log.debug("Git-Service: Update on Branch " + branch.getKey() + " | property " + branchChange.getFieldName());
                    Instance subtype = this.gitService.transferBranch(branch);
                    if (branchChange.getModificationType() == GitChange.ModificationType.LIST_ENTRY_MODIFY) {
                        subtype.getProperty(BaseElementType.FULLY_FETCHED).set(false);
                        this.gitService.transferBranch(branch);
                        return true;
                    }
                    return processSubtypeModification(subjectToChange, branchChange, subtype);
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyMapChange(GitChange<Map<String, Object>> mapChange) {
        boolean newInstance = !this.gitService.isIssueFetched(mapChange.getRepoName(), mapChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(mapChange.getRepoName(), mapChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            log.debug("Git-Service: Update on Issue " + subjectToChange.getProperty(BaseElementType.ID) + " | property " + mapChange.getFieldName());
            if (subjectToChange != null && subjectToChange.hasProperty(mapChange.getFieldName())) {
                Map<String, Object> map = mapChange.getNewValue();
                if (map != null) {
                    return processPrimitiveModification(subjectToChange, mapChange, map);
                }
            }
        }

        return false;
    }

    @Override
    public boolean applyRepositoryChange(GitChange<IGitRepository> repositoryChange) {
        return false;
    }

    @Override
    public boolean applyIntegerChange(GitChange<Integer> integerChange) {
        boolean newInstance = !this.gitService.isIssueFetched(integerChange.getRepoName(), integerChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(integerChange.getRepoName(), integerChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            log.debug("Git-Service: Update on Issue " + subjectToChange.getProperty(BaseElementType.ID) + " | property " + integerChange.getFieldName());
            if (subjectToChange != null && subjectToChange.hasProperty(integerChange.getFieldName())) {
                return processPrimitiveModification(subjectToChange, integerChange, integerChange.getNewValue());
            }
        }

        return false;
    }

    @Override
    public boolean applyStringChange(GitChange<String> stringChange) {
        Optional<Instance> issue_ = this.gitService.getIssue(stringChange.getRepoName(), stringChange.getKey());
        if (issue_.isPresent()) {
            Instance subjectToChange = issue_.get();
            log.debug("Git-Service: Update on Issue " + subjectToChange.getProperty(BaseElementType.ID) + " | property " + stringChange.getFieldName());
            if (subjectToChange != null && subjectToChange.hasProperty(stringChange.getFieldName())) {
                return processPrimitiveModification(subjectToChange, stringChange, stringChange.getNewValue());
            }
        }

        return false;
    }

    @Override
    public boolean applyBooleanChange(GitChange<Boolean> booleanChange) {
        boolean newInstance = !this.gitService.isIssueFetched(booleanChange.getRepoName(), booleanChange.getKey());
        Optional<Instance> issue_ = this.gitService.getIssue(booleanChange.getRepoName(), booleanChange.getKey());
        if (issue_.isPresent() && !newInstance) {
            Instance subjectToChange = issue_.get();
            log.debug("Git-Service: Update on Issue " + subjectToChange.getProperty(BaseElementType.ID) + " | property " + booleanChange.getFieldName());
            if (subjectToChange != null && subjectToChange.hasProperty(booleanChange.getFieldName())) {
                return processPrimitiveModification(subjectToChange, booleanChange, booleanChange.getNewValue());
            }
        }

        return false;
    }


    private boolean processSubtypeModification(Instance instance, GitChange<?> change, Instance subtypeValue) {
        if (!processSimpleModification(instance, change, subtypeValue)) {
            if (change.getModificationType() == GitChange.ModificationType.LIST_REMOVE) {
                ListProperty<Instance> listProperty = instance.getPropertyAsList(change.getFieldName());
                listProperty.remove(subtypeValue);
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean processPrimitiveModification(Instance instance, GitChange<?> change, Object value) {
        if (!processSimpleModification(instance, change, value)) {
            switch (change.getModificationType()) {
                case LIST_REMOVE:
                    instance.getPropertyAsList(change.getFieldName()).remove(value);
                    return true;
                case LIST_ENTRY_MODIFY:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    private boolean processSimpleModification(Instance instance, GitChange<?> change, Object value) {
        switch (change.getModificationType()) {
            case ASSIGNMENT:
                instance.getProperty(change.getFieldName()).set(value);
                return true;
            case LIST_ADD:
                ListProperty<Object> listProperty = instance.getPropertyAsList(change.getFieldName());
                if (!listProperty.contains(value)){
                    listProperty.add(value);
                }
                return true;
            case MAP_ADD:
                Map<String, Object> newEntries = (Map<String, Object>) value;
                for (String key : newEntries.keySet()) {
                    MapProperty instanceMap = instance.getPropertyAsMap(change.getFieldName());
                    //ToDo: find a better fix for this problem
                    if (instanceMap.size() == 1) {
                        //This is necessary, because the updates delive project names for keys
                        //but the initial fetch only gives links with project id's
                        //no override would happen --> corrupt information
                        instanceMap.clear();
                    }
                    instanceMap.put(key, newEntries.get(key));
                }
                return true;
            case MAP_REMOVE:
                Map<String, Object> removedEntries = (Map<String, Object>) value;
                for (String key : removedEntries.keySet()) {
                    instance.getPropertyAsMap(change.getFieldName()).remove(key);
                }
                return true;
            case CREATION:
                //nothing to do
                return true;
        }

        return false;
    }

    private void updateIssueLinks(Instance subjectToChange, String repoName, String oldText, String newText) {
        ArrayList<Integer> oldKeys = getKeysFromText(oldText);
        ArrayList<Integer> newKeys = getKeysFromText(newText);

        ListProperty<Instance> linkedIssues = subjectToChange.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);

        for (Integer key : newKeys) {
            if (!oldKeys.contains(key)) {
                //create link
                String idToCreate = repoName + "/" + GitService.ISSUE_MIDFIX + "/" + key;
                Instance linkedIssue = this.gitService.getInstanceOrCreatePlaceholder(idToCreate, String.valueOf(key), idToCreate, GitBaseElementType.GIT_ISSUE);
                linkedIssues.add(linkedIssue);

                ListProperty<Instance> linksOfLink;
                if (subjectToChange.getInstanceType() == GitBaseElementType.GIT_COMMIT.getType()) {
                    linksOfLink = linkedIssue.getPropertyAsList(GitBaseElementType.COMMITS);
                } else {
                    linksOfLink = linkedIssue.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);
                }
                linksOfLink.add(subjectToChange);
            }
        }

        for (Integer key : oldKeys) {
            if (!newKeys.contains(key)) {
                //delete link
                String idToDelete = repoName + "/" + GitService.ISSUE_MIDFIX + "/" + key;

                for (Instance linkedIssue : linkedIssues) {
                    String idCur = (String) linkedIssue.getProperty(BaseElementType.ID).get();
                    if (idToDelete.equals(idCur)) {
                        linkedIssues.remove(linkedIssue);

                        ListProperty<Instance> linksOfLink;
                        if (subjectToChange.getInstanceType() == GitBaseElementType.GIT_COMMIT.getType()) {
                            linksOfLink = linkedIssue.getPropertyAsList(GitBaseElementType.COMMITS);
                        } else {
                            linksOfLink = linkedIssue.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);
                        }
                        linksOfLink.remove(subjectToChange);
                        break;
                    }
                }
            }
        }
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

}
