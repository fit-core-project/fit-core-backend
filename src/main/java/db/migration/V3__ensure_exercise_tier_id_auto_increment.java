package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V3__ensure_exercise_tier_id_auto_increment extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        if (!hasPrimaryKey(connection)) {
            execute(connection, "ALTER TABLE exercise_tier MODIFY id BIGINT NOT NULL");
            execute(connection, "ALTER TABLE exercise_tier ADD PRIMARY KEY (id)");
        }

        if (!isAutoIncrement(connection)) {
            execute(connection, "ALTER TABLE exercise_tier MODIFY id BIGINT NOT NULL AUTO_INCREMENT");
        }
    }

    private boolean hasPrimaryKey(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet keys = metaData.getPrimaryKeys(connection.getCatalog(), null, "exercise_tier")) {
            if (keys.next()) {
                return true;
            }
        }
        try (ResultSet keys = metaData.getPrimaryKeys(connection.getCatalog(), null, "EXERCISE_TIER")) {
            return keys.next();
        }
    }

    private boolean isAutoIncrement(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "exercise_tier", "id")) {
            if (columns.next()) {
                return isYes(columns.getString("IS_AUTOINCREMENT"));
            }
        }
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "EXERCISE_TIER", "ID")) {
            return columns.next() && isYes(columns.getString("IS_AUTOINCREMENT"));
        }
    }

    private boolean isYes(String value) {
        return "YES".equalsIgnoreCase(value);
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            if (message.contains("already exists") || message.contains("multiple primary key")) {
                return;
            }
            throw ex;
        }
    }
}
