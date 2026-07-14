package com.expenseapp.app.exceptions;

public class MaxAccountReachException extends RuntimeException{

    public MaxAccountReachException(String message) {
        super(message);
    }

}
