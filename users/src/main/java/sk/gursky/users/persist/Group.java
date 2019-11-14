package sk.gursky.users.persist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {
    private Long id;
    private String name;
    private List<String> permissions = new ArrayList<>();
    
    public Group() {
    }
    
    public Group(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void addPermission(String permission) {
    	if (! permissions.contains(permission))
    		permissions.add(permission);
    }
    
    public Group clone() {
    	Group g = new Group();
    	g.id = id;
    	g.name = name;
    	if (permissions == null) {
    		g.permissions = new ArrayList<>();
    	} else {
    		g.permissions = new ArrayList<>(permissions);
    	}
    	return g;
    }
    
    public static List<Group> cloneGroups(List<Group> groups) {
    	List<Group> list = new ArrayList<>();
    	if (groups == null)
    		return list;
    	for (Group g : groups) {
    		list.add(g.clone());
    	}
    	return list;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
      
    @Override
    public String toString() {
        return "Group: " + name;
    }
}
