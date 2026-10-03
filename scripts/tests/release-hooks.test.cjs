const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const { test } = require('node:test');

const root = path.resolve(__dirname, '../..');
const config = JSON.parse(fs.readFileSync(path.join(root, '.releaserc'), 'utf8'));
const githubOptions = config.plugins.find(
    (plugin) => Array.isArray(plugin) && plugin[0] === '@semantic-release/github',
)[1];
const pluginRoot = path.dirname(require.resolve('@semantic-release/github'));

async function loadHook(name) {
    return (await import(pathToFileURL(path.join(pluginRoot, 'lib', `${name}.js`)).href)).default;
}

function fixture() {
    const requests = [];
    class Octokit {
        async request(route) {
            requests.push(route);
            assert.equal(route, 'GET /repos/{owner}/{repo}');
            return { data: { full_name: 'test-owner/test-repository' } };
        }

        async graphql() {
            throw new Error('Issue access is unavailable');
        }
    }
    const context = {
        options: { repositoryUrl: 'https://github.com/test-owner/test-repository.git' },
        env: {},
        commits: [{ hash: '0123456789abcdef', message: 'feat: add a patch' }],
        nextRelease: { version: '1.0.0' },
        releases: [],
        branch: { name: 'main' },
        errors: [],
        logger: { log() {}, warn() {}, error() {} },
    };
    return { requests, context, Octokit };
}

test('release success completes without issue permissions', async () => {
    const success = await loadHook('success');
    const { requests, context, Octokit } = fixture();
    await success(githubOptions, context, { Octokit });
    assert.deepEqual(requests, ['GET /repos/{owner}/{repo}']);
});

test('release failures stay in workflow logs without issue creation', async () => {
    const fail = await loadHook('fail');
    const { requests, context, Octokit } = fixture();
    await fail(githubOptions, context, { Octokit });
    assert.deepEqual(requests, []);
});

test('previous comment setting reproduces the issue-access failure', async () => {
    const success = await loadHook('success');
    const { context, Octokit } = fixture();
    const { successCommentCondition, failCommentCondition, ...previousOptions } = githubOptions;
    await assert.rejects(
        success({ ...previousOptions, successComment: false }, context, { Octokit }),
        /Issue access is unavailable/,
    );
});
