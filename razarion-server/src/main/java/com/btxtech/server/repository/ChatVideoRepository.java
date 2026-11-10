package com.btxtech.server.repository;

import com.btxtech.server.model.ChatVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatVideoRepository extends JpaRepository<ChatVideoEntity, Integer> {
    /** Newest first, ids only: what is beyond the kept ones is removed without loading a clip. */
    @Query("select v.id from ChatVideoEntity v order by v.id desc")
    List<Integer> findIdsNewestFirst();
}
