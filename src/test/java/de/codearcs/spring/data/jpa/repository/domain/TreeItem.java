package de.codearcs.spring.data.jpa.repository.domain;

import de.codearcs.spring.data.jpa.repository.ITreeItem;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
public class TreeItem implements ITreeItem<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity = TreeItem.class)
    private TreeItem parent;

    private String name;

    private int left;

    private int right;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<TreeItem> children;

    @Override
    public void setParent(ITreeItem<Long> treeItem) {
        this.parent = parent;
    }

    @Override
    public void setChildren(Set<? extends ITreeItem> children) {
        this.children = (Set<TreeItem>) children;
    }
}
