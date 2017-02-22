package de.codearcs.spring.data.jpa.repository;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class TreeRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
    extends JpaRepositoryFactoryBean<R, T, I> {

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new SimpleJpaExecutorFactory(entityManager);
    }

    /**
     * Simple jpa executor factory
     * 
     * @param <T>
     * @param <I>
     */
    private static class SimpleJpaExecutorFactory<T extends ITreeItem<I>, I extends Serializable> extends JpaRepositoryFactory {

        private EntityManager entityManager;

        /**
         * Simple jpa executor factory constructor
         * 
         * @param entityManager entity manager
         */
        public SimpleJpaExecutorFactory(EntityManager entityManager) {
            super(entityManager);
            this.entityManager = entityManager;
        }

        @Override
        protected Object getTargetRepository(RepositoryInformation metadata) {
            JpaEntityInformation entityInformation = getEntityInformation(metadata.getDomainType());
            return new TreeRepositoryImpl<T, I>(entityInformation, entityManager);
        }

        @Override
        protected Class getRepositoryBaseClass(RepositoryMetadata metadata) {
            return TreeRepositoryImpl.class;
        }
    }
}
