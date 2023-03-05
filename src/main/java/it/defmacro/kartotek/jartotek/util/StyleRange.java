package it.defmacro.kartotek.jartotek.util;

import java.util.Objects;

public class StyleRange {
    protected String _type;
    protected int _start;
    protected int _end;

    public StyleRange(String type, int start, int end) {
        this._type = type;
        this._start = start;
        this._end = end;
    }

    public String type() {
        return _type;
    }

    public int start() {
        return _start;
    }

    public int end() {
        return _end;
    }

    public String toString() {
        return String.format("<StyleRange '%s', [%d; %d]>", _type, _start, _end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyleRange that = (StyleRange) o;
        return _start == that._start && _end == that._end && _type.equals(that._type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_type, _start, _end);
    }
}