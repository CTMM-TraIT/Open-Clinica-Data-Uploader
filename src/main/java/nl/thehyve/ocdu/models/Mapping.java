package nl.thehyve.ocdu.models;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Bean to combine the mapping with a certain {@link UploadSession}
 * Created by jacob on 9/15/16.
 */
public class Mapping {

    private Map<String, String> mapping;

    private UploadSession uploadSession;

    public Mapping(Map<String, String> mapping, UploadSession uploadSession) {
        this.mapping = mapping;
        this.uploadSession = uploadSession;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public UploadSession getUploadSession() {
        return uploadSession;
    }

    public void setUploadSession(UploadSession uploadSession) {
        this.uploadSession = uploadSession;
    }

    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dumpAsMap(this);
    }
}
