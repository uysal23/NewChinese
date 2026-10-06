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
- HSK1: 50/50 seviye kabulü tamamlandı.
- HSK2: tamamlanan üretim adımları repo durumuna göre korunur.
- HSK3: **50/50 visual + audio COMPLETE**; seviye kapanışı passed ve HSK4 geçişi allowed.
- HSK4_SC001–SC010: **10/10 visual + audio complete**; first batch closure PASSED.
- HSK4_SC011: 5/5 canonical Office visual complete.
- HSK4_SC012: 5/5 canonical Cafe visual complete.
- HSK4_SC013: 5/5 canonical Office visual complete.
- HSK4_SC014: 5/5 canonical Cafe visual complete.
- HSK4_SC015: 5/5 canonical Office visual complete.
- HSK4_SC016: 5/5 canonical Cafe visual complete.
- HSK4_SC017: 5/5 canonical Office visual complete.
- HSK4_SC018: 5/5 canonical Cafe visual complete.
- HSK4_SC019: 5/5 canonical Office visual complete; HSK3_SC045 accepted Office seti yeniden kullanıldı.
- HSK4_SC020: 5/5 canonical Cafe visual complete; HSK3_SC012 accepted Cafe seti yeniden kullanıldı.
- HSK4_SC011–SC020 second batch: **10/10 visual complete, 10/10 audio complete; physical closure audit PASSED**.
- HSK4_SC021: 5/5 canonical Zhang Home visual complete; HSK3_SC021 accepted Home seti yeniden kullanıldı.
- HSK4_SC022: 5/5 canonical City Park visual complete; HSK2_SC020 accepted Park seti yeniden kullanıldı.
- HSK4_SC023: 5/5 canonical West Lake visual complete; HSK2_SC040 accepted West Lake seti yeniden kullanıldı.
- HSK4_SC024: 5/5 canonical Zhang Home visual complete; HSK2_SC049 accepted Home seti yeniden kullanıldı.
- HSK4_SC025: 5/5 canonical City Park visual complete; HSK2_SC020 accepted Park seti yeniden kullanıldı.
- HSK4_SC026: 5/5 canonical West Lake visual complete; HSK2_SC050 accepted West Lake seti yeniden kullanıldı.
- HSK4_SC027: 5/5 canonical Zhang Home visual complete; HSK3_SC021 accepted Home seti yeniden kullanıldı.
- HSK4_SC028: 5/5 canonical City Park visual complete; HSK2_SC020 accepted Park seti yeniden kullanıldı.
- HSK4_SC029: 5/5 canonical West Lake visual complete; HSK2_SC040 accepted West Lake seti yeniden kullanıldı.
- HSK4_SC030: 5/5 canonical Zhang Home visual complete; HSK2_SC049 accepted Home seti yeniden kullanıldı.
- HSK4_SC021–SC030 third batch: **10/10 visual complete, 10/10 audio complete; physical closure audit PASSED**.
- HSK4_SC031: 5/5 canonical Community Center visual complete; HSK2_SC041 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC032: 5/5 canonical Day Trip Town composite visual complete; HSK2_SC034 BG/FG + HSK2_SC045 Zhang Wei/Liu Mei layers + scene-specific preview kullanıldı.
- HSK4_SC033: 5/5 canonical Public Library composite visual complete; HSK2_SC023 BG/FG + HSK2_SC041 Zhang Wei/Chen Yu layers + scene-specific preview kullanıldı.
- HSK4_SC034: 5/5 canonical Community Center visual complete; HSK2_SC045 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC035: 5/5 canonical Day Trip Town composite visual complete; HSK2_SC034 BG/FG + HSK2_SC041 Zhang Wei/Chen Yu layers + scene-specific preview kullanıldı.
- HSK4_SC036: 5/5 canonical Public Library visual complete; HSK3_SC020 accepted Public Library seti yeniden kullanıldı.
- HSK4_SC037: 5/5 canonical Community Center visual complete; HSK2_SC041 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC038: 5/5 canonical Day Trip Town composite visual complete; HSK4_SC032 accepted composite seti yeniden kullanıldı.
- HSK4_SC039: 5/5 canonical Public Library composite visual complete; HSK4_SC033 accepted composite seti yeniden kullanıldı.
- HSK4_SC040: 5/5 canonical Community Center visual complete; HSK2_SC045 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC031–SC040 fourth batch: **10/10 visual complete, 10/10 audio complete; physical closure audit PASSED**.
- HSK4_SC041: 5/5 canonical Zhang Office visual complete; HSK3_SC041 accepted Office seti yeniden kullanıldı.
- HSK4_SC042: 5/5 canonical Community Center visual complete; HSK2_SC041 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC043: 5/5 canonical Cafe composite visual complete; HSK3_SC048 BG/FG + HSK3_SC041 Zhang Wei/Wang Ming layers + scene-specific preview kullanıldı.
- HSK4_SC044: 5/5 canonical Zhang Office composite visual complete; HSK3_SC041 BG/FG + HSK2_SC041 Zhang Wei/Chen Yu layers + scene-specific preview kullanıldı.
- HSK4_SC045: 5/5 canonical Community Center composite visual complete; HSK2_SC041 BG/FG + HSK3_SC041 Zhang Wei/Wang Ming layers + scene-specific preview kullanıldı.
- HSK4_SC046: 5/5 canonical Cafe visual complete; HSK3_SC048 accepted Cafe seti yeniden kullanıldı.
- HSK4_SC047: 5/5 canonical Zhang Office visual complete; HSK3_SC041 accepted Office seti yeniden kullanıldı.
- HSK4_SC048: 5/5 canonical Community Center visual complete; HSK2_SC041 accepted Community Center seti yeniden kullanıldı.
- HSK4_SC049: 5/5 canonical Cafe composite visual complete; HSK4_SC043 accepted Cafe composite seti yeniden kullanıldı.
- HSK4_SC050: 5/5 canonical Zhang Office composite visual complete; HSK4_SC044 accepted Office composite seti yeniden kullanıldı.
- HSK4_SC041–SC050 fifth batch: **10/10 visual complete, 10/10 audio complete; physical closure audit PASSED**.
- HSK4: **50/50 visual + audio COMPLETE**; five 10-scene batch closure audits PASSED.
- HSK5_SC001: **5/5 canonical Zhang Office visual complete**; HSK4_SC041 accepted Office seti yeniden kullanıldı; HSK4_SC050 → HSK5_SC001 level-transition continuity approved.
- HSK5_SC002: **5/5 canonical Electronics Store visual complete**; HSK3_SC042 accepted Electronics Store seti yeniden kullanıldı; HSK5_SC001 → HSK5_SC002 continuity approved.
- HSK5_SC003: **5/5 canonical Cafe visual complete**; HSK4_SC043 accepted Cafe composite seti yeniden kullanıldı; HSK5_SC002 → HSK5_SC003 continuity approved.
- HSK5_SC004: **5/5 canonical Zhang Office visual complete**; HSK4_SC044 accepted Office composite seti yeniden kullanıldı; HSK5_SC003 → HSK5_SC004 continuity approved.
- HSK5_SC005: **5/5 canonical Electronics Store composite visual complete**; HSK3_SC042 BG/FG + HSK4_SC041 Zhang Wei/Wang Ming layers kullanıldı; textless 9:16 PREVIEW deterministic olarak türetildi; HSK5_SC004 → HSK5_SC005 continuity approved.
- HSK5_SC006: **5/5 canonical Cafe visual complete**; HSK4_SC046 accepted Cafe seti yeniden kullanıldı; HSK5_SC005 → HSK5_SC006 continuity approved.
- HSK5_SC007: **5/5 canonical Zhang Office visual complete**; HSK5_SC001 accepted Office seti yeniden kullanıldı; HSK5_SC006 → HSK5_SC007 continuity approved.
- HSK5_SC008: **5/5 canonical Electronics Store visual complete**; HSK5_SC002 accepted Electronics Store seti yeniden kullanıldı; HSK5_SC007 → HSK5_SC008 continuity approved.
- HSK5_SC009: **5/5 canonical Cafe visual complete**; HSK5_SC003 accepted Cafe seti yeniden kullanıldı; HSK5_SC008 → HSK5_SC009 continuity approved.
- HSK5_SC010: **5/5 canonical Zhang Office visual complete**; HSK5_SC004 accepted Office seti yeniden kullanıldı; HSK5_SC009 → HSK5_SC010 continuity approved.
- HSK5_SC001–SC010 first batch: **10/10 visual complete, 10/10 audio complete, 10/10 accepted; physical closure audit PASSED**.
- HSK5_SC011: **5/5 canonical Train Station visual complete**; HSK2_SC033 accepted Train Station seti yeniden kullanıldı; HSK5_SC010 → HSK5_SC011 continuity approved.
- HSK5_SC012: **5/5 canonical Day Trip Town visual complete**; HSK4_SC035 accepted composite seti yeniden kullanıldı; HSK5_SC011 → HSK5_SC012 continuity approved.
- HSK5_SC013: **5/5 canonical Guesthouse visual complete**; HSK2_SC036 accepted Guesthouse seti yeniden kullanıldı; HSK5_SC012 → HSK5_SC013 continuity approved.
- HSK5_SC014: **5/5 canonical West Lake composite visual complete**; HSK2_SC040 BG/FG + HSK4_SC035 Zhang Wei/Chen Yu layers kullanıldı; textless 9:16 PREVIEW deterministic olarak türetildi; HSK5_SC013 → HSK5_SC014 continuity approved.
- HSK5_SC015: **5/5 canonical Train Station visual complete**; HSK2_SC033 accepted Train Station 5'li seti birebir yeniden kullanıldı; HSK5_SC014 → HSK5_SC015 continuity approved.
- HSK5_SC016: **5/5 canonical Day Trip Town visual complete**; HSK4_SC035 accepted Day Trip Town composite 5'li seti birebir yeniden kullanıldı; HSK5_SC015 → HSK5_SC016 continuity approved.
- HSK5_SC017: **5/5 canonical Guesthouse visual complete**; HSK2_SC036 accepted Guesthouse 5'li seti birebir yeniden kullanıldı; HSK5_SC016 → HSK5_SC017 continuity approved.
- HSK5_SC018: **5/5 canonical West Lake composite visual complete**; HSK5_SC014 accepted West Lake + Zhang Wei/Chen Yu 5'li composite seti birebir yeniden kullanıldı; HSK5_SC017 → HSK5_SC018 continuity approved.
- HSK5_SC019: **5/5 canonical Train Station visual complete**; HSK2_SC033 accepted Train Station 5'li seti birebir yeniden kullanıldı; HSK5_SC018 → HSK5_SC019 continuity approved.
- HSK5_SC020: **5/5 canonical Day Trip Town visual complete**; HSK4_SC035 accepted Day Trip Town composite 5'li seti birebir yeniden kullanıldı; HSK5_SC019 → HSK5_SC020 continuity approved.
- HSK5_SC011–SC020 second batch: **10/10 visual complete, 10/10 audio complete, 10/10 accepted; physical closure audit PASSED (each scene 5 WebP + 54 M4A)**.
- HSK5_SC021: **5/5 canonical Zhang Home visual complete**; HSK3_SC021 accepted Zhang Home 5'li seti birebir yeniden kullanıldı; HSK5_SC020 → HSK5_SC021 continuity approved.
- HSK5_SC022: **5/5 canonical City Park visual complete**; HSK4_SC022 accepted City Park seti reuse edildi ve visual manifest canonical Li Na face_right + Zhang Wei face_left orientation’a hizalandı; HSK5_SC021 → HSK5_SC022 continuity approved.
- HSK5_SC023: **5/5 canonical Public Library visual complete**; HSK2_SC023 accepted Public Library 5'li seti birebir yeniden kullanıldı; HSK5_SC022 → HSK5_SC023 continuity approved.
- HSK5_SC024: **5/5 canonical Zhang Home visual complete**; HSK2_SC049 accepted Zhang Home 5'li seti birebir yeniden kullanıldı; HSK5_SC023 → HSK5_SC024 continuity approved.
- HSK5_SC025: **5/5 canonical City Park visual complete**; HSK2_SC020 accepted City Park 5'li seti birebir yeniden kullanıldı; HSK5_SC024 → HSK5_SC025 continuity approved.
- HSK5_SC026: **5/5 canonical Public Library visual complete**; HSK2_SC023 accepted Public Library seti reuse edildi ve visual manifest canonical Li Na face_right + Zhang Wei face_left orientation’a hizalandı; HSK5_SC025 → HSK5_SC026 continuity approved.
- HSK5_SC027: **5/5 canonical Zhang Home visual complete**; HSK3_SC021 accepted Zhang Home 5'li seti birebir yeniden kullanıldı; HSK5_SC026 → HSK5_SC027 continuity approved.
- HSK5_SC028: **5/5 canonical City Park visual complete**; HSK2_SC020 accepted City Park seti reuse edildi ve visual manifest canonical Li Na face_right + Zhang Wei face_left orientation’a hizalandı; HSK5_SC027 → HSK5_SC028 continuity approved.
- HSK5_SC029: **5/5 canonical Public Library visual complete**; HSK2_SC023 accepted Public Library 5'li seti birebir yeniden kullanıldı; HSK5_SC028 → HSK5_SC029 continuity approved.
- HSK5_SC030: **5/5 canonical Zhang Home visual complete**; HSK2_SC049 accepted Zhang Home 5'li seti birebir yeniden kullanıldı; HSK5_SC029 → HSK5_SC030 continuity approved.
- HSK5_SC021–SC030 third batch: **10/10 visual complete, 10/10 audio complete, 10/10 accepted; physical closure audit PASSED (each scene 5 WebP + 54 M4A)**.
- **Aktif Adım: Adım 14 — HSK5'i 50/50 Tamamla**
- **Anlık iş kalemi: HSK5_SC031 için manifest + dialogue + continuity kontrolünü yapıp BG → CHAR_A → CHAR_B → FG → PREVIEW → repo → validation zincirini yürüt. Actions tüketmemek için [skip ci] kullan.**
