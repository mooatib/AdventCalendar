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
                amount int NOT NULL,
                custom_name text,
                enchantment_type text,
                enchantment_level int
            )
            """;

    public static final String GET_MISSING_REWARDS = """
            SELECT
                DR.day as "day_item",
                DR.item,
                DR.amount,
                DR.custom_name,
                DR.enchantment_type,
                DR.enchantment_level,
                DC.remaining_amount
            FROM
                day_reward DR
            LEFT JOIN
                days_claimed DC
                ON DR.day = DC.day AND DC.player_uuid = ?
            WHERE
                (DC.day IS NULL OR DC.remaining_amount > 0)
                AND DR.day <= ?
            ORDER BY DR.day;
            """;

    public static final String ADD_DAY_CLAIMED = """
                INSERT OR REPLACE INTO days_claimed
                VALUES (?,?,?);
            """;

    public static final String FILL_REWARDS_TABLE = """
                INSERT OR REPLACE INTO day_reward (day, item, amount, custom_name, enchantment_type, enchantment_level)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

    public static final String RESET_PLAYER_REWARDS = """
                DELETE FROM days_claimed
                WHERE player_uuid = ?;
            """;

}
