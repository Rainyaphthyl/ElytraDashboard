package io.github.rainyaphthyl.util.versions;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class AppendingVersion extends AbstractList<String> implements Comparable<AppendingVersion> {
    private final String[] identifiers;
    private final AtomicReference<String> text = new AtomicReference<>(null);

    @ParametersAreNullableByDefault
    private AppendingVersion(String... identifiers) {
        if (identifiers == null) {
            this.identifiers = new String[0];
        } else {
            this.identifiers = new String[identifiers.length];
            System.arraycopy(identifiers, 0, this.identifiers, 0, identifiers.length);
        }
    }

    public static AppendingVersion getAppendix(String label) {
        if (label == null) {
            return null;
        }
        String[] subLabels = label.split("\\.");
        for (String subLabel : subLabels) {
            if (!ModVersion.PATTERN_ALPHA_NUM.matcher(subLabel).matches() && !ModVersion.PATTERN_PURE_NUM.matcher(subLabel).matches()) {
                System.out.println("[Invalid] - PATTERN_ALPHA_NUM");
                return null;
            }
        }
        return new AppendingVersion(subLabels);
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
        return ModVersion.PATTERN_LAZY_NUM.matcher(section).matches();
    }

    @Override
    public int size() {
        return identifiers.length;
    }

    @Override
    public String toString() {
        synchronized (text) {
            if (text.get() == null) {
                StringBuilder builder = new StringBuilder();
                if (0 < identifiers.length) {
                    builder.append(identifiers[0]);
                }
                for (int i = 1; i < identifiers.length; ++i) {
                    builder.append('.').append(identifiers[i]);
                }
                text.set(builder.toString());
            }
        }
        return text.get();
    }

    @Override
    @ParametersAreNonnullByDefault
    public int compareTo(AppendingVersion that) {
        int minLength = Math.min(identifiers.length, that.identifiers.length);
        for (int i = 0; i < minLength; ++i) {
            int flag = compare_section(identifiers[i], that.identifiers[i]);
            if (flag != 0) {
                return flag;
            }
        }
        return Integer.compare(identifiers.length, that.identifiers.length);
    }

    @Override
    public String get(int index) {
        if (index < identifiers.length && index >= 0) {
            return identifiers[index];
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
            return Arrays.equals(identifiers, version.identifiers);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(identifiers);
    }
}
