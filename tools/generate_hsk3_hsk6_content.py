from __future__ import annotations

import json
import re
from pathlib import Path

from pypinyin import Style, lazy_pinyin

ROOT = Path(__file__).resolve().parents[1]
# Trigger marker: complete HSK3-HSK6 nonvisual production

CHARACTERS = {
    "CHAR_ZHANG_WEI_001": {"voice": "VOICE_ZHANG_WEI_001", "slug": "zhang_wei", "name": "Zhang Wei"},
    "CHAR_LI_NA_001": {"voice": "VOICE_LI_NA_001", "slug": "li_na", "name": "Li Na"},
    "CHAR_WANG_MING_001": {"voice": "VOICE_WANG_MING_001", "slug": "wang_ming", "name": "Wang Ming"},
    "CHAR_CHEN_YU_001": {"voice": "VOICE_CHEN_YU_001", "slug": "chen_yu", "name": "Chen Yu"},
    "CHAR_LIU_MEI_001": {"voice": "VOICE_LIU_MEI_001", "slug": "liu_mei", "name": "Liu Mei"},
}

VOICE_ENGINE = {
    "VOICE_LI_NA_001": "zh-CN-XiaoxiaoNeural",
    "VOICE_ZHANG_WEI_001": "zh-CN-YunxiNeural",
    "VOICE_WANG_MING_001": "zh-CN-YunjianNeural",
    "VOICE_CHEN_YU_001": "zh-CN-YunyangNeural",
    "VOICE_LIU_MEI_001": "zh-CN-XiaoyiNeural",
}

OUTFITS = {
    "CHAR_ZHANG_WEI_001": "OUTFIT_ZHANG_WEI_CASUAL_01",
    "CHAR_LI_NA_001": "OUTFIT_LI_NA_CASUAL_01",
    "CHAR_WANG_MING_001": "OUTFIT_WANG_MING_WORK_01",
    "CHAR_CHEN_YU_001": "OUTFIT_CHEN_YU_CASUAL_01",
    "CHAR_LIU_MEI_001": "OUTFIT_LIU_MEI_CASUAL_01",
}

