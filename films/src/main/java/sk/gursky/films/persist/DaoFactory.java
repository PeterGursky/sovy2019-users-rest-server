package sk.gursky.films.persist;

import sk.gursky.films.persist.films.FileFilmDao;
import sk.gursky.films.persist.films.FilmDao;
import sk.gursky.films.persist.users.Group;
import sk.gursky.films.persist.users.GroupDao;
import sk.gursky.films.persist.users.MemoryGroupDao;
import sk.gursky.films.persist.users.MemoryUserDao;
import sk.gursky.films.persist.users.User;
import sk.gursky.films.persist.users.UserDao;

public enum DaoFactory {
	INSTANCE;

//	private JdbcTemplate jdbcTemplate;
	private FilmDao filmDao;
	private UserDao userDao;
	private GroupDao groupDao;
	private boolean usersInserted = false;
	
	private DaoFactory() {
		userDao = new MemoryUserDao();
		groupDao = new MemoryGroupDao();
	}

	public UserDao getUserDao() {
		if (!usersInserted)
			addDummyUserData();
		return userDao;
	}

	public GroupDao getGroupDao() {
		if (!usersInserted)
			addDummyUserData();
		return groupDao;
	}

	void addDummyUserData() {
		usersInserted = true;
		Group g1 = new Group();
		g1.setName("admin");
		g1.addPermission("manage_users");
		g1.addPermission("see_bg-user");
		g1.addPermission("view_users");
		g1.addPermission("manage_groups");
		g1.addPermission("show_films");
		g1 = groupDao.save(g1);
		Group g2 = new Group();
		g2.setName("employee");
		g2.addPermission("view_users");
		g2.addPermission("show_films");
		g2.addPermission("manage_films");
		g2 = groupDao.save(g2);
		Group g3 = new Group();
		g3.setName("secretary");
		g1.addPermission("manage_users");
		g3.addPermission("view_users");
		g3 = groupDao.save(g3);
		User u1 = new User();
		u1.setName("Peter");
		u1.setEmail("peter.gursky@upjs.sk");
		u1.setPassword("upjs");
		u1.setActive(true);
		u1.addGroup(g1);
		User u2 = new User();
		u2.setName("Lucia");
		u2.setEmail("lucia@lucia.sk");
		u2.setPassword("lucia");
		u2.setActive(true);
		u2.addGroup(g2);
		User u3 = new User();
		u3.setName("John");
		u3.setEmail("john@theripper.com");
		u3.setPassword("john");
		u3.setActive(true);
		User u4 = new User();
		u4.setName("Andrej");
		u4.setEmail("andrej@parlament.sk");
		u4.setPassword("andre");
		u4.setActive(false);
		u4.addGroup(g1);
		u4.addGroup(g2);
		u4.addGroup(g3);
		userDao.save(u1);
		userDao.save(u2);
		userDao.save(u3);
		userDao.save(u4);
		userDao.authorizeAndGetToken("Peter", "upjs");
	}

	public FilmDao getFilmDao() {
		if (filmDao == null)
//			filmDao = new MysqlFilmDao(getJdbcTemplate());
			filmDao = new FileFilmDao();
		return filmDao;
	}

//	private JdbcTemplate getJdbcTemplate() {
//		if (jdbcTemplate == null) {
//			MysqlDataSource dataSource = new MysqlDataSource();
//			dataSource.setUser("film");
//			dataSource.setPassword("film");
//			dataSource.setUrl("jdbc:mysql://localhost/film3?serverTimezone=Europe/Bratislava");
//			jdbcTemplate = new JdbcTemplate(dataSource);
//		}
//		return jdbcTemplate;
//	}
}
