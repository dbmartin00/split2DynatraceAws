# split2dynatrace

![alt text](http://www.cortazar-split.com/dynatrace2split.png)

This source code is for deployment as an AWS Lambda. The lambda is a webhook for a Split audit trail of a specific Split environment.  The webhook takes changes from Split and annotates tagged Dynatrace entities with these changes using Dynatrace APIs. The results are discoverable in the Dynatrace UI.

To install, 

Edit the src/main/java/split2dynatrace.config file

split2dynatrace.config:
```
{
	"dynatraceUrl" : "https://YOUR_URL.live.dynatrace.com",
	"dynatraceApiKey" : "YOUR.KEY.FROM_DYNATRACE",
	"entities" : [
		"HOST",
		"APPLICATION",
		"SERVICE"
	]
}
```
 * dyntraceUrl : copied from your browser when logged into Dynatrace
 * dynatraceApiKey : created and copied from Dynatrace; see Dyntrace manual; be sure to enable API V1 "Access problem and event feed, metrics, and topology"
 * entities : host, application, service, or any on the list of meTypes found at this URL (https://www.dynatrace.com/support/help/dynatrace-api/environment-api/events/post-event/#expand-the-element-can-hold-these-values-382)

Deploy the AWS Lambda

I used the AWS Toolkit for Eclipse (https://aws.amazon.com/eclipse/).  Note that you will need to be working with Java 8, as higher versions are not yet supported by AWS.

When you deploy your lambda at the API Gateway, it should be a public REST API.  Save the URL of your deployed lambda.

The lambda logs helpful information and debugging output to CloudWatch.

Tag your entities

 - Find instances of the entities you want configured to be annotated 

Follow Dynatrace instructions for creating a tag on each entitiy.

Use this format:
```
key:   splitTag
value: exact split name
```

For example, if your split is called "new_onboarding" in the Split UI, use key "splitTag" and value "new_onboarding" in Dynatrace.

You can use Dynatrace's advanced tagging functionality to automatically apply tags to larger portions of your infrastructure.

Tags on entities that are not of a type listed in split2dynatrace.config are ignored.

 - Register your AWS Lambda with Split

Follow Split official instructions for creating a webhook.  Provide the URL you saved previously when you deployed your lambda.  Choose if you want changes to be pass from DEV, QA, or PROD environment (changes to other environments will be ignored).

ADVANCED MOVE: Deploy a webhook for each of your environments separately.  Tag dev, qa, and prod environments with unique tags.  Environment-specific webhooks will annotate with changes only from that environment.

HOW DOES IT WORK

Assuming the webhook was made for the production environment, a change to a split in that production environment will trigger a notification to the AWS lambda webhook you've installed.  Watch the CloudWatch log to verify receipt and send to Dynatrace.

TROUBLESHOOTING

CloudWatch log will show
 - successfully parsed change
 - DEBUG of full JSON from Split
 - "sending annotations to Dynatrace"

Should exit 200.  Time to finish will depend on entity types defined and number of tags.

If you see this kind of error...

```
INFO - post to dynatrace status code: 400 response body: 
{
    "error": {
        "code": 400,
        "message": "Invalid attachRules object provided. No entity IDs match: Matching rule: PushEventAttachRules{entityIds=null, tagRules=[TagMatchRule{meTypes=[HOST, APPLICATION, SERVICE], tags=[[CONTEXTLESS]splitTag:dynamic_boxes]}]}"
    }
}
```

It actually just means that no matching entities were found with the tag generated for this annotation ("splitTag:dynamic_boxes").  Tag in Dynatrace and make a change to the corresponding split in the Split UI again.



