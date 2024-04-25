package at.jku.isse.designspace.git.updateservice;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.artifactconnector.core.updateservice.UpdateManager;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.ReactiveConnection;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.api.core.webhookparser.IChangeFactory;
import at.jku.isse.designspace.git.service.GitService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class GitConnection extends ReactiveConnection {

	private GitService gitService;
	private IChangeFactory changeFactory;
	private UpdateManager updateManager;

	public GitConnection(String serverName, GitService gitService, IGitChangePatcher changePatcher) {
		super(serverName, ServerKind.GIT);
		if (gitService.getAPI().isPresent()) {
			this.gitService = gitService;
			IGitAPI gitAPI = gitService.getAPI().get();
			this.updateManager = gitService.getUpdateManager();

			Properties props = new Properties();
			try {
				try {
					FileReader reader = new FileReader("./application.properties");
					props.load(reader);
				} catch (IOException ioe) {
					try {
						props = PropertiesLoaderUtils.loadAllProperties("application.properties");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			this.changeFactory = gitService.getAPI().get().getChangeFactory();
			if (changeFactory != null) {
				changeFactory.addIssueChangeListener(changePatcher::applyIssueChange);
				changeFactory.addPullRequestChangeListener(changePatcher::applyPullRequestChange);
				changeFactory.addCommentChangeListener(changePatcher::applyCommentChange);
				changeFactory.addCommitChangeListener(changePatcher::applyCommitChange);
				changeFactory.addBranchChangeListener(changePatcher::applyBranchChange);
				changeFactory.addUserChangeListener(changePatcher::applyUserChange);
				changeFactory.addBooleanChangeListener(changePatcher::applyBooleanChange);
				changeFactory.addIntegerChangeListener(changePatcher::applyIntegerChange);
				changeFactory.addStringChangeListener(changePatcher::applyStringChange);
				changeFactory.addMapChangeListener(changePatcher::applyMapChange);
			} else {
				log.error("Git-Service: GitChangeFactory is not initalized --> no updates will be processed");
			}
		}
	}

	public void createChanges(Map<String, Object> input) {
		addActionToUpdateQueue(new GitUpdateAction(new Timestamp(System.currentTimeMillis()), this.gitService, UpdateAction.ActionKind.UPDATE_ACTION, ServerKind.GIT, input));
	}

}
