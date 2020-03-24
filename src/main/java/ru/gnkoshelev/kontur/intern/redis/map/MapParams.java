package ru.gnkoshelev.kontur.intern.redis.map;

import java.util.ArrayList;
import java.util.List;

public class MapParams {
    private Long changeCounter;
    private List<String> basicParams;
    private String subCounterName;

    public MapParams(String hmapName, String changeCounterName, String subCounterName, Long changeCounter) {
        basicParams = new ArrayList<>(4);
        basicParams.add(changeCounter.toString());
        basicParams.add(changeCounterName);
        basicParams.add(hmapName);
        this.subCounterName = subCounterName;
    }

    List<String> getBasicParams() {
        basicParams.set(0, changeCounter.toString());
        return basicParams;
    }

    public void setChangeCounter(Long changeCounter) {
        this.changeCounter = changeCounter;
    }

    public Long getChangeCounter() {
        return changeCounter;
    }

    public String getChangeCounterName() {
        return basicParams.get(1);
    }

    public String getMapName() { return basicParams.get(2); }

    public String getSubCounterName() { return subCounterName; }
}
