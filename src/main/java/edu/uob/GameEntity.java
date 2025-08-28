package edu.uob;

import java.util.*;
import java.util.Objects;

public abstract class GameEntity {
    private String name;
    private String description;

    public GameEntity(String name, String description) {
        this.name = name.toLowerCase();
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GameEntity)) return false;

        GameEntity other = (GameEntity) obj;
        return this.getClass().equals(other.getClass())
                && this.name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), name.toLowerCase());
    }
}

class Character extends GameEntity {
    public Character(String name, String description) {
        super(name, description);
    }
}

class Location extends GameEntity {
    private Set<String> paths = new HashSet<>();
    private Set<GameEntity> entities = new HashSet<>();

    public Location(String name, String description) {
        super(name, description);
    }

    public void addPath(String toLocation) {
        this.paths.add(toLocation.toLowerCase());
    }

    public Set<String> getPaths() {
        return this.paths;
    }

    public void addEntity(GameEntity e) {
        this.entities.add(e);
    }

    public void removeEntity(GameEntity e) {
        this.entities.remove(e);
    }

    public Set<GameEntity> getEntities() {
        return this.entities;
    }

    public GameEntity removeEntityByName(String name) {
        Iterator<GameEntity> it = entities.iterator();
        while (it.hasNext()) {
            GameEntity e = it.next();
            if (e.getName().equalsIgnoreCase(name)) {
                it.remove();
                return e;
            }
        }
        return null;
    }
}

class Artefact extends GameEntity {
    public Artefact(String name, String description) {
        super(name, description);
    }
}

class Furniture extends GameEntity {
    public Furniture(String name, String description) {
        super(name, description);
    }
}

class Player extends GameEntity {
    private int health;
    private Set<GameEntity> inventory;

    public Player(String name, String description) {
        super(name, description);
        this.health = 3;
        this.inventory = new HashSet<>(); // Initialized with HashSet
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health > 3) {
            this.health = 3;
        } else {
            this.health = health;
        }
    }

    public Set<GameEntity> getInventory() {
        return inventory;
    }

    public void addToInventory(GameEntity entity) {
        inventory.add(entity);
    }

    public void removeFromInventory(GameEntity entity) {
        inventory.remove(entity);
    }
}
