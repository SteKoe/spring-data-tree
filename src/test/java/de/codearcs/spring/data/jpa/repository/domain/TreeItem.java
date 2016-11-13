package de.codearcs.spring.data.jpa.repository.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import de.codearcs.spring.data.jpa.repository.ITreeItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeItem implements ITreeItem<Long> {

    @Id
    @Setter
    @GeneratedValue
    private Long id;

    @Setter
    private Long parentId;

    @Setter
    private String name;

    @Setter
    private int left;

    @Setter
    private int right;
}
