package com.betvictor.processing.exception;

public class NoWordsFoundException extends RuntimeException {

    public NoWordsFoundException() {
        super("Hipsum paragraphs contained no words");
    }
}
