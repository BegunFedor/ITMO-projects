package javaweb.stats;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Named
@ApplicationScoped
public class StatsBean implements Serializable {

    private final Map<String, LongAdder> timezoneCount = new ConcurrentHashMap<>();

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder passedFilter = new LongAdder();
    private final LongAdder blockedByFilter = new LongAdder();

    public void recordTimezone(String tz) {
        timezoneCount.computeIfAbsent(tz, key -> new LongAdder()).increment();
        totalRequests.increment();
    }

    public void recordFilterResult(boolean passed) {
        if (passed) passedFilter.increment();
        else blockedByFilter.increment();
    }

    public Map<String, Object> getStats() {
        long total = totalRequests.sum();
        long passed = passedFilter.sum();
        long blocked = blockedByFilter.sum();

        Map<String, Double> tzPercent = timezoneCount.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> total == 0 ? 0.0 :
                                Math.round((e.getValue().sum() * 10000.0 / total)) / 100.0
                ));

        double passedPercent = (passed + blocked == 0) ? 0 :
                Math.round((passed * 10000.0 / (passed + blocked))) / 100.0;
        double blockedPercent = 100.0 - passedPercent;

        return Map.of(
                "totalRequests", total,
                "timezonePercent", tzPercent,
                "passedPercent", passedPercent,
                "blockedPercent", blockedPercent
        );
    }
}