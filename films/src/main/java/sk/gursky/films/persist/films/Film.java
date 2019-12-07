package sk.gursky.films.persist.films;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Film {
	private Long id;
	private String nazov;
	private String slovenskyNazov;
	private int rok;
	private String imdbID;
	private List<Person> reziser = new ArrayList<>();
	private List<Postava> postava = new ArrayList<>();
	private Map<String, Integer> poradieVRebricku = new HashMap<String, Integer>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNazov() {
		return nazov;
	}
	public void setNazov(String nazov) {
		this.nazov = nazov;
	}
	public String getSlovenskyNazov() {
		return slovenskyNazov;
	}
	public void setSlovenskyNazov(String slovenskyNazov) {
		this.slovenskyNazov = slovenskyNazov;
	}
	public int getRok() {
		return rok;
	}
	public void setRok(int rok) {
		this.rok = rok;
	}
	public List<Person> getReziser() {
		return reziser;
	}
	public void setReziser(List<Person> reziser) {
		this.reziser = reziser;
	}
	public List<Postava> getPostava() {
		return postava;
	}
	public void setPostava(List<Postava> postava) {
		this.postava = postava;
	}
	public Map<String, Integer> getPoradieVRebricku() {
		return poradieVRebricku;
	}
	public void setPoradieVRebricku(Map<String, Integer> poradieVRebricku) {
		this.poradieVRebricku = poradieVRebricku;
	}
	public String getImdbID() {
		return imdbID;
	}
	public void setImdbID(String imdbID) {
		this.imdbID = imdbID;
	}	
	@Override
	public String toString() {
		return "Film [id=" + id + ", nazov=" + nazov + ", slovenskyNazov=" + slovenskyNazov + ", rok=" + rok
				+ ", reziser=" + reziser + ", poradieVRebricku=" + poradieVRebricku + ", pocet postav= " + postava.size() + "]";
	}
}
