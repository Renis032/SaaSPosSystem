package com.renko.service.impl;

import com.renko.service.DevCleanupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DevCleanupServiceImpl implements DevCleanupService
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<String> clearAllTables()
    {
        entityManager.flush();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        @SuppressWarnings("unchecked")
        List<Object> tables = entityManager.createNativeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'"
        ).getResultList();

        List<String> truncated = new ArrayList<>();
        List<String> sequenceTables = new ArrayList<>();

        for (Object row : tables)
        {
            String table = row == null ? null : String.valueOf(row);
            if (table == null || table.isBlank() || "null".equalsIgnoreCase(table))
            {
                continue;
            }

            entityManager.createNativeQuery("TRUNCATE TABLE `" + table + "`").executeUpdate();
            truncated.add(table);

            if (isSequenceTable(table))
            {
                sequenceTables.add(table);
            }
        }

        // Hibernate TABLE generators need a next_val row; TRUNCATE leaves *_seq empty.
        for (String sequenceTable : sequenceTables)
        {
            entityManager.createNativeQuery(
                    "INSERT INTO `" + sequenceTable + "` (next_val) VALUES (1)"
            ).executeUpdate();
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        entityManager.clear();
        return truncated;
    }

    private static boolean isSequenceTable(String table)
    {
        String name = table.toLowerCase(Locale.ROOT);
        return name.endsWith("_seq") || "hibernate_sequence".equals(name);
    }
}
