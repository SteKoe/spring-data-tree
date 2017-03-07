package de.codearcs.spring.data.jpa.repository;

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

@NoRepositoryBean
public class TreeRepositoryImpl<T extends ITreeItem<ID>, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements TreeRepository<T, ID> {

    private enum Operator {
            PLUS, MINUS
    }

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
        boolean isNew = isNew(entity);

        if (isNew && parentId == null) {
            int maxRight = this.getMaxRight();
            entity.setLeft(maxRight + 1);
            entity.setRight(maxRight + 2);
        } else if (isNew) {
            T parent = (T) super.findOne(parentId);
            int edge = parent.getRight();
            entity.setLeft(edge);
            entity.setRight(edge + 1);

            List<T> nodes = this.findByLeftGreaterThanEqualOrRightGreaterThanEqual(edge);
            nodes.forEach(node -> {
                int left = node.getLeft();
                if (left >= edge) {
                    node.setLeft(left + 2);
                }
                int right = node.getRight();
                if (right >= edge) {
                    node.setRight(right + 2);
                }

                super.save(node);
            });
        }

        return (S) super.save(entity);
    }

    /**
     * Retrieves the max value for "right" field.
     *
     * @return the max value for right field
     */
    private int getMaxRight() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Number> query = builder.createQuery(Number.class);

        Root<T> root = query.from(this.getDomainClass());
        query.select(builder.max(root.get("right")));
        Number singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult == null ? 0 : singleResult.intValue();
    }

    /**
     * Checks if the given entity is already persisted in database which is then not assumed as "new".
     *
     * @param entity the entity to check
     * @return a flag whether the entity is new or not
     */
    private <S extends T> boolean isNew(S entity) {
        return entity.getId() == null || this.findOne(entity.getId()) != null;
    }

    @Override
    public void delete(ID id) {
        T entity = findOne(id);
        int parentLeft = entity.getRight();

        List<T> items = this.findByLeftGreaterThanEqualOrRightGreaterThanEqual(parentLeft);
        items.forEach(item -> {
            int left = item.getLeft();
            if (left >= parentLeft) {
                item.setLeft(left - 2);
            }
            int right = item.getRight();
            if (right >= parentLeft) {
                item.setRight(right - 2);
            }

            super.save(item);
        });

        super.delete(entity);
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
