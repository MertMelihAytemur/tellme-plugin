package com.tellme.tellmeplugin.ui.render

/**
 * HTML template utilities for rendering content.
 */
object HtmlTemplates {

    /** Typing caret character for streaming effect */
    const val CARET_CHAR = "▍"

    /**
     * Escapes HTML special characters.
     */
    fun escapeHtml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    /**
     * Escapes a string for safe use in JavaScript.
     */
    fun jsString(s: String): String {
        val escaped = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
        return "\"$escaped\""
    }

    /**
     * Base HTML template for JCEF browser.
     * Styled similar to Antigravity walkthrough markdown.
     */
    fun cefBaseHtml(): String = """
        <html>
        <head>
          <meta charset="utf-8"/>
          <style>
            :root { 
              color-scheme: dark; 
              --bg: #1e1e1e;
              --text: #d4d4d4;
              --text-muted: #808080;
              --accent: #569cd6;
              --accent-orange: #ce9178;
              --code-bg: #0d1117;
              --border: #30363d;
              --heading: #e6e6e6;
            }
            
            body { 
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
              font-size: 14px; 
              line-height: 1.6; 
              margin: 0; 
              padding: 20px 24px;
              background: var(--bg);
              color: var(--text);
            }
            
            .wrap { max-width: 900px; margin: 0 auto; }
            
            h1 { 
              font-size: 24px; 
              font-weight: 600;
              margin: 0 0 16px; 
              color: var(--heading);
              border-bottom: 1px solid var(--border);
              padding-bottom: 8px;
            }
            
            h2 { 
              font-size: 18px; 
              font-weight: 600;
              margin: 24px 0 12px; 
              color: var(--heading);
            }
            
            h3 { 
              font-size: 15px; 
              font-weight: 600;
              margin: 16px 0 8px; 
              color: var(--accent);
            }
            
            p { margin: 12px 0; }
            
            ul, ol { margin: 12px 0; padding-left: 24px; }
            li { margin: 6px 0; }
            li::marker { color: var(--accent); }
            
            blockquote { 
              margin: 16px 0; 
              padding: 12px 16px; 
              border-left: 4px solid var(--accent-orange); 
              background: rgba(206, 145, 120, 0.1); 
              border-radius: 0 8px 8px 0; 
            }
            
            pre { 
              padding: 16px; 
              border-radius: 8px; 
              overflow-x: auto; 
              background: var(--code-bg);
              border: 1px solid var(--border);
              margin: 12px 0;
            }
            
            code { 
              font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', Menlo, Monaco, Consolas, monospace; 
              font-size: 13px; 
            }
            
            p code, li code { 
              padding: 2px 6px; 
              border-radius: 4px; 
              background: rgba(110, 118, 129, 0.25);
              color: var(--accent-orange);
            }
            
            table { border-collapse: collapse; margin: 16px 0; width: 100%; }
            th { 
              background: rgba(86, 156, 214, 0.15); 
              border: 1px solid var(--border); 
              padding: 10px 12px; 
              text-align: left;
              font-weight: 600;
            }
            td { 
              border: 1px solid var(--border); 
              padding: 10px 12px; 
            }
            
            a { color: var(--accent); text-decoration: none; }
            a:hover { text-decoration: underline; }
            
            hr { border: none; border-top: 1px solid var(--border); margin: 24px 0; }
            
            strong { color: var(--heading); }
            
            /* Shimmer loading animation */
            @keyframes shimmer { 100% { transform: translateX(100%); } }
            .sk-card { border-radius: 12px; padding: 16px; background: rgba(255,255,255,0.05); margin-bottom: 16px; }
            .sk-row { height: 14px; border-radius: 8px; margin: 12px 0; position: relative; overflow: hidden; background: rgba(255,255,255,0.08); }
            .sk-row::after { content:""; position:absolute; inset:0; transform: translateX(-100%); background: linear-gradient(90deg, transparent, rgba(255,255,255,0.12), transparent); animation: shimmer 1.2s infinite; }
          </style>
          <script>
            window.__setContent = function(html) {
              document.getElementById('content').innerHTML = html;
              window.scrollTo(0, document.body.scrollHeight);
            }
          </script>
        </head>
        <body>
          <div class="wrap" id="content"></div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Loading skeleton HTML for initial state.
     */
    fun skeletonInnerHtml(label: String): String = """
        <div style="font-size: 18px; font-weight: 700; margin: 2px 0 12px; opacity: 0.9;">Tell Me</div>
        <div class="sk-card">
          <div class="sk-row" style="width:70%"></div>
          <div class="sk-row" style="width:95%"></div>
          <div class="sk-row" style="width:85%"></div>
        </div>
        <div class="sk-card">
          <div class="sk-row" style="width:60%"></div>
          <div class="sk-row" style="width:95%"></div>
          <div class="sk-row" style="width:70%"></div>
        </div>
        <div style="opacity:0.7; margin-top:10px; font-size:12px;">$label</div>
    """.trimIndent()

    /**
     * Onboarding HTML when no file is open.
     * Shows welcome message and usage instructions.
     */
    fun onboardingHtml(): String = """
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; padding: 40px 20px;">
          
