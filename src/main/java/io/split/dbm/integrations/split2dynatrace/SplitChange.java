package io.split.dbm.integrations.split2dynatrace;

public class SplitChange {
	
	String name;
	String type;
	long changeNumber;
	long time;
	String definition;
	String description;
	String link;
	String environmentName;
	String editor;
	long schemaVersion;
	
	@Override
	public String toString() {
		return "SplitChange [name=" + name + ", type=" + type + ", changeNumber=" + changeNumber + ", time=" + time
				+ ", definition=" + definition + ", description=" + description + ", link=" + link
				+ ", environmentName=" + environmentName + ", editor=" + editor + ", schemaVersion=" + schemaVersion
				+ "]";
	}
	
	
}
