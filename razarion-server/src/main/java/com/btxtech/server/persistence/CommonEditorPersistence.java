package com.btxtech.server.persistence;

import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.I18nStringEditor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 10.09.2017.
 */
public class CommonEditorPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @SecurityCheck
    public List<I18nStringEditor> loadAllI18NEntries() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<I18nBundleEntity> query = criteriaBuilder.createQuery(I18nBundleEntity.class);
        Root<I18nBundleEntity> root = query.from(I18nBundleEntity.class);
        CriteriaQuery<I18nBundleEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getResultList().stream().map(i18nBundleEntity -> new I18nStringEditor().setId(i18nBundleEntity.getId()).setEnString(i18nBundleEntity.getStringOrNull(Locale.ENGLISH)).setDeString(i18nBundleEntity.getStringOrNull(Locale.GERMAN))).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void saveI8NEntries(List<I18nStringEditor> i18nStringEditors) {
        for (I18nStringEditor i18nStringEditor : i18nStringEditors) {
            I18nBundleEntity i18nBundleEntity = entityManager.find(I18nBundleEntity.class, i18nStringEditor.getId());
            i18nBundleEntity.putString(Locale.ENGLISH, i18nStringEditor.getEnString());
            i18nBundleEntity.putString(Locale.GERMAN, i18nStringEditor.getDeString());
            entityManager.merge(i18nBundleEntity);
        }
    }
}