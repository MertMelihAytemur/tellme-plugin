# Tell Me 🤖

**Tell Me**, IntelliJ IDEA içindeki kodlarınızı yerel (local) yapay zeka modelleri kullanarak analiz eden, açıklayan ve iyileştiren güçlü bir asistandır. Tüm işlemler kendi makinenizde gerçekleşir; kodunuz asla dışarı çıkmaz.

## ✨ Özellikler

- **Yerel & Gizli (Private):** [Ollama](https://ollama.com/) entegrasyonu sayesinde verileriniz tamamen yerelinizde işlenir.
- **Kod Analizi (Explain):** Karmaşık fonksiyonları veya dosyaları saniyeler içinde analiz eder.
- **Refactor Önerileri:** Modern Kotlin/Android standartlarına uygun iyileştirme tavsiyeleri sunar.
- **Çoklu Sekme (Multi-tab):** Aynı anda birden fazla dosya üzerinde analiz yapabilir ve sekmeler arasında kolayca geçiş yapabilirsiniz.
- **Premium UI:** IntelliJ ortamına tam uyumlu, şık ve modern kullanıcı arayüzü.

## 🚀 Başlangıç

Eklentiyi kullanabilmek için makinenizde Ollama'nın yüklü ve çalışır durumda olması gerekir.

1.  **Ollama Kurulumu:** [ollama.com](https://ollama.com/) üzerinden indirin ve kurun.
2.  **Modeli İndirin:** Terminale şu komutu yazarak eklentinin kullandığı ana modeli indirin:
    ```bash
    ollama run qwen2.5-coder:7b
    ```
3.  **Kullanım:** Editörde herhangi bir kodun üzerine sağ tıklayın ve **"Tell Me"** seçeneğini seçin.

## 🛠️ Teknik Altyapı

- **Dil:** Kotlin
- **Model:** Qwen2.5-Coder (Ollama üzerinden)
- **Arayüz:** JCEF & Swing

## 👨‍💻 Geliştirici

[Mert Melih Aytemur](https://github.com/MertMelihAytemur) tarafından geliştirilmiştir.

---
*Bu eklenti, geliştiricilerin kod anlama sürecini hızlandırmak ve yerel yapıları korumak amacıyla tasarlanmıştır.*