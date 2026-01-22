package io.onemonitor.common.domain.vo;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Value Object representing a set of labels for correlation with monitoring data.
 * 
 * Labels are used to associate CI with metrics, logs, and traces from
 * Prometheus, Loki, and Tempo.
 */
@Embeddable
public class LabelSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @JdbcTypeCode(JsonType.NAME)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> labels;

    protected LabelSet() {
        this.labels = new HashMap<>();
    }

    public LabelSet(Map<String, String> labels) {
        this.labels = new HashMap<>(Objects.requireNonNull(labels, "Labels cannot be null"));
        validateLabels(this.labels);
    }

    private void validateLabels(Map<String, String> labels) {
        if (labels == null) {
            return;
        }
        labels.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Label key cannot be null or blank");
            }
            if (value == null) {
                throw new IllegalArgumentException("Label value for key '" + key + "' cannot be null");
            }
        });
    }

    /**
     * Returns an empty label set.
     */
    public static LabelSet empty() {
        return new LabelSet(Collections.emptyMap());
    }

    /**
     * Returns a label set with a single label.
     */
    public static LabelSet of(String key, String value) {
        return new LabelSet(Map.of(key, value));
    }

    /**
     * Returns the label value for the given key.
     */
    public String get(String key) {
        return this.labels.get(key);
    }

    /**
     * Returns the label value for the given key, or the default value if not present.
     */
    public String getOrDefault(String key, String defaultValue) {
        return this.labels.getOrDefault(key, defaultValue);
    }

    /**
     * Returns true if this label set contains the given key.
     */
    public boolean containsKey(String key) {
        return this.labels.containsKey(key);
    }

    /**
     * Returns true if this label set contains all the labels of the other label set.
     */
    public boolean containsAll(LabelSet other) {
        if (other == null || other.labels.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> entry : other.labels.entrySet()) {
            String thisValue = this.labels.get(entry.getKey());
            if (!Objects.equals(thisValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this label set is a subset of the other label set.
     */
    public boolean isSubsetOf(LabelSet other) {
        if (other == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : this.labels.entrySet()) {
            String otherValue = other.labels.get(entry.getKey());
            if (otherValue == null || !otherValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this label set matches the given label set pattern.
     * A match occurs when all labels in this set have the same values in the other set.
     */
    public boolean matches(LabelSet other) {
        if (other == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : this.labels.entrySet()) {
            String otherValue = other.labels.get(entry.getKey());
            if (!Objects.equals(otherValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new label set with the given label added.
     */
    public LabelSet with(String key, String value) {
        Map<String, String> newLabels = new HashMap<>(this.labels);
        newLabels.put(key, value);
        return new LabelSet(newLabels);
    }

    /**
     * Returns a new label set with the given labels added.
     */
    public LabelSet withAll(Map<String, String> additionalLabels) {
        Map<String, String> newLabels = new HashMap<>(this.labels);
        newLabels.putAll(Objects.requireNonNull(additionalLabels, "Additional labels cannot be null"));
        return new LabelSet(newLabels);
    }

    /**
     * Returns a new label set with the given label removed.
     */
    public LabelSet without(String key) {
        Map<String, String> newLabels = new HashMap<>(this.labels);
        newLabels.remove(key);
        return new LabelSet(newLabels);
    }

    /**
     * Merges this label set with another label set.
     * The other label set's values take precedence.
     */
    public LabelSet merge(LabelSet other) {
        if (other == null) {
            return this;
        }
        Map<String, String> merged = new HashMap<>(this.labels);
        merged.putAll(other.labels);
        return new LabelSet(merged);
    }

    /**
     * Returns a new label set with only the specified keys.
     */
    public LabelSet filter(Set<String> keys) {
        Map<String, String> filtered = this.labels.entrySet().stream()
                .filter(e -> keys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new LabelSet(filtered);
    }

    /**
     * Returns a new label set with the specified keys removed.
     */
    public LabelSet exclude(Set<String> keys) {
        Map<String, String> filtered = this.labels.entrySet().stream()
                .filter(e -> !keys.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new LabelSet(filtered);
    }

    /**
     * Returns the labels as an unmodifiable map.
     */
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(new HashMap<>(this.labels));
    }

    /**
     * Returns the keys of this label set.
     */
    public Set<String> keySet() {
        return Collections.unmodifiableSet(this.labels.keySet());
    }

    /**
     * Returns the values of this label set.
     */
    public Collection<String> values() {
        return Collections.unmodifiableCollection(this.labels.values());
    }

    /**
     * Returns the size of this label set.
     */
    public int size() {
        return this.labels.size();
    }

    /**
     * Returns true if this label set is empty.
     */
    public boolean isEmpty() {
        return this.labels.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelSet labelSet = (LabelSet) o;
        return Objects.equals(labels, labelSet.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels);
    }

    @Override
    public String toString() {
        return "LabelSet{" +
                "labels=" + labels +
                '}';
    }
}
