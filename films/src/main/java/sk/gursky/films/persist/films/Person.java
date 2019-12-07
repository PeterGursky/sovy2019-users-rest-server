package sk.gursky.films.persist.films;

public class Person {
	private Long id;
	private String krstneMeno;
	private String stredneMeno;
	private String priezvisko;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getKrstneMeno() {
		return krstneMeno;
	}
	public void setKrstneMeno(String krstneMeno) {
		this.krstneMeno = krstneMeno;
	}
	public String getStredneMeno() {
		return stredneMeno;
	}
	public void setStredneMeno(String stredneMeno) {
		this.stredneMeno = stredneMeno;
	}
	public String getPriezvisko() {
		return priezvisko;
	}
	public void setPriezvisko(String priezvisko) {
		this.priezvisko = priezvisko;
	}
	@Override
	public String toString() {
		return "Person [id=" + id + ", Meno=" + krstneMeno + " " + stredneMeno + " " + priezvisko + "]";
	}
	
}
