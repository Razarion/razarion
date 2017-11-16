package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Beat
 * 26.03.2016.
 */
public class CollectionUtils {

    public static int getCorrectedIndex(int index, int listSize) {
        int correctedIndex = index % listSize;
        if (correctedIndex < 0) {
            correctedIndex += listSize;
        }
        return correctedIndex;
    }

    public static int getCorrectedIndexInvert(int index, int listSize) {
        return listSize - 1 - getCorrectedIndex(index, listSize);
    }

    public static int getCorrectedIndex(int index, List list) {
        return getCorrectedIndex(index, list.size());
    }

    public static <T> T getCorrectedElement(int index, List<T> list) {
        return list.get(getCorrectedIndex(index, list.size()));
    }

    public static int getCorrectedDelta(int startIndex, int endIndex, int listSize) {
        int correctedStartIndex = getCorrectedIndex(startIndex, listSize);
        int correctedEndIndex = getCorrectedIndex(endIndex, listSize);
        if (correctedEndIndex >= correctedStartIndex) {
            return correctedEndIndex - correctedStartIndex;
        } else {
            return listSize - correctedEndIndex - correctedStartIndex;
        }
    }

    public static double[][] to2dArray(List<List<Double>> list) {
        if (list == null) {
            return new double[0][0];
        }
        double[][] result = new double[list.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = toArray(list.get(i));
        }
        return result;
    }

    public static double[] toArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static List<List<Double>> toList(double[][] array) {
        List<List<Double>> list = new ArrayList<>();
        for (double[] doubleArray : array) {
            list.add(toList(doubleArray));
        }
        return list;
    }

    public static List<Double> toList(double[] doubleArray) {
        List<Double> list = new ArrayList<>();
        for (double d : doubleArray) {
            list.add(d);
        }
        return list;
    }

    public static List<Double> verticesToDoubles(List<Vertex> vertices) {
        List<Double> doubleList = new ArrayList<>();
        for (Vertex vertex : vertices) {
            vertex.appendTo(doubleList);
        }
        return doubleList;
    }

    public static <T> T getFirst(Iterable<T> iterable) {
        return iterable.iterator().next();
    }

    public static <T> T removeFirst(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        T t = iterator.next();
        iterator.remove();
        return t;
    }

    public static <T> T getNth(Collection<T> collection, int index) {
        return new ArrayList<T>(collection).get(index);
    }

    public static <T> T getLast(Collection<T> collection) {
        List<T> list = new ArrayList<T>(collection);
        return list.get(list.size() - 1);
    }

    public static boolean hasNullElements(Collection collection) {
        for (Object o : collection) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<T> saveArrayListCopy(List<T> list) {
        if (list != null) {
            return new ArrayList<T>(list);
        } else {
            return null;
        }
    }

    public static double getMax(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values not allowed to be null or empty");
        }
        double last = Double.MIN_VALUE;
        for (Double value : values) {
            last = Math.max(last, value);
        }
        return last;
    }

    public static double getMin(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values not allowed to be null or empty");
        }
        double last = Double.MAX_VALUE;
        for (Double value : values) {
            last = Math.min(last, value);
        }
        return last;
    }

    public static double sum(Collection<Double> doubles) {
        double sum = 0;
        for (double number : doubles) {
            sum += number;
        }
        return sum;
    }

    public static <T> int findStart(List<T> list, Predicate<T> predicate) {
        int offset = -1;
        for (int i = 0; i < list.size(); i++) {
            T current = list.get(i);
            if (predicate.test(current)) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            throw new IllegalArgumentException("CollectionUtils.findStart(): Can not find valid position in list");
        }
        return offset;
    }
}
