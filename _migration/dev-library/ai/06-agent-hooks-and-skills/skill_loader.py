"""Two-phase skill discovery: scan a skills/ directory reading only cheap YAML
frontmatter (name + description) at startup, then lazily load a skill's full body
only when a task actually matches it. See skills-pattern.md for why this shape
keeps context cost proportional to what's actually used, not to how many skills exist.

A skill is a directory containing a SKILL.md like:

    ---
    name: pdf-form-filler
    description: Fill out a PDF form given field values. Use when the user has a
      PDF form (tax form, application, contract) that needs specific fields filled.
    ---

    # PDF form filling

    1. Read the form's field names with `pdftk form.pdf dump_data_fields`
    ...

Install: pip install -r ../requirements.txt (needs pyyaml)
"""

from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import ConfigError, get_logger  # noqa: E402

logger = get_logger(__name__)

FRONTMATTER_DELIMITER = "---"


@dataclass
class SkillSummary:
    """Cheap to hold many of: just what's needed to decide whether to load the body."""
    name: str
    description: str
    path: Path


@dataclass
class Skill(SkillSummary):
    body: str


class SkillLoader:
    def __init__(self, skills_dir: Path | str):
        self.skills_dir = Path(skills_dir)
        if not self.skills_dir.is_dir():
            raise ConfigError(f"Skills directory not found: {self.skills_dir}")
        self._summaries: dict[str, SkillSummary] = {}
        self._scan()

    def _scan(self) -> None:
        """Phase 1: read only the frontmatter of every SKILL.md. This is what stays
        resident — the equivalent of a menu of names and descriptions, not the
        recipes themselves.
        """
        for skill_file in sorted(self.skills_dir.glob("*/SKILL.md")):
            try:
                name, description = self._parse_frontmatter(skill_file)
            except ValueError as exc:
                logger.warning("skipping malformed skill at %s: %s", skill_file, exc)
                continue
            self._summaries[name] = SkillSummary(name=name, description=description, path=skill_file)

        logger.info("discovered %d skill(s) in %s", len(self._summaries), self.skills_dir)

    @staticmethod
    def _parse_frontmatter(skill_file: Path) -> tuple[str, str]:
        import yaml

        text = skill_file.read_text(encoding="utf-8")
        lines = text.splitlines()
        if not lines or lines[0].strip() != FRONTMATTER_DELIMITER:
            raise ValueError("missing opening --- frontmatter delimiter")

        try:
            end_index = lines[1:].index(FRONTMATTER_DELIMITER) + 1
        except ValueError as exc:
            raise ValueError("missing closing --- frontmatter delimiter") from exc

        frontmatter = yaml.safe_load("\n".join(lines[1:end_index])) or {}
        name = frontmatter.get("name")
        description = frontmatter.get("description")
        if not name or not description:
            raise ValueError("frontmatter must define both 'name' and 'description'")
        return name, description

    def list_summaries(self) -> list[SkillSummary]:
        """What an agent should always see: names and descriptions, cheap enough
        to keep in every prompt regardless of how many skills are installed.
        """
        return list(self._summaries.values())

    def load(self, name: str) -> Skill:
        """Phase 2: load one skill's full body. Only called once a task is judged
        to match — this is the expensive step, deliberately deferred.
        """
        summary = self._summaries.get(name)
        if summary is None:
            raise ConfigError(f"No skill named '{name}'. Known skills: {list(self._summaries)}")

        text = summary.path.read_text(encoding="utf-8")
        lines = text.splitlines()
        end_index = lines[1:].index(FRONTMATTER_DELIMITER) + 1
        body = "\n".join(lines[end_index + 1:]).strip()

        logger.info("loaded skill '%s' (%d chars)", name, len(body))
        return Skill(name=summary.name, description=summary.description, path=summary.path, body=body)

    def find_best_match(self, task_description: str) -> SkillSummary | None:
        """Naive keyword-overlap matching for the demo below. A real agent does this
        matching step with the LLM itself (show it the list of name/description
        pairs, let it pick), not a hand-rolled heuristic — this stand-in exists so
        the module runs end-to-end without an API key.
        """
        task_words = set(task_description.lower().split())
        best_summary, best_score = None, 0
        for summary in self._summaries.values():
            overlap = len(task_words & set(summary.description.lower().split()))
            if overlap > best_score:
                best_summary, best_score = summary, overlap
        return best_summary


def _demo() -> None:
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        skills_root = Path(tmp)
        _write_demo_skill(
            skills_root / "pdf-form-filler",
            name="pdf-form-filler",
            description="Fill out a PDF form given field values. Use for tax forms, "
                        "applications, or contracts that need specific fields filled.",
            body="1. Read the form's field names with `pdftk form.pdf dump_data_fields`\n"
                 "2. Map provided values to field names\n3. Fill with `pdftk fill_form`",
        )
        _write_demo_skill(
            skills_root / "commit-message-writer",
            name="commit-message-writer",
            description="Write a git commit message summarizing staged changes. Use "
                        "whenever the user asks to commit code changes.",
            body="1. Run `git diff --staged`\n2. Summarize the *why*, not the *what*\n"
                 "3. Keep the subject line under 70 characters",
        )

        loader = SkillLoader(skills_root)
        print("Always-resident summaries:")
        for s in loader.list_summaries():
            print(f"  - {s.name}: {s.description}")

        task = "I need to commit these code changes with a good message"
        match = loader.find_best_match(task)
        print(f"\nTask: {task!r}")
        print(f"Matched skill: {match.name if match else None}")

        if match:
            full_skill = loader.load(match.name)
            print(f"\nLoaded body:\n{full_skill.body}")


def _write_demo_skill(directory: Path, *, name: str, description: str, body: str) -> None:
    directory.mkdir(parents=True, exist_ok=True)
    content = f"---\nname: {name}\ndescription: {description}\n---\n\n{body}\n"
    (directory / "SKILL.md").write_text(content, encoding="utf-8")


if __name__ == "__main__":
    _demo()
