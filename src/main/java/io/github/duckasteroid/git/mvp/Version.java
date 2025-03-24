package io.github.duckasteroid.git.mvp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Version {
    private List<Object> segments;
    private List<Integer> numericSegments;

    public Version(List<Object> segs, List<Integer> ints) {
        this.segments = new ArrayList<>(segs);
        this.numericSegments = ints;
    }

    public Version(String version) {
        String[] strs = version.split("\\.|-");
        this.segments = new ArrayList<>(strs.length);
        List<Integer> numSeg = new ArrayList<Integer>();
        for (int i = 0; i < strs.length; i++) {
            try {
                segments.set(i, Integer.parseInt(strs[i]));
                numSeg.add(i);
            } catch (NumberFormatException nfe) {
                segments.set(i, strs[i]);
            }

        }
        this.numericSegments = numSeg;
    }

    public boolean hasNumericSegment() {
        return !numericSegments.isEmpty();
    }

    public int lastSegment() {
        return numericSegments.stream().mapToInt(Integer::intValue).max().orElse(-1);
    }

    public Version add(int amount, int index) {
        if (!numericSegments.contains(index)) {
            throw new IllegalArgumentException(index + " is not a numeric segment");
        }
        ArrayList<Object> copy = new ArrayList<>(segments);
        Integer newAmount = ((Integer)copy.get(index)) + amount;
        copy.set(index, newAmount);
        return new Version(copy, numericSegments);
    }

    public Version addToLast(int amount) {
        return add(amount, lastSegment());
    }

    public String toString() {
        return segments.stream().map(Object::toString).collect(Collectors.joining("."));
    }
}
