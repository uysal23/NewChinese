# NewChinese — Master Execution Steps v1

Bu dosya, kabul edilen **Master Execution Plan v1** için kısa ve sıralı yürütme planıdır.

## Sabit Referans
- **Golden Demo V1 = GitHub Actions Run #163**
- Sonraki tüm sahneler görünüm, akış, karakter sürekliliği, ses eşleşmesi ve öğrenme mantığı açısından bu referansa bağlı kalacaktır.
- `LOCKED_STEP_01...` → `LOCKED_STEP_20...`, `LOCKS.md` ve merkezi manifestler değiştirilmeden ana kaynak kabul edilir.
- Bir medya kategorisi fiziksel dosyaları gerçekten mevcut değilse `complete` sayılmaz.

## Adım 1 — Görsel Üretim Hattını Onar + SC002 Görsellerini Tamamla
#175 sonrası bozulan staged visual/import akışını düzelt. SC002 için bg, Li Na, Zhang Wei, fg ve preview dosyalarını tamamla. `visual.status = complete` yap ve validation/build geçir.

## Adım 2 — SC003 Görsellerini Tamamla
Golden Demo #163 stilinde SC003'ün 5 görsel assetini tamamla, bağla ve validate et.

## Adım 3 — SC003 Seslerini Üret
Hazır üretim kuyruğundan 18 diyalog + 8 kelime + 2 cümle sesini üret, import et ve `audio.status = complete` yap.

## Adım 4 — SC004'ü Tamamla
SC004 içerik, görsel, ses, uygulama bağlantıları, validation ve build adımlarını Golden Demo standardında tamamla.

## Adım 5 — SC005–SC010 Paketini Tamamla
6 sahnenin içerik kontrollerini yap; görselleri, sesleri ve uygulama bağlantılarını tamamla. Paket sonunda 10/10 sahne doğrulaması yap.

## Adım 6 — HSK1 SC011–SC020 Paketini Tamamla
10 sahneyi aynı sabit pipeline ile içerik → görsel → ses → bağlama → validation/build sırasıyla tamamla.

## Adım 7 — HSK1 SC021–SC030 Paketini Tamamla
10 sahneyi aynı kalite ve continuity kurallarıyla tamamla.

## Adım 8 — HSK1 SC031–SC040 Paketini Tamamla
10 sahneyi aynı kalite ve continuity kurallarıyla tamamla.

## Adım 9 — HSK1 SC041–SC050 Paketini Tamamla
HSK1'in son 10 sahnesini tamamla.

## Adım 10 — HSK1 Seviye Kabul Testi
HSK1'i 50/50 kontrol et: sahne kilitleri, sınavlar, favoriler, shadowing, progress, free study, görsel/ses bütünlüğü ve offline çalışma.

## Adım 11 — HSK2'yi 50/50 Tamamla
HSK2 sahnelerini 10'lu paketler halinde üret, doğrula ve tam seviye kabul testini geçir.

## Adım 12 — HSK3'ü 50/50 Tamamla
HSK3 sahnelerini 10'lu paketler halinde üret, doğrula ve tam seviye kabul testini geçir.

## Adım 13 — HSK4'ü 50/50 Tamamla
HSK4 sahnelerini 10'lu paketler halinde üret, doğrula ve tam seviye kabul testini geçir.

## Adım 14 — HSK5'i 50/50 Tamamla
HSK5 sahnelerini 10'lu paketler halinde üret, doğrula ve tam seviye kabul testini geçir.

## Adım 15 — HSK6'yı 50/50 Tamamla
HSK6 sahnelerini 10'lu paketler halinde üret, doğrula ve tam seviye kabul testini geçir. Hedef: **300/300 sahne**.

## Adım 16 — Tam Uygulama Sistem Testi
300 sahne üzerinde navigation, unlock, sınav, shadowing, ses hızı, tema, bildirim, favoriler, progress, free study, küçük ekran safe-area ve offline davranışı test et.

## Adım 17 — Final Görsel/Ses/UI Polish
Metin taşmaları, küçük ekran uyumu, ses loudness eşitleme, preview kalite kontrolü, tema/dark mode, ikon/splash, dashboard ve ayarlar son düzenlemelerini yap.

## Adım 18 — Release Hazırlığı
Versioning, signing hazırlığı, package kontrolü, release notes, Play Store/AAB gereksinimleri ve son release validation işlemlerini tamamla.

## Adım 19 — Final APK + AAB Üret
Final Debug/Release APK ve Release AAB üret. Validation report ile tüm zorunlu içeriklerin **300/300 complete** olduğunu doğrula.

## Adım 20 — Projeyi Tamamla
Final kurulum testi, son kabul kontrolü ve teslim artefaktlarını doğrula. Bu adım geçtiğinde proje **tamamlanmış** kabul edilir.

---

## Çalışma Kuralı
Her adım bitince bir sonraki adıma geçilir. Durum raporu kısa tutulur:
- **Tamamlanan**
- **Şu an yapılan**
- **Sıradaki**
- **Sorun/Risk**
- **Kullanıcı kararı gerekiyorsa:** yalnızca o zaman sorulur.

## Güncel Durum
- Golden Demo #163: kilitli referans.
- SC001–SC030: tamam.
- SC031–SC040: tamamlandı ve validation geçti.
- SC041–SC050: tamamlandı; içerik, 50/50 görsel ve 240/240 ses hazır.
- HSK1: 50/50 seviye kabulü tamamlandı. Run #280 validation, unit test, lint, debug APK ve release APK/AAB build SUCCESS.
- HSK2: tamamlanan üretim adımları repo durumuna göre korunur; bu satır artık aktif çalışma noktası değildir.
- HSK3: SC001–SC027 için görsel ve ses durumları complete.
- HSK3_SC027: 5/5 canonical elektronik mağaza görseli doğrulandı; final validation, unit test ve Android lint SUCCESS.
- HSK3_SC028: audio complete, visual pending (AUTO-PROGRESS canonical pharmacy stage başlatılıyor).
- **Aktif Adım: Adım 12 — HSK3'ü 50/50 Tamamla**
- **Anlık iş kalemi: HSK3_SC028 görsel assetlerini canonical kaynakla stage etmek, validate etmek ve complete duruma getirmek**
