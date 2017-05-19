package com.btxtech.server.persistence.tracker;

import com.btxtech.shared.datatypes.I18nString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: beat
 * Date: 07.01.13
 * Time: 12:58
 */
@Entity(name = "I18N_BUNDLE")
public class I18nBundleEntity {
    public static final String DEFAULT = "DEFAULT";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "locale")
    @Column(name = "i18nString", length = 10000)
    @CollectionTable(name = "I18N_BUNDLE_STRING", joinColumns = @JoinColumn(name = "bundle"))
    private Map<String, String> localizedStrings;

    public Integer getId() {
        return id;
    }

    public String getString(Locale locale) {
        if (localizedStrings == null) {
            return null;
        }

        String language = I18nString.convert(locale.getLanguage());
        return toI18nString().getString(language);
    }

    public void putString(Locale locale, String string) {
        if (localizedStrings == null) {
            localizedStrings = new HashMap<>();
        }
        String language = I18nString.convert(locale.getLanguage());
        localizedStrings.put(language, string);
    }

    public boolean isEmpty() {
        if (localizedStrings == null) {
            return true;
        }
        for (String s : localizedStrings.values()) {
            if (StringUtils.isNotBlank(s)) {
                return false;
            }
        }
        return true;
    }

    public I18nString toI18nString() {
        Map<String, String> localizedStrings = new HashMap<>();
        if (this.localizedStrings != null)
            for (Map.Entry<String, String> entry : this.localizedStrings.entrySet()) {
                localizedStrings.put(I18nString.convert(entry.getKey()), entry.getValue());
            }
        return new I18nString(localizedStrings);
    }

    public void fromI18nString(I18nString i18nString) {
        if (localizedStrings == null) {
            localizedStrings = new HashMap<>();
        }
        localizedStrings.clear();
        if (i18nString.getLocalizedStrings() != null) {
            for (Map.Entry<String, String> entry : i18nString.getLocalizedStrings().entrySet()) {
                localizedStrings.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof I18nBundleEntity)) {
            return false;
        }

        I18nBundleEntity that = (I18nBundleEntity) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id : System.identityHashCode(this);
    }

    public static I18nBundleEntity fromI18nStringSafe(I18nString i18nString, I18nBundleEntity i18nBundleEntity) {
        if (i18nString == null) {
            return null;
        }
        if (i18nBundleEntity == null) {
            i18nBundleEntity = new I18nBundleEntity();
        }
        i18nBundleEntity.fromI18nString(i18nString);
        return i18nBundleEntity;
    }
}
