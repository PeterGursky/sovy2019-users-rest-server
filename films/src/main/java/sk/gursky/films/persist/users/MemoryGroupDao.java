package sk.gursky.films.persist.users;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sk.gursky.films.persist.DaoFactory;

public class MemoryGroupDao implements GroupDao {
	private List<Group> groups = new ArrayList<>();
	private Long lastId = 0L;
	
	@Override
	public synchronized Group getById(Long groupId) {
		if (groupId == null)
			throw new NullPointerException("groupId cannot be null");
		for (Group g : groups) {
			if (g.getId() == groupId) {
				return g.clone();
			}
		}
		throw new DaoException("Group with id = " + groupId + " not found");
	}	
	
	@Override
	public synchronized List<Group> getAll() {
		return Group.cloneGroups(groups); 
	}
	
	@Override
	public synchronized Group save(Group group) throws NullPointerException, DaoException {
		if (group == null)
			throw new NullPointerException("Group cannot be null");
		if (group.getName() == null) 
			throw new DaoException("Group name canot be null");
		Group g = group.clone();
		if (g.getId() == null) {
			g.setId(++lastId);
			groups.add(g);
			return g.clone();
		} else {
			for (int i = 0; i < groups.size(); i++) {
				if (groups.get(i).getId() == g.getId()) {
					groups.set(i, g);
					return g.clone();
				}
			}
			throw new DaoException("Group with id = " + g.getId() + " not found - cannot be replaced" );
		}
	}
	
	@Override
	public boolean remove(Long groupId) throws NullPointerException, DaoException {
		if (groupId == 1L) {
			throw new DaoException("Admin group cannot be removed");
		}
		UserDao userDao = DaoFactory.INSTANCE.getUserDao();
		synchronized (userDao) {	// aby sme nemali deadlock
			synchronized (this) {
				if (groupId == null)
					throw new NullPointerException("idGroup cannot be null");
				Iterator<Group> grIt = groups.iterator();
				while(grIt.hasNext()) {
					Group gr = grIt.next();
					if (gr.getId() == groupId) {
						if (userDao.getByGroupId(groupId).isEmpty()) {
							grIt.remove();
							return true;
						} else {
							throw new DaoException("Group with id = " + groupId + " is not empty");
						}
					}
				}
				return false;
			}
		}
	}
}
