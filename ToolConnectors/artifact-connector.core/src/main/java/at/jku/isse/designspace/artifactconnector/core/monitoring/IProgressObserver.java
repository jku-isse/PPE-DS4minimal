package at.jku.isse.designspace.artifactconnector.core.monitoring;

public interface IProgressObserver {

	
	public void dispatchNewEntry(ProgressEntry entry);
	
	public void updatedEntry(ProgressEntry entry); 
}
