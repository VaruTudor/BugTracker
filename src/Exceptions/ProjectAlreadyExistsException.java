package Exceptions;

public class ProjectAlreadyExistsException extends RuntimeException {
    @Override
    public String toString() {
        return "there is already a project with the same id";
    }
}
