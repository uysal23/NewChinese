# incoming_assets

Bu klasör yalnızca geçici medya yüklemeleri içindir.

Desteklenen paket adları:

- `HSK1_SC001_visual_assets.zip`
- `HSK1_SC001_audio_assets.zip`

Aynı adlandırma HSK1-HSK6 ve SC001-SC050 için kullanılabilir.

## Kullanım

ZIP dosyasını bu klasöre yükleyin. GitHub Actions otomatik olarak:

1. Scene ID'yi ZIP adından okur.
2. Dosyaları ilgili `content/hskX/scYYY/assets/` klasörüne çıkarır.
3. `media_status.json` içindeki fiziksel asset listesini doğrular.
4. Yalnızca tüm gerekli dosyalar mevcutsa ilgili medya kategorisini `complete` yapar.
5. Validation çalıştırır.
6. Değişiklikleri repoya commit eder.
7. İşlenen ZIP dosyasını `incoming_assets/` klasöründen siler.

Kilitli proje yapısı ve mevcut içerik dosyaları bu işlem sırasında değiştirilmez.


## 10-sahnelik batch ZIP

Tek tek 10 ZIP yüklemek yerine şu adlandırma desteklenir:

- `HSK2_SC011_SC020_visual_assets_batch.zip`
- aynı kalıp diğer HSK seviyeleri, sahne aralıkları ve audio paketleri için de kullanılabilir.

Batch ZIP'in içinde her sahne için normal importer adını taşıyan ZIP bulunmalıdır:
`HSK2_SC011_visual_assets.zip` ... `HSK2_SC020_visual_assets.zip`.

Workflow batch ZIP'i açar, 10 scene paketini doğrular, ilgili sahnelere import eder, media status değerlerini günceller ve validation çalıştırır.
