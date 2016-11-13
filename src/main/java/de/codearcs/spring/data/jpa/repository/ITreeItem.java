package de.codearcs.spring.data.jpa.repository;

import java.io.Serializable;

public interface ITreeItem<ID extends Serializable> {
    int getLeft();
    void setLeft(int left);

    int getRight();
    void setRight(int right);

    ID getParentId();
    void setParentId(ID id);
}
