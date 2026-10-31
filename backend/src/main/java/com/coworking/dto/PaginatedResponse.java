package com.coworking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> content;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageNumber;
    private Integer pageSize;

    public static <E, T> PaginatedResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return PaginatedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
