import com.tellme.tellmeplugin.client.OllamaConfig

object HtmlTemplates {

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
            
            html, body {
              margin: 0;
              padding: 0;
              background: var(--bg);
              color: var(--text);
            }
            
            html {
              overflow-y: auto;
              overflow-x: hidden;
            }
            
            body { 
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
              font-size: 14px; 
              line-height: 1.6; 
              width: 100%;
              min-height: 100vh;
            }
            
            .wrap { 
              padding: 16px 20px;
              box-sizing: border-box;
              width: 100%;
              max-width: 100%;
              position: relative;
            }
            
            h1 { font-size: 24px; font-weight: 600; margin: 0 0 16px; color: var(--heading); border-bottom: 1px solid var(--border); padding-bottom: 8px; max-width: 100%; overflow-wrap: break-word; }
            h2 { font-size: 18px; font-weight: 600; margin: 24px 0 12px; color: var(--heading); max-width: 100%; overflow-wrap: break-word; }
            h3 { font-size: 15px; font-weight: 600; margin: 16px 0 8px; color: var(--accent); max-width: 100%; overflow-wrap: break-word; }
            p { margin: 12px 0; max-width: 100%; overflow-wrap: break-word; }
            ul, ol { margin: 12px 0; padding-left: 24px; max-width: 100%; }
            li { margin: 6px 0; overflow-wrap: break-word; }
            li::marker { color: var(--accent); }
            
