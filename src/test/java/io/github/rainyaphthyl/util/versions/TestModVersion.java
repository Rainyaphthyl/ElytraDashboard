package io.github.rainyaphthyl.util.versions;

public class TestModVersion {
    public static final String[] TEST_BENCH = new String[]{
            "0.1.0", "0.1.0-alpha-beta", "1.0.0-rc.1.114", "2.5.6-alpha+ext", "2.5.6+ext.2.0", "2.7-0.1", "2.7.0-0.1", "2.a.6", "2.0.06"
    };

    public static void main(String[] args) {
        for (String sample : TEST_BENCH) {
            System.out.println("Test: " + sample);
            ModVersion version = ModVersion.getVersion(sample);
            System.out.println(version);
            System.out.println();
        }
    }
}
