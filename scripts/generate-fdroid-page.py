#!/usr/bin/env python3
"""
Generates a modern, responsive landing page for the PureAdmin F-Droid repository.
"""

import sys
import os

def generate_html(output_path: str, version: str = "v1.0.0"):
    repo_url = "https://dheeth.github.io/purelyadmin/fdroid/repo"
    fingerprint = "701488C226FBC630EEB76F4770CDE5D92F7FA93C04F5C950D249CAE32D7BDD30"
    fdroid_intent = f"fdroidrepos://dheeth.github.io/purelyadmin/fdroid/repo?fingerprint={fingerprint}"
    apk_url = f"https://github.com/dheeth/purelyadmin/releases/download/{version}/pureadmin-{version}.apk"

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>PureAdmin F-Droid Repository</title>
  <meta name="description" content="Official F-Droid repository for PureAdmin — lightweight, privacy-focused native Android client for Purelymail.">
  <link rel="icon" type="image/png" href="fdroid/repo/icons/icon.png">
  <style>
    :root {{
      --bg: #0F0E17;
      --card-bg: #1A1829;
      --card-border: #2E2A4A;
      --primary: #7C4DFF;
      --primary-hover: #9E75FF;
      --text: #FFFFFE;
      --text-muted: #A7A9BE;
      --accent: #00E5FF;
      --code-bg: #13121F;
    }}
    * {{
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }}
    body {{
      background-color: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 2rem 1rem;
    }}
    .container {{
      max-width: 680px;
      width: 100%;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 24px;
      padding: 2.5rem 2rem;
      box-shadow: 0 12px 36px rgba(0, 0, 0, 0.45);
      text-align: center;
    }}
    .icon {{
      width: 96px;
      height: 96px;
      border-radius: 20px;
      box-shadow: 0 8px 24px rgba(124, 77, 255, 0.35);
      margin-bottom: 1.25rem;
    }}
    h1 {{
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }}
    .tagline {{
      color: var(--text-muted);
      font-size: 1.05rem;
      line-height: 1.5;
      margin-bottom: 1.75rem;
    }}
    .btn-group {{
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
      margin-bottom: 2rem;
    }}
    @media (min-width: 480px) {{
      .btn-group {{
        flex-direction: row;
        justify-content: center;
      }}
    }}
    .btn {{
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0.85rem 1.75rem;
      border-radius: 14px;
      font-weight: 600;
      font-size: 1rem;
      text-decoration: none;
      transition: all 0.2s ease;
      cursor: pointer;
      border: none;
    }}
    .btn-primary {{
      background: var(--primary);
      color: #FFFFFF;
    }}
    .btn-primary:hover {{
      background: var(--primary-hover);
      transform: translateY(-2px);
    }}
    .btn-secondary {{
      background: var(--code-bg);
      color: var(--text);
      border: 1px solid var(--card-border);
    }}
    .btn-secondary:hover {{
      background: #252238;
      border-color: var(--primary);
      transform: translateY(-2px);
    }}
    .qr-card {{
      background: var(--code-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 1.5rem;
      margin-bottom: 1.75rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
    }}
    .qr-card img {{
      background: #FFFFFF;
      padding: 8px;
      border-radius: 12px;
      width: 180px;
      height: 180px;
    }}
    .qr-text {{
      font-size: 0.9rem;
      color: var(--text-muted);
    }}
    .meta-box {{
      text-align: left;
      background: var(--code-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 1rem 1.25rem;
      margin-bottom: 1rem;
    }}
    .meta-label {{
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--text-muted);
      margin-bottom: 0.35rem;
    }}
    .meta-value-row {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.5rem;
    }}
    .meta-value {{
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      font-size: 0.82rem;
      color: var(--accent);
      word-break: break-all;
    }}
    .copy-btn {{
      background: rgba(124, 77, 255, 0.15);
      color: var(--primary);
      border: 1px solid rgba(124, 77, 255, 0.3);
      padding: 0.35rem 0.65rem;
      border-radius: 8px;
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
      flex-shrink: 0;
      transition: background 0.15s;
    }}
    .copy-btn:hover {{
      background: rgba(124, 77, 255, 0.3);
    }}
    .features {{
      text-align: left;
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--card-border);
      color: var(--text-muted);
      font-size: 0.88rem;
      line-height: 1.6;
    }}
    .features ul {{
      list-style-position: inside;
      margin-top: 0.5rem;
    }}
    footer {{
      margin-top: 2rem;
      color: var(--text-muted);
      font-size: 0.8rem;
    }}
    footer a {{
      color: var(--accent);
      text-decoration: none;
    }}
  </style>
</head>
<body>
  <div class="container">
    <img src="fdroid/repo/icons/icon.png" alt="PureAdmin Logo" class="icon" onerror="this.src='https://raw.githubusercontent.com/dheeth/purelyadmin/main/fastlane/metadata/android/en-US/images/icon.png'">
    <h1>PureAdmin F-Droid Repo</h1>
    <p class="tagline">Official F-Droid repository for PureAdmin — lightweight, open-source management client for Purelymail.</p>

    <div class="btn-group">
      <a href="{fdroid_intent}" class="btn btn-primary">
        📱 Add to F-Droid
      </a>
      <a href="{apk_url}" class="btn btn-secondary">
        ⬇️ Direct APK ({version})
      </a>
    </div>

    <div class="qr-card">
      <img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data={fdroid_intent}" alt="F-Droid QR Code">
      <div class="qr-text">Scan with your phone's camera or F-Droid app to add this repository automatically.</div>
    </div>

    <div class="meta-box">
      <div class="meta-label">Repository URL</div>
      <div class="meta-value-row">
        <span class="meta-value" id="repoUrl">{repo_url}</span>
        <button class="copy-btn" onclick="copyText('repoUrl')">Copy</button>
      </div>
    </div>

    <div class="meta-box">
      <div class="meta-label">Signing Key SHA-256 Fingerprint</div>
      <div class="meta-value-row">
        <span class="meta-value" id="fingerprint">{fingerprint}</span>
        <button class="copy-btn" onclick="copyText('fingerprint')">Copy</button>
      </div>
    </div>

    <div class="features">
      <strong>Zero Telemetry &bull; Direct TLS 1.3 &bull; Keystore Encrypted</strong>
      <ul>
        <li>Prepaid balance & billing management</li>
        <li>Mailbox provisioning & app password generation</li>
        <li>Live MX, SPF, DKIM, and DMARC health diagnostics</li>
        <li>Instant disposable service aliases & catch-all routing</li>
      </ul>
    </div>
  </div>

  <footer>
    <a href="https://github.com/dheeth/purelyadmin" target="_blank" rel="noopener">Source Code (GitHub)</a> &bull;
    <a href="https://github.com/dheeth/purelyadmin/blob/main/LICENSE" target="_blank" rel="noopener">Apache-2.0 License</a>
  </footer>

  <script>
    function copyText(elementId) {{
      const text = document.getElementById(elementId).innerText;
      navigator.clipboard.writeText(text).then(() => {{
        const btn = event.target;
        const orig = btn.innerText;
        btn.innerText = 'Copied!';
        setTimeout(() => {{ btn.innerText = orig; }}, 1800);
      }});
    }}
  </script>
</body>
</html>
"""
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Generated landing page at {output_path}")

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "public/index.html"
    ver = sys.argv[2] if len(sys.argv) > 2 else "v1.0.0"
    generate_html(out, ver)
