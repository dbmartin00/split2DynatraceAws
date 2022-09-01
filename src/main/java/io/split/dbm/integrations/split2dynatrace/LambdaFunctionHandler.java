package io.split.dbm.integrations.split2dynatrace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.io.IOUtils;

public class LambdaFunctionHandler implements RequestStreamHandler {

	static LambdaLogger logger;
	static ClassLoader classLoader;
	
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

        logger = context.getLogger();
		long start = System.currentTimeMillis();
		String event = IOUtils.toString(input, StandardCharsets.UTF_8);
		logger.log("event: " + event);
		final JSONObject eventObj = new JSONObject(event);

		logger.log("event as JSONObject: " + eventObj);

		String body = eventObj.getString("body");
		logger.log("body string: " + body);
		SplitChange change = new Gson().fromJson(body, SplitChange.class);
		logger.log("successfully parsed change in " + (System.currentTimeMillis() - start) + "ms");
		logger.log("change: " + change.toString());

		classLoader = getClass().getClassLoader();
		File configFile = new File(classLoader.getResource("split2dynatrace.config").getFile());		
		logger.log("reading config from file with path: " + configFile.getAbsolutePath());
		Configuration config = Configuration.fromFile(configFile.getAbsolutePath());

		JSONObject annotation = buildPostForDynatrace(change, config);

		System.out.println("DEBUG - " + annotation.toString());

		try {
			postEventToDynatrace(config, annotation);
		} catch (Exception e) {
			logger.log("ERROR posting to Dynatrace: " + e.getMessage());
		}
			
		PrintWriter writer = new PrintWriter(output);
		writer.println("change parsed and posted to DynaTrace");
		writer.flush();
		writer.close();
		System.out.println("finished in " + (System.currentTimeMillis() - start) + "ms");
    }
    
	private static JSONObject buildPostForDynatrace(SplitChange change, Configuration config)
			throws IOException {
		File rulesFile = new File(classLoader.getResource("attachedRules.json").getFile());		
		AttachedRules rules = AttachedRules.fromFile(rulesFile.getAbsolutePath());

		JSONObject annotation = new JSONObject();
		annotation.put("eventType", "CUSTOM_ANNOTATION");
//		annotation.put("start", change.time - (5 * 60 * 1000)); // started five minutes ago
//		annotation.put("end", change.time);

		rules.tagRule[0].meTypes = config.entities;
		rules.tagRule[0].tags[0].key = "splitTag";
		rules.tagRule[0].tags[0].value = change.name;
		
		annotation.put("attachRules", new JSONObject(new Gson().toJson(rules))).toString(2);
		annotation.put("source", "Split.io");
		annotation.put("annotationType", "split rule change in environment " + change.environmentName);
		annotation.put("annotationDescription", change.description);
		Map<String, Object> customProperties = new TreeMap<String, Object>();
		customProperties.put("a. description", change.description);
		customProperties.put("b. editor", change.editor);
		customProperties.put("c. environmentName", change.environmentName);
		customProperties.put("d. link", change.link);
		customProperties.put("e. split name", change.name);
		customProperties.put("f. changeNumber", "" + change.changeNumber);
		customProperties.put("g. schemaVersion", "" + change.schemaVersion);
		customProperties.put("h. type", change.type);
		customProperties.put("i. definition", change.definition);
		annotation.put("customProperties", customProperties);
		
		return annotation;
	}

	private static void postEventToDynatrace(Configuration config, JSONObject annotation)
			throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		logger.log("INFO - Sending annotations to Dynatrace");
		OkHttpClient client = new OkHttpClient();
		
		RequestBody body = RequestBody.create(
			      MediaType.parse("application/json"), annotation.toString());
		
		Request request = new Request.Builder()
			      .url(config.dynatraceUrl + "/api/v1/events")
			      .addHeader("Content-Type", "application/json")
			      .addHeader("Authorization", "Api-Token " + config.dynatraceApiKey)
			      .post(body)
			      .build();
		
	    Call call = client.newCall(request);
	    Response response = call.execute();
	    logger.log("INFO - post to dynatrace status code: " + response.code() + " response body: " + response.body().string());
	    
		logger.log("INFO - finished sending annotations to Dynatrace in " + (System.currentTimeMillis() - start) + "ms");
	}
}
