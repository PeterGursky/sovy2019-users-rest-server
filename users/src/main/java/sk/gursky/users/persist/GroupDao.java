package sk.gursky.users.persist;

import java.util.List;

public interface GroupDao {

	Group getById(Long groupId);
	
	List<Group> getAll();

	Group save(Group group) throws NullPointerException, DaoException;

	boolean remove(Long groupId) throws NullPointerException, DaoException;

}