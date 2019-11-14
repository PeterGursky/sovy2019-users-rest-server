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
	public synchronized User save(User user) {
		if (user == null)
			throw new NullPointerException("User cannot be null");
		if (user.getName() == null) 
			throw new DaoException("User name canot be null");
		removeUnknownGroups(user);
		if (user.getId() == null) {
			for(MyUser myUser : users) {
				if (myUser.getName().equals(user.getName())) {
					throw new DaoException("User with name " + user.getName() + " already exists");
				}
			}
			MyUser u = new MyUser(user);
			u.setId(++lastId);
			users.add(u);
			return getUser(u);
		} else {
			for(MyUser myUser : users) {
				if (myUser.getName().equals(user.getName()) && myUser.getId() != user.getId()) {
					throw new DaoException("Another user with name " + user.getName() + " already exists");
				}
			}
			for(MyUser myUser : users) {
				if (myUser.getId() == user.getId()) {
					myUser.setUser(user);
					return getUser(myUser);
				}
			}
			throw new DaoException("User with id = " + user.getId() + " not found - cannot be replaced" );
		}
	}

	@Override
	public synchronized boolean remove(Long userId) {
		if (userId == null)
			throw new NullPointerException("userId cannot be null");
		int admins = 0;
		for (MyUser u : users) {
			if (u.hasGroupWithId(1L) && u.isActive())
				admins++;
		}
		Iterator<MyUser> usIt = users.iterator();
		while(usIt.hasNext()) {
			MyUser us = usIt.next();
			if (us.getId() == userId) {
				if (admins == 1 && us.hasGroupWithId(1L) && us.isActive()) {
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
	public User getById(Long userId) {
		return getUser(getMyUserById(userId));
	}
}
