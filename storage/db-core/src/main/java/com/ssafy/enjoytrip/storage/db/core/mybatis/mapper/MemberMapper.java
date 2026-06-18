package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {
    List<MemberRecord> findAllOrderByCreatedAtDesc();

    MemberRecord findByUserId(String userId);

    MemberRecord findByEmail(String email);

    MemberRecord findByUserIdAndEmail(@Param("userId") String userId, @Param("email") String email);

    int existsByUserId(String userId);

    int existsByEmail(String email);

    int insert(MemberRecord record);

    int update(MemberRecord record);

    int deleteByUserId(String userId);
}
