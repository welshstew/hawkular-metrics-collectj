package org.swinchester.metrics.collectj.buildconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.TagReference;

public class ImageStreamKubernetesModelProcessor {

    public void on(ImageStreamBuilder builder) {
        builder.withSpec(builder.getSpec())
                .editMetadata()
                    .withName(ConfigParameters.APP_NAME)
                .endMetadata()
                .withNewSpec()
                    .withTags(getTags())
                    .withDockerImageRepository("${REGISTRY}/${IS_PULL_NAMESPACE}/" + ConfigParameters.APP_NAME)
                .endSpec()
            .build();
    }

    private List<TagReference> getTags() {
        ObjectReference fromLatest = new ObjectReference();
        fromLatest.setName(ConfigParameters.APP_NAME);
        fromLatest.setKind("ImageStreamTag");

        Map<String, String> latestAnnotations = new HashMap<String, String>();
        latestAnnotations.put("tags", "${IS_TAG}");

        TagReference latest = new TagReference();
        latest.setName("${IS_TAG}");
        latest.setFrom(fromLatest);
        latest.setAnnotations(latestAnnotations);

        List<TagReference> tags = new ArrayList<TagReference>();
        tags.add(latest);

        return tags;
    }
}
