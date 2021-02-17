package Exceptions;

public class BugAlreadyExistsException extends RuntimeException{
    @Override
    public String toString() {
        return "There is already a bug with the same id!";
    }
}
