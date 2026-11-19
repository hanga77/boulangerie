#!/usr/bin/env python3
"""Harmonise container-fluid + page-header sur les templates Thymeleaf."""
import re
import sys
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/resources/templates")


def convert(html: str) -> str:
    html = re.sub(r"<body[^>]*>", '<body class="d-flex flex-column min-vh-100">', html, count=1)
    html = re.sub(
        r'<div\s+class="container mt-4"[^>]*>',
        '<main class="container-fluid px-3 px-md-4 py-4 flex-grow-1">',
        html,
        count=1,
    )
    if "<main" not in html:
        html = re.sub(
            r'<div\s+class="container"[^>]*>',
            '<main class="container-fluid px-3 px-md-4 py-4 flex-grow-1">',
            html,
            count=1,
        )
    if "<main" in html and "</main>" not in html:
        html = re.sub(
            r'(<div th:replace="~\{fragments/layout :: footer\}"></div>)',
            r"</main>\n\1",
            html,
            count=1,
        )
    if "<main" in html and "</main>" not in html:
        html = html.replace("</body>", "</main>\n</body>", 1)
    if "page-header" not in html and "<main" in html:
        m = re.search(r"<main[^>]*>", html)
        hm = re.search(r"<h1[^>]*>(.*?)</h1>", html[m.end() : m.end() + 1000], re.S)
        if hm:
            inner = re.sub(r"\s+", " ", hm.group(1)).strip()
            ph = (
                "\n    <div class=\"page-header\">\n"
                "        <div>\n"
                f"            <h1 class=\"h4 fw-bold mb-0\">{inner}</h1>\n"
                "        </div>\n"
                "    </div>\n"
            )
            html = html[: m.end()] + ph + html[m.end() :]
    return html


def main() -> None:
    n = 0
    for path in ROOT.rglob("*.html"):
        if "fragments" in path.parts:
            continue
        text = path.read_text(encoding="utf-8")
        if "container mt-4" not in text and "page-header" in text:
            continue
        new = convert(text)
        if new != text:
            path.write_text(new, encoding="utf-8")
            n += 1
            print("updated", path)
    print(f"done: {n} files")


if __name__ == "__main__":
    main()
