// Sous-ensemble Markdown maison : titres, gras, italique, listes, liens.
// Volontairement simple — pas de tables, pas de blocs de code, pas de citations.

function escapeHtml(s) {
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

// Applique les styles inline (gras, italique, liens) sur une ligne déjà échappée.
function renderInline(text) {
  // Liens : [texte](url)
  text = text.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
  // Gras : **texte** ou __texte__
  text = text.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  text = text.replace(/__([^_]+)__/g, "<strong>$1</strong>");
  // Italique : *texte* ou _texte_
  text = text.replace(/\*([^*]+)\*/g, "<em>$1</em>");
  text = text.replace(/_([^_]+)_/g, "<em>$1</em>");
  return text;
}

function renderMarkdown(source) {
  const lines = source.split("\n");
  const htmlParts = [];
  let inList = false;

  function closeList() {
    if (inList) {
      htmlParts.push("</ul>");
      inList = false;
    }
  }

  for (const rawLine of lines) {
    const line = rawLine;

    if (line.trim() === "") {
      closeList();
      continue;
    }

    const headingMatch = line.match(/^(#{1,6})\s+(.*)$/);
    if (headingMatch) {
      closeList();
      const level = headingMatch[1].length;
      const content = renderInline(escapeHtml(headingMatch[2]));
      htmlParts.push(`<h${level}>${content}</h${level}>`);
      continue;
    }

    const listMatch = line.match(/^[-*]\s+(.*)$/);
    if (listMatch) {
      if (!inList) {
        htmlParts.push("<ul>");
        inList = true;
      }
      const content = renderInline(escapeHtml(listMatch[1]));
      htmlParts.push(`<li>${content}</li>`);
      continue;
    }

    closeList();
    const content = renderInline(escapeHtml(line));
    htmlParts.push(`<p>${content}</p>`);
  }
  closeList();

  return htmlParts.join("\n");
}
