package org.swinchester.metrics.collectj.buildconfig;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.openshift.api.model.BuildTriggerPolicy;
import io.fabric8.openshift.api.model.ImageChangeTrigger;
import io.fabric8.openshift.api.model.TemplateBuilder;

public class BuildConfigKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {
        builder.addNewBuildConfigObject()
                .withNewMetadata()
                    .withName(ConfigParameters.APP_NAME)
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withTriggers(getTriggers())
                    .withNewSource()
                        .withNewGit()
                            .withUri("${GIT_URI}")
                        .endGit()
                        .withType("Git")
                    .endSource()
                .withNewStrategy()
                .withNewSourceStrategy()
                .addNewEnv()
                .withName("JAVA_MAIN_CLASS")
                .withValue("org.swinchester.metrics.collectj.app.CollectJApplication")
                .endEnv()
                .addNewEnv()
                .withName("ARTIFACT_DIR")
                .withValue("metrics-collectj/target")
                .endEnv()
                .addNewEnv()
                .withName("HAWTAPP_VERSION")
                .withValue("2.2.0.redhat-079")
                .endEnv()
                .withNewFrom()
                .withKind("ImageStreamTag")
                .withName("fis-java-openshift:1.0")
                .withNamespace("openshift")
                .endFrom()
                .endSourceStrategy()
                .withType("Source")
                .endStrategy()
                    .withNewOutput()
                        .withNewTo()
                            .withKind("ImageStreamTag")
                            .withName(ConfigParameters.APP_NAME + ":${IS_TAG}")
                        .endTo()
                    .endOutput()
                .endSpec()
            .endBuildConfigObject()
            .build();
    }

    private BuildTriggerPolicy getTriggers() {
        BuildTriggerPolicy policy = new BuildTriggerPolicy();
        policy.setType("ImageChange");
        return policy;
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", ConfigParameters.APP_NAME);
        labels.put("project", ConfigParameters.APP_NAME);
        labels.put("version", "1.0-SNAPSHOT");
        labels.put("group", ConfigParameters.GROUP_NAME);

        return labels;
    }

}
