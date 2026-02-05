# Algorena Skills

This directory contains specialized skills for Claude Code to help with common development tasks in the Algorena project.

## Available Skills

### `/spring-boot`
Handles backend development following the domain-driven architecture. Use for:
- Creating new REST endpoints
- Adding domains, entities, services, repositories
- Writing tests
- Adding new games to the platform

### `/react-frontend`
Handles frontend development with terminal-inspired design system. Use for:
- Creating new pages and components
- Adding TanStack Query hooks for data fetching
- Building forms and dialogs
- Implementing filters and search functionality
- Following the dark terminal aesthetic (zinc palette, emerald accents)

## How Skills Work

**Automatic invocation:** Claude will automatically use the appropriate skill when your request matches what the skill does.

**Manual invocation:** You can explicitly call a skill using `/skill-name <task>`

Example:
```
/spring-boot add tournaments domain with CRUD endpoints
```

## Adding New Skills

To add a new skill:

1. Create a new `.md` file in this directory: `.claude/skills/your-skill-name.md`

2. Structure your skill file:
   ```markdown
   Brief description of what this skill does.

   **Usage:** `/your-skill-name <task description>`

   **Examples:**
   - Example 1
   - Example 2

   **What this skill does:**
   [Detailed instructions and patterns]
   ```

3. The skill will be automatically available in the next Claude Code session

## Future Skill Ideas

Consider creating skills for:
- **database** - Flyway migrations, schema changes, indexing strategies
- **testing** - Test writing guidelines, mocking patterns, test data creation
- **deployment** - Docker, environment setup, production readiness
- **ci-cd** - GitHub Actions, automated testing, deployment pipelines
