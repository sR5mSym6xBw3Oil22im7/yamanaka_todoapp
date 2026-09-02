package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword,
                      @Param("category") String category,
                      @Param("order") String order,
                      @Param("from") LocalDate from,
                      @Param("to") LocalDate to,
                      @Param("trash") boolean trash);

    List<Todo> searchPage(@Param("keyword") String keyword,
                          @Param("category") String category,
                          @Param("order") String order,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to,
                          @Param("limit") int limit,
                          @Param("offset") int offset,
                          @Param("trash") boolean trash);

    int countSearch(@Param("keyword") String keyword,
                    @Param("category") String category,
                    @Param("from") LocalDate from,
                    @Param("to") LocalDate to,
                    @Param("trash") boolean trash);

    Todo findById(Long id);

    Todo findDeletedById(Long id);

    void insert(Todo todo);

    void update(Todo todo);

    void markDeleted(Long id);

    void restoreById(Long id);
}
