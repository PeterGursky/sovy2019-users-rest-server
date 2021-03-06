package sk.gursky.films.persist.users;

import java.util.List;

public interface UserDao {
	String authorizeAndGetToken(String name, String password);
	User authorizeByToken(String token);
	MyUser getMyUserById(Long userId);
	User getById(Long userId);
	List<SimpleUser> getSimpleUsers();
	List<User> getAll();
	List<User> getByGroupId(Long groupId);
	User save(User user) throws DaoException;
	boolean remove(Long userId);
	void deleteToken(String token);
	List<String> conflict(User user);
}