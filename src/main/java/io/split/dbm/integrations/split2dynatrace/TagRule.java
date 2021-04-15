package io.split.dbm.integrations.split2dynatrace;

public class TagRule {

	public String[] meTypes;
	public Tag[] tags;
	
	public TagRule(String[] meTypes, Tag[] tags) {
		super();
		this.meTypes = meTypes;
		this.tags = tags;
	}
	public String[] getMeTypes() {
		return meTypes;
	}
	public void setMeTypes(String[] meTypes) {
		this.meTypes = meTypes;
	}
	public Tag[] getTags() {
		return tags;
	}
	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
	
	
}
