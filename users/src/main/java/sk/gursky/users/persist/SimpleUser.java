package sk.gursky.users.persist;

public class SimpleUser {
    private Long id;
    private String name;
    private String email;
	
    public SimpleUser() {
    }
    
    public SimpleUser(MyUser u) {
    	id = u.getId();
    	name = u.getName();
    	email = u.getEmail();
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