          <!-- Logo/Icon -->
          <div style="
            width: 80px; 
            height: 80px; 
            border-radius: 20px; 
            background: linear-gradient(135deg, #569cd6 0%, #4fc3f7 100%);
            display: flex; 
            align-items: center; 
            justify-content: center;
            margin-bottom: 24px;
            box-shadow: 0 8px 32px rgba(86, 156, 214, 0.3);
          ">
            <span style="font-size: 36px;">💡</span>
          </div>
          
          <!-- Title -->
          <h1 style="
            font-size: 28px; 
            font-weight: 700; 
            margin: 0 0 12px;
            background: linear-gradient(135deg, #e6e6e6 0%, #a0a0a0 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          ">Welcome to Tell Me</h1>
          
          <!-- Subtitle -->
          <p style="
            color: #808080; 
            font-size: 15px; 
            margin: 0 0 32px;
            max-width: 320px;
          ">
            AI-powered code analysis using your local Ollama model
          </p>
          
          <!-- Instructions Card -->
          <div style="
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid #30363d;
            border-radius: 12px;
            padding: 24px;
            max-width: 340px;
            text-align: left;
          ">
            <div style="font-weight: 600; margin-bottom: 16px; color: #e6e6e6;">
              🚀 Getting Started
            </div>
            
            <div style="display: flex; align-items: flex-start; margin-bottom: 14px;">
              <span style="
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background: rgba(86, 156, 214, 0.2);
                color: #569cd6;
                font-size: 12px;
                font-weight: 600;
                margin-right: 12px;
                flex-shrink: 0;
              ">1</span>
              <span style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">
                Open any code file in the editor
              </span>
            </div>
            
            <div style="display: flex; align-items: flex-start; margin-bottom: 14px;">
              <span style="
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background: rgba(86, 156, 214, 0.2);
                color: #569cd6;
                font-size: 12px;
                font-weight: 600;
                margin-right: 12px;
                flex-shrink: 0;
              ">2</span>
              <span style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">
                Right-click and select <code style="background: rgba(110, 118, 129, 0.25); padding: 2px 6px; border-radius: 4px; color: #ce9178;">Tell Me</code>
              </span>
            </div>
            
            <div style="display: flex; align-items: flex-start;">
              <span style="
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background: rgba(86, 156, 214, 0.2);
                color: #569cd6;
                font-size: 12px;
                font-weight: 600;
                margin-right: 12px;
                flex-shrink: 0;
              ">3</span>
              <span style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">
                Get instant AI analysis of your code
              </span>
            </div>
          </div>
          
          <!-- Footer hint -->
          <p style="
            color: #606060; 
            font-size: 12px; 
            margin-top: 24px;
          ">
            Also available from <strong style="color: #808080;">Tools → Tell Me</strong>
          </p>
        </div>
    """.trimIndent()

    /**
     * Ready state HTML - now shows onboarding.
     */
    fun readyHtml(): String = onboardingHtml()

    /**
     * Wrap body HTML for Swing JEditorPane.
     */
    fun swingSafeHtml(body: String): String = """
        <html><head><meta charset="utf-8"/>
        <style type="text/css">
          body { font-family: SansSerif; font-size: 13px; line-height: 1.5; margin: 0; padding: 10px; }
          pre { margin: 8px 0; padding: 10px; background-color: #f2f2f2; color: #111; border: 1px solid #d0d0d0; white-space: pre-wrap; word-wrap: break-word; overflow: auto; }
          code { font-family: Monospaced; font-size: 12px; }
        </style></head><body>$body</body></html>
    """.trimIndent()
}
