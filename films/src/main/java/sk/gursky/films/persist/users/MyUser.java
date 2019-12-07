package sk.gursky.films.persist.users;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class MyUser {

	public static final long TOKEN_VALIDITY = 300000L; //5 mins in ms
	
    private Long id;
    private String name;
    private String passwordHash;
    private String salt;
    private String email;
    private LocalDateTime lastLogin;
    private boolean active;
    private List<Long> groupIds;
    private Map<String, Long> tokens = new HashMap<>();

    public MyUser() {
    }
    
    public MyUser(User user) {
    	setUser(user);
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

    public void setPassword(String password) {
        if (salt == null) {
            salt = BCrypt.gensalt();
        }
        this.passwordHash = BCrypt.hashpw(password, salt);
    }

    public String checkPasswordAndGetToken(String password) {
    	if (tokens.size() > 1000) {
    		Iterator<Entry<String, Long>> it = tokens.entrySet().iterator();
    		while (it.hasNext()) {
    			Entry<String, Long> entry = it.next();
    			if (entry.getValue() + TOKEN_VALIDITY < System.currentTimeMillis()) {
    				it.remove();
    			}
    		}
    		if (tokens.size() > 1000) {
    			throw new RuntimeException("Too many logins of the same user in 5 minutes");
    		}
    	}
        boolean ok = BCrypt.checkpw(password, this.passwordHash);
        if (ok) {
        	lastLogin = LocalDateTime.now();
        	String token = new BigInteger(130, new SecureRandom()).toString(32);
        	tokens.put(token, System.currentTimeMillis());
        	return token;
        }
        return null;
    }
    
    public boolean checkToken(String token) {
    	Long tokenTime = tokens.get(token);
    	if (tokenTime == null) {
        	return false;
    	}
    	if (tokenTime + TOKEN_VALIDITY < System.currentTimeMillis()) {
    		tokens.remove(token);
    		return false;
    	}
    	tokens.put(token, System.currentTimeMillis());
		return true;
    }
    
    public boolean deleteToken(String token) {
    	return tokens.remove(token) != null;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public boolean isActive() {
		return active;
	}
    
    public void setActive(boolean active) {
		this.active = active;
	}
    
    public void addGroupId(long groupId) {
    	if (! groupIds.contains(groupId)) {
    		groupIds.add(groupId);
    	}
    }
    
    public boolean hasGroupWithId(long groupId) {
    	return groupIds.contains(groupId);
    }

    public void setUser(User user) {
    	name = user.getName();
    	email = user.getEmail();
    	active = user.isActive();
    	groupIds = new ArrayList<>();
    	if (user.getPassword() != null && ! user.getPassword().trim().isEmpty()) {
    		setPassword(user.getPassword().trim());
    		tokens = new HashMap<>();
    	}
    	for (Group g: user.getGroups()) {
    		groupIds.add(g.getId());
    	}
    }
    
    public User getUser() {
    	User u = new User();
    	u.setId(id);
    	u.setName(name);
    	u.setEmail(email);
    	u.setLastLogin(lastLogin);
    	u.setActive(active);
    	return u;
    }
    
    public SimpleUser getSimpleUser() {
    	SimpleUser su = new SimpleUser();
    	su.setId(id);
    	su.setName(name);
    	su.setEmail(email);
    	return su;
    }
    
    @Override
    public String toString() {
        return id + ". " + name;
    }

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getSalt() {
		return salt;
	}

	public List<Long> getGroupIds() {
		return groupIds;
	}

	public Map<String, Long> getTokens() {
		return tokens;
	}
}
