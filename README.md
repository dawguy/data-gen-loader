# Data Gen

Want to create a system which makes it easier to bulk create and delete dummy test data for performance testing.

I'm okay with the commands all being run from command line or through REPL as this project is only for me.

## Outline

Want to create domain directory containing the models used. These should all contain good enough defaults. PK field should be included listing what the primary key is, as the primary key will be saved to sqlite for deletion purposes. Should also list the table name to save to.

Note: No plans to create table with this definition automatically.

From here we should be able to specify value range function or uuid generator for specific fields. The output of these functions will be executed in place of the default value.

## Data Loading
Should parallelize saving to Cassandra tables.
Every saved row should have the pk name, table name, pk value saved into sqlite.

## Data Deleting
Should read from sqlite and delete all saved rows for easy cleanup.

# Running Project
Should primarily be ran via REPL commands.

Optional usage of `clj -A:dev` (And later additional commands where we can specify test case.)
