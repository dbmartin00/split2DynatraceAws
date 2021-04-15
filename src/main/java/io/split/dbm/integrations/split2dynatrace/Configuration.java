package io.split.dbm.integrations.split2dynatrace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

public class Configuration {

    public String dynatraceApiKey;
    public String dynatraceUrl;
    public String dynatraceTag;
    
    public String[] entities;

    public static Configuration fromFile(String configFilePath) throws IOException {
        String configContents = FileUtils.readFileToString(new File(configFilePath), StandardCharsets.UTF_8);
        return new Gson().fromJson(configContents, Configuration.class);
    }
}
