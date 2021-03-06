package de.codearcs.spring.data.jpa.repository;

import de.codearcs.spring.data.jpa.repository.domain.TreeItem;
import lombok.Setter;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class TreeRepositoryTest {

    @Setter(onMethod = @__({@Autowired}))
    private DirectoryTreeRepository directoryTreeRepository;

    @After
    public void removeAllData() {
        directoryTreeRepository.deleteAll();
        assertThat(directoryTreeRepository.count(), is(0L));
    }

    /**
     * <img src="doc-files/savingSingleRoot.png">
     */
    @Test
    public void savingSingleRoot() {
        TreeItem ti = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti);

        assertTreeItem(ti, nullValue(), 1, 2);
        assertThat(ti.getChildren(), nullValue());
    }

    /**
     * <img src="doc-files/saveItemWithExistingParent.png">
     */
    @Test
    public void saveItemWithExistingParent() {
        TreeItem ti1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().parent(ti1).name("2").build();
        directoryTreeRepository.save(ti2);

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());

        assertThat(ti2.getParent(), is(ti1));
        assertThat(ti2.getLeft(), is(2));
        assertThat(ti2.getRight(), is(3));
        assertThat(ti1.getChildren(), hasSize(1));
        assertThat(ti1.getChildren(), contains(ti2));
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

        TreeItem ti3 = TreeItem.builder().parent(ti2).name("1.2.1").build();
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

        TreeItem ti3 = TreeItem.builder().parent(ti1).name("1.1").build();
        directoryTreeRepository.save(ti3);

        ti1 = directoryTreeRepository.findOne(ti1.getId());
        ti2 = directoryTreeRepository.findOne(ti2.getId());
        ti3 = directoryTreeRepository.findOne(ti3.getId());

        assertTreeItem(ti1, nullValue(), 1, 4);
        assertTreeItem(ti2, nullValue(), 5, 6);
        assertThat(ti3.getParent(), is(ti1));
        assertThat(ti3.getLeft(), is(2));
        assertThat(ti3.getRight(), is(3));
    }

    @Test
    public void removeChild() {
        TreeItem ti1 = TreeItem.builder().name("1").build();
        directoryTreeRepository.save(ti1);

        TreeItem ti2 = TreeItem.builder().name("2").build();
        directoryTreeRepository.save(ti2);

        TreeItem ti3 = TreeItem.builder().parent(ti2).name("2.1").build();
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

        TreeItem child11 = TreeItem.builder().name("1.1").parent(child1).build();
        directoryTreeRepository.save(child11);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        assertTreeItem(child1, nullValue(), 1, 4);
        assertTreeItem(child11, is(child1), 2, 3);

        TreeItem child12 = TreeItem.builder().name("1.2").parent(child11).build();
        directoryTreeRepository.save(child12);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        child12 = directoryTreeRepository.findOne(child12.getId());
        assertTreeItem(child1, nullValue(), 1, 6);
        assertTreeItem(child11, is(child1), 2, 5);
        assertTreeItem(child12, is(child11), 3, 4);

        TreeItem child111 = TreeItem.builder().name("1.1.1").parent(child11).build();
        directoryTreeRepository.save(child111);
        child1 = directoryTreeRepository.findOne(child1.getId());
        child11 = directoryTreeRepository.findOne(child11.getId());
        child111 = directoryTreeRepository.findOne(child111.getId());
        child12 = directoryTreeRepository.findOne(child12.getId());
        assertTreeItem(child1, nullValue(), 1, 8);
        assertTreeItem(child11, is(child1), 2, 7);
        assertTreeItem(child12, is(child11), 3, 4);
        assertTreeItem(child111, is(child11), 5, 6);
    }

    @Test
    public void countAllChildren() {
        TreeItem child1 = TreeItem.builder().name("1").build();
        child1 = directoryTreeRepository.save(child1);

        TreeItem child2 = TreeItem.builder().parent(child1).name("2").build();
        directoryTreeRepository.save(child2);

        assertThat(directoryTreeRepository.count(), is(2L));
    }

    private void assertTreeItem(TreeItem ti, Matcher<Object> parentItem, int left, int right) {
        assertThat(ti.getParent(), parentItem);
        assertThat(ti.getLeft(), is(left));
        assertThat(ti.getRight(), is(right));
    }
}
