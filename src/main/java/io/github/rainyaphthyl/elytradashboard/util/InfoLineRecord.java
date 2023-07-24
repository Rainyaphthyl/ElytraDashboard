package io.github.rainyaphthyl.elytradashboard.util;

import java.util.Objects;

public class InfoLineRecord {
    public final String text;
    public final int txtWidth;
    public final int txtHeight;
    public final int color;
    public final boolean split;

    public InfoLineRecord(String text, int txtWidth, int txtHeight, int color, boolean split) {
        this.text = text;
        this.txtWidth = txtWidth;
        this.txtHeight = txtHeight;
        this.color = color;
        this.split = split;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InfoLineRecord)) return false;
        InfoLineRecord record = (InfoLineRecord) o;
        if (txtWidth != record.txtWidth) return false;
        if (txtHeight != record.txtHeight) return false;
        if (color != record.color) return false;
        if (split != record.split) return false;
        return Objects.equals(text, record.text);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + txtWidth;
        result = 31 * result + txtHeight;
        result = 31 * result + color;
        result = 31 * result + (split ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InfoLineRecord{" +
                "text='" + text + '\'' +
                ", posDeltaX=" + txtWidth +
                ", posDeltaY=" + txtHeight +
                ", color=" + color +
                ", split=" + split +
                '}';
    }
}
