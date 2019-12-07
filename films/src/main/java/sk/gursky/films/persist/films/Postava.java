package sk.gursky.films.persist.films;

public class Postava {
	private String postava;
	private String dolezitost;
	private Person herec;
	public String getPostava() {
		return postava;
	}
	public void setPostava(String postava) {
		this.postava = postava;
	}
	public String getDolezitost() {
		return dolezitost;
	}
	public void setDolezitost(String dolezitost) {
		this.dolezitost = dolezitost;
	}
	public Person getHerec() {
		return herec;
	}
	public void setHerec(Person herec) {
		this.herec = herec;
	}
	
}
