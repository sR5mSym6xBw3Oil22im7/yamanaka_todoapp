package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword,
                      @Param("category") String category,
                      @Param("order") String order);

    Todo findById(Long id);

    void insert(Todo todo);

    void update(Todo todo);

    void deleteById(Long id);
}
