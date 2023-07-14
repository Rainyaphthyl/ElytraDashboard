package io.github.rainyaphthyl.elytradashboard.util.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
        File file = new File("run/test/TestModVersion.txt");
        try (FileOutputStream writer = new FileOutputStream(file)) {
            PrintStream stream = new PrintStream(writer);
            for (String sample : TEST_BENCH) {
                stream.println("Test: " + sample);
                ModVersion version = ModVersion.getVersion(sample);
                stream.println(version);
                stream.println();
            }
            stream.println("----------------");
            SortedSet<ModVersion> testSet = new TreeSet<>();
            for (String versionName : TO_BE_SORTED) {
                ModVersion version = ModVersion.getVersion(versionName);
                if (version != null) {
                    testSet.add(version);
                    stream.println(version);
                }
            }
            stream.println("----------------");
            for (ModVersion version : testSet) {
                stream.println(version);
            }
            stream.println("----------------");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