TOPICS = {
    3: [
        ("接手新任务","Yeni bir görevi devralmak"),("安排一天的工作","Günün işlerini planlamak"),
        ("确认截止时间","Son tarihi netleştirmek"),("和同事分工","İş bölümü yapmak"),
        ("处理临时变化","Ani değişikliklerle başa çıkmak"),("向同事求助","Bir iş arkadaşından yardım istemek"),
        ("汇报工作进度","İş ilerlemesini raporlamak"),("检查细节","Ayrıntıları kontrol etmek"),
        ("解决小误会","Küçük bir yanlış anlaşılmayı çözmek"),("一周工作回顾","Haftalık iş değerlendirmesi"),
        ("制定学习计划","Öğrenme planı yapmak"),("利用通勤时间","Yolculuk süresini değerlendirmek"),
        ("记录新表达","Yeni ifadeleri not etmek"),("练习听力","Dinleme çalışmak"),
        ("练习复述","Yeniden anlatma çalışması"),("从错误中学习","Hatalardan öğrenmek"),
        ("选择学习材料","Öğrenme materyali seçmek"),("和朋友练中文","Arkadaşla Çince pratik yapmak"),
        ("复习旧内容","Eski konuları tekrar etmek"),("调整学习方法","Öğrenme yöntemini ayarlamak"),
        ("家里设备出问题","Evdeki bir cihaz sorun çıkardığında"),("预约维修","Tamir randevusu almak"),
        ("快递没按时到","Kargo zamanında gelmediğinde"),("更改出行计划","Ulaşım planını değiştirmek"),
        ("去医院前准备","Hastaneye gitmeden önce hazırlanmak"),("处理银行小事","Bankadaki küçük bir işi halletmek"),
        ("解决网络问题","İnternet sorununu çözmek"),("邻里沟通","Komşularla iletişim"),
        ("安排家庭预算","Ev bütçesini planlamak"),("周末事情太多","Hafta sonu çok iş olduğunda"),
        ("参加摄影活动","Fotoğraf etkinliğine katılmak"),("和朋友徒步","Arkadaşlarla yürüyüş yapmak"),
        ("一起做饭","Birlikte yemek yapmak"),("读书会讨论","Kitap kulübünde tartışmak"),
        ("第一次桌游","İlk masa oyunu buluşması"),("计划短途骑行","Kısa bisiklet gezisi planlamak"),
        ("拍城市夜景","Şehir gece fotoğrafları çekmek"),("邀请朋友来家里","Arkadaşları eve davet etmek"),
        ("认识新成员","Yeni biriyle tanışmak"),("平衡社交和休息","Sosyallik ile dinlenmeyi dengelemek"),
        ("整理数字文件","Dijital dosyaları düzenlemek"),("尝试新应用","Yeni bir uygulama denemek"),
        ("给照片分类","Fotoğrafları sınıflandırmak"),("做一个小项目","Küçük bir proje yapmak"),
        ("记录项目进度","Proje ilerlemesini kaydetmek"),("和王明讨论工具","Wang Ming ile araçları tartışmak"),
        ("解决同步问题","Senkronizasyon sorununu çözmek"),("分享项目结果","Proje sonucunu paylaşmak"),
        ("总结技术经验","Teknik deneyimi özetlemek"),("准备进入HSK4","HSK4'e hazırlanmak"),
    ],
    4: [
        ("承担更重要的任务","Daha önemli bir görev üstlenmek"),("准备团队会议","Takım toplantısına hazırlanmak"),
        ("解释项目风险","Proje risklerini açıklamak"),("讨论优先顺序","Öncelikleri tartışmak"),
        ("面对进度压力","Takvim baskısıyla başa çıkmak"),("提出改进建议","İyileştirme önerisi sunmak"),
        ("和同事协调资源","Kaynakları iş arkadaşlarıyla koordine etmek"),("处理不同意见","Farklı görüşleri ele almak"),
        ("向负责人汇报","Sorumlu kişiye rapor vermek"),("阶段性复盘","Ara değerlendirme yapmak"),
        ("新同事加入","Yeni bir ekip arkadaşının katılması"),("如何给反馈","Nasıl geri bildirim verilir"),
        ("接受批评","Eleştiriyi kabul etmek"),("跨部门沟通","Birimler arası iletişim"),
        ("避免信息误解","Bilgi yanlış anlaşılmalarını önlemek"),("帮助同事但不过度","İş arkadaşına ölçülü yardım etmek"),
        ("讨论责任边界","Sorumluluk sınırlarını konuşmak"),("处理会议冲突","Toplantı çatışmasını çözmek"),
        ("建立信任","Güven oluşturmak"),("团队关系回顾","Takım ilişkilerini değerlendirmek"),
        ("工作后真正休息","İşten sonra gerçekten dinlenmek"),("减少无效加班","Gereksiz mesaiyi azaltmak"),
        ("安排运动时间","Egzersiz için zaman ayırmak"),("照顾睡眠","Uykuya özen göstermek"),
        ("周末不看工作消息","Hafta sonu iş mesajlarına bakmamak"),("和李娜讨论压力","Li Na ile stresi konuşmak"),
        ("学会拒绝不合理安排","Makul olmayan planlara hayır demek"),("恢复专注","Odaklanmayı geri kazanmak"),
        ("给自己留空白","Kendine boş zaman bırakmak"),("重新定义效率","Verimliliği yeniden tanımlamak"),
        ("第一次看地方戏","İlk kez yerel tiyatro izlemek"),("参观传统街区","Geleneksel mahalleyi gezmek"),
        ("讨论节日变化","Bayramların değişimini konuşmak"),("学习茶文化","Çay kültürünü öğrenmek"),
        ("博物馆里的故事","Müzedeki hikâyeler"),("老建筑与新城市","Eski yapılar ve yeni şehir"),
        ("和陈宇聊摄影","Chen Yu ile fotoğrafçılık konuşmak"),("社区文化活动","Toplum kültür etkinliği"),
        ("年轻人的生活方式","Gençlerin yaşam biçimi"),("传统与现代","Gelenek ve modernlik"),
        ("确定共同项目","Ortak projeyi belirlemek"),("明确目标","Hedefi netleştirmek"),
        ("分配任务","Görev dağıtmak"),("第一次原型","İlk prototip"),
        ("收集反馈","Geri bildirim toplamak"),("修改方案","Planı revize etmek"),
        ("遇到分歧","Görüş ayrılığı yaşamak"),("决定取舍","Öncelik ve fedakârlık kararı vermek"),
        ("公开展示","Herkese açık sunum yapmak"),("HSK4项目总结","HSK4 projesini değerlendirmek"),
    ],
    5: [
        ("人工智能进入日常工作","Yapay zekânın günlük işe girmesi"),("自动化与人的判断","Otomasyon ve insan yargısı"),
        ("数据隐私","Veri gizliliği"),("数字工具依赖","Dijital araç bağımlılığı"),
        ("远程协作","Uzaktan işbirliği"),("信息过载","Bilgi aşırı yükü"),
        ("推荐算法","Öneri algoritmaları"),("技术更新太快","Teknolojinin çok hızlı değişmesi"),
        ("选择合适工具","Doğru aracı seçmek"),("技术与生活边界","Teknoloji ile yaşam arasındaki sınır"),
        ("慢旅行","Yavaş seyahat"),("计划外的风景","Plansız karşılaşılan manzaralar"),
        ("旅行中的文化差异","Seyahatte kültürel farklılıklar"),("独自旅行的能力","Tek başına seyahat edebilme"),
        ("和当地人交流","Yerel insanlarla iletişim"),("预算与体验","Bütçe ve deneyim"),
        ("旅行中的环保选择","Seyahatte çevreci tercihler"),("照片之外的记忆","Fotoğrafların ötesindeki anılar"),
        ("回到熟悉城市","Tanıdık şehre dönmek"),("旅行改变了什么","Seyahat neyi değiştirdi"),
        ("长期目标","Uzun vadeli hedefler"),("面对失败","Başarısızlıkla yüzleşmek"),
        ("保持好奇心","Merakı korumak"),("建立稳定习惯","Kalıcı alışkanlık kurmak"),
        ("比较与焦虑","Karşılaştırma ve kaygı"),("接受不完美","Kusursuz olmamayı kabul etmek"),
        ("如何做决定","Nasıl karar verilir"),("恢复行动力","Harekete geçme gücünü geri kazanmak"),
        ("学习耐心","Sabırlı olmayı öğrenmek"),("重新认识自己","Kendini yeniden tanımak"),
        ("社区公共空间","Mahalledeki kamusal alanlar"),("城市里的孤独","Şehirde yalnızlık"),
        ("年轻人的住房压力","Gençlerin konut baskısı"),("工作与家庭责任","İş ve aile sorumlulukları"),
        ("公共交通体验","Toplu taşıma deneyimi"),("志愿活动","Gönüllülük"),
        ("代际沟通","Kuşaklar arası iletişim"),("消费选择","Tüketim tercihleri"),
        ("网络舆论","Çevrimiçi kamuoyu"),("城市生活质量","Şehirde yaşam kalitesi"),
        ("复杂项目启动","Karmaşık bir projeyi başlatmak"),("资源有限怎么办","Kaynaklar sınırlıyken ne yapılır"),
        ("团队中的领导力","Takımda liderlik"),("让不同专业合作","Farklı uzmanlıkları birlikte çalıştırmak"),
        ("风险与机会","Risk ve fırsat"),("长期规划","Uzun vadeli planlama"),
        ("培养新人","Yeni çalışanları geliştirmek"),("决定停止一个方案","Bir planı durdurma kararı"),
        ("未来三年的方向","Önümüzdeki üç yılın yönü"),("HSK5阶段总结","HSK5 aşamasını değerlendirmek"),
    ],
    6: [
        ("工作只是工作吗","İş sadece iş midir"),("职业成就与生活价值","Kariyer başarısı ve yaşam değeri"),
        ("选择稳定还是变化","İstikrar mı değişim mi"),("能力与身份","Yetenek ve kimlik"),
        ("长期职业倦怠","Uzun süreli mesleki tükenmişlik"),("重新定义成功","Başarıyı yeniden tanımlamak"),
        ("承担更大的责任","Daha büyük sorumluluk almak"),("专业判断与道德","Mesleki yargı ve etik"),
        ("什么时候应该离开","Ne zaman ayrılmak gerekir"),("职业道路回顾","Kariyer yolunu geriye dönük değerlendirmek"),
        ("成年人为什么还要学习","Yetişkinler neden öğrenmeye devam eder"),("知识与经验","Bilgi ve deneyim"),
        ("深度学习与碎片信息","Derin öğrenme ve parçalı bilgi"),("如何提出好问题","İyi soru nasıl sorulur"),
        ("跨领域学习","Alanlar arası öğrenme"),("教育公平","Eğitimde fırsat eşitliği"),
        ("考试与真实能力","Sınav ve gerçek yeterlilik"),("老师和学习者的角色","Öğretmen ve öğrenenin rolü"),
        ("终身学习的成本","Yaşam boyu öğrenmenin maliyeti"),("学习方式的未来","Öğrenmenin geleceği"),
        ("城市记忆如何保存","Şehir hafızası nasıl korunur"),("文化商业化","Kültürün ticarileşmesi"),
        ("公共讨论中的耐心","Kamusal tartışmada sabır"),("不同价值观共处","Farklı değerlerle birlikte yaşamak"),
        ("家庭结构变化","Aile yapısının değişimi"),("人口流动","Nüfus hareketliliği"),
        ("科技改变人际关系","Teknolojinin ilişkileri değiştirmesi"),("社区归属感","Topluluk aidiyeti"),
        ("传统是否必须保留","Gelenekler mutlaka korunmalı mı"),("理解而不是同意","Onaylamak yerine anlamak"),
        ("人工智能会替代什么","Yapay zekâ neyin yerini alacak"),("人类判断的价值","İnsan yargısının değeri"),
        ("自动化决策的责任","Otomatik kararların sorumluluğu"),("隐私与便利交换","Gizlilik ve kolaylık takası"),
        ("技术乐观与技术焦虑","Teknoloji iyimserliği ve kaygısı"),("虚拟世界与现实关系","Sanal dünya ve gerçek ilişkiler"),
        ("未来工作的形态","Geleceğin çalışma biçimi"),("数字身份","Dijital kimlik"),
        ("不确定性下的决策","Belirsizlik altında karar vermek"),("我们希望怎样的未来","Nasıl bir gelecek istiyoruz"),
        ("回看六个阶段","Altı aşamaya geriye bakmak"),("语言改变了生活什么","Dil yaşamda neyi değiştirdi"),
        ("从陌生到熟悉","Yabancılıktan aşinalığa"),("关系如何慢慢建立","İlişkiler nasıl yavaşça kurulur"),
        ("失败留下的东西","Başarısızlığın bıraktıkları"),("选择留下还是出发","Kalmak mı yola çıkmak mı"),
        ("给过去的自己一句话","Geçmişteki kendine bir cümle"),("未来仍然会犯错","Gelecekte de hata yapmak"),
        ("新的计划不必完美","Yeni planların kusursuz olması gerekmez"),("故事没有真正结束","Hikâye gerçekten bitmez"),
    ],
}

