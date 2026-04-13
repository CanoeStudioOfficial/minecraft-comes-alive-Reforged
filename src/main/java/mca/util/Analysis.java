package mca.util;

import java.util.LinkedList;
import java.util.List;

public class Analysis {
    private final List<AnalysisElement> elements = new LinkedList<>();

    public Analysis() {
    }

    public void add(String key, int value) {
        elements.add(new AnalysisElement(key, value));
    }

    public List<AnalysisElement> getElements() {
        return elements;
    }

    public int getTotal() {
        int total = 0;
        for (AnalysisElement element : elements) {
            total += element.value;
        }
        return total;
    }

    public static class AnalysisElement {
        public final String key;
        public final int value;

        public AnalysisElement(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
