package it.defmacro.kartotek.jartotek.util;

import java.util.Optional;

public class LinkStyleRange extends StyleRange {
    protected String _href;
    protected Optional<String> _lbl;
    public LinkStyleRange(String href, Optional<String> lbl, int start, int end) {
        super(StyleType.LINK, start, end);
        this._href = href;
        this._lbl = lbl;
    }

    public String href() {
        return this._href;
    }

    public Optional<String> lbl() {
        return this._lbl;
    }
}
