package com.common.logging;

/**
 * Centralized log message constants related to database initialization.
 */
public final class DatabaseLogMessages {
    private DatabaseLogMessages() {}

    public static final String MSG_DB_PREFIX = "Database '";
    public static final String MSG_DB_CREATED_SUFFIX = "' created successfully.";
    public static final String MSG_DB_EXISTS_SUFFIX = "' already exists.";
    public static final String MSG_ERROR_CREATING_DB = "Error creating database: ";
}
