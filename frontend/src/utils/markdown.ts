/** 轻量 Markdown → HTML（法律条款页用，覆盖标题/段落/列表/表格/引用/粗体） */

function escapeHtml(s: string) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function inline(s: string) {
  let t = escapeHtml(s)
  t = t.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  t = t.replace(/(^|[^*])\*(.+?)\*(?!\*)/g, '$1<em>$2</em>')
  t = t.replace(/`(.+?)`/g, '<code>$1</code>')
  return t
}

export function renderMarkdown(src: string): string {
  const lines = src.replace(/\r\n/g, '\n').split('\n')
  const out: string[] = []
  let i = 0

  while (i < lines.length) {
    const line = lines[i]
    const trimmed = line.trim()

    if (!trimmed) {
      i += 1
      continue
    }

    if (/^---+$/.test(trimmed)) {
      out.push('<hr />')
      i += 1
      continue
    }

    if (trimmed.startsWith('> ')) {
      const parts: string[] = []
      while (i < lines.length && lines[i].trim().startsWith('>')) {
        parts.push(lines[i].trim().replace(/^>\s?/, ''))
        i += 1
      }
      out.push(`<blockquote>${parts.map((p) => `<p>${inline(p)}</p>`).join('')}</blockquote>`)
      continue
    }

    const heading = trimmed.match(/^(#{1,3})\s+(.+)$/)
    if (heading) {
      const level = heading[1].length
      out.push(`<h${level}>${inline(heading[2])}</h${level}>`)
      i += 1
      continue
    }

    if (trimmed.startsWith('|') && trimmed.includes('|', 1)) {
      const rows: string[][] = []
      while (i < lines.length && lines[i].trim().startsWith('|')) {
        const row = lines[i].trim()
        i += 1
        if (/^\|(\s*:?-+:?\s*\|)+\s*$/.test(row)) continue
        const cells = row
          .replace(/^\|/, '')
          .replace(/\|$/, '')
          .split('|')
          .map((c) => c.trim())
        rows.push(cells)
      }
      if (rows.length) {
        const [head, ...body] = rows
        const buf = ['<table><thead><tr>']
        head.forEach((c) => buf.push(`<th>${inline(c)}</th>`))
        buf.push('</tr></thead><tbody>')
        body.forEach((r) => {
          buf.push('<tr>')
          r.forEach((c) => buf.push(`<td>${inline(c)}</td>`))
          buf.push('</tr>')
        })
        buf.push('</tbody></table>')
        out.push(buf.join(''))
      }
      continue
    }

    if (/^[-*]\s+/.test(trimmed)) {
      out.push('<ul>')
      while (i < lines.length && /^[-*]\s+/.test(lines[i].trim())) {
        out.push(`<li>${inline(lines[i].trim().replace(/^[-*]\s+/, ''))}</li>`)
        i += 1
      }
      out.push('</ul>')
      continue
    }

    if (/^\d+\.\s+/.test(trimmed)) {
      out.push('<ol>')
      while (i < lines.length && /^\d+\.\s+/.test(lines[i].trim())) {
        out.push(`<li>${inline(lines[i].trim().replace(/^\d+\.\s+/, ''))}</li>`)
        i += 1
      }
      out.push('</ol>')
      continue
    }

    const para: string[] = [trimmed]
    i += 1
    while (
      i < lines.length &&
      lines[i].trim() &&
      !lines[i].trim().startsWith('#') &&
      !lines[i].trim().startsWith('>') &&
      !lines[i].trim().startsWith('|') &&
      !/^[-*]\s+/.test(lines[i].trim()) &&
      !/^\d+\.\s+/.test(lines[i].trim()) &&
      !/^---+$/.test(lines[i].trim())
    ) {
      para.push(lines[i].trim())
      i += 1
    }
    out.push(`<p>${inline(para.join(' '))}</p>`)
  }

  return out.join('\n')
}
