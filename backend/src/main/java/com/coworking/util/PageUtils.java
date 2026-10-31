package com.coworking.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    public static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    /** Normaliza page/size recibidos del cliente para evitar páginas negativas o gigantes. */
    public static Pageable of(Integer page, Integer size, Sort sort) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 10 : Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
