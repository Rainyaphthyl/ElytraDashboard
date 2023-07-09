package io.github.rainyaphthyl.util.versions;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
 *
 * <pre>{@code <valid semver> ::= <version core>
 *                  | <version core> "-" <pre-release>
 *                  | <version core> "+" <build>
 *                  | <version core> "-" <pre-release> "+" <build>
 *
 * <version core> ::= <major> "." <minor> "." <patch>
 *
 * <major> ::= <numeric identifier>
 *
 * <minor> ::= <numeric identifier>
 *
 * <patch> ::= <numeric identifier>
 *
 * <pre-release> ::= <dot-separated pre-release identifiers>
 *
 * <dot-separated pre-release identifiers> ::= <pre-release identifier>
 *                                           | <pre-release identifier> "." <dot-separated pre-release identifiers>
 *
 * <build> ::= <dot-separated build identifiers>
 *
 * <dot-separated build identifiers> ::= <build identifier>
 *                                     | <build identifier> "." <dot-separated build identifiers>
 *
 * <pre-release identifier> ::= <alphanumeric identifier>
 *                            | <numeric identifier>
 *
 * <build identifier> ::= <alphanumeric identifier>
 *                      | <digits>
 *
 * <alphanumeric identifier> ::= <non-digit>
 *                             | <non-digit> <identifier characters>
 *                             | <identifier characters> <non-digit>
 *                             | <identifier characters> <non-digit> <identifier characters>
 *
 * <numeric identifier> ::= "0"
 *                        | <positive digit>
 *                        | <positive digit> <digits>
 *
 * <identifier characters> ::= <identifier character>
 *                           | <identifier character> <identifier characters>
 *
 * <identifier character> ::= <digit>
 *                          | <non-digit>
 *
 * <non-digit> ::= <letter>
 *               | "-"
 *
 * <digits> ::= <digit>
 *            | <digit> <digits>
 *
 * <digit> ::= "0"
 *           | <positive digit>
 *
 * <positive digit> ::= "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
 *
 * <letter> ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J"
 *            | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T"
 *            | "U" | "V" | "W" | "X" | "Y" | "Z" | "a" | "b" | "c" | "d"
 *            | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n"
 *            | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x"
 *            | "y" | "z"}</pre>
 */
public class ModVersion implements Comparable<ModVersion> {
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
        return new ModVersion(0, 0, 0, null);
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
