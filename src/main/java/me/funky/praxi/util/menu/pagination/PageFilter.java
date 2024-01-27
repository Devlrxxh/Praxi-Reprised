package me.funky.praxi.util.menu.pagination;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PageFilter<T> {

	@Getter private final String name;
	@Getter @Setter private boolean enabled;
	private final Predicate<T> predicate;

	public boolean test(T t) {
		return !enabled || predicate.test(t);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof PageFilter && ((PageFilter) object).getName().equals(name);
	}

}
