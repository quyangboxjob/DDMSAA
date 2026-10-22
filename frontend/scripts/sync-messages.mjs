// spec-09 のメッセージカタログ（ja 正本 / zh / en）から i18n リソースを生成する。
// 実行：pnpm sync:messages。生成物は src/i18n/generated/messages.<lang>.json（手で編集しない）。
// {0},{1} のプレースホルダーは i18next の {{0}},{{1}} へ変換する。
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { parse } from 'yaml'

const here = dirname(fileURLToPath(import.meta.url))
const sourceDir = resolve(here, '../../docs/基本設計/共通')
const outDir = resolve(here, '../src/i18n/generated')
const sources = {
  ja: 'spec-09-message-list.yaml',
  zh: 'spec-09-message-list.zh.yaml',
  en: 'spec-09-message-list.en.yaml',
}
const idPattern = /^[IWE]\d{4}$/

mkdirSync(outDir, { recursive: true })
const idSets = {}
for (const [lang, file] of Object.entries(sources)) {
  const doc = parse(readFileSync(resolve(sourceDir, file), 'utf-8'))
  const out = {}
  for (const [id, entry] of Object.entries(doc.messages)) {
    if (!idPattern.test(id)) throw new Error(`${file}: invalid messageId ${id}`)
    out[id] = String(entry.message).replace(/\{(\d+)\}/g, '{{$1}}')
  }
  idSets[lang] = Object.keys(out)
  writeFileSync(
    resolve(outDir, `messages.${lang}.json`),
    `${JSON.stringify(out, null, 2)}\n`,
    'utf-8',
  )
  console.log(`${lang}: ${idSets[lang].length} messages`)
}
const base = new Set(idSets.ja)
for (const lang of ['zh', 'en']) {
  const missing = [...base].filter((id) => !idSets[lang].includes(id))
  if (missing.length > 0) throw new Error(`${lang} is missing: ${missing.join(', ')}`)
}
