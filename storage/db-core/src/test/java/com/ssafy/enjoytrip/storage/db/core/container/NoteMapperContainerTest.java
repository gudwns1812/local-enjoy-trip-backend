package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
class NoteMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private NoteMapper noteMapper;

    @DisplayName("NoteMapper는 migration schema에서 insert/nearby/map-pin/update/delete SQL을 실행한다")
    @Test
    void noteMapperWorksAgainstMigratedPostgisSchema() {
        Long authorMemberId = seedMember("note-author", uniqueId("note-author") + "@example.com");
        NoteRecord saved = noteMapper.insert(new NoteRecord(
                authorMemberId,
                "서비스커넥션 노트",
                "실제 PostgreSQL에서 저장되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                "notes/service-connection.png",
                "https://example.com/notes/service-connection.png",
                "image/png"
        ));

        List<NoteRecord> nearby = noteMapper.findNearbyAccessible(
                126.9781,
                37.5666,
                100,
                10,
                null
        );
        List<NoteMapPinRecord> pins = noteMapper.findMapPins(
                126.9781,
                37.5666,
                100,
                10,
                null,
                "TIP",
                false
        );
        NoteRecord updated = noteMapper.updateOwned(new NoteRecord(
                saved.getId(),
                authorMemberId,
                "서비스커넥션 노트 수정",
                "실제 PostgreSQL에서 수정되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5667000"),
                new BigDecimal("126.9782000"),
                "서울 중구",
                "notes/service-connection-updated.png",
                "https://example.com/notes/service-connection-updated.png",
                "image/png"
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(nearby).extracting(NoteRecord::getId).contains(saved.getId());
        assertThat(pins).extracting(NoteMapPinRecord::id).contains(saved.getId());
        assertThat(updated.getTitle()).isEqualTo("서비스커넥션 노트 수정");
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(noteMapper.softDeleteOwned(saved.getId(), authorMemberId)).isEqualTo(1);
        assertThat(noteMapper.findById(saved.getId()).getDeletedAt()).isNotNull();
    }
    @DisplayName("NoteMapper는 저장 목록에서 접근 가능한 active 쪽지만 반환한다")
    @Test
    void noteMapperFindsOnlySavedAccessibleActiveNotes() {
        Long viewerMemberId = seedMember("note-save-viewer", uniqueId("note-viewer") + "@example.com");
        Long otherMemberId = seedMember("note-save-other", uniqueId("note-other") + "@example.com");
        NoteRecord selfPrivate = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "내 비공개 저장 쪽지",
                "내 쪽지는 저장 목록에 보인다",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord otherPrivate = noteMapper.insert(new NoteRecord(
                otherMemberId,
                "타인 비공개 저장 쪽지",
                "저장 row가 있어도 보이면 안 된다",
                "TIP",
                "PRIVATE",
                new BigDecimal("37.5666000"),
                new BigDecimal("126.9781000"),
                "서울 중구",
                null,
                null,
                null
        ));
        NoteRecord deleted = noteMapper.insert(new NoteRecord(
                viewerMemberId,
                "삭제된 저장 쪽지",
                "삭제되면 저장 목록에서 제외된다",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5667000"),
                new BigDecimal("126.9782000"),
                "서울 중구",
                null,
                null,
                null
        ));

        assertThat(noteMapper.existsAccessibleActive(selfPrivate.getId(), viewerMemberId)).isEqualTo(1);
        assertThat(noteMapper.existsAccessibleActive(otherPrivate.getId(), viewerMemberId)).isZero();
        noteMapper.insertSave(selfPrivate.getId(), viewerMemberId);
        noteMapper.insertSave(otherPrivate.getId(), viewerMemberId);
        noteMapper.insertSave(deleted.getId(), viewerMemberId);
        noteMapper.softDeleteOwned(deleted.getId(), viewerMemberId);

        List<NoteRecord> saved = noteMapper.findSavedAccessible(viewerMemberId, 10);

        assertThat(saved).extracting(NoteRecord::getId).containsExactly(selfPrivate.getId());
    }

}
