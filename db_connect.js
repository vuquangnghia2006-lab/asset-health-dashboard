const { Client } = require('pg');

const client = new Client({
  host: '103.163.118.212',
  port: 31147,
  database: 'tester',
  user: 'appuser',
  password: '25ce064873467c132f44646b104515eb',

});

async function main() {
  try {
    await client.connect();
    console.log('Connected to PostgreSQL successfully!');

    const tables = await client.query(
      "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name"
    );
    console.log('\nTables:');
    tables.rows.forEach(r => console.log(' -', r.table_name));

    for (const row of tables.rows) {
      const cols = await client.query(
        `SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name=$1 AND table_schema='public' ORDER BY ordinal_position`,
        [row.table_name]
      );
      console.log(`\n--- ${row.table_name} ---`);
      cols.rows.forEach(c => console.log(`  ${c.column_name} (${c.data_type}) ${c.is_nullable === 'YES' ? 'NULL' : 'NOT NULL'}`));
    }
  } catch (err) {
    console.error('Connection error:', err.message);
  } finally {
    await client.end();
  }
}

main();
