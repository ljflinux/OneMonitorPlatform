package io.onemonitor.monitoring.domain.model.identifier;

import io.onemonitor.common.domain.base.Identifier;

/**
 * Identifier for a Metric Series.
 */
public final class MetricSeriesId extends Identifier.AbstractIdentifier {

    public MetricSeriesId() {
        super(java.util.UUID.randomUUID().toString());
    }

    public MetricSeriesId(String value) {
        super(value);
    }

    @Override
    protected void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Metric Series ID cannot be null or blank");
        }
    }

    public static MetricSeriesId fromString(String value) {
        return new MetricSeriesId(value);
    }

    public static MetricSeriesId generate() {
        return new MetricSeriesId();
    }
}