LEVEL_CONFIG = {
    3: {"pairs": 12, "vocab": 10, "sentence": 8, "minutes": 14},
    4: {"pairs": 14, "vocab": 10, "sentence": 8, "minutes": 16},
    5: {"pairs": 16, "vocab": 12, "sentence": 10, "minutes": 18},
    6: {"pairs": 18, "vocab": 12, "sentence": 10, "minutes": 20},
}

VOCAB = {
    3: [("责任","sorumluluk"),("计划","plan"),("调整","ayarlamak"),("经验","deneyim"),("沟通","iletişim"),
        ("解决","çözmek"),("习惯","alışkanlık"),("进步","ilerleme"),("影响","etki"),("选择","seçim")],
    4: [("协调","koordine etmek"),("观点","görüş"),("责任","sorumluluk"),("边界","sınır"),("效率","verimlilik"),
        ("压力","baskı"),("信任","güven"),("反馈","geri bildirim"),("优先","öncelik"),("改进","iyileştirmek")],
    5: [("趋势","eğilim"),("价值","değer"),("判断","yargı"),("资源","kaynak"),("策略","strateji"),("平衡","denge"),
        ("风险","risk"),("机会","fırsat"),("长期","uzun vadeli"),("影响","etki"),("选择","seçim"),("责任","sorumluluk")],
    6: [("意义","anlam"),("立场","tutum"),("机制","mekanizma"),("假设","varsayım"),("权衡","dengeleyerek değerlendirmek"),
        ("边界","sınır"),("责任","sorumluluk"),("反思","öz değerlendirme"),("长期","uzun vadeli"),("不确定性","belirsizlik"),
        ("可能性","olasılık"),("判断","yargı")],
}

