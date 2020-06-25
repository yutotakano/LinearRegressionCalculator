package io.takano.linearregressioncalculator;

public class InvalidInputLengthException extends Exception {
    public InvalidInputLengthException() {}

    public InvalidInputLengthException(String message) {
        super(message);
    }
}
