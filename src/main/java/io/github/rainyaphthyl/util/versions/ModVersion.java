package io.github.rainyaphthyl.util.versions;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
 */
public class ModVersion implements Comparable<ModVersion> {
    private static final Pattern PATTERN_CORE_SEC = Pattern.compile("^0|([1-9][0-9]*)$");
    private static final Pattern PATTERN_FULL = Pattern.compile("^([^-+]+)-([^-+]+)\\+([^-+]+)$");
    private static final Pattern PATTERN_WITH_PRE = Pattern.compile("^([^-+]+)-([^-+]+)$");
    private static final Pattern PATTERN_WITH_BUILD = Pattern.compile("^([^-+]+)\\+([^-+]+)$");
    private static final Pattern PATTERN_SIMPLE = Pattern.compile("^[^-+]+$");
    private final int major;
    private final int minor;
    private final int patch;
    private final AppendingVersion preLabels;
    private final AtomicReference<String> text = new AtomicReference<>(null);

    @ParametersAreNullableByDefault
    private ModVersion(int major, int minor, int patch, AppendingVersion preLabels) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preLabels = preLabels;
    }

    public static ModVersion getVersion(String versionName) {
        if (versionName == null) {
            return null;
        }
        String core, pre, build;
        if (PATTERN_FULL.matcher(versionName).matches()) {
            int indexPre = versionName.indexOf('-');
            int indexBuild = versionName.indexOf('+');
            core = versionName.substring(0, indexPre);
            pre = versionName.substring(indexPre + 1, indexBuild);
            build = versionName.substring(indexBuild + 1);
        } else if (PATTERN_WITH_PRE.matcher(versionName).matches()) {
            int indexPre = versionName.indexOf('-');
            core = versionName.substring(0, indexPre);
            pre = versionName.substring(indexPre + 1);
            build = null;
        } else if (PATTERN_WITH_BUILD.matcher(versionName).matches()) {
            int indexBuild = versionName.indexOf('+');
            core = versionName.substring(0, indexBuild);
            pre = null;
            build = versionName.substring(indexBuild + 1);
        } else if (PATTERN_SIMPLE.matcher(versionName).matches()) {
            core = versionName;
            pre = null;
            build = null;
        } else {
            System.out.println("[Invalid] - pattern total");
            return null;
        }
        String[] coreSecs = core.split("\\.");
        if (coreSecs.length != 3) {
            System.out.println("[Invalid] - coreSecs.length");
            return null;
        }
        int[] coreNums = new int[coreSecs.length];
        try {
            for (int i = 0; i < coreNums.length; ++i) {
                if (PATTERN_CORE_SEC.matcher(coreSecs[i]).matches()) {
                    coreNums[i] = Integer.parseInt(coreSecs[i]);
                } else {
                    System.out.println("[Invalid] - PATTERN_CORE_SEC");
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("[Invalid] - NumberFormatException");
            return null;
        }
        System.out.println(core + " " + pre + " " + build);
        return new ModVersion(coreNums[0], coreNums[1], coreNums[2], null);
    }

    @Override
    public String toString() {
        synchronized (text) {
            if (text.get() == null) {
                StringBuilder builder = new StringBuilder();
                builder.append(major).append('.').append(minor).append('.').append(patch);
                if (preLabels != null) {
                    builder.append('-').append(preLabels);
                }
                text.set(builder.toString());
            }
        }
        return text.get();
    }

    /**
     * @param that the object to be compared.
     * @return A positive number if {@code this} is later than {@code that}, i.e. {@code this > that}
     */
    @Override
    @ParametersAreNonnullByDefault
    public int compareTo(ModVersion that) {
        if (this == that) {
            return 0;
        } else if (major != that.major) {
            return major > that.major ? 1 : -1;
        } else if (minor != that.minor) {
            return minor > that.minor ? 1 : -1;
        } else if (patch != that.patch) {
            return patch > that.patch ? 1 : -1;
        } else {
            return AppendingVersion.compare_appendix(preLabels, that.preLabels);
        }
    }

}
