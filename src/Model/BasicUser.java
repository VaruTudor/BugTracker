package Model;

import java.util.Objects;

public class BasicUser implements User{
    private final String username, password;

    public BasicUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "BasicUser " +
                username  +
                ", password='" + password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicUser basicUser = (BasicUser) o;
        return Objects.equals(username, basicUser.username) && Objects.equals(password, basicUser.password);
    }

}
