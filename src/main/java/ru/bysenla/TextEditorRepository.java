package ru.bysenla;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextEditorRepository extends JpaRepository<TextEntity, Long> {
    @Query("SELECT t from TextEntity t WHERE t.status = TaskStatus.NEW")
    public List<TextEntity> findNewTasks();
}
