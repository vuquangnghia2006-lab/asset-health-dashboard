const { Client } = require('pg');
const c = new Client({ host: '103.163.118.212', port: 31147, database: 'tester', user: 'appuser', password: '25ce064873467c132f44646b104515eb' });
c.connect().then(async () => {
  const r1 = await c.query("SELECT status, COUNT(*) FROM transformer GROUP BY status");
  console.log('Transformer statuses:', r1.rows);
  const r2 = await c.query("SELECT status, COUNT(*) FROM asset_info GROUP BY status");
  console.log('AssetInfo statuses:', r2.rows);
  const r3 = await c.query("SELECT COUNT(*) FROM transformer");
  console.log('Total transformers:', r3.rows);
  const r4 = await c.query("SELECT t.base_voltage, COUNT(*) FROM transformer t GROUP BY t.base_voltage ORDER BY t.base_voltage DESC LIMIT 10");
  console.log('Voltage levels:', r4.rows);
  await c.end();
}).catch(e => { console.error(e.message); c.end(); });
