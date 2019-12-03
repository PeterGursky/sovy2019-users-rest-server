package sk.gursky.users.persist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryUserDao implements UserDao {
	
	private List<MyUser> users = new ArrayList<>();
	private Long lastId = 0L;
	
	private User getUser(MyUser myUser) {
		User u = myUser.getUser();
		List<Group> groups = DaoFactory.INSTANCE.getGroupDao().getAll();
		List<Group> uGroups = new ArrayList<>();
		for (Group g : groups) {
			if (myUser.hasGroupWithId(g.getId())) {
				uGroups.add(g);
			}
		}
		u.setGroups(uGroups);
		return u;
	}
	
	@Override
	public synchronized MyUser getMyUserById(Long userId) {
		if (userId == null)
			throw new NullPointerException("userId cannot be null");
		for (MyUser u : users) {
			if (u.getId() == userId) {
				return u;
			}
		}
		throw new DaoException("User with id = " + userId + " not found");
	}

	@Override
	public synchronized List<User> getAll() {
		List<User> userList = new ArrayList<>();
		for (MyUser mu : users) {
			userList.add(getUser(mu));
		}
		return userList;
	}

	@Override
	public synchronized List<SimpleUser> getSimpleUsers() {
		List<SimpleUser> userList = new ArrayList<>();
		for (MyUser mu : users) {
			if (mu.isActive())
				userList.add(mu.getSimpleUser());
		}
		return userList;
	}
	

	@Override
	public synchronized List<User> getByGroupId(Long groupId) {
		List<User> result = new ArrayList<>();
		for (MyUser u : users) {
			if (u.hasGroupWithId(groupId)) {
				result.add(getUser(u));
			}
		}
		return result;
	}
	
	private void removeUnknownGroups(User user) {
		if (user.getGroups() != null) {
			Iterator<Group> itGr = user.getGroups().iterator();
			List<Group> groups = DaoFactory.INSTANCE.getGroupDao().getAll();
			
			bigWhile: while (itGr.hasNext()) {
				Group ug = itGr.next();
				for (Group g : groups) {
					if (g.getId() == ug.getId()) {
						continue bigWhile;
					}
				}
				itGr.remove();
			}
		} else {
			user.setGroups(new ArrayList<>());
		}		
	}

	@Override
	public synchronized User save(User user) throws DaoException {
		if (user == null)
			throw new NullPointerException("User cannot be null");
		if (user.getName() == null) 
			throw new DaoException("User name canot be null");
		removeUnknownGroups(user);
		testConflict(user);
		if (user.getId() == null) {
			MyUser u = new MyUser(user);
			u.setId(++lastId);
			users.add(u);
			return getUser(u);
		} else {
			for(MyUser myUser : users) {
				if (myUser.getId() == user.getId()) {
					if (isLastAdmin(user.getId())) {
						boolean hasAdmin = false;
						for (Group g: user.getGroups()) {
							if (g.getId() == 1L)
								hasAdmin = true;
						}
						if (!hasAdmin) {
							throw new DaoException("You cannot loose the last active admin");
						}
					}
					myUser.setUser(user);
					return getUser(myUser);
				}
			}
			throw new DaoException("User with id = " + user.getId() + " not found - cannot be replaced" );
		}
	}
	
	@Override
	public synchronized List<String> conflict(User user){
		List<String> confictFields = new ArrayList<String>();
		for (MyUser mu: users) {
			if (mu.getEmail().equals(user.getEmail().trim()))
				confictFields.add("email");
			if (mu.getName().equals(user.getName().trim()))
				confictFields.add("name");;
		}
		return confictFields;
	}
	
	private synchronized void testConflict(User user) throws DaoException{
		for (MyUser mu: users) {
			if (mu.getEmail().equals(user.getEmail().trim()) && user.getId() != mu.getId())
				throw new DaoException("user with the same email already exists");
			if (mu.getName().equals(user.getName().trim()) && user.getId() != mu.getId())
				throw new DaoException("user with the same name already exists");
		}
	}

	private synchronized boolean isLastAdmin(Long userId) {
		int admins = 0;
		boolean isAdmin = false;
		for (MyUser u : users) {
			if (u.hasGroupWithId(1L) && u.isActive())
				admins++;
			if (userId == u.getId() && u.hasGroupWithId(1L) && u.isActive())
				isAdmin = true;
		}
		return (isAdmin && admins == 1);
	}

	@Override
	public synchronized boolean remove(Long userId) {
		if (userId == null)
			throw new NullPointerException("userId cannot be null");
		Iterator<MyUser> usIt = users.iterator();
		while(usIt.hasNext()) {
			MyUser us = usIt.next();
			if (us.getId() == userId) {
				if (isLastAdmin(userId)) {
					throw new DaoException("You cannot remove the last active admin");
				}
				usIt.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized String authorizeAndGetToken(String name, String password) {
		for (MyUser u: users) {
			if (u.getName().equals(name)) {
				if (u.isActive())
					return u.checkPasswordAndGetToken(password);
				else 
					throw new DaoException("user is not active");
			}
		}
		return null;
	}

	@Override
	public synchronized User authorizeByToken(String token) {
		for (MyUser u: users) {
			if (u.isActive()) {
				if (u.checkToken(token)) {
					return getUser(u);
				} 
			}
		}
		return null;
	}

	@Override
	public synchronized User getById(Long userId) {
		return getUser(getMyUserById(userId));
	}

	@Override
	public synchronized void deleteToken(String token) {
		for (MyUser u: users) {
			if (u.isActive()) {
				if (u.deleteToken(token)) {
					return;
				} 
			}
		}
	}
}
