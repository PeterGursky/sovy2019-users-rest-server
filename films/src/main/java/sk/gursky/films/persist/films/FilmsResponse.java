package sk.gursky.films.persist.films;

import java.util.List;

public class FilmsResponse {
	private List<Film> items;
	private long totalCount;
	public FilmsResponse(List<Film> items, long totalCount) {
		super();
		this.items = items;
		this.totalCount = totalCount;
	}
	public List<Film> getItems() {
		return items;
	}
	public void setItems(List<Film> items) {
		this.items = items;
	}
	public long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
