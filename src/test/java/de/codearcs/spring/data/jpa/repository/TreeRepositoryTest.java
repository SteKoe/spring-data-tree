package de.codearcs.spring.data.jpa.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Ignore;
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

        assertTreeItem(ti, nullValue(), 1, 2);
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

        assertTreeItem(ti1, nullValue(), 1, 2);
        assertTreeItem(ti2, nullValue(), 3, 4);
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

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());
        ti3 = directoryTreeRepository.findOne(ti3.getId());

        assertTreeItem(ti1, nullValue(), 1, 2);
        assertTreeItem(ti2, nullValue(), 3, 6);
        assertTreeItem(ti3, notNullValue(), 4, 5);
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

        assertTreeItem(ti1, nullValue(), 1, 4);
        assertTreeItem(ti2, nullValue(), 5, 6);
        assertThat(ti3.getParentId(), is(ti1.getId()));
        assertThat(ti3.getLeft(), is(2));
        assertThat(ti3.getRight(), is(3));
    }

    @Test
    @Ignore
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

        assertTreeItem(ti1, nullValue(), 1, 2);
        assertTreeItem(ti2, nullValue(), 3, 4);
        assertThat(ti3, nullValue());
    }

    @Test
    public void addChildren() {
        TreeItem child1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(child1);
        child1 = directoryTreeRepository.findOne(child1.getId());
        assertTreeItem(child1, nullValue(), 1, 2);

        TreeItem child11 = TreeItem.builder().name("1.1").parentId(child1.getId()).build();
        directoryTreeRepository.save(child11);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        assertTreeItem(child1, nullValue(), 1, 4);
        assertTreeItem(child11, is(child1.getId()), 2, 3);

        TreeItem child12 = TreeItem.builder().name("1.2").parentId(child11.getId()).build();
        directoryTreeRepository.save(child12);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        child12 = directoryTreeRepository.findOne(child12.getId());
        assertTreeItem(child1, nullValue(), 1, 6);
        assertTreeItem(child11, is(child1.getId()), 2, 5);
        assertTreeItem(child12, is(child11.getId()), 3, 4);

        TreeItem child111 = TreeItem.builder().name("1.1.1").parentId(child11.getId()).build();
        directoryTreeRepository.save(child111);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        child111 = directoryTreeRepository.findOne(child111.getId());
        child12 = directoryTreeRepository.findOne(child12.getId());
        assertTreeItem(child1, nullValue(), 1, 8);
        assertTreeItem(child11, is(child1.getId()), 2, 7);
        assertTreeItem(child12, is(child11.getId()), 3, 4);
        assertTreeItem(child111, is(child11.getId()), 5, 6);
    }

    private void assertTreeItem(TreeItem ti, Matcher<Object> parentValue, int left, int right) {
        assertThat(ti.getParentId(), parentValue);
        assertThat(ti.getLeft(), is(left));
        assertThat(ti.getRight(), is(right));
    }
}
