package sk.gursky.users.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sk.gursky.users.persist.DaoException;
import sk.gursky.users.persist.DaoFactory;
import sk.gursky.users.persist.Group;
import sk.gursky.users.persist.GroupDao;
import sk.gursky.users.persist.MyUser;
import sk.gursky.users.persist.SimpleUser;
import sk.gursky.users.persist.User;
import sk.gursky.users.persist.UserDao;

@CrossOrigin
@RestController
public class UsersController {

	private UserDao userDao = DaoFactory.INSTANCE.getUserDao();
	private GroupDao groupDao = DaoFactory.INSTANCE.getGroupDao();
	
	@RequestMapping("/users")
    public List<SimpleUser> getSimpleUsers() {
        return userDao.getSimpleUsers();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String getToken(@RequestBody NameAndPassword nameAndPassword) {
        String token = userDao.authorizeAndGetToken(nameAndPassword.getName(), nameAndPassword.getPassword());
        if (token == null)
        	throw new UnauthorizedActionException("wrong name or password");
        return token;
    }

    @RequestMapping(value = "/logout/{token}")
    public void getToken(@PathVariable String token) {
        userDao.deleteToken(token);
    }
	
    @RequestMapping(value = "/check-token/{token}")
    public void checkToken(@PathVariable String token) {
		if (null == userDao.authorizeByToken(token)) {
			throw new UnauthorizedActionException("unknown token");
		}
    }
    
    /**
     * Returns list of conflict fields. Possible values are 'name' and 'email'.
     * @param user
     * @return
     */
    @RequestMapping(value = "/test-conflict", method = RequestMethod.POST)
    public List<String> testConflict(@RequestBody User user) {
		return userDao.conflict(user);
    }    

    @RequestMapping("/users/{token}")
    public List<User> getUsers(@PathVariable String token) {
    	User user = userDao.authorizeByToken(token);
    	if (user != null)
    		if (user.hasPermission("view_users"))
    			return userDao.getAll();
    		else
    			throw new ForbiddenActionException("view_users permission needed");
   		throw new UnauthorizedActionException("unknown token");
    }

    @RequestMapping("/bg-user/{id}/{token}")
    public MyUser getMyUserById(@PathVariable Long id, @PathVariable String token) {
    	User user = userDao.authorizeByToken(token);
    	if (user != null)
    		if (user.hasPermission("see_bg-user"))
    			return userDao.getMyUserById(id);
    		else
    			throw new ForbiddenActionException("see_bg-user permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }

    @RequestMapping("/user/{id}/{token}")
    public User getUserById(@PathVariable Long id, @PathVariable String token) {
    	User user = userDao.authorizeByToken(token);
    	if (user != null)
    		if (user.hasPermission("view_users"))
    			return userDao.getById(id);
    		else
    			throw new ForbiddenActionException("view_users permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/users/{token}", method = RequestMethod.POST)
    public User saveUser(@PathVariable String token, @RequestBody User user) {
    	User u = userDao.authorizeByToken(token);
    	if (u != null)
    		if (u.hasPermission("manage_users"))
				try {
					return userDao.save(user);
				} catch (DaoException e) {
					throw new ForbiddenActionException(e.getMessage());
				}
			else
    			throw new ForbiddenActionException("manage_users permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public User register(@RequestBody User user) {
		try {
			user.setId(null);
			user.setActive(true);
			user.setGroups(new ArrayList<>());
			user.setLastLogin(null);
			return userDao.save(user);
		} catch (DaoException e) {
			throw new ForbiddenActionException(e.getMessage());
		}    			
    }
    
    @RequestMapping(value = "/user/{id}/{token}", method = RequestMethod.DELETE)
    public void removeUserById(@PathVariable Long id, @PathVariable String token) {
    	User u = userDao.authorizeByToken(token);
    	if (u != null)
    		if (u.hasPermission("manage_users")) {
    			if (userDao.remove(id)) {
    				return;
    			} else {
    				throw new DaoException("User with id = " + id + " not found - cannot be removed");
    			}
    		} else {
    			throw new ForbiddenActionException("manage_users permission needed");
    		}
    	throw new UnauthorizedActionException("unknown token");
    }

	@RequestMapping("/groups")
    public List<Group> getAllGroups() {
        return groupDao.getAll();
    }

    @RequestMapping("/group/{id}")
    public Group getGroupById(@PathVariable Long id) {
        return groupDao.getById(id);
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/groups/{token}", method = RequestMethod.POST)
    public Group saveGroup(@RequestBody Group group, @PathVariable String token) {
    	User u = userDao.authorizeByToken(token);
    	if (u != null)
    		if (u.hasPermission("manage_groups"))
    			return groupDao.save(group);
    		else
    			throw new ForbiddenActionException("manage_groups permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }
    
    @RequestMapping(value = "/group/{id}/{token}", method = RequestMethod.DELETE)
    public void removeGroupById(@PathVariable Long id, @PathVariable String token) {
    	User u = userDao.authorizeByToken(token);
    	if (u != null)
    		if (u.hasPermission("manage_groups")) {
    			if (groupDao.remove(id)) {
    				return;
    			} else {
    				throw new DaoException("Group with id = " + id + " not found - cannot be removed");
    			}
    		} else {
    			throw new ForbiddenActionException("manage_groups permission needed");
    		}
    	throw new UnauthorizedActionException("unknown token");
    }
}
