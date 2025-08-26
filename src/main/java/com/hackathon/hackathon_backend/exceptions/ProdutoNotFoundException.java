package com.hackathon.hackathon_backend.exceptions;

public class ProdutoNotFoundException extends RuntimeException{
    public ProdutoNotFoundException(String message){
        super(message);
    }
}
