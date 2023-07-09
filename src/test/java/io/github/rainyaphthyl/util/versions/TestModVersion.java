package io.github.rainyaphthyl.util.versions;

import java.util.SortedSet;
import java.util.TreeSet;

public class TestModVersion {
    public static final String[] TEST_BENCH = new String[]{
            "0.1.0", "0.1.0-alpha-beta", "1.0.0-rc.1.114", "2.5.6-alpha+ext", "2.5.6+ext.2.0", "2.7-0.1", "2.7.0-0.1", "2.a.6", "2.0.06", "3.75.2-beta.a8.i---8.0.7540"
    };

    public static final String[] TO_BE_SORTED = new String[]{
            "1.0.0-alpha.1", "1.0.0-beta.2", "1.0.0-alpha", "1.0.0-alpha.beta", "1.0.0", "1.0.0-beta", "1.0.0-beta.11", "1.0.0-rc.1"
    };

    public static void main(String[] args) {
        for (String sample : TEST_BENCH) {
            System.out.println("Test: " + sample);
            ModVersion version = ModVersion.getVersion(sample);
            System.out.println(version);
            System.out.println();
        }
        System.out.println("----------------");
        SortedSet<ModVersion> testSet = new TreeSet<>();
        for (String versionName : TO_BE_SORTED) {
            ModVersion version = ModVersion.getVersion(versionName);
            if (version != null) {
                testSet.add(version);
                System.out.println(version);
            }
        }
        System.out.println("----------------");
        for (ModVersion version : testSet) {
            System.out.println(version);
        }
        System.out.println("----------------");
    }
}
