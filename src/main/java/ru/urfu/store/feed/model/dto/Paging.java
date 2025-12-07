package ru.urfu.store.feed.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Paging<T> {
    Long totalCount;
    Integer limit;
    Integer offset;
    List<T> currentValues;
}
