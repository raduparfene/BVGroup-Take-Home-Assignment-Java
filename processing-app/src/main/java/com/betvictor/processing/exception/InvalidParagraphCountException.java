package com.betvictor.processing.exception;

public class InvalidParagraphCountException extends RuntimeException {

    public InvalidParagraphCountException() {
        super("p must be greater than zero");
    }
}
