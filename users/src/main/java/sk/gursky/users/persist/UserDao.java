package sk.gursky.users.persist;

import java.util.List;

public interface UserDao {
	String authorizeAndGetToken(String name, String password);
	User authorizeByToken(String token);
	MyUser getMyUserById(Long userId);
	User getById(Long userId);
	List<SimpleUser> getSimpleUsers();
	List<User> getAll();
	List<User> getByGroupId(Long groupId);
	User save(User user);
	boolean remove(Long userId);
}