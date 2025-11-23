package com.dib.repository;

public class Queries {
    public static final String CREATE_TABLE_DAYS_CLAIMED = """
        CREATE TABLE IF NOT EXISTS days_claimed (
            player_uuid text NOT NULL,
            day int NOT NULL,
            remaining_amount int NOT NULL,
            PRIMARY KEY (player_uuid, day)
        )
        """;

    public static final String CREATE_TABLE_DAY_REWARD = """
            CREATE TABLE IF NOT EXISTS day_reward (
                day int PRIMARY KEY NOT NULL,
                item text NOT NULL,
                amount int NOT NULL
            )
            """;

    public static final String GET_MISSING_REWARDS = """
        SELECT
            DR.day as "day_item",
            DR.item,
            DR.amount,
            DC.remaining_amount
        FROM
            day_reward DR
        LEFT JOIN
            days_claimed DC
            ON DR.day = DC.day AND DC.player_uuid = ?
        WHERE
            (DC.day IS NULL OR DC.remaining_amount > 0)
            AND DR.day <= ?;
        """;

    public static final String ADD_DAY_CLAIMED = """
            INSERT OR REPLACE INTO days_claimed
            VALUES (?,?,?);
            """;

    public static final String FILL_REWARDS_TABLE = """
            INSERT OR IGNORE INTO day_reward
            VALUES (?,?,?);
            """;

}
