package Exceptions;

public class UserNotFoundException extends RuntimeException {
    @Override
    public String toString() {
        return "user not found";
    }
}
