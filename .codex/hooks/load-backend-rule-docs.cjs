#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const REQUIRED_DOCS = [
  "backend/CONSTITUTION.md",
  "backend/RULES.md",
];

function readStdin() {
  try {
    return fs.readFileSync(0, "utf8");
  } catch {
    return "";
  }
}

function parsePayload(text) {
  if (!text.trim()) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch {
    return {};
  }
}

function repoRoot(payload) {
  if (typeof payload.cwd === "string" && payload.cwd.trim()) {
    return findGitRoot(payload.cwd) || path.resolve(payload.cwd);
  }

  return path.resolve(__dirname, "..", "..");
}

function findGitRoot(startDir) {
  let current = path.resolve(startDir);

  while (true) {
    if (fs.existsSync(path.join(current, ".git"))) {
      return current;
    }

    const parent = path.dirname(current);
    if (parent === current) {
      return "";
    }
    current = parent;
  }
}

function renderDoc(root, relativePath) {
  const fullPath = path.join(root, relativePath);

  try {
    const content = fs.readFileSync(fullPath, "utf8").trim();
    return [
      `## ${relativePath}`,
      "",
      content,
    ].join("\n");
  } catch (error) {
    return [
      `## ${relativePath}`,
      "",
      `Could not read ${relativePath}: ${error.message}`,
    ].join("\n");
  }
}

function buildAdditionalContext(root, payload) {
  const source = typeof payload.source === "string" ? payload.source : "unknown";
  const docs = REQUIRED_DOCS.map((doc) => renderDoc(root, doc)).join("\n\n---\n\n");

  return [
    "[local-enjoy-trip-backend session-start rule documents]",
    "",
    "The repository requires these backend rule documents to be read before backend work in a new Codex session.",
    "Treat backend/CONSTITUTION.md as the highest-priority backend rule source, and use backend/RULES.md as the concrete operating rulebook.",
    "For module-specific backend edits, still read the nearest applicable backend/**/AGENTS.md before changing files.",
    `SessionStart source: ${source}`,
    "",
    docs,
  ].join("\n");
}

function main() {
  const payload = parsePayload(readStdin());
  const eventName = payload.hook_event_name || payload.hookEventName || payload.event;

  if (eventName && eventName !== "SessionStart") {
    process.stdout.write("{}");
    return;
  }

  process.stdout.write(JSON.stringify({
    hookSpecificOutput: {
      hookEventName: "SessionStart",
      additionalContext: buildAdditionalContext(repoRoot(payload), payload),
    },
  }));
}

main();
