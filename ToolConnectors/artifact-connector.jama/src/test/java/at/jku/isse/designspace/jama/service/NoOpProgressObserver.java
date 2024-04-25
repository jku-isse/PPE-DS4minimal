package at.jku.isse.designspace.jama.service;

import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;

public class NoOpProgressObserver implements IProgressObserver{

	@Override
	public void dispatchNewEntry(ProgressEntry entry) {
		//noop
		
	}

	@Override
	public void updatedEntry(ProgressEntry entry) {
		// noop
		
	}

}
