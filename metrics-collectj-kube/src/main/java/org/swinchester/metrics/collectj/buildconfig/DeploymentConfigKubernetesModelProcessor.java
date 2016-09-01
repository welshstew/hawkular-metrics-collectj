package org.swinchester.metrics.collectj.buildconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerImageChangeParams;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicy;
import io.fabric8.utils.Lists;

public class DeploymentConfigKubernetesModelProcessor {


    public void on(DeploymentConfigBuilder builder) {
        builder.withSpec(builder.getSpec())
                .editMetadata()
                    .withName(ConfigParameters.APP_NAME)
                .endMetadata()
                .editSpec()
                    .withReplicas(1)
//                    .withSelector(getSelectors())
                    .withNewStrategy()
                        .withType("Recreate")
                    .endStrategy()
                    .editTemplate()
                        .editSpec()
                            .withContainers(getContainers())
                            .withRestartPolicy("Always")
                            .withVolumes(getVolumes())
                        .endSpec()
                    .endTemplate()
                    .withTriggers(getTriggers())
                .endSpec()
            .build();
    }

    private List<DeploymentTriggerPolicy> getTriggers() {
        DeploymentTriggerPolicy configChange = new DeploymentTriggerPolicy();
        configChange.setType("ConfigChange");

        ObjectReference from = new ObjectReference();
        from.setName(ConfigParameters.APP_NAME + ":${IS_TAG}");
        from.setKind("ImageStreamTag");
        from.setNamespace("${IS_PULL_NAMESPACE}");

        DeploymentTriggerImageChangeParams imageChangeParms = new DeploymentTriggerImageChangeParams();
        imageChangeParms.setFrom(from);
        imageChangeParms.setAutomatic(true);

        DeploymentTriggerPolicy imageChange = new DeploymentTriggerPolicy();
        imageChange.setType("ImageChange");
        imageChange.setImageChangeParams(imageChangeParms);
        imageChangeParms.setContainerNames(Lists.newArrayList(ConfigParameters.APP_NAME));

        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(configChange);
        triggers.add(imageChange);

        return triggers;
    }

    private List<ContainerPort> getPorts() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();
        ContainerPort jolokia = new ContainerPort();
        jolokia.setContainerPort(8778);
        jolokia.setProtocol("TCP");
        jolokia.setName("jolokia");
        ports.add(jolokia);

        return ports;
    }

    private List<EnvVar> getEnvironmentVariables() {
        List<EnvVar> envVars = new ArrayList<EnvVar>();

        EnvVarSource namespaceSource = new EnvVarSource();
        namespaceSource.setFieldRef(new ObjectFieldSelector(null, "metadata.namespace"));

        EnvVar namespace = new EnvVar();
        namespace.setName("KUBERNETES_NAMESPACE");
        namespace.setValueFrom(namespaceSource);

        EnvVar config = new EnvVar();
        config.setName("SPRING_CONFIG_LOCATION");
        config.setValue("file:///etc/config/application.yml");

        envVars.add(config);
        envVars.add(namespace);
        return envVars;
    }

    private List<Volume> getVolumes(){

        Volume configMap = new Volume();
        configMap.setConfigMap(new ConfigMapVolumeSource(null, ConfigParameters.CONFIGMAP_NAME));
        configMap.setName(ConfigParameters.CONFIGMAP_NAME);

        List<Volume> volList = new ArrayList<>();
        volList.add(configMap);

        return volList;
    }

    private Container getContainers() {
        Container container = new Container();
        container.setImage("${IS_PULL_NAMESPACE}/"+ ConfigParameters.APP_NAME +  ":${IS_TAG}");
        container.setImagePullPolicy("Always");
        container.setName(ConfigParameters.APP_NAME);
        container.setPorts(getPorts());
        container.setEnv(getEnvironmentVariables());
        container.setResources(getResourceRequirements());
        container.setVolumeMounts(getVolumeMounts());
        return container;
    }

    private List<VolumeMount> getVolumeMounts(){
        ArrayList<VolumeMount> avm = new ArrayList<>();
        avm.add(new VolumeMount(ConfigParameters.CONFIGMAP_MOUNT,ConfigParameters.CONFIGMAP_NAME,true));
        return avm;
    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("app", ConfigParameters.APP_NAME);
        selectors.put("deploymentconfig", ConfigParameters.APP_NAME);

        return selectors;
    }

    private ResourceRequirements getResourceRequirements() {
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        resourceRequirements.setRequests(getRequests());
        resourceRequirements.setLimits(getLimits());

        return resourceRequirements;
    }

    private Map<String, Quantity> getRequests() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("200m"));
        limits.put("memory", new Quantity("512Mi"));

        return limits;
    }

    private Map<String, Quantity> getLimits() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("400m"));
        limits.put("memory", new Quantity("1024Mi"));

        return limits;
    }
}
