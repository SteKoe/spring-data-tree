package de.codearcs.spring.data.jpa.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TreeRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

    List<T> findAllByParentIdIsNull();

    List<T> findAllChildren(ID parentId);
}
