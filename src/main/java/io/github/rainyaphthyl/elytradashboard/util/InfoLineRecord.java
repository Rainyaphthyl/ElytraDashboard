package io.github.rainyaphthyl.elytradashboard.util;

import java.util.Objects;

public class InfoLineRecord {
    public final String text;
    public final int posDeltaX;
    public final int posDeltaY;
    public final int color;
    public final boolean split;

    public InfoLineRecord(String text, int posDeltaX, int posDeltaY, int color, boolean split) {
        this.text = text;
        this.posDeltaX = posDeltaX;
        this.posDeltaY = posDeltaY;
        this.color = color;
        this.split = split;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InfoLineRecord)) return false;
        InfoLineRecord record = (InfoLineRecord) o;
        if (posDeltaX != record.posDeltaX) return false;
        if (posDeltaY != record.posDeltaY) return false;
        if (color != record.color) return false;
        if (split != record.split) return false;
        return Objects.equals(text, record.text);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + posDeltaX;
        result = 31 * result + posDeltaY;
        result = 31 * result + color;
        result = 31 * result + (split ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InfoLineRecord{" +
                "text='" + text + '\'' +
                ", posDeltaX=" + posDeltaX +
                ", posDeltaY=" + posDeltaY +
                ", color=" + color +
                ", split=" + split +
                '}';
    }
}
