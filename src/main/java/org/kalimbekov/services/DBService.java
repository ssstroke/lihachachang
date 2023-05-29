package org.kalimbekov.services;

import com.ptsmods.mysqlw.Database;
import com.ptsmods.mysqlw.Pair;
import com.ptsmods.mysqlw.query.QueryCondition;
import com.ptsmods.mysqlw.query.SelectResults;
import org.kalimbekov.entities.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBService {
    private final Database db;

    public DBService(String host, int port, String name, String username, String password) throws SQLException {
        this.db = Database.connect(host, port, name, username, password);
    }

    /**
     * Using this method makes sense only with values
     * 'id' or 'chat_id' for column parameter, since
     * they are unique.
     * That is why this method is private
     */
    private User getUserByColumn(String column, Object value) {
        SelectResults resultRows = db.selectBuilder("users")
                .select("id", "chat_id", "created_at", "points")
                .where(QueryCondition.equals(column, value))
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        SelectResults.SelectResultRow userRow = resultRows.get(0);
        return new User(
                userRow.getUUID("id"),
                userRow.getLong("chat_id"),
                userRow.getTimestamp("created_at"),
                userRow.getInt("points")
        );
    }

    public User getUserById(UUID id) {
        return this.getUserByColumn("id", id);
    }

    public User getUserByChatId(long chatId) {
        return this.getUserByColumn("chat_id", chatId);
    }

    public Pair<User, State> createUser(long chatId) {
        User user = new User(UUID.randomUUID(), chatId, new Date(), 0);
        State state = new State(UUID.randomUUID(), user);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        db.insert("users",
                new String[] { "id", "chat_id", "created_at", "points" },
                new Object[] {
                        user.getId(),
                        chatId,
                        dateFormat.format(user.getRegistrationDate()),
                        user.getPoints()
                }
        );

        db.insert("states",
                new String[] { "id", "user_id" },
                new Object[] { state.getId(), user.getId() }
        );

        return new Pair<>(user, state);
    }

    public void updateUser(User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("id", user.getId());
        updates.put("chat_id", user.getChatId());
        updates.put("created_at", user.getRegistrationDate());
        updates.put("points", user.getPoints());

        db.update("users",
                updates,
                QueryCondition.equals("id", user.getId())
        );
    }

    public void deleteUser(User user) {
        db.delete("users",
                QueryCondition.equals("id", user.getId()));
    }

    public Task getTaskById(UUID id) {
        SelectResults resultRows = db.selectBuilder("tasks")
                .select("id", "question", "answer_options", "answer", "points")
                .where(QueryCondition.equals("id", id))
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        SelectResults.SelectResultRow taskRow = resultRows.get(0);
        return new Task(
                taskRow.getUUID("id"),
                taskRow.getString("question"),
                taskRow.getString("answer_options"),
                taskRow.getInt("answer"),
                taskRow.getInt("points")
        );
    }

    public Task getRandomTask() {
        SelectResults resultRows = db.selectBuilder("tasks")
                .select("id", "question", "answer_options", "answer", "points")
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        Random random = new Random();

        SelectResults.SelectResultRow taskRow = resultRows.get(random.nextInt(resultRows.size()));
        return new Task(
                taskRow.getUUID("id"),
                taskRow.getString("question"),
                taskRow.getString("answer_options"),
                taskRow.getInt("answer"),
                taskRow.getInt("points")
        );
    }

    public Text getTextById(UUID id) {
        SelectResults resultRows = db.selectBuilder("texts")
                .select("id", "question", "points")
                .where(QueryCondition.equals("id", id))
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        SelectResults.SelectResultRow textRow = resultRows.get(0);
        return new Text(
                textRow.getUUID("id"),
                textRow.getString("question"),
                textRow.getInt("points")
        );
    }

    public Text getRandomText() {
        SelectResults resultRows = db.selectBuilder("texts")
                .select("id", "question", "points")
                .execute();

        Random random = new Random();
        SelectResults.SelectResultRow textRow = resultRows.get(random.nextInt(resultRows.size()));

        return new Text(
                textRow.getUUID("id"),
                textRow.getString("question"),
                textRow.getInt("points")
        );
    }

    public State getStateByUserId(UUID id) {
        SelectResults resultRows = db.selectBuilder("states")
                .select("id", "user_id", "task_id", "text_id", "word_id", "description")
                .where(QueryCondition.equals("user_id", id)) // This guarantees that only 1 row at max will be returned
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        SelectResults.SelectResultRow stateRow = resultRows.get(0);
        return new State(
                stateRow.getUUID("id"),
                this.getUserById(stateRow.getUUID("user_id")),
                this.getTaskById(stateRow.getUUID("task_id")),
                this.getTextById(stateRow.getUUID("text_id")),
                this.getWordById(stateRow.getUUID("word_id")),
                stateRow.getString("description")
        );
    }

    public void updateState(State state) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("user_id", state.getUser());
        updates.put("task_id", state.getTask());
        updates.put("text_id", state.getText());
        updates.put("word_id", state.getWord());
        updates.put("description", state.getDescription());

        db.update("states",
                updates,
                QueryCondition.equals("id", state.getId())
        );
    }

    public void createWord(User user, String word, String answer) {
        db.insert("user_words",
                new String[] { "id", "user_id", "word", "answer" },
                new Object[] { UUID.randomUUID(), user.getId(), word, answer }
        );
    }

    public Word getWordById(UUID id) {
        SelectResults resultRows = db.selectBuilder("user_words")
                .select("id", "user_id", "word", "answer")
                .where(QueryCondition.equals("id", id))
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        SelectResults.SelectResultRow wordRow = resultRows.get(0);
        return new Word(
                wordRow.getUUID("id"),
                this.getUserById(wordRow.getUUID("user_id")),
                wordRow.getString("word"),
                wordRow.getString("answer")
        );
    }

    public Word getRandomWord(User user) {
        SelectResults resultRows = db.selectBuilder("user_words")
                .select("id", "user_id", "word", "answer")
                .where(QueryCondition.equals("user_id", user.getId()))
                .execute();

        if (resultRows.size() == 0) {
            return null;
        }

        Random random = new Random();

        SelectResults.SelectResultRow wordRow = resultRows.get(random.nextInt(resultRows.size()));
        return new Word(
                wordRow.getUUID("id"),
                this.getUserById(wordRow.getUUID("user_id")),
                wordRow.getString("word"),
                wordRow.getString("answer")
        );
    }
}
