import pandas as pd
import sqlalchemy
import snowflake.connector
from snowflake.connector.pandas_tools import write_pandas

print("Starting Snowflake Sync...")

# -------------------
# MYSQL CONNECTION
# -------------------

mysql_engine = sqlalchemy.create_engine(
    "mysql+pymysql://root@localhost:3306/banking_system_db"
)


tables = ["users", "accounts", "transactions"]

# -------------------
# SNOWFLAKE CONNECTION
# -------------------

sf_conn = snowflake.connector.connect(
    user="PARTHAADI",
    password="AdityaKumar@123",
    account="db54057.me-central-1.aws",
    warehouse="COMPUTE_WH",
    database="RETAIL_ANALYTICS",   # <-- YOU ALREADY CREATED THIS
    schema="RAW"
)


cursor = sf_conn.cursor()

# -------------------
# SYNC LOOP
# -------------------

for table in tables:

    print(f"Syncing {table}")

    df = pd.read_sql(f"SELECT * FROM {table}", mysql_engine)

    success, nchunks, nrows, _ = write_pandas(
    sf_conn,
    df,
    table.upper(),
    auto_create_table=True,
    overwrite=True
)

    print(f"Inserted rows: {nrows}")

print("SYNC COMPLETE")