DIALOGUE_PAIRS = {
    3: [
        ("今天想和你聊聊“{topic}”。这件事最近对我影响挺大。","Bugün seninle “{topic_tr}” hakkında konuşmak istiyorum. Son zamanlarda bu konu beni epey etkiliyor.",
         "可以。你现在最想解决的是什么？","Olur. Şu anda en çok neyi çözmek istiyorsun?"),
        ("我先做了一个计划，可实际情况和我想的不太一样。","Önce bir plan yaptım ama gerçek durum düşündüğüm gibi olmadı.",
         "计划本来就需要调整，先看最重要的部分。","Planların zaten ayarlanması gerekir; önce en önemli kısma bakalım."),
        ("我发现自己有时候太快做选择，没有先问清楚。","Bazen yeterince sormadan çok hızlı seçim yaptığımı fark ettim.",
         "这和经验有关，多做几次会更稳。","Bu deneyimle ilgili; birkaç kez daha yapınca daha sağlam olursun."),
        ("还有沟通的问题，我怕说得太直接。","Bir de iletişim konusu var; fazla doğrudan konuşmaktan çekiniyorum.",
         "把原因说明白，再听听对方的想法，通常会好很多。","Nedenini açıkça anlatıp karşı tarafı dinlersen genelde çok daha iyi olur."),
        ("那我的责任应该到哪里？","Peki benim sorumluluğum nerede bitmeli?",
         "先把自己负责的部分做好，遇到不确定的地方及时确认。","Önce kendi sorumluluğundaki kısmı iyi yap; emin olmadığın noktaları zamanında netleştir."),
        ("我想把这件事变成一个稳定的习惯。","Bunu kalıcı bir alışkanlığa dönüştürmek istiyorum.",
         "那就从小一点的动作开始，不用一次做得太多。","O zaman küçük bir adımla başla; bir seferde çok fazla yapmana gerek yok."),
        ("如果中间又出问题呢？","Arada yine sorun çıkarsa?",
         "先解决眼前的问题，再记录原因，下次就会更快。","Önce önündeki sorunu çöz, sonra nedenini not et; bir dahaki sefere daha hızlı olursun."),
        ("这样做真的能看到进步吗？","Böyle yapınca gerçekten ilerleme görülebilir mi?",
         "能。你可以每周回头看一次，会发现变化比想象中明显。","Evet. Haftada bir geriye bakarsan değişimin düşündüğünden daha belirgin olduğunu görürsün."),
        ("我以前总觉得一定要有最好的方法。","Eskiden mutlaka en iyi yöntemi bulmam gerektiğini düşünürdüm.",
         "其实适合现在情况的方法更重要。","Aslında şu anki duruma uygun yöntem daha önemli."),
        ("听起来我应该少一点着急，多一点观察。","Anlaşılan biraz daha az acele edip biraz daha çok gözlemlemeliyim.",
         "对，尤其是“{topic}”这种事，过程本身也很重要。","Evet, özellikle “{topic_tr}” gibi konularda sürecin kendisi de önemli."),
        ("那我今天先做一个具体的小步骤。","O halde bugün somut bir küçük adım atacağım.",
         "很好，做完以后再决定下一步，不需要一次想完。","Harika. Bitirdikten sonra sonraki adımı belirlersin; her şeyi bir kerede düşünmek zorunda değilsin."),
        ("谢谢，我现在思路清楚多了。","Teşekkürler, şimdi kafam çok daha net.",
         "不客气。过几天我们再看看效果。","Rica ederim. Birkaç gün sonra sonucu yine değerlendiririz."),
    ],
    4: [
        ("关于“{topic}”，我发现大家的观点不太一样。","“{topic_tr}” konusunda herkesin görüşünün biraz farklı olduğunu fark ettim.",
         "这很正常。先把共同目标和优先顺序说清楚。","Bu normal. Önce ortak hedefi ve öncelikleri netleştirmek gerekir."),
        ("我担心协调的时候会让人觉得我在指挥别人。","Koordinasyon yaparken insanlara emir veriyormuşum gibi görünmekten endişeliyim.",
         "你可以先说明责任和限制，再邀请大家补充。","Önce sorumlulukları ve sınırlamaları anlatıp sonra herkesten katkı isteyebilirsin."),
        ("如果有人不同意我的方案呢？","Biri önerime katılmazsa ne olacak?",
         "先问他担心什么。不同意见有时能帮助我们改进方案。","Önce neye kaygı duyduğunu sor. Farklı görüşler bazen planı iyileştirmemize yardım eder."),
        ("我以前一有压力就想快点决定。","Eskiden baskı hissedince hemen karar vermek isterdim.",
         "越有压力，越需要把事实和判断分开。","Baskı arttıkça olguları yorumlardan ayırmak daha da önemli olur."),
        ("效率和质量有时候会冲突。","Verimlilik ile kalite bazen çatışıyor.",
         "那就明确什么必须做好，什么可以先做到够用。","O zaman neyin mutlaka iyi yapılması gerektiğini ve neyin şimdilik yeterli olabileceğini netleştir."),
        ("我也想给同事真实的反馈，但怕伤人。","İş arkadaşıma gerçek geri bildirim vermek istiyorum ama kırmaktan çekiniyorum.",
         "针对事情，不针对人；说具体情况，也说你希望怎么改。","Kişiye değil konuya odaklan; somut durumu ve nasıl iyileşmesini istediğini anlat."),
        ("这样是不是更容易建立信任？","Bu güven oluşturmayı kolaylaştırır mı?",
         "对。稳定、透明的沟通比偶尔说好听的话更有用。","Evet. Tutarlı ve şeffaf iletişim ara sıra hoş sözler söylemekten daha yararlıdır."),
        ("责任边界怎么判断？","Sorumluluk sınırı nasıl belirlenir?",
         "看角色、承诺和实际能力，超出边界的事情要及时说明。","Rol, verilen söz ve gerçek kapasiteye bakılır; sınırı aşan işler zamanında belirtilmelidir."),
        ("如果会议里出现冲突呢？","Toplantıda çatışma çıkarsa?",
         "先让每个人把理由说完整，再找真正的分歧点。","Önce herkesin gerekçesini tamamlamasına izin ver, sonra gerçek ayrılık noktasını bul."),
        ("我发现很多误会其实来自信息不完整。","Birçok yanlış anlaşılmanın eksik bilgiden kaynaklandığını fark ettim.",
         "所以重要决定最好留下一段清楚的记录。","Bu yüzden önemli kararlar için açık bir kayıt bırakmak iyi olur."),
        ("这和“{topic}”也有关系。","Bu da “{topic_tr}” konusuyla bağlantılı.",
         "对，把问题放回真实情境里，判断会更准确。","Evet. Sorunu gerçek bağlamına yerleştirince yargı daha isabetli olur."),
        ("那我先把优先顺序和责任写下来。","O halde önce öncelikleri ve sorumlulukları yazacağım.",
         "然后跟相关的人确认一次，这一步很关键。","Sonra ilgili kişilerle bir kez doğrula; bu adım çok önemli."),
        ("我希望最后不是只完成任务，也能让合作更顺。","Sonunda yalnızca görevi bitirmek değil, işbirliğini de daha akıcı hale getirmek istiyorum.",
         "这就是成熟的协调：结果和关系都要照顾。","Olgun koordinasyon tam da budur: hem sonuç hem ilişki gözetilir."),
        ("好，我觉得现在有一个比较清楚的方向了。","Tamam, şimdi daha net bir yön görüyorum.",
         "那就按这个方向试一次，再根据反馈调整。","O halde bu yönde bir kez dene, sonra geri bildirime göre ayarla."),
    ],
    5: [
        ("最近我在想“{topic}”，感觉背后不只是一个简单问题。","Son zamanlarda “{topic_tr}” üzerine düşünüyorum; arkasında tek bir basit sorun olmadığını hissediyorum.",
         "对，这类问题通常同时涉及价值、资源和长期影响。","Evet, bu tür konular genellikle değerleri, kaynakları ve uzun vadeli etkileri birlikte içerir."),
        ("如果只看眼前效率，可能会忽略别的东西。","Yalnızca kısa vadeli verimliliğe bakarsak başka şeyleri gözden kaçırabiliriz.",
         "所以需要平衡，不是把一个指标做到最高就结束。","Bu yüzden denge gerekir; tek bir göstergenin en yükseğe çıkarılmasıyla iş bitmez."),
        ("我发现趋势很重要，但趋势也可能让人跟风。","Eğilimlerin önemli olduğunu ama insanı sürükleyebildiğini fark ettim.",
         "没错。趋势可以参考，判断还是要回到自己的目标。","Doğru. Eğilimler referans olabilir ama yargı kendi hedeflerine dönmelidir."),
        ("那选择工具或方案时，应该先看什么？","Bir araç ya da plan seçerken önce neye bakmalıyız?",
         "先看问题本身，再看资源、成本和实际使用条件。","Önce sorunun kendisine, sonra kaynaklara, maliyete ve gerçek kullanım koşullarına bakmalıyız."),
        ("有时候机会和风险会同时出现。","Bazen fırsat ile risk aynı anda ortaya çıkıyor.",
         "这很常见。成熟的策略不是消除所有风险，而是知道能承担多少。","Bu çok yaygın. Olgun strateji tüm riskleri yok etmek değil, ne kadarının taşınabileceğini bilmektir."),
        ("责任又该怎么分？","Peki sorumluluk nasıl paylaşılmalı?",
         "谁做决定、谁掌握信息、谁承担后果，都应该说清楚。","Kararı kimin verdiği, bilgiyi kimin tuttuğu ve sonucu kimin üstleneceği net olmalı."),
        ("我以前常常把短期结果看得太重。","Eskiden kısa vadeli sonuçlara fazla ağırlık verirdim.",
         "长期来看，稳定的方法和可持续的节奏更有价值。","Uzun vadede istikrarlı yöntem ve sürdürülebilir tempo daha değerlidir."),
        ("如果信息不完整，还能做判断吗？","Bilgi eksikken yine de yargıda bulunabilir miyiz?",
         "可以，但要承认不确定性，也要准备根据新信息调整。","Evet, fakat belirsizliği kabul etmeli ve yeni bilgi geldikçe ayarlamaya hazır olmalıyız."),
        ("这和“{topic}”特别相关。","Bu özellikle “{topic_tr}” ile ilgili.",
         "是，因为这里的影响可能比表面看到的更广。","Evet, çünkü buradaki etki görünenden daha geniş olabilir."),
        ("我们是不是也要考虑没有被直接听见的人？","Doğrudan sesi duyulmayan insanları da düşünmeli miyiz?",
         "当然。做公共或团队选择时，这一点很重要。","Elbette. Kamusal ya da ekip kararlarında bu çok önemlidir."),
        ("如果大家的价值判断不同呢？","İnsanların değer yargıları farklıysa ne olacak?",
         "先找共同底线，再把分歧保留下来，不一定马上统一。","Önce ortak alt sınırı bulur, ayrılıkları koruruz; hemen aynı görüşe gelmek gerekmez."),
        ("听起来好的策略需要留出变化空间。","Anlaşılan iyi bir strateji değişime alan bırakmalı.",
         "对，过度确定反而可能让系统失去适应能力。","Evet, aşırı kesinlik sistemi uyum sağlama yeteneğinden mahrum bırakabilir."),
        ("那我想先列出目标、资源、风险和机会。","O halde önce hedefleri, kaynakları, riskleri ve fırsatları listelemek istiyorum.",
         "再写下你最看重的价值，判断会更透明。","Sonra en çok önem verdiğin değerleri yaz; yargın daha şeffaf olur."),
        ("我也想记录为什么做这个选择。","Bu seçimi neden yaptığımı da kaydetmek istiyorum.",
         "很好。以后回看时，你能分清结果不好和判断过程不好。","Harika. Sonra geriye baktığında kötü sonuç ile kötü karar sürecini ayırt edebilirsin."),
        ("这样讨论以后，我对“{topic}”没有那么简单化了。","Bu konuşmadan sonra “{topic_tr}” konusunu artık o kadar basite indirgemiyorum.",
         "这就是进步：不是答案更多，而是看问题的角度更完整。","İlerleme tam da budur: daha çok cevap değil, probleme daha bütünlüklü bakabilmek."),
        ("好，我们先按这个框架继续观察。","Tamam, bu çerçeveyle gözlemlemeye devam edelim.",
         "可以，等有新信息时再更新判断。","Olur; yeni bilgi geldiğinde yargımızı güncelleriz."),
    ],
    6: [
        ("最近我反复想到“{topic}”，越想越觉得它和个人选择、社会环境都有关系。","Son zamanlarda “{topic_tr}” üzerine tekrar tekrar düşünüyorum; düşündükçe hem kişisel tercihlerle hem toplumsal ortamla bağlantılı olduğunu görüyorum.",
         "这类问题很难靠单一立场解释，先把不同层次分开会更清楚。","Bu tür konular tek bir tutumla açıklanamaz; farklı katmanları ayırmak daha netlik sağlar."),
        ("我以前总想先找到一个明确答案，现在反而更能接受不确定性。","Eskiden önce kesin bir cevap bulmak isterdim; şimdi belirsizliği daha çok kabul edebiliyorum.",
         "接受不确定性不等于放弃判断，而是知道判断建立在什么假设上。","Belirsizliği kabul etmek yargıdan vazgeçmek değil, yargının hangi varsayımlara dayandığını bilmektir."),
        ("如果假设本身有问题，后面的结论可能都不稳。","Varsayımın kendisi sorunluysa sonraki sonuçların tamamı zayıf olabilir.",
         "所以成熟的讨论会主动检查假设，而不是只争结论。","Bu yüzden olgun tartışma yalnız sonuçları değil, varsayımları da inceler."),
        ("不同立场之间有没有真正的共同点？","Farklı tutumlar arasında gerçekten ortak nokta olabilir mi?",
         "通常有。比如安全、公平、尊重和长期可持续性，只是权衡方式不同。","Genellikle vardır. Güvenlik, adalet, saygı ve uzun vadeli sürdürülebilirlik gibi; yalnızca dengeleme biçimleri farklıdır."),
        ("权衡最难的地方是什么？","Dengeleyerek karar vermenin en zor yanı nedir?",
         "是承认每个选择都有代价，而且代价可能由不同的人承担。","Her seçimin bir bedeli olduğunu ve bu bedeli farklı insanların taşıyabileceğini kabul etmektir."),
        ("这就涉及责任了。","Bu da sorumluluk meselesine giriyor.",
         "对。责任不仅是做错以后负责，也包括决定之前认真考虑影响。","Evet. Sorumluluk yalnız hata sonrası hesap vermek değil, karar öncesi etkileri ciddi biçimde düşünmektir."),
        ("边界应该怎么设定？","Sınırlar nasıl belirlenmeli?",
         "边界需要明确目的、适用范围和例外，否则容易变成空话。","Sınırların amacı, kapsamı ve istisnaları net olmalı; yoksa boş söze dönüşür."),
        ("有些机制设计得很好，现实中还是会出现意外。","Bazı mekanizmalar iyi tasarlansa bile gerçekte beklenmedik sonuçlar çıkabiliyor.",
         "因为机制会遇到人的行为、资源限制和新的环境变化。","Çünkü mekanizmalar insan davranışı, kaynak sınırlamaları ve yeni çevresel değişimlerle karşılaşır."),
        ("所以不能只看制度文本，还要看实际运行。","O halde yalnız kurala değil, gerçek işleyişe de bakmak gerekir.",
         "没错，长期效果往往和最初设计不完全一样。","Aynen; uzun vadeli sonuçlar çoğu zaman ilk tasarımla tam aynı olmaz."),
        ("我觉得“{topic}”最有意义的地方，是它逼着我们重新问问题。","Bence “{topic_tr}” konusunun en anlamlı yanı bizi soruları yeniden sormaya zorlaması.",
         "而且有些问题没有一次性的答案，只能持续反思。","Üstelik bazı soruların tek seferlik cevabı yoktur; sürekli öz değerlendirme gerekir."),
        ("反思会不会让人变得犹豫？","Öz değerlendirme insanı kararsız yapmaz mı?",
         "如果没有行动标准会。但好的反思应该让下一次判断更清楚。","Eylem ölçütü yoksa yapabilir. Ama iyi öz değerlendirme sonraki yargıyı daha net hale getirmelidir."),
        ("那我们需要什么样的判断标准？","Peki nasıl yargı ölçütlerine ihtiyacımız var?",
         "事实是否可靠、影响是否可接受、过程是否公平、结果能否长期维持。","Olguların güvenilirliği, etkinin kabul edilebilirliği, sürecin adilliği ve sonucun uzun vadede sürdürülebilirliği."),
        ("如果这些标准互相冲突呢？","Bu ölçütler birbiriyle çatışırsa?",
         "那就公开说明权衡，而不是假装没有代价。","O zaman ödünleşmeyi açıkça anlatmak gerekir; bedel yokmuş gibi davranmamak."),
        ("我越来越觉得透明本身也是一种责任。","Şeffaflığın kendisinin de bir sorumluluk olduğunu giderek daha çok düşünüyorum.",
         "是，尤其当一个决定会影响很多人时，解释过程很重要。","Evet, özellikle bir karar çok kişiyi etkiliyorsa süreci açıklamak önemlidir."),
        ("未来的可能性很多，我们很难预测得很准。","Gelecekte çok olasılık var; tam isabetli öngörmek zor.",
         "所以比预测唯一结果更重要的，是提高适应不同结果的能力。","Bu yüzden tek sonucu tahmin etmekten daha önemlisi farklı sonuçlara uyum kapasitesini artırmaktır."),
        ("这和不确定性下的决策很像。","Bu, belirsizlik altında karar vermeye çok benziyor.",
         "对。保留选择空间、设置检查点、允许修正，都是有用的方法。","Evet. Seçenek alanı bırakmak, kontrol noktaları koymak ve düzeltmeye izin vermek yararlı yöntemlerdir."),
        ("聊到这里，我对“{topic}”的看法比开始时更立体了。","Buraya geldiğimizde “{topic_tr}” konusuna bakışım başlangıca göre daha çok boyut kazandı.",
         "这就够了。复杂问题不一定要立刻得到简单结论。","Bu yeterli. Karmaşık konuların hemen basit bir sonuca bağlanması gerekmez."),
        ("那我们先保留这些问题，继续观察，也继续学习。","O halde bu soruları açık bırakalım; gözlemlemeye ve öğrenmeye devam edelim.",
         "好。真正重要的不是永远正确，而是愿意根据新证据修正自己。","Olur. Asıl önemli olan hep haklı olmak değil, yeni kanıtlara göre kendini düzeltebilmektir."),
    ],
}

