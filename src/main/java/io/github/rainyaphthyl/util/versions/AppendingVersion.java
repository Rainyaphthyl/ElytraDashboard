package io.github.rainyaphthyl.util.versions;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class AppendingVersion extends AbstractList<String> implements Comparable<AppendingVersion> {
    private final String[] labels;
    private final AtomicReference<String> text = new AtomicReference<>(null);

    @ParametersAreNullableByDefault
    private AppendingVersion(String... labels) {
        if (labels == null) {
            this.labels = new String[0];
        } else {
            this.labels = new String[labels.length];
            System.arraycopy(labels, 0, this.labels, 0, labels.length);
        }
    }

    /**
     * {@code null} is higher
     */
    public static int compare_appendix(AppendingVersion v1, AppendingVersion v2) {
        if (v1 == v2) {
            return 0;
        } else if (v1 == null) {
            return 1;
        } else if (v2 == null) {
            return -1;
        } else {
            return v1.compareTo(v2);
        }
    }

    @ParametersAreNonnullByDefault
    public static int compare_section(String s1, String s2) {
        boolean pure1 = is_pure_numeric(s1);
        boolean pure2 = is_pure_numeric(s2);
        if (pure1 == pure2) {
            if (pure1) {
                int num1 = Integer.parseInt(s1);
                int num2 = Integer.parseInt(s2);
                return Integer.compare(num1, num2);
            } else {
                return s1.compareTo(s2);
            }
        } else {
            return pure1 ? -1 : 1;
        }
    }

    @ParametersAreNonnullByDefault
    public static boolean is_pure_numeric(String section) {
        return section.matches("^[0-9]+$");
    }

    @Override
    public int size() {
        return labels.length;
    }

    @Override
    public String toString() {
        synchronized (text) {
            if (text.get() == null) {
                StringBuilder builder = new StringBuilder();
                if (0 < labels.length) {
                    builder.append(labels[0]);
                }
                for (int i = 1; i < labels.length; ++i) {
                    builder.append('.').append(labels[i]);
                }
                text.set(builder.toString());
            }
        }
        return text.get();
    }

    @Override
    @ParametersAreNonnullByDefault
    public int compareTo(AppendingVersion that) {
        int minLength = Math.min(labels.length, that.labels.length);
        for (int i = 0; i < minLength; ++i) {
            int flag = compare_section(labels[i], that.labels[i]);
            if (flag != 0) {
                return flag;
            }
        }
        return Integer.compare(labels.length, that.labels.length);
    }

    @Override
    public String get(int index) {
        if (index < labels.length && index >= 0) {
            return labels[index];
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof AppendingVersion) {
            AppendingVersion version = (AppendingVersion) that;
            return Arrays.equals(labels, version.labels);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(labels);
    }
}
