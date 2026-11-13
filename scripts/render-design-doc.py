#!/usr/bin/env python3
"""Render docs/SYSTEM_DESIGN.md to a styled HTML file with Mermaid diagrams."""

from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MD_PATH = ROOT / "docs" / "SYSTEM_DESIGN.md"
OUT_HTML = ROOT / "docs" / "SYSTEM_DESIGN.html"
CSS_HREF = "assets/design-doc.css"


def extract_toc(md: str) -> list[tuple[int, str, str]]:
    items: list[tuple[int, str, str]] = []
    for line in md.splitlines():
        m = re.match(r"^(#{1,3})\s+(.+)$", line)
        if not m:
            continue
        level = len(m.group(1))
        title = m.group(2).strip()
        anchor = re.sub(r"[^\w\s-]", "", title.lower())
        anchor = re.sub(r"\s+", "-", anchor)
        anchor = re.sub(r"-+", "-", anchor)
        items.append((level, title, anchor))
    return items


def slugify_heading(text: str) -> str:
    anchor = re.sub(r"[^\w\s-]", "", text.lower())
    anchor = re.sub(r"\s+", "-", anchor)
    return re.sub(r"-+", "-", anchor)


def md_to_html_body(md: str) -> str:
    try:
        import markdown

        return markdown.markdown(
            md,
            extensions=["tables", "fenced_code", "sane_lists", "nl2br"],
        )
    except ImportError:
        return fallback_md_to_html(md)


def fallback_md_to_html(md: str) -> str:
    """Minimal fallback when the markdown package is not installed."""
    lines = md.splitlines()
    out: list[str] = []
    in_pre = False
    for line in lines:
        if line.startswith("```"):
            if not in_pre:
                lang = line[3:].strip()
                if lang == "mermaid":
                    out.append('<div class="mermaid-wrap"><pre class="mermaid">')
                    in_pre = "mermaid"
                else:
                    out.append(f"<pre><code>")
                    in_pre = "code"
            else:
                if in_pre == "mermaid":
                    out.append("</pre></div>")
                else:
                    out.append("</code></pre>")
                in_pre = False
            continue
        if in_pre:
            out.append(line)
            continue
        if m := re.match(r"^(#{1,3})\s+(.+)$", line):
            level = len(m.group(1))
            title = m.group(2)
            sid = slugify_heading(title)
            out.append(f'<h{level} id="{sid}">{title}</h{level}>')
        elif line.strip() == "---":
            out.append("<hr>")
        elif line.startswith("|"):
            if not out or "<table>" not in out[-1]:
                out.append("<table>")
            cells = [c.strip() for c in line.strip("|").split("|")]
            if all(set(c) <= {"-", ":", " "} for c in cells):
                continue
            tag = "th" if out[-1] == "<table>" or (out and "<tr>" not in out[-1]) else "td"
            if out[-1] == "<table>":
                out.append("<tr>" + "".join(f"<th>{c}</th>" for c in cells) + "</tr>")
            else:
                out.append("<tr>" + "".join(f"<td>{c}</td>" for c in cells) + "</tr>")
        elif line.strip().startswith("- "):
            if not out or out[-1] != "<ul>":
                out.append("<ul>")
            text = line.strip()[2:]
            text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
            text = re.sub(r"`(.+?)`", r"<code>\1</code>", text)
            out.append(f"<li>{text}</li>")
        elif line.strip() == "":
            if out and out[-1] == "</ul>":
                pass
            continue
        else:
            text = line
            text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
            text = re.sub(r"`(.+?)`", r"<code>\1</code>", text)
            out.append(f"<p>{text}</p>")
    if out and out[-1] == "<ul>":
        out.append("</ul>")
    return "\n".join(out)


def promote_mermaid_fences(html: str) -> str:
    pattern = re.compile(
        r'<pre><code class="language-mermaid">(.*?)</code></pre>',
        re.DOTALL,
    )
    return pattern.sub(
        r'<div class="mermaid-wrap"><pre class="mermaid">\1</pre></div>',
        html,
    )

    # Also handle unclassified mermaid blocks from fallback


def add_heading_ids(html: str) -> str:
    def repl(match: re.Match[str]) -> str:
        tag = match.group(1)
        inner = match.group(2)
        sid = slugify_heading(re.sub(r"<[^>]+>", "", inner))
        if 'id="' in match.group(0):
            return match.group(0)
        return f'<h{tag} id="{sid}">{inner}</h{tag}>'

    return re.sub(r"<h([123])>([^<]+)</h\1>", repl, html)


def build_html(md: str) -> str:
    body_parts: list[str] = []
    pattern = re.compile(r"```mermaid\n(.*?)```", re.DOTALL)
    last = 0
    for match in pattern.finditer(md):
        before = md[last : match.start()]
        if before.strip():
            html = md_to_html_body(before)
            html = promote_mermaid_fences(html)
            html = add_heading_ids(html)
            body_parts.append(html)
        diagram = match.group(1).strip()
        body_parts.append(
            f'<div class="mermaid-wrap"><pre class="mermaid">\n{diagram}\n</pre></div>'
        )
        last = match.end()
    tail = md[last:]
    if tail.strip():
        html = md_to_html_body(tail)
        html = promote_mermaid_fences(html)
        html = add_heading_ids(html)
        body_parts.append(html)

    body = "\n".join(body_parts)

    toc_items = extract_toc(MD_PATH.read_text(encoding="utf-8"))
    toc_links = []
    for level, title, anchor in toc_items:
        if level > 3:
            continue
        indent = "padding-left: " + str((level - 1) * 12) + "px"
        toc_links.append(f'<a href="#{anchor}" style="{indent}">{title}</a>')

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>URL Shortener — System Design</title>
  <link rel="stylesheet" href="{CSS_HREF}">
  <script type="module">
    import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
    mermaid.initialize({{
      startOnLoad: true,
      theme: 'dark',
      securityLevel: 'loose',
      flowchart: {{ useMaxWidth: true, htmlLabels: true }},
    }});
  </script>
</head>
<body>
  <div class="layout">
    <nav class="toc">
      <h2>Contents</h2>
      {"".join(toc_links)}
    </nav>
    <main class="doc">
      <header class="hero">
        <h1>URL Shortener — System Design</h1>
        <p class="subtitle">Architecture, tradeoffs, and diagrams for the Spring Boot implementation.</p>
        <p class="meta">
          <span class="badge">Spring Boot 3</span>
          <span class="badge">Java 17</span>
          Generated {date.today().isoformat()} · Source: <code>docs/SYSTEM_DESIGN.md</code>
        </p>
      </header>
      <article class="doc-content">
        {body}
      </article>
    </main>
  </div>
  <div class="toolbar no-print">
    <button type="button" class="secondary" onclick="window.print()">Save as PDF</button>
    <button type="button" onclick="location.reload()">Refresh diagrams</button>
  </div>
</body>
</html>
"""


def main() -> int:
    if not MD_PATH.is_file():
        print(f"Missing {MD_PATH}", file=sys.stderr)
        return 1
    md = MD_PATH.read_text(encoding="utf-8")
    html = build_html(md)
    OUT_HTML.write_text(html, encoding="utf-8")
    print(f"Wrote {OUT_HTML}")
    print("Open in a browser; use 'Save as PDF' for a PDF copy.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
