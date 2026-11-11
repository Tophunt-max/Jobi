#!/usr/bin/env python3
"""
Fills missing strings in src/i18n/locales/<lang>.json from en.json with Cloudflare Workers AI.

    export CLOUDFLARE_ACCOUNT_ID=...  CLOUDFLARE_API_TOKEN=...   # token needs "Workers AI: Read"
    python3 scripts/translate-locales.py                 # every language
    python3 scripts/translate-locales.py pa ur kn        # only some

Resumable: only keys missing from a locale are sent, and each group is saved as soon as it is
translated, so the script can be stopped and run again. Strings already in a locale are never
overwritten. A result is accepted only when it has the same keys, {{placeholders}} and line breaks
as the English text.

The free plan allows 10,000 neurons a day (about 150 groups). When the quota runs out the
remaining groups print "FAILED"; run the script again the next day.
"""
import concurrent.futures as cf
import json
import os
import re
import sys
import threading
import urllib.request

MODEL = '@cf/openai/gpt-oss-120b'
LOCALES = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'i18n', 'locales')
LANGS = {
    'or': 'Odia', 'bn': 'Bengali', 'mr': 'Marathi', 'ta': 'Tamil', 'te': 'Telugu', 'gu': 'Gujarati',
    'pa': 'Punjabi (Gurmukhi script)', 'ur': 'Urdu', 'kn': 'Kannada', 'ml': 'Malayalam', 'as': 'Assamese',
}
PLACEHOLDER = re.compile(r'\{\{\w+\}\}')
ACCOUNT = os.environ.get('CLOUDFLARE_ACCOUNT_ID', '')
TOKEN = os.environ.get('CLOUDFLARE_API_TOKEN', '')


def path(lang):
    return os.path.join(LOCALES, f'{lang}.json')


def load(lang):
    with open(path(lang), encoding='utf-8') as f:
        return json.load(f)


def ask(prompt):
    req = urllib.request.Request(
        f'https://api.cloudflare.com/client/v4/accounts/{ACCOUNT}/ai/run/{MODEL}',
        data=json.dumps({'messages': [{'role': 'user', 'content': prompt}], 'max_tokens': 8000}).encode(),
        headers={'Authorization': f'Bearer {TOKEN}', 'Content-Type': 'application/json'},
    )
    with urllib.request.urlopen(req, timeout=300) as res:
        return json.load(res)['result']['choices'][0]['message']['content']


def valid(src, out):
    if not isinstance(out, dict) or set(src) != set(out):
        return False
    return all(
        isinstance(out[k], str)
        and sorted(PLACEHOLDER.findall(src[k])) == sorted(PLACEHOLDER.findall(out[k]))
        and out[k].count('\n') == src[k].count('\n')
        for k in src
    )


def translate(lang, src, hindi):
    prompt = (
        f'Translate the JSON values from English to {LANGS[lang]} for RozgarOne, an Indian government jobs mobile app. '
        'Rules: keep every key; keep {{placeholders}}, \\n line breaks, emoji, symbols (✓ ✗ • → ₹) and names/acronyms '
        '(RozgarOne, SSC, UPSC, RRB, IBPS, SBI, RBI, PSC, PSU, TET, PGT, TGT, PDF, AI, OBC, SC, ST, EWS, PwD, BHEL, ONGC, IAF, NTA) unchanged. '
        "Use short, natural words people in India see in apps; English loanwords like 'Admit Card' or 'Result' may be "
        'written in the local script if that is more common. Keys ending in _one/_other are plural forms. '
        'A Hindi version is given for meaning reference. Reply with one JSON object only.\n'
        f'ENGLISH:\n{json.dumps(src, ensure_ascii=False)}\nHINDI:\n{json.dumps(hindi, ensure_ascii=False)}'
    )
    for _ in range(3):
        try:
            match = re.search(r'\{.*\}', ask(prompt), re.S)
            out = json.loads(match.group(0)) if match else None
            if valid(src, out):
                return out
        except Exception as e:  # network error, quota (HTTP 429), bad JSON
            if '429' in str(e):
                return None
    return None


def main():
    if not ACCOUNT or not TOKEN:
        sys.exit('Set CLOUDFLARE_ACCOUNT_ID and CLOUDFLARE_API_TOKEN.')
    en, hi = load('en'), load('hi')
    langs = sys.argv[1:] or list(LANGS)
    lock = threading.Lock()

    def save(lang, group, out):
        with lock:
            cur = load(lang)
            cur.setdefault(group, {}).update(out)
            ordered = {g: {k: cur[g][k] for k in en[g] if k in cur.get(g, {})} for g in en if g in cur}
            with open(path(lang), 'w', encoding='utf-8') as f:
                f.write(json.dumps(ordered, ensure_ascii=False, indent=2) + '\n')

    jobs = []
    for lang in langs:
        cur = load(lang)
        for group, values in en.items():
            todo = {k: v for k, v in values.items() if k not in cur.get(group, {})}
            if todo:
                jobs.append((lang, group, todo))
    print(f'{len(jobs)} groups to translate', flush=True)

    def work(job):
        lang, group, todo = job
        out = translate(lang, todo, {k: hi.get(group, {}).get(k, '') for k in todo})
        if out is None:
            return f'{lang}.{group} FAILED'
        save(lang, group, out)
        return None

    failed = 0
    with cf.ThreadPoolExecutor(5) as pool:
        for result in pool.map(work, jobs):
            if result:
                failed += 1
                print(result, flush=True)
    print(f'done, {failed} failed', flush=True)


if __name__ == '__main__':
    main()
