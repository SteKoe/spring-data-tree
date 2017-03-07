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
            int right = parent.getRight();
            entity.setLeft(right);
            entity.setRight(right + 1);

            List<T> nodes = this.findByLeftGreaterThanEqualOrRightGreaterThanEqual(right);
            nodes.forEach(node -> {
                int lft = node.getLeft();
                if (lft >= right) {
                    node.setLeft(lft + 2);
                }
                int rght = node.getRight();
                if (rght >= right) {
                    node.setRight(rght + 2);
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
    @Transactional
    public void delete(ID id) {
        T entity = findOne(id);
        int left = entity.getLeft();
        int right = entity.getRight();
        int delta = right - left + 1;

        if (delta > 2) {
            List<T> nodes = this.findByLeftGreaterEqualThanAndRightLowerEqualThan(left, right);
            nodes.forEach(super::delete);
        }

        List<T> nodes = this.findByRightGreaterThan(right);
        nodes.forEach(node -> {
            int lft = node.getLeft();
            if(lft > right) {
                node.setLeft(lft - delta);
            }

            int rght = node.getRight();
            if(rght > right) {
                node.setRight(rght - delta);
            }
            super.save(node);
        });

        super.delete(id);
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

    private List<T> findByLeftGreaterThanEqualOrRightGreaterThanEqual(int i) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(
            builder.or(builder.greaterThanOrEqualTo(from.get("left"), i), builder.greaterThanOrEqualTo(from.get("right"), i)));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }

    private List<T> findByLeftGreaterThanOrRightGreaterThan(int i) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(builder.or(builder.greaterThan(from.get("left"), i), builder.greaterThan(from.get("right"), i)));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }

    private List<T> findByRightGreaterThan(int right) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(builder.greaterThan(from.get("right"), right));
        List<T> resultList = entityManager.createQuery(query).getResultList();
        return resultList;
    }

    private List<T> findByLeftGreaterEqualThanAndRightLowerEqualThan(int left, int right) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.getDomainClass());
        Root<T> from = query.from(this.getDomainClass());
        query.select(from).where(builder.and(builder.greaterThanOrEqualTo(from.get("left"), left), builder.lessThanOrEqualTo(from.get("right"), right)));
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
