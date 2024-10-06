package com.btxtech.server.persistence;

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
import java.util.Map;

/**
 * User: beat
 * Date: 07.01.13
 * Time: 12:58
 */
@Entity(name = "I18N_BUNDLE")
public class I18nBundleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(length = 180, name = "locale") // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @Column(name = "i18nString", length = 10000)
    @CollectionTable(name = "I18N_BUNDLE_STRING", joinColumns = @JoinColumn(name = "bundle"))
    private Map<String, String> localizedStrings;

    public Integer getId() {
        return id;
    }

    public String getString() {
        if (localizedStrings == null) {
            return null;
        }
        return toI18nString().getString();
    }

    public String getStringOrNull() {
        return getString();
    }

    public void putString(String string) {
        if (localizedStrings == null) {
            localizedStrings = new HashMap<>();
        }
        localizedStrings.put("EN", string);
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
        if (this.localizedStrings != null) {
            String en = this.localizedStrings.get("EN");
            if (en != null) {
                return new I18nString().string(en);
            }
            return new I18nString().string(localizedStrings.get("DE"));
        }
        return new I18nString().string("???");
    }

    public void fromI18nString(I18nString i18nString) {
        if (localizedStrings == null) {
            localizedStrings = new HashMap<>();
        }
        localizedStrings.clear();
        localizedStrings.put("EN", i18nString.getString());
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
