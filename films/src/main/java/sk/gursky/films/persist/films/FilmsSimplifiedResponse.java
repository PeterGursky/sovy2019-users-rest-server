package sk.gursky.films.persist.films;

import java.util.List;

public class FilmsSimplifiedResponse {
	private List<FilmSimplified> items;
	private long totalCount;
	public FilmsSimplifiedResponse(List<FilmSimplified> items, long totalCount) {
		super();
		this.items = items;
		this.totalCount = totalCount;
	}
	public List<FilmSimplified> getItems() {
		return items;
	}
	public void setItems(List<FilmSimplified> items) {
		this.items = items;
	}
	public long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