            blockquote { margin: 16px 0; padding: 12px 16px; border-left: 4px solid var(--accent-orange); background: rgba(206, 145, 120, 0.1); border-radius: 0 8px 8px 0; max-width: 100%; }
            pre { display: block; padding: 16px; border-radius: 8px; background: var(--code-bg); border: 1px solid var(--border); margin: 12px 0; overflow-x: auto; overflow-y: hidden !important; height: auto !important; max-height: none !important; max-width: 100%; box-sizing: border-box; }
            code { font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', Menlo, Monaco, Consolas, monospace; font-size: 13px; white-space: pre; }
            p code, li code { padding: 2px 6px; border-radius: 4px; background: rgba(110, 118, 129, 0.25); color: var(--accent-orange); white-space: normal; }
            
            table { border-collapse: collapse; margin: 16px 0; width: 100%; table-layout: fixed; }
            th { background: rgba(86, 156, 214, 0.15); border: 1px solid var(--border); padding: 10px 12px; text-align: left; font-weight: 600; }
            td { border: 1px solid var(--border); padding: 10px 12px; overflow-wrap: break-word; }
            
            a { color: var(--accent); text-decoration: none; }
            a:hover { text-decoration: none; opacity: 0.8; }
            hr { border: none; border-top: 1px solid var(--border); margin: 24px 0; }
            strong { color: var(--heading); }
            
            @keyframes shimmer { 100% { transform: translateX(100%); } }
            .sk-card { border-radius: 12px; padding: 16px; background: rgba(255,255,255,0.05); margin-bottom: 16px; }
            .sk-row { height: 14px; border-radius: 8px; margin: 12px 0; position: relative; overflow: hidden; background: rgba(255,255,255,0.08); }
            .sk-row::after { content:""; position:absolute; inset:0; transform: translateX(-100%); background: linear-gradient(90deg, transparent, rgba(255,255,255,0.12), transparent); animation: shimmer 1.2s infinite; }
          </style>
          <script>
            window.__setContent = function(html) {
              const content = document.getElementById('content');
              if (!content) return;
              
              // Detect if user is scrolled near bottom of the document
              const isNearBottom = (window.innerHeight + window.scrollY) >= document.body.offsetHeight - 150;
              
              content.innerHTML = html;
              
              if (isNearBottom) {
                window.scrollTo(0, document.body.scrollHeight);
              }
            }
            window.__scrollToTop = function() { 
              window.scrollTo(0, 0);
            }
            window.addEventListener('load', () => {
              setTimeout(() => window.dispatchEvent(new Event('resize')), 100);
            });
          </script>
        </head>
        <body><div class="wrap" id="content"></div></body>
        </html>
    """.trimIndent()

    /**
     * Loading skeleton HTML.
     */
    fun skeletonInnerHtml(label: String): String = """
        <div style="font-size: 18px; font-weight: 700; margin: 2px 0 12px; opacity: 0.9;">Tell Me</div>
        <div class="sk-card">
          <div class="sk-row" style="width:70%"></div>
          <div class="sk-row" style="width:95%"></div>
        </div>
        <div style="opacity:0.7; margin-top:10px; font-size:12px;">$label</div>
    """.trimIndent()

    /**
     * Welcome screen HTML.
     */
    fun welcomeHtml(): String = """
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; padding: 40px 20px;">
          <div style="width: 80px; height: 80px; border-radius: 20px; background: linear-gradient(135deg, #569cd6 0%, #4fc3f7 100%); display: flex; align-items: center; justify-content: center; margin-bottom: 24px; box-shadow: 0 8px 32px rgba(86, 156, 214, 0.3);">
            <span style="font-size: 36px;">💡</span>
          </div>
          <h1 style="border:none; font-size: 28px; margin: 0 0 12px; color: var(--heading);">Welcome to Tell Me</h1>
          <p style="color: #808080; font-size: 15px; margin: 0 0 32px; max-width: 320px;">AI-powered code analysis using your local Ollama model</p>
          <div style="background: rgba(255, 255, 255, 0.03); border: 1px solid #30363d; border-radius: 12px; padding: 24px; max-width: 340px; text-align: left;">
            <div style="font-weight: 600; margin-bottom: 16px; color: #e6e6e6;">🚀 Getting Started</div>
            <p style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">1. Open any code file in the editor</p>
            <p style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">2. Right-click and select <code>Tell Me</code></p>
            <p style="color: #d4d4d4; font-size: 13px; line-height: 1.5;">3. Get instant AI insights</p>
          </div>
        </div>
    """.trimIndent()

    fun readyHtml(): String = welcomeHtml()

    /**
     * HTML for selecting analysis type.
     */
    fun selectionScreenHtml(fileName: String): String = """
        <style>
            .sel-wrapper {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                min-height: 100%;
                padding: 40px 24px;
                box-sizing: border-box;
                animation: fadeIn 0.5s ease-out;
            }
            @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
            
            .sel-icon { font-size: 56px; margin-bottom: 24px; filter: drop-shadow(0 0 15px rgba(206, 145, 120, 0.3)); }
            
            .sel-title { 
                font-size: 26px; 
                font-weight: 700; 
                margin: 0 0 8px 0; 
                color: var(--heading);
                letter-spacing: -0.5px;
            }
            
            .sel-subtitle {
                font-size: 14px;
                color: var(--text-muted);
                margin-bottom: 40px;
                max-width: 400px;
                line-height: 1.5;
            }
            .sel-filename { color: var(--accent); font-family: monospace; }
            
            .sel-actions { 
                display: grid;
                grid-template-columns: 1fr;
                gap: 16px;
                width: 100%;
                max-width: 360px;
            }
            
            .sel-btn {
                display: flex;
                align-items: center;
                gap: 16px;
                padding: 18px 24px;
                border-radius: 14px;
                text-decoration: none;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                background: rgba(255, 255, 255, 0.03);
                border: 1px solid rgba(255, 255, 255, 0.08);
                position: relative;
                overflow: hidden;
            }
            
            .sel-btn:hover {
                background: rgba(255, 255, 255, 0.06);
                border-color: rgba(255, 255, 255, 0.15);
                transform: translateY(-2px);
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
            }
            
            .sel-btn-icon {
                font-size: 24px;
                width: 44px;
                height: 44px;
                border-radius: 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: rgba(255, 255, 255, 0.05);
                transition: transform 0.3s;
            }
            .sel-btn:hover .sel-btn-icon { transform: scale(1.1); }
            
            .sel-btn-text { flex: 1; text-align: left; }
            .sel-btn-text h3 { margin: 0 0 2px 0; font-size: 15px; color: var(--heading); }
            .sel-btn-text p { margin: 0; font-size: 12px; color: var(--text-muted); }
            
            .btn-accent { border-color: rgba(86, 156, 214, 0.3); }
            .btn-accent:hover { background: rgba(86, 156, 214, 0.08); border-color: var(--accent); }
            .btn-accent .sel-btn-icon { color: var(--accent); }
            
            .btn-orange { border-color: rgba(206, 145, 120, 0.3); }
            .btn-orange:hover { background: rgba(206, 145, 120, 0.08); border-color: var(--accent-orange); }
            .btn-orange .sel-btn-icon { color: var(--accent-orange); }
            
            .sel-footer {
                margin-top: 48px;
                font-size: 12px;
                color: var(--text-muted);
                opacity: 0.6;
            }
        </style>
        
        <div class="sel-wrapper">
            <div class="sel-icon">🧠</div>
            <h1 class="sel-title">How can I help you?</h1>
            <p class="sel-subtitle">Select an action for <span class="sel-filename">$fileName</span></p>
            
            <div class="sel-actions">
                <a href="tellme://explain" class="sel-btn btn-accent">
                    <div class="sel-btn-icon">⚡</div>
                    <div class="sel-btn-text">
                        <h3>Analyze this file</h3>
                        <p>Get a comprehensive explanation and insights.</p>
                    </div>
                </a>
                
                <a href="tellme://refactor" class="sel-btn btn-orange">
                    <div class="sel-btn-icon">🛠️</div>
                    <div class="sel-btn-text">
                        <h3>Refactor this code</h3>
                        <p>Improve structure, clarity, and performance.</p>
                    </div>
                </a>
            </div>
            
            <p class="sel-footer">Powered by your local AI model.</p>
        </div>
    """.trimIndent()

    /**
     * Wrap body HTML for Swing JEditorPane.
     */
    fun swingSafeHtml(body: String): String = """
        <html><head><meta charset="utf-8"/>
        <style type="text/css">
          body { font-family: SansSerif; font-size: 13px; line-height: 1.5; margin: 0; padding: 10px; color: #d4d4d4; background-color: #1e1e1e; }
          pre { margin: 8px 0; padding: 10px; background-color: #0d1117; color: #d4d4d4; border: 1px solid #30363d; white-space: pre-wrap; word-wrap: break-word; }
          code { font-family: Monospaced; font-size: 12px; color: #ce9178; }
          a { color: #569cd6; text-decoration: none; }
          h1, h2, h3 { color: #e6e6e6; }
        </style></head><body>$body</body></html>
    """.trimIndent()

    /**
     * Onboarding HTML for setup guide.
     */
    fun onboardingHtml(ollamaRunning: Boolean, modelDownloaded: Boolean): String {
        val title = if (!ollamaRunning) "Let's get started!" else "Almost there!"
        val icon = if (!ollamaRunning) "🚀" else "📦"
        
        val step1Class = if (!ollamaRunning) "step-active" else "step-done"
        val step2Class = if (ollamaRunning && !modelDownloaded) "step-active" else if (modelDownloaded) "step-done" else "step-pending"

        return """
        <style>
            .ob-wrapper { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100%; padding: 40px 24px; box-sizing: border-box; }
            .ob-container { text-align: center; max-width: 500px; width: 100%; }
            .ob-icon { font-size: 64px; margin-bottom: 24px; animation: bounce 2s infinite; }
            @keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
            
            .steps { text-align: left; margin: 32px 0; background: rgba(255,255,255,0.03); border-radius: 16px; padding: 24px; border: 1px solid rgba(255,255,255,0.05); }
            .step { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 24px; opacity: 0.5; transition: all 0.3s; }
            .step-active { opacity: 1; transform: scale(1.02); }
            .step-done { opacity: 0.8; }
            .step-done .step-num { background: #4CAF50 !important; color: white; }
            
            .step-num { width: 28px; height: 28px; border-radius: 50%; background: var(--border); display: flex; align-items: center; justify-content: center; font-weight: bold; flex-shrink: 0; font-size: 14px; }
            .step-content h3 { margin: 0 0 4px 0; font-size: 16px; color: var(--heading); }
            .step-content p { margin: 0; font-size: 13px; color: var(--text-muted); line-height: 1.4; }
            
            .code-block { background: rgba(0,0,0,0.3); padding: 8px 12px; border-radius: 6px; font-family: monospace; margin-top: 8px; font-size: 12px; border: 1px solid rgba(255,255,255,0.1); color: #569CD6; display: block; }
            
            .btn-check { display: inline-block; background: var(--accent); color: white; padding: 12px 32px; border-radius: 8px; text-decoration: none; font-weight: bold; margin-top: 24px; transition: all 0.2s; }
            .btn-check:hover { transform: translateY(-2px); opacity: 0.9; }
            
            .link { color: #569cd6; text-decoration: none; }
        </style>
        
        <div class="ob-wrapper">
          <div class="ob-container">
              <div class="ob-icon">$icon</div>
              <h1 style="border:none; font-size: 28px; margin-bottom: 12px;">$title</h1>
              <p style="color: var(--text-muted); font-size: 15px;">To use local AI analysis, we need to set up Ollama on your machine.</p>
              
              <div class="steps">
                  <div class="step $step1Class">
                      <div class="step-num">1</div>
                      <div class="step-content">
                          <h3>Install Ollama</h3>
                          <p>Download from <a href="https://ollama.com" class="link" target="_blank">ollama.com</a>. Make sure it's running.</p>
                      </div>
                  </div>
                  
                  <div class="step $step2Class">
                      <div class="step-num">2</div>
                      <div class="step-content">
                          <h3>Download Model</h3>
                          <p>Run this command in your terminal:</p>
                          <span class="code-block">ollama run ${OllamaConfig.MODEL}</span>
                      </div>
                  </div>
              </div>
              
              <a href="tellme://check" class="btn-check">Check Connection</a>
              <p style="margin-top: 20px; font-size: 12px; color: var(--text-muted);">Everything runs 100% locally on your machine.</p>
          </div>
        </div>
        """.trimIndent()
    }
}
