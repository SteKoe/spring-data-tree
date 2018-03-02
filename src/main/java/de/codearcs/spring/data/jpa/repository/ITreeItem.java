package de.codearcs.spring.data.jpa.repository;

import java.io.Serializable;
import java.util.Set;

public interface ITreeItem<ID extends Serializable> {
    ID getId();
    void setId(ID id);

    int getLeft();
    void setLeft(int left);

    int getRight();
    void setRight(int right);

    ITreeItem<ID> getParent();
    void setParent(ITreeItem<ID> treeItem);

    Set<? extends ITreeItem> getChildren();
    void setChildren(Set<? extends ITreeItem> children);
}
