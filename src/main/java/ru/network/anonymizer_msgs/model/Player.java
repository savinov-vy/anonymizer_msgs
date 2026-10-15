package ru.network.anonymizer_msgs.model;

import lombok.Setter;
import ru.network.anonymizer_msgs.enums.Role;

@Setter
public class Player {
    private final String id;
    private final String name;
    private Role role;
    private boolean isAlive;

    public Player(String id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.isAlive = true;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    public void setRole(Role role) {
        this.role = role;
    }
}