ARC_META = {
    3: [
        (["LOC_ZHANG_OFFICE_001","LOC_OFFICE_BREAKROOM_001"], ["CHAR_WANG_MING_001"]),
        (["LOC_PUBLIC_LIBRARY_001","LOC_CAFE_001","LOC_ZHANG_HOME_001"], ["CHAR_LI_NA_001","CHAR_LIU_MEI_001"]),
        (["LOC_ZHANG_HOME_001","LOC_NEIGHBORHOOD_001","LOC_ELECTRONICS_STORE_001","LOC_PHARMACY_001"], ["CHAR_LI_NA_001"]),
        (["LOC_CITY_PARK_001","LOC_COMMUNITY_CENTER_001","LOC_CAFE_001","LOC_WEST_LAKE_001"], ["CHAR_CHEN_YU_001","CHAR_LIU_MEI_001"]),
        (["LOC_ZHANG_OFFICE_001","LOC_ELECTRONICS_STORE_001","LOC_PUBLIC_LIBRARY_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001","CHAR_CHEN_YU_001"]),
    ],
    4: [
        (["LOC_ZHANG_OFFICE_001","LOC_OFFICE_BREAKROOM_001"], ["CHAR_WANG_MING_001"]),
        (["LOC_ZHANG_OFFICE_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001","CHAR_LIU_MEI_001"]),
        (["LOC_ZHANG_HOME_001","LOC_CITY_PARK_001","LOC_WEST_LAKE_001"], ["CHAR_LI_NA_001"]),
        (["LOC_COMMUNITY_CENTER_001","LOC_DAY_TRIP_TOWN_001","LOC_PUBLIC_LIBRARY_001"], ["CHAR_CHEN_YU_001","CHAR_LIU_MEI_001"]),
        (["LOC_ZHANG_OFFICE_001","LOC_COMMUNITY_CENTER_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001","CHAR_CHEN_YU_001"]),
    ],
    5: [
        (["LOC_ZHANG_OFFICE_001","LOC_ELECTRONICS_STORE_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001","CHAR_CHEN_YU_001"]),
        (["LOC_TRAIN_STATION_001","LOC_DAY_TRIP_TOWN_001","LOC_GUESTHOUSE_001","LOC_WEST_LAKE_001"], ["CHAR_LI_NA_001","CHAR_CHEN_YU_001"]),
        (["LOC_ZHANG_HOME_001","LOC_CITY_PARK_001","LOC_PUBLIC_LIBRARY_001"], ["CHAR_LI_NA_001"]),
        (["LOC_COMMUNITY_CENTER_001","LOC_NEIGHBORHOOD_001","LOC_CITY_PARK_001"], ["CHAR_LIU_MEI_001","CHAR_CHEN_YU_001"]),
        (["LOC_ZHANG_OFFICE_001","LOC_OFFICE_BREAKROOM_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001"]),
    ],
    6: [
        (["LOC_ZHANG_OFFICE_001","LOC_WEST_LAKE_001","LOC_CAFE_001"], ["CHAR_WANG_MING_001"]),
        (["LOC_PUBLIC_LIBRARY_001","LOC_COMMUNITY_CENTER_001","LOC_ZHANG_HOME_001"], ["CHAR_LIU_MEI_001","CHAR_LI_NA_001"]),
        (["LOC_COMMUNITY_CENTER_001","LOC_WEST_LAKE_001","LOC_NEIGHBORHOOD_001"], ["CHAR_CHEN_YU_001","CHAR_LIU_MEI_001"]),
        (["LOC_ZHANG_OFFICE_001","LOC_ELECTRONICS_STORE_001","LOC_PUBLIC_LIBRARY_001"], ["CHAR_WANG_MING_001","CHAR_CHEN_YU_001"]),
        (["LOC_WEST_LAKE_001","LOC_ZHANG_HOME_001","LOC_CITY_PARK_001"], ["CHAR_LI_NA_001"]),
    ],
}

