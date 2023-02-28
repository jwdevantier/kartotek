package it.defmacro.kartotek.jartotek.util;

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
}