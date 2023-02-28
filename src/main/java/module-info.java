open module jartotek {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires org.fxmisc.richtext;
    requires reactfx;

    exports it.defmacro.kartotek.jartotek;
    exports it.defmacro.kartotek.jartotek.persistence;
    exports it.defmacro.kartotek.jartotek.model;
    exports it.defmacro.kartotek.jartotek.search;
    exports it.defmacro.kartotek.jartotek.ui;
    exports it.defmacro.kartotek.jartotek.util;
}