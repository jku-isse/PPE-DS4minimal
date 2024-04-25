package at.jku.isse.designspace.artifactconnector.core.artifactapi;


public class ArtifactIdentifier {
    private String id;
    private String artType;
    private String idType;
    
    public ArtifactIdentifier() {}

	public ArtifactIdentifier(String id, String type) {
		super();
		this.id = id;
		this.artType = type;
	}

	public ArtifactIdentifier(String id, String type, String idType) {
		super();
		this.id = id;
		this.artType = type;
		this.idType = idType;
	}
	
	public String getId() {
		return id;
	}

	public String getType() {
		return artType;
	}
	
	public String getIdType() {
		return idType != null ? idType : artType; //for backward compatibility and default/single idType use case
	}
    

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((artType == null) ? 0 : artType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactIdentifier other = (ArtifactIdentifier) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (artType == null) {
			if (other.artType != null)
				return false;
		} else if (!artType.equals(other.artType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ArtID [" + id + "("+getIdType()+") ::" + artType + "]";
	}


    
}
