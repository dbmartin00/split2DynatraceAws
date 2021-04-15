package io.split.dbm.integrations.split2dynatrace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

public class AttachedRules {
	
	public TagRule[] tagRule;

    public static AttachedRules fromFile(String attachedRuleFilePath) throws IOException {
        String configContents = FileUtils.readFileToString(new File(attachedRuleFilePath), StandardCharsets.UTF_8);
        AttachedRules fromJson = new Gson().fromJson(configContents, AttachedRules.class);
        return fromJson;
    }

	public AttachedRules(String[] entityIds, TagRule[] tagRules) {
		super();
		this.tagRule = tagRules;
	}

	public TagRule[] getTagRule() {
		return tagRule;
	}

	public void setTagRule(TagRule[] tagRule) {
		this.tagRule = tagRule;
	}
    
    
}
