package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitUser;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsersResource extends ListResource<Map<String, Object>, IGitUser> {


    private final ArrayList<Map<String, Object>> rawUsersIncomplete;
    private ArrayList<Map<String, Object>> rawUsersComplete;

    public UsersResource(IGithubRestClient source, ArrayList<Map<String, Object>> rawUsersIncomplete) {
        super(source);
        assert rawUsersIncomplete != null;
        this.rawUsersIncomplete = rawUsersIncomplete;
    }

    @Override
    public List<IGitUser> getResources() {
        ArrayList<IGitUser> users = new ArrayList<>();
        ArrayList<Map<String, Object>> rawUsers = this.getResource();

        for (Map<String, Object> rawUser : rawUsers) {
            Object login = rawUser.get("login");
            if (login != null) {
                IGitUser user = new UserResource(this.getSource(), (String) login);
                users.add(user);
            }
        }

        return users;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        if (this.rawUsersComplete == null) {
            this.rawUsersComplete = new ArrayList<>();
            for (Map<String, Object> rawUserIncomplete : rawUsersIncomplete) {
                Object userName_ = rawUserIncomplete.get("login");
                Map<String, Object> rawUser = this.getSource().getUser((String) userName_);
                if (rawUser != null) {
                    rawUsersComplete.add(rawUser);
                }
            }
        }

        return this.rawUsersComplete;
    }

}
