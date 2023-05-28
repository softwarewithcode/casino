module casino.texasholdem {
    exports com.casino.poker.export;

    requires transitive casino.common;
    requires jakarta.websocket;
    requires java.logging;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
}