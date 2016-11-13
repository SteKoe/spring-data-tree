package de.codearcs.spring.data.jpa.repository.support;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import de.codearcs.spring.data.jpa.repository.ITreeItem;
import de.codearcs.spring.data.jpa.repository.TreeRepository;

@NoRepositoryBean
public class TreeRepositoryImpl<T extends ITreeItem<ID>, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements TreeRepository<T, ID> {

    EntityManager entityManager;

    public TreeRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    public TreeRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.entityManager = em;
    }

    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        ID parentId = (ID) entity.getParentId();

        List<T> sameLevelChildren = findAllChildren(parentId);
        if (sameLevelChildren.size() != 0) {
            T t = sameLevelChildren.get(sameLevelChildren.size() - 1);
            entity.setLeft(t.getRight() + 1);
            entity.setRight(t.getRight() + 2);

            return (S) super.save(entity);
        } else {
            if (parentId != null) {
                T parent = (T) super.findOne(parentId);
                int parentRight = parent.getRight();
                entity.setLeft(parentRight);
                entity.setRight(parentRight + 1);
                this.moveOtherItems(parentRight);
            } else {
                entity.setLeft(1);
                entity.setRight(2);
            }
            return (S) super.save(entity);
        }
    }

    private void moveOtherItems(int parentRight) {
        List<T> items = this.findByLeftGreaterThanEqualOrRightGreaterThanEqual(parentRight);

        items.forEach(item -> {
            int left = item.getLeft();
            if(left >= parentRight) {
                item.setLeft(left + 2);
            }
            int right = item.getRight();
            if(right >= parentRight) {
                item.setRight(right + 2);
            }

            super.save(item);
        });
    }

    @Override
    public List<T> findAllChildren(ID parentId) {
        if (parentId == null) {
            return this.findAllByParentIdIsNull();
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(builder.equal(from.get("parentId"), parentId)).orderBy(builder.asc(from.get("left")));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }

    public List<T> findByLeftGreaterThanEqualOrRightGreaterThanEqual(int i) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(
            builder.or(builder.greaterThanOrEqualTo(from.get("left"), i), builder.greaterThanOrEqualTo(from.get("right"), i)));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }

    @Override
    public List<T> findAllByParentIdIsNull() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(builder.isNull(from.get("parentId"))).orderBy(builder.asc(from.get("left")));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }
}