def dump(path: Path, obj: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

def pinyin(text: str) -> str:
    tokens = lazy_pinyin(text, style=Style.TONE, neutral_tone_with_five=False, errors=lambda x: list(x))
    result = " ".join(tokens)
    result = re.sub(r"\s+([，。？！；：、“”‘’（）])", r"\1", result)
    result = re.sub(r"([“‘（])\s+", r"\1", result)
    result = re.sub(r"\s+", " ", result).strip()
    return result

def chunks(text: str, size: int = 4) -> list[str]:
    clean = re.sub(r"[，。？！；：、“”‘’（）\s]", "", text)
    return [clean[i:i+size] for i in range(0, len(clean), size)] or [clean]

def scene_pair(level: int, scene_num: int) -> tuple[str, str, str]:
    block = (scene_num - 1) // 10
    within = (scene_num - 1) % 10
    locations, partners = ARC_META[level][block]
    partner = partners[within % len(partners)]
    location = locations[within % len(locations)]
    if scene_num % 2:
        return partner, "CHAR_ZHANG_WEI_001", location
    return "CHAR_ZHANG_WEI_001", partner, location

def dialogue_for(level: int, scene_num: int, topic: str, topic_tr: str, a: str, b: str) -> list[dict]:
    count = LEVEL_CONFIG[level]["pairs"]
    templates = DIALOGUE_PAIRS[level][:count]
    lines = []
    order = 1
    for azh, atr, bzh, btr in templates:
        for speaker, zh, tr in ((a, azh, atr), (b, bzh, btr)):
            zh = zh.format(topic=topic, topic_tr=topic_tr)
            tr = tr.format(topic=topic, topic_tr=topic_tr)
            line_id = f"HSK{level}_SC{scene_num:03d}_L{order:03d}"
            lines.append({
                "lineId": line_id,
                "order": order,
                "speakerId": speaker,
                "textZh": zh,
                "pinyin": pinyin(zh),
                "translationTr": tr,
                "voiceId": CHARACTERS[speaker]["voice"],
                "audioFile": f"assets/hsk{level}_sc{scene_num:03d}_line_{order:03d}.m4a",
                "emotion": "natural",
            })
            order += 1
    return lines

def make_scene(level: int, scene_num: int) -> None:
    cfg = LEVEL_CONFIG[level]
    topic, topic_tr = TOPICS[level][scene_num - 1]
    a, b, location = scene_pair(level, scene_num)
    scene_id = f"HSK{level}_SC{scene_num:03d}"
    prefix = f"hsk{level}_sc{scene_num:03d}"
    scene_dir = ROOT / "content" / f"hsk{level}" / f"sc{scene_num:03d}"

    previous = "HSK2_SC050" if level == 3 and scene_num == 1 else (
        f"HSK{level-1}_SC050" if scene_num == 1 else f"HSK{level}_SC{scene_num-1:03d}"
    )
    next_id = None if level == 6 and scene_num == 50 else (
        f"HSK{level+1}_SC001" if scene_num == 50 else f"HSK{level}_SC{scene_num+1:03d}"
    )

    summary = (
        f"{CHARACTERS[a]['name']} ile {CHARACTERS[b]['name']}, {topic_tr.lower()} konusunu "
        f"Hangzhou'daki gündelik hayat bağlamında doğal bir konuşmayla ele alır; "
        f"farklı seçenekleri değerlendirip uygulanabilir bir sonuç çıkarır."
    )

    dump(scene_dir / "scene.json", {
        "schemaVersion": 1,
        "sceneId": scene_id,
        "level": f"HSK{level}",
        "sceneNumber": scene_num,
        "titleZh": topic,
        "titleTr": topic_tr,
        "locationId": location,
        "characterIds": [a, b],
        "estimatedMinutes": cfg["minutes"],
        "isInitiallyUnlocked": False,
        "previousSceneId": previous,
        "nextSceneId": next_id,
        "summaryTr": summary,
    })

    lines = dialogue_for(level, scene_num, topic, topic_tr, a, b)
    dump(scene_dir / "dialogue.json", {
        "sceneId": scene_id,
        "status": "locked-reference",
        "lines": lines,
    })

    words = []
    for i, (hanzi, tr) in enumerate(VOCAB[level][:cfg["vocab"]], 1):
        speaker = a if i % 2 else b
        words.append({
            "wordId": f"WORD_HSK{level}_SC{scene_num:03d}_{i:03d}",
            "hanzi": hanzi,
            "pinyin": pinyin(hanzi),
            "translationTr": tr,
            "isExamTarget": True,
            "voiceId": CHARACTERS[speaker]["voice"],
            "audioFile": f"assets/{prefix}_word_{i:03d}.m4a",
        })
    dump(scene_dir / "vocabulary.json", {"sceneId": scene_id, "words": words})

    selected_indexes = [
        round(i * (len(lines) - 1) / max(cfg["sentence"] - 1, 1))
        for i in range(cfg["sentence"])
    ]
    exercises = []
    types = ["reorder", "fill_blank", "listen_select"]
    for i, idx in enumerate(selected_indexes, 1):
        line = lines[idx]
        typ = types[(i - 1) % len(types)]
        correct = line["textZh"]
        item = {
            "exerciseId": f"EX_SENT_HSK{level}_SC{scene_num:03d}_{i:03d}",
            "type": typ,
            "sentenceId": f"SENT_HSK{level}_SC{scene_num:03d}_{i:03d}",
            "correctZh": correct,
            "pinyin": line["pinyin"],
            "translationTr": line["translationTr"],
            "sentenceZh": correct,
            "correctAnswer": correct,
            "options": [],
            "tokens": [],
            "voiceId": line["voiceId"],
            "audioFile": f"assets/{prefix}_sentence_{i:03d}.m4a",
        }
        if typ == "reorder":
            item["tokens"] = chunks(correct)
        elif typ == "fill_blank":
            answer = re.sub(r"[，。？！；：、“”‘’（）\s]", "", correct)[:2]
            item["correctAnswer"] = answer
            item["sentenceZh"] = correct.replace(answer, "___", 1)
        else:
            distractors = [x["textZh"] for x in lines if x["lineId"] != line["lineId"]][:3]
            item["options"] = [correct] + distractors
        exercises.append(item)
    dump(scene_dir / "sentence_exercises.json", {
        "sceneId": scene_id,
        "sourcePolicy": "exam dialogue-derived; full study uses every dialogue line × three locked modes",
        "exercises": exercises,
    })

    dump(scene_dir / "word_exam.json", {
        "sceneId": scene_id,
        "passingScore": 90,
        "shuffleQuestions": True,
        "source": "allExamTargetVocabulary",
    })
    dump(scene_dir / "sentence_exam.json", {
        "sceneId": scene_id,
        "passingScore": 85,
        "shuffleQuestions": True,
        "types": ["reorder", "fill_blank", "listen_select"],
        "source": "dialogue-derived sentence exercises",
    })

    char_a_slug = CHARACTERS[a]["slug"]
    char_b_slug = CHARACTERS[b]["slug"]
    visual_required = [
        f"assets/{prefix}_bg.webp",
        f"assets/{prefix}_char_{char_a_slug}.webp",
        f"assets/{prefix}_char_{char_b_slug}.webp",
        f"assets/{prefix}_fg.webp",
        f"assets/{prefix}_preview.webp",
    ]
    dump(scene_dir / "visual_manifest.json", {
        "sceneId": scene_id,
        "locationId": location,
        "assets": {
            "background": visual_required[0],
            "characterA": visual_required[1],
            "characterB": visual_required[2],
            "foreground": visual_required[3],
            "preview": visual_required[4],
        },
        "characters": [
            {"characterId": a, "outfit": OUTFITS[a], "speechBubbleAnchor": "face_right"},
            {"characterId": b, "outfit": OUTFITS[b], "speechBubbleAnchor": "face_left"},
        ],
        "cameraPreset": f"CAM_HSK{level}_SC{scene_num:03d}_01",
        "lightingPreset": "LIGHT_WARM_NATURAL_01",
        "activeSpeakerBubble": {"fill": "white", "opacity": "light", "outline": "dashed", "text": False},
        "status": "visual-assets-pending",
    })
    dump(scene_dir / "visual_production_queue.json", {
        "sceneId": scene_id,
        "style": "locked-2.5D-3D-CGI",
        "locationId": location,
        "continuity": [a, b, OUTFITS[a], OUTFITS[b]],
        "composition": f"{topic}; canonical {location}; two adult characters only; natural conversational body language; no readable text.",
        "assets": [
            {"file": Path(x).name, "purpose": purpose}
            for x, purpose in zip(
                visual_required,
                ["background without characters", f"{a} transparent full-body layer", f"{b} transparent full-body layer", "subtle foreground depth layer", "scene preview two-shot"]
            )
        ],
        "rules": ["no baked readable text","family-friendly","canonical identities","modest clothing","mobile-safe framing","location/dialogue fidelity"],
    })

    audio_dialogue = [
        {"file": line["audioFile"], "lineId": line["lineId"], "voiceId": line["voiceId"]}
        for line in lines
    ]
    audio_vocab = [
        {"file": word["audioFile"], "wordId": word["wordId"], "voiceId": word["voiceId"]}
        for word in words
    ]
    audio_sentences = [
        {"file": ex["audioFile"], "exerciseId": ex["exerciseId"], "voiceId": ex["voiceId"]}
        for ex in exercises
    ]
    dump(scene_dir / "audio_manifest.json", {
        "sceneId": scene_id,
        "status": "audio-assets-pending",
        "format": "m4a/aac",
        "language": "zh-CN",
        "voiceBindings": {a: CHARACTERS[a]["voice"], b: CHARACTERS[b]["voice"]},
        "dialogue": audio_dialogue,
        "vocabulary": audio_vocab,
        "sentences": audio_sentences,
    })

    queue_items = []
    order = 1
    for line in lines:
        queue_items.append({
            "order": order,
            "file": Path(line["audioFile"]).name,
            "type": "dialogue",
            "voiceId": line["voiceId"],
            "textZh": line["textZh"],
            "emotion": line["emotion"],
        })
        order += 1
    for word in words:
        queue_items.append({
            "order": order,
            "file": Path(word["audioFile"]).name,
            "type": "vocabulary",
            "voiceId": word["voiceId"],
            "textZh": word["hanzi"],
            "emotion": "neutral",
        })
        order += 1
    for ex in exercises:
        queue_items.append({
            "order": order,
            "file": Path(ex["audioFile"]).name,
            "type": "sentence",
            "voiceId": ex["voiceId"],
            "textZh": ex["correctZh"],
            "emotion": "natural",
        })
        order += 1

    voices = {CHARACTERS[c]["voice"]: VOICE_ENGINE[CHARACTERS[c]["voice"]] for c in (a, b)}
    dump(scene_dir / "audio_production_queue.json", {
        "sceneId": scene_id,
        "language": "zh-CN",
        "outputFormat": "m4a",
        "packageName": f"{scene_id}_audio_assets.zip",
        "outputDirectory": f"content/hsk{level}/sc{scene_num:03d}/assets",
        "productionProfile": {
            "engine": "edge-tts",
            "generator": "tools/generate_audio_assets.py",
            "voices": voices,
            "rateByType": {"dialogue": "+0%", "vocabulary": "-8%", "sentence": "-3%"},
            "encoding": {"codec": "aac", "bitrate": "128k", "sampleRate": 44100, "channels": 1},
            "loudness": "loudnorm=I=-18:TP=-2:LRA=7",
        },
        "items": queue_items,
    })

    audio_required = [x["file"] for x in audio_dialogue + audio_vocab + audio_sentences]
    dump(scene_dir / "media_status.json", {
        "sceneId": scene_id,
        "visual": {"status": "pending", "required": visual_required, "missing": visual_required},
        "audio": {"status": "pending", "required": audio_required, "missing": audio_required},
        "rule": "Audio may be complete only after every declared physical M4A exists and validation passes; visuals intentionally remain pending until produced.",
    })
    dump(scene_dir / "narration.json", {
        "sceneId": scene_id,
        "introTr": summary,
        "narratorVoiceId": "VOICE_NARRATOR_TR_001",
        "status": "script-ready",
    })

def make_batch(level: int, start: int, end: int) -> None:
    scenes = []
    for n in range(start, end + 1):
        a, b, location = scene_pair(level, n)
        scenes.append({
            "sceneId": f"HSK{level}_SC{n:03d}",
            "status": "content_ready_audio_pending_visual_pending",
            "locationId": location,
            "characterIds": [a, b],
        })
    dump(ROOT / "production_batches" / f"HSK{level}_SC{start:03d}_SC{end:03d}.json", {
        "level": f"HSK{level}",
        "startScene": start,
        "endScene": end,
        "status": "content_ready_audio_pending_visual_pending",
        "progress": {
            "contentReady": end - start + 1,
            "totalScenes": end - start + 1,
            "visualComplete": 0,
            "audioComplete": 0,
            "acceptedScenes": 0,
        },
        "scenes": scenes,
        "notes": ["HSK3-HSK6 content and audio queues generated from locked plans. Visual production intentionally pending."],
    })

def main() -> int:
    for level in range(3, 7):
        if len(TOPICS[level]) != 50:
            raise RuntimeError(f"HSK{level} topic count must be 50")
        for scene_num in range(1, 51):
            make_scene(level, scene_num)
        for start in range(1, 51, 10):
            make_batch(level, start, min(start + 9, 50))
    print("Generated HSK3-HSK6: 200 complete content packages with audio queues; visuals pending.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
