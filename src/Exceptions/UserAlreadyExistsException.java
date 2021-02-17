package Exceptions;

public class UserAlreadyExistsException extends RuntimeException{
    @Override
    public String toString() {
        return "The username is taken by another user!";
    }
}
