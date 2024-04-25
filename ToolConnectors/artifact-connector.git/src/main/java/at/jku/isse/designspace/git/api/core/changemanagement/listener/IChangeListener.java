package at.jku.isse.designspace.git.api.core.changemanagement.listener;

import at.jku.isse.designspace.git.api.core.changemanagement.GitChange;

public interface IChangeListener<T> {

    void update(GitChange<T> change);

}
