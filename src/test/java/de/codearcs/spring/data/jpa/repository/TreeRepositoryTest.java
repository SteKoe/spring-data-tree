package de.codearcs.spring.data.jpa.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codearcs.spring.data.jpa.repository.domain.TreeItem;
import lombok.Setter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class TreeRepositoryTest {

    @Setter(onMethod = @__({ @Autowired }))
    private DirectoryTreeRepository directoryTreeRepository;

    @After
    public void removeAllData() {
        directoryTreeRepository.deleteAll();
    }

    /**
     * <img src="doc-files/savingSingleRoot.png">
     */
    @Test
    public void savingSingleRoot() {
        TreeItem ti = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti);

        assertThat(ti.getParentId(), nullValue());
        assertThat(ti.getLeft(), is(1));
        assertThat(ti.getRight(), is(2));
    }

    /**
     * <img src="doc-files/saveItemWithExistingParent.png">
     */
    @Test
    public void saveItemWithExistingParent() {
        TreeItem ti1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().parentId(ti1.getId()).name("2").build();
        directoryTreeRepository.save(ti2);

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());

        assertThat(ti2.getParentId(), is(ti1.getId()));
        assertThat(ti2.getLeft(), is(2));
        assertThat(ti2.getRight(), is(3));
    }

    /**
     * <img src="doc-files/addTwoRoots.png">
     */
    @Test
    public void addTwoRoots() {
        TreeItem ti1 = TreeItem.builder().name("1.1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().name("1.2").build();
        directoryTreeRepository.save(ti2);

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());

        assertThat(ti1.getParentId(), nullValue());
        assertThat(ti1.getLeft(), is(1));
        assertThat(ti1.getRight(), is(2));
        assertThat(ti2.getParentId(), nullValue());
        assertThat(ti2.getLeft(), is(3));
        assertThat(ti2.getRight(), is(4));
    }

    /**
     * <img src="doc-files/savingTwoRootsWithChild.png">
     */
    @Test
    public void savingTwoRootsWithChildOnRight() {
        TreeItem ti1 = TreeItem.builder().name("1.1").build();
        ti1 = directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().name("1.2").build();
        ti2 = directoryTreeRepository.save(ti2);

        TreeItem ti3 = TreeItem.builder().parentId(ti2.getId()).name("1.2.1").build();
        ti3 = directoryTreeRepository.save(ti3);

//        ti1 = directoryTreeRepository.findOne(ti1.getId());
//        ti2 = directoryTreeRepository.findOne(ti2.getId());
//        ti3 = directoryTreeRepository.findOne(ti3.getId());

        assertThat(ti1.getParentId(), nullValue());
        assertThat(ti1.getLeft(), is(1));
        assertThat(ti1.getRight(), is(2));
        assertThat(ti2.getParentId(), nullValue());
        assertThat(ti2.getLeft(), is(3));
        assertThat(ti2.getRight(), is(6));
        assertThat(ti3.getParentId(), notNullValue());
        assertThat(ti3.getLeft(), is(4));
        assertThat(ti3.getRight(), is(5));
    }

    @Test
    public void savingTwoRootsWithChildOnLeft() {
        TreeItem ti1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().name("2").build();
        directoryTreeRepository.save(ti2);

        TreeItem ti3 = TreeItem.builder().parentId(ti1.getId()).name("1.1").build();
        directoryTreeRepository.save(ti3);

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());
        ti3 = directoryTreeRepository.findOne(ti3.getId());

        assertThat(ti1.getParentId(), nullValue());
        assertThat(ti1.getLeft(), is(1));
        assertThat(ti1.getRight(), is(4));
        assertThat(ti2.getParentId(), nullValue());
        assertThat(ti2.getLeft(), is(5));
        assertThat(ti2.getRight(), is(6));
        assertThat(ti3.getParentId(), is(ti1.getId()));
        assertThat(ti3.getLeft(), is(2));
        assertThat(ti3.getRight(), is(3));
    }

    @Test
    public void removeAChild() {
        TreeItem ti1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().name("2").build();
        directoryTreeRepository.save(ti2);

        TreeItem ti3 = TreeItem.builder().parentId(ti2.getId()).name("2.1").build();
        directoryTreeRepository.save(ti3);

        directoryTreeRepository.delete(ti3.getId());

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());
        ti3 = directoryTreeRepository.findOne(ti3.getId());

        assertThat(ti1.getParentId(), nullValue());
        assertThat(ti1.getLeft(), is(1));
        assertThat(ti1.getRight(), is(2));
        assertThat(ti2.getParentId(), nullValue());
        assertThat(ti2.getLeft(), is(3));
        assertThat(ti2.getRight(), is(4));
        assertThat(ti3, nullValue());
    }
}
