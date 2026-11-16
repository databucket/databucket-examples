// Creates a project with the DEMO dataset in a fresh Databucket, so the examples have data to work on.
// Does nothing when the project already exists. Needs Node.js 20+ (no dependencies).
//
//   node scripts/setup-demo-project.mjs
//
// Settings (environment variables): DATABUCKET_URL (http://databucket.localhost), DATABUCKET_PROJECT (Acme Shop),
// SUPER_USER / SUPER_PASSWORD (super / super - the default super user of a new Databucket).
// The import also creates the demo users (demo-admin, demo-member, demo-robot, password "test") if they are missing.

const url = process.env.DATABUCKET_URL || 'http://databucket.localhost';
const projectName = process.env.DATABUCKET_PROJECT || 'Acme Shop';
const superUser = process.env.SUPER_USER || 'super';
const superPassword = process.env.SUPER_PASSWORD || 'super';

async function call(method, path, token, body) {
  const response = await fetch(url + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  if (!response.ok)
    throw new Error(`${method} ${path}: ${response.status} ${text}`);
  return text ? JSON.parse(text) : null;
}

const { token } = await call('POST', '/api/public/signin', null, { username: superUser, password: superPassword });

const projects = await call('GET', '/api/manage/projects', token);
if (projects.some((project) => project.name === projectName)) {
  console.log(`Project "${projectName}" already exists`);
  process.exit(0);
}

const project = await call('POST', '/api/manage/projects', token, {
  name: projectName,
  description: 'DEMO dataset for the Databucket examples',
  enabled: true,
});
await call('POST', `/api/manage/projects/${project.id}/datasets/demo`, token);
console.log(`Project "${projectName}" (id ${project.id}) created with the DEMO dataset`);
