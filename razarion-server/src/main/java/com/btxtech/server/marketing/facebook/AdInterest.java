package com.btxtech.server.marketing.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 21.03.2017.
 */
public class AdInterest {
    private String id;
    private String name;
    @JsonProperty("audience_size")
    private long audienceSize;
    private List<String> path;
    private String description;
    private String topic;
    @JsonProperty("disambiguation_category")
    private String disambiguationCategory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAudienceSize() {
        return audienceSize;
    }

    public void setAudienceSize(long audienceSize) {
        this.audienceSize = audienceSize;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDisambiguationCategory() {
        return disambiguationCategory;
    }

    public void setDisambiguationCategory(String disambiguationCategory) {
        this.disambiguationCategory = disambiguationCategory;
    }

    public String toNiceString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append("|");
        stringBuilder.append(id);
        stringBuilder.append(" [audience size:");
        stringBuilder.append(audienceSize);
        stringBuilder.append("] [topic:");
        stringBuilder.append(topic);
        if (description != null) {
            stringBuilder.append("]decription:");
            stringBuilder.append(description);
        }
        if (disambiguationCategory != null) {
            stringBuilder.append("]disambiguationCategory: ");
            stringBuilder.append(disambiguationCategory);
        }
        stringBuilder.append("]path: ");
        for (Iterator<String> iterator = path.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            stringBuilder.append(s);
            if (iterator.hasNext()) {
                stringBuilder.append("->");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "AdInterest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", audienceSize=" + audienceSize +
                ", path=" + path +
                ", description='" + description + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdInterest that = (AdInterest) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
